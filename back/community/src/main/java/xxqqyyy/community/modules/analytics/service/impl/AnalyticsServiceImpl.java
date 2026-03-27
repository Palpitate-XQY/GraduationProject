package xxqqyyy.community.modules.analytics.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.common.util.MarkdownRenderService;
import xxqqyyy.community.modules.activity.mapper.BizActivityMapper;
import xxqqyyy.community.modules.analytics.config.AnalyticsProperties;
import xxqqyyy.community.modules.analytics.dto.AnalyticsGenerateRequest;
import xxqqyyy.community.modules.analytics.dto.AnalyticsReportPageQuery;
import xxqqyyy.community.modules.analytics.entity.BizAnalyticsReport;
import xxqqyyy.community.modules.analytics.enums.AnalyticsReportTypeEnum;
import xxqqyyy.community.modules.analytics.enums.AnalyticsScopeKindEnum;
import xxqqyyy.community.modules.analytics.mapper.BizAnalyticsReportMapper;
import xxqqyyy.community.modules.analytics.model.AiReportRequest;
import xxqqyyy.community.modules.analytics.model.AiReportResult;
import xxqqyyy.community.modules.analytics.model.AnalyticsPeriodWindow;
import xxqqyyy.community.modules.analytics.model.AnalyticsScopeContext;
import xxqqyyy.community.modules.analytics.service.AnalyticsService;
import xxqqyyy.community.modules.analytics.vo.AnalyticsReportVO;
import xxqqyyy.community.modules.analytics.vo.AnalyticsWordCloudVO;
import xxqqyyy.community.modules.analytics.vo.WordCloudItemVO;
import xxqqyyy.community.modules.notice.mapper.BizNoticeMapper;
import xxqqyyy.community.modules.org.dto.OrgQuery;
import xxqqyyy.community.modules.org.entity.BizComplexPropertyRel;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.repair.mapper.BizRepairOrderLogMapper;
import xxqqyyy.community.modules.repair.mapper.BizRepairOrderMapper;
import xxqqyyy.community.modules.system.entity.SysRoleScope;
import xxqqyyy.community.modules.system.enums.DataScopeTypeEnum;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleScopeMapper;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DEFAULT_REPORT_STATUS = "SUCCESS";
    private static final String DAILY = AnalyticsReportTypeEnum.DAILY.getCode();
    private static final String WEEKLY = AnalyticsReportTypeEnum.WEEKLY.getCode();
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final Set<String> STOP_WORDS = Set.of(
        "the", "and", "for", "with", "from", "that", "this", "is", "are", "was", "were", "to", "of", "in", "on",
        "a", "an", "or", "at", "by", "as", "be", "it"
    );
    private final BizAnalyticsReportMapper bizAnalyticsReportMapper;
    private final BizNoticeMapper bizNoticeMapper;
    private final BizActivityMapper bizActivityMapper;
    private final BizRepairOrderMapper bizRepairOrderMapper;
    private final BizRepairOrderLogMapper bizRepairOrderLogMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleScopeMapper sysRoleScopeMapper;
    private final SysOrgMapper sysOrgMapper;
    private final BizComplexPropertyRelMapper bizComplexPropertyRelMapper;
    private final DataScopeService dataScopeService;
    private final AnalyticsProperties analyticsProperties;
    private final OpenAiCompatibleReportClient openAiCompatibleReportClient;
    private final FallbackRuleReportGenerator fallbackRuleReportGenerator;
    private final MarkdownRenderService markdownRenderService;
    private final ObjectMapper objectMapper;

    private final JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

    @Override
    public AnalyticsWordCloudVO getWordCloud(String periodType, LocalDate anchorDate) {
        assertEnabled();
        String reportType = normalizeReportType(periodType);
        LocalDate safeAnchorDate = anchorDate == null ? LocalDate.now(resolveZoneId()) : anchorDate;
        AnalyticsScopeContext scope = resolveCurrentScopeContext();
        BizAnalyticsReport report = loadOrGenerate(reportType, safeAnchorDate, scope, false);
        return toWordCloudVO(report);
    }

    @Override
    public AnalyticsReportVO getLatestReport(String periodType) {
        assertEnabled();
        String reportType = normalizeReportType(periodType);
        AnalyticsScopeContext scope = resolveCurrentScopeContext();
        BizAnalyticsReport latest = bizAnalyticsReportMapper.selectLatest(reportType, scope.getScopeKey());
        if (latest == null) {
            latest = loadOrGenerate(reportType, LocalDate.now(resolveZoneId()), scope, false);
        }
        return toReportVO(latest);
    }

    @Override
    public PageResult<AnalyticsReportVO> pageReports(AnalyticsReportPageQuery query) {
        assertEnabled();
        String reportType = normalizeReportType(query == null ? null : query.getPeriodType());
        long current = query == null || query.getCurrent() < 1 ? 1L : query.getCurrent();
        long size = query == null || query.getSize() < 1 ? DEFAULT_PAGE_SIZE : Math.min(100, query.getSize());
        AnalyticsScopeContext scope = resolveCurrentScopeContext();
        long total = bizAnalyticsReportMapper.countPage(reportType, scope.getScopeKey());
        if (total == 0) {
            return PageResult.empty(current, size);
        }
        long offset = (current - 1) * size;
        List<AnalyticsReportVO> records = bizAnalyticsReportMapper.selectPage(reportType, scope.getScopeKey(), offset, size)
            .stream()
            .map(this::toReportVO)
            .toList();
        return PageResult.<AnalyticsReportVO>builder()
            .records(records)
            .total(total)
            .current(current)
            .size(size)
            .build();
    }

    @Override
    public AnalyticsReportVO generateReport(AnalyticsGenerateRequest request) {
        assertEnabled();
        String reportType = normalizeReportType(request == null ? null : request.getPeriodType());
        LocalDate anchorDate = request == null || request.getAnchorDate() == null
            ? LocalDate.now(resolveZoneId())
            : request.getAnchorDate();
        boolean force = request != null && Boolean.TRUE.equals(request.getForce());
        AnalyticsScopeContext scope = resolveCurrentScopeContext();
        BizAnalyticsReport report = loadOrGenerate(reportType, anchorDate, scope, force);
        return toReportVO(report);
    }

    @Override
    public void generateTemplateReports(String periodType, LocalDate anchorDate) {
        if (!analyticsProperties.isEnabled()) {
            return;
        }
        String reportType = normalizeReportType(periodType);
        LocalDate safeAnchorDate = anchorDate == null ? LocalDate.now(resolveZoneId()) : anchorDate;
        List<AnalyticsScopeContext> contexts = resolveTemplateScopeContexts();
        for (AnalyticsScopeContext context : contexts) {
            try {
                loadOrGenerate(reportType, safeAnchorDate, context, false);
            } catch (Exception ex) {
                log.warn("Generate analytics report failed. reportType={}, anchorDate={}, scopeKey={}, reason={}",
                    reportType, safeAnchorDate, context.getScopeKey(), ex.getMessage());
            }
        }
    }

    private BizAnalyticsReport loadOrGenerate(String reportType, LocalDate anchorDate, AnalyticsScopeContext scope, boolean force) {
        AnalyticsPeriodWindow periodWindow = buildPeriodWindow(reportType, anchorDate);
        if (!force) {
            BizAnalyticsReport existing = bizAnalyticsReportMapper.selectByUnique(
                periodWindow.getReportType(),
                periodWindow.getReportDate(),
                scope.getScopeKey()
            );
            if (existing != null) {
                return existing;
            }
        }
        generateAndSave(periodWindow, scope);
        BizAnalyticsReport generated = bizAnalyticsReportMapper.selectByUnique(
            periodWindow.getReportType(),
            periodWindow.getReportDate(),
            scope.getScopeKey()
        );
        if (generated == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Generate analytics report failed");
        }
        return generated;
    }

    private void generateAndSave(AnalyticsPeriodWindow periodWindow, AnalyticsScopeContext scope) {
        MetricSnapshot currentMetrics = aggregateMetrics(periodWindow.getStartTime(), periodWindow.getEndTime(), scope);
        MetricSnapshot previousMetrics = aggregateMetrics(periodWindow.getPreviousStartTime(), periodWindow.getPreviousEndTime(), scope);
        Map<String, Object> metrics = mergeMetrics(periodWindow, scope, currentMetrics, previousMetrics);
        List<WordCloudItemVO> wordCloudItems = buildWordCloudItems(periodWindow, scope);
        AiReportResult aiResult = generateSummary(periodWindow, scope, metrics, wordCloudItems);

        BizAnalyticsReport report = new BizAnalyticsReport();
        report.setReportType(periodWindow.getReportType());
        report.setReportDate(periodWindow.getReportDate());
        report.setScopeKey(scope.getScopeKey());
        report.setScopeKind(scope.getScopeKind());
        report.setPeriodStart(periodWindow.getStartTime());
        report.setPeriodEnd(periodWindow.getEndTime());
        report.setMetricsJson(writeJson(metrics));
        report.setWordcloudJson(writeJson(wordCloudItems));
        report.setSummaryMarkdown(aiResult.getMarkdown());
        report.setAiMode(aiResult.getAiMode());
        report.setModelName(aiResult.getModelName());
        report.setStatus(DEFAULT_REPORT_STATUS);
        report.setGeneratedAt(LocalDateTime.now(resolveZoneId()));
        bizAnalyticsReportMapper.upsert(report);
    }

    private AiReportResult generateSummary(
        AnalyticsPeriodWindow periodWindow,
        AnalyticsScopeContext scope,
        Map<String, Object> metrics,
        List<WordCloudItemVO> words
    ) {
        List<AiReportRequest.WordFrequency> topWords = words.stream()
            .map(item -> AiReportRequest.WordFrequency.builder().word(item.getWord()).weight(item.getWeight()).build())
            .toList();
        AiReportRequest request = AiReportRequest.builder()
            .reportType(periodWindow.getReportType())
            .scopeKind(scope.getScopeKind())
            .periodStart(periodWindow.getStartTime())
            .periodEnd(periodWindow.getEndTime())
            .metrics(metrics)
            .topWords(topWords)
            .build();
        if (openAiCompatibleReportClient.isEnabled()) {
            try {
                return openAiCompatibleReportClient.generate(request);
            } catch (Exception ex) {
                log.warn("AI report generation failed, fallback to rule mode. scopeKey={}, reason={}",
                    scope.getScopeKey(), ex.getMessage());
            }
        }
        return fallbackRuleReportGenerator.generate(request);
    }

    private Map<String, Object> mergeMetrics(
        AnalyticsPeriodWindow periodWindow,
        AnalyticsScopeContext scope,
        MetricSnapshot current,
        MetricSnapshot previous
    ) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("reportType", periodWindow.getReportType());
        metrics.put("reportDate", periodWindow.getReportDate());
        metrics.put("scopeKind", scope.getScopeKind());
        metrics.put("scopeKey", scope.getScopeKey());
        metrics.put("periodStart", periodWindow.getStartTime());
        metrics.put("periodEnd", periodWindow.getEndTime());

        metrics.put("noticePublishedCount", current.noticePublishedCount());
        metrics.put("noticePublishedTrend", trend(current.noticePublishedCount(), previous.noticePublishedCount()));
        metrics.put("activityPublishedCount", current.activityPublishedCount());
        metrics.put("activityPublishedTrend", trend(current.activityPublishedCount(), previous.activityPublishedCount()));
        metrics.put("activitySignupCount", current.activitySignupCount());
        metrics.put("activitySignupTrend", trend(current.activitySignupCount(), previous.activitySignupCount()));
        metrics.put("repairCreatedCount", current.repairCreatedCount());
        metrics.put("repairCreatedTrend", trend(current.repairCreatedCount(), previous.repairCreatedCount()));
        metrics.put("repairFinishedCount", current.repairFinishedCount());
        metrics.put("repairFinishedTrend", trend(current.repairFinishedCount(), previous.repairFinishedCount()));
        metrics.put("repairReopenedCount", current.repairReopenedCount());
        metrics.put("repairReopenedTrend", trend(current.repairReopenedCount(), previous.repairReopenedCount()));
        metrics.put("repairUrgeCount", current.repairUrgeCount());
        metrics.put("repairUrgeTrend", trend(current.repairUrgeCount(), previous.repairUrgeCount()));
        metrics.put("repairAvgHandleMinutes", roundDouble(current.repairAvgHandleMinutes(), 2));
        metrics.put("repairAvgHandleTrend", trend(current.repairAvgHandleMinutes(), previous.repairAvgHandleMinutes()));
        return metrics;
    }

    private MetricSnapshot aggregateMetrics(LocalDateTime startTime, LocalDateTime endTime, AnalyticsScopeContext scope) {
        boolean allAccess = scope.isAllAccess();
        Set<Long> orgIds = scope.getSafeOrgIds();
        long noticePublishedCount = bizNoticeMapper.countPublishedInPeriod(startTime, endTime, allAccess, orgIds);
        long activityPublishedCount = bizActivityMapper.countPublishedInPeriod(startTime, endTime, allAccess, orgIds);
        long activitySignupCount = bizActivityMapper.countSignupInPeriod(startTime, endTime, allAccess, orgIds);
        long repairCreatedCount = bizRepairOrderMapper.countCreatedInPeriod(startTime, endTime, allAccess, orgIds);
        long repairFinishedCount = bizRepairOrderMapper.countFinishedInPeriod(startTime, endTime, allAccess, orgIds);
        long repairReopenedCount = bizRepairOrderLogMapper.countOperationInPeriod("REOPEN", startTime, endTime, allAccess, orgIds);
        long repairUrgeCount = bizRepairOrderLogMapper.countOperationInPeriod("URGE", startTime, endTime, allAccess, orgIds);
        double repairAvgHandleMinutes = defaultZero(bizRepairOrderMapper.avgHandleMinutesInPeriod(startTime, endTime, allAccess, orgIds));
        return new MetricSnapshot(
            noticePublishedCount,
            activityPublishedCount,
            activitySignupCount,
            repairCreatedCount,
            repairFinishedCount,
            repairReopenedCount,
            repairUrgeCount,
            repairAvgHandleMinutes
        );
    }

    private List<WordCloudItemVO> buildWordCloudItems(AnalyticsPeriodWindow periodWindow, AnalyticsScopeContext scope) {
        int maxCorpusPerSource = Math.max(50, analyticsProperties.getMaxCorpusPerSource());
        List<String> corpus = new ArrayList<>();
        corpus.addAll(bizNoticeMapper.selectTextCorpusInPeriod(
            periodWindow.getStartTime(), periodWindow.getEndTime(), scope.isAllAccess(), scope.getSafeOrgIds(), maxCorpusPerSource
        ));
        corpus.addAll(bizActivityMapper.selectTextCorpusInPeriod(
            periodWindow.getStartTime(), periodWindow.getEndTime(), scope.isAllAccess(), scope.getSafeOrgIds(), maxCorpusPerSource
        ));
        corpus.addAll(bizRepairOrderMapper.selectTextCorpusInPeriod(
            periodWindow.getStartTime(), periodWindow.getEndTime(), scope.isAllAccess(), scope.getSafeOrgIds(), maxCorpusPerSource
        ));
        if (CollectionUtils.isEmpty(corpus)) {
            return Collections.emptyList();
        }

        Map<String, Long> counter = new HashMap<>();
        for (String text : corpus) {
            if (StringUtils.isBlank(text)) {
                continue;
            }
            List<SegToken> tokens = jiebaSegmenter.process(text, SegMode.SEARCH);
            for (SegToken token : tokens) {
                String normalized = normalizeWord(token.word);
                if (normalized == null) {
                    continue;
                }
                counter.merge(normalized, 1L, Long::sum);
            }
        }

        if (counter.isEmpty()) {
            return Collections.emptyList();
        }

        int minWordFrequency = Math.max(1, analyticsProperties.getMinWordFrequency());
        List<Map.Entry<String, Long>> entries = counter.entrySet().stream()
            .filter(item -> item.getValue() >= minWordFrequency)
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
            .toList();

        if (entries.isEmpty() && minWordFrequency > 1) {
            entries = counter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
                .toList();
        }

        int topN = Math.max(20, analyticsProperties.getWordCloudTopN());
        List<WordCloudItemVO> wordCloudItems = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, entries.size()); i++) {
            Map.Entry<String, Long> entry = entries.get(i);
            wordCloudItems.add(WordCloudItemVO.builder().word(entry.getKey()).weight(entry.getValue()).build());
        }
        return wordCloudItems;
    }

    private String normalizeWord(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String normalized = token.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[\\p{Punct}\\p{IsPunctuation}\\s]+", "");
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        if (normalized.startsWith("http") || normalized.startsWith("www")) {
            return null;
        }
        if (STOP_WORDS.contains(normalized)) {
            return null;
        }
        if (normalized.matches("^[0-9]+$")) {
            return null;
        }
        boolean simpleAscii = normalized.matches("^[a-z0-9_-]+$");
        if (simpleAscii && normalized.length() < 3) {
            return null;
        }
        if (!simpleAscii && normalized.length() < 2) {
            return null;
        }
        if (normalized.length() > 30) {
            return null;
        }
        return normalized;
    }

    private AnalyticsScopeContext resolveCurrentScopeContext() {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        List<Long> roleIds = sysRoleMapper.selectRoleIdsByUserId(principal.getUserId());
        List<SysRoleScope> roleScopes = roleIds.isEmpty() ? Collections.emptyList() : sysRoleScopeMapper.selectByRoleIds(roleIds);
        if (principal.isSuperAdmin() || containsScopeType(roleScopes, DataScopeTypeEnum.ALL.getCode())) {
            return AnalyticsScopeContext.builder()
                .scopeKey("ALL")
                .scopeKind(AnalyticsScopeKindEnum.ALL.getCode())
                .allAccess(true)
                .userId(principal.getUserId())
                .orgIds(Collections.emptySet())
                .build();
        }

        DataScopeResult dataScopeResult = dataScopeService.resolveByUserId(principal.getUserId());
        Set<Long> dataScopeOrgIds = new LinkedHashSet<>(dataScopeResult.getSafeOrgIds());

        boolean hasOnlySelfScope = !roleScopes.isEmpty() && roleScopes.stream()
            .allMatch(scope -> StringUtils.equalsIgnoreCase(scope.getScopeType(), DataScopeTypeEnum.SELF.getCode()));
        if (hasOnlySelfScope || (!dataScopeResult.isAllAccess() && dataScopeOrgIds.isEmpty())) {
            Set<Long> visibleOrgIds = resolveSelfVisibleOrgIds(principal.getOrgId());
            return AnalyticsScopeContext.builder()
                .scopeKey("SELF:" + principal.getUserId())
                .scopeKind(AnalyticsScopeKindEnum.SELF.getCode())
                .allAccess(false)
                .userId(principal.getUserId())
                .orgIds(visibleOrgIds)
                .build();
        }

        Set<String> concreteScopeTypes = new LinkedHashSet<>();
        Set<Long> concreteScopeRefIds = new LinkedHashSet<>();
        for (SysRoleScope roleScope : roleScopes) {
            if (roleScope == null || StringUtils.isBlank(roleScope.getScopeType())) {
                continue;
            }
            String scopeType = roleScope.getScopeType().toUpperCase(Locale.ROOT);
            if (DataScopeTypeEnum.ALL.getCode().equals(scopeType) || DataScopeTypeEnum.SELF.getCode().equals(scopeType)) {
                continue;
            }
            concreteScopeTypes.add(scopeType);
            if (roleScope.getScopeRefId() != null) {
                concreteScopeRefIds.add(roleScope.getScopeRefId());
            }
        }

        if (concreteScopeTypes.size() == 1 && concreteScopeRefIds.size() == 1) {
            String scopeType = concreteScopeTypes.iterator().next();
            Long scopeRefId = concreteScopeRefIds.iterator().next();
            if (isKnownScopeKind(scopeType)) {
                Set<Long> orgIds = dataScopeOrgIds.isEmpty() ? resolveOrgIdsByScopeType(scopeType, scopeRefId) : dataScopeOrgIds;
                return AnalyticsScopeContext.builder()
                    .scopeKey(scopeType + ":" + scopeRefId)
                    .scopeKind(scopeType)
                    .allAccess(false)
                    .userId(principal.getUserId())
                    .orgIds(orgIds)
                    .build();
            }
        }

        return AnalyticsScopeContext.builder()
            .scopeKey(buildCustomScopeKey(dataScopeOrgIds, principal.getUserId()))
            .scopeKind(AnalyticsScopeKindEnum.CUSTOM.getCode())
            .allAccess(false)
            .userId(principal.getUserId())
            .orgIds(dataScopeOrgIds)
            .build();
    }

    private List<AnalyticsScopeContext> resolveTemplateScopeContexts() {
        Map<String, AnalyticsScopeContext> contextMap = new LinkedHashMap<>();
        contextMap.put("ALL", AnalyticsScopeContext.builder()
            .scopeKey("ALL")
            .scopeKind(AnalyticsScopeKindEnum.ALL.getCode())
            .allAccess(true)
            .orgIds(Collections.emptySet())
            .build());

        OrgQuery orgQuery = new OrgQuery();
        orgQuery.setStatus(1);
        List<SysOrg> allOrgs = sysOrgMapper.selectTree(orgQuery, true, Collections.emptySet());
        for (SysOrg org : allOrgs) {
            if (org == null || org.getId() == null || StringUtils.isBlank(org.getOrgType())) {
                continue;
            }
            String scopeKind = org.getOrgType().toUpperCase(Locale.ROOT);
            if (!isKnownScopeKind(scopeKind)) {
                continue;
            }
            Set<Long> orgIds = resolveOrgIdsByScopeType(scopeKind, org.getId());
            AnalyticsScopeContext context = AnalyticsScopeContext.builder()
                .scopeKey(scopeKind + ":" + org.getId())
                .scopeKind(scopeKind)
                .allAccess(false)
                .orgIds(orgIds)
                .build();
            contextMap.putIfAbsent(context.getScopeKey(), context);
        }
        return new ArrayList<>(contextMap.values());
    }

    private Set<Long> resolveSelfVisibleOrgIds(Long orgId) {
        if (orgId == null) {
            return Collections.emptySet();
        }
        Set<Long> orgIds = new LinkedHashSet<>();
        orgIds.add(orgId);
        orgIds.addAll(sysOrgMapper.selectDescendantIds(orgId));
        SysOrg org = sysOrgMapper.selectById(orgId);
        if (org != null && StringUtils.isNotBlank(org.getAncestorPath())) {
            String[] parts = org.getAncestorPath().split("/");
            for (String part : parts) {
                if (StringUtils.isBlank(part)) {
                    continue;
                }
                try {
                    orgIds.add(Long.parseLong(part));
                } catch (NumberFormatException ignore) {
                    // ignored
                }
            }
        }
        return orgIds;
    }

    private Set<Long> resolveOrgIdsByScopeType(String scopeType, Long scopeRefId) {
        if (scopeRefId == null) {
            return Collections.emptySet();
        }
        Set<Long> orgIds = new LinkedHashSet<>();
        orgIds.add(scopeRefId);
        if (DataScopeTypeEnum.PROPERTY_COMPANY.getCode().equalsIgnoreCase(scopeType)) {
            List<BizComplexPropertyRel> relList = bizComplexPropertyRelMapper.selectByPropertyCompanyOrgId(scopeRefId);
            for (BizComplexPropertyRel rel : relList) {
                if (rel.getStatus() == null || rel.getStatus() != 1 || rel.getComplexOrgId() == null) {
                    continue;
                }
                orgIds.add(rel.getComplexOrgId());
            }
            return orgIds;
        }
        orgIds.addAll(sysOrgMapper.selectDescendantIds(scopeRefId));
        return orgIds;
    }

    private String buildCustomScopeKey(Set<Long> orgIds, Long userId) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return "CUSTOM:USER:" + userId;
        }
        List<Long> sorted = orgIds.stream().filter(Objects::nonNull).sorted().toList();
        String raw = sorted.toString();
        return "CUSTOM:" + digest(raw);
    }

    private String digest(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < hash.length && builder.length() < 16; i++) {
                builder.append(String.format("%02x", hash[i]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Digest algorithm unavailable");
        }
    }

    private AnalyticsPeriodWindow buildPeriodWindow(String reportType, LocalDate anchorDate) {
        ZoneId zoneId = resolveZoneId();
        LocalDate safeAnchorDate = anchorDate == null ? LocalDate.now(zoneId) : anchorDate;
        if (WEEKLY.equalsIgnoreCase(reportType)) {
            LocalDate weekStartDate = safeAnchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDateTime startTime = weekStartDate.atStartOfDay();
            LocalDateTime endTime = startTime.plusWeeks(1);
            return AnalyticsPeriodWindow.builder()
                .reportType(WEEKLY)
                .anchorDate(safeAnchorDate)
                .reportDate(weekStartDate)
                .startTime(startTime)
                .endTime(endTime)
                .previousStartTime(startTime.minusWeeks(1))
                .previousEndTime(startTime)
                .build();
        }
        LocalDateTime startTime = safeAnchorDate.atStartOfDay();
        LocalDateTime endTime = startTime.plusDays(1);
        return AnalyticsPeriodWindow.builder()
            .reportType(DAILY)
            .anchorDate(safeAnchorDate)
            .reportDate(safeAnchorDate)
            .startTime(startTime)
            .endTime(endTime)
            .previousStartTime(startTime.minusDays(1))
            .previousEndTime(startTime)
            .build();
    }

    private AnalyticsWordCloudVO toWordCloudVO(BizAnalyticsReport report) {
        return AnalyticsWordCloudVO.builder()
            .reportType(report.getReportType())
            .reportDate(report.getReportDate())
            .scopeKind(report.getScopeKind())
            .generatedAt(report.getGeneratedAt())
            .items(parseWordCloud(report.getWordcloudJson()))
            .build();
    }

    private AnalyticsReportVO toReportVO(BizAnalyticsReport report) {
        Map<String, Object> metrics = parseMetrics(report.getMetricsJson());
        String markdown = report.getSummaryMarkdown();
        return AnalyticsReportVO.builder()
            .id(report.getId())
            .reportType(report.getReportType())
            .reportDate(report.getReportDate())
            .scopeKind(report.getScopeKind())
            .scopeKey(report.getScopeKey())
            .periodStart(report.getPeriodStart())
            .periodEnd(report.getPeriodEnd())
            .metrics(metrics)
            .summaryMarkdown(markdown)
            .summaryHtml(markdownRenderService.renderToHtml(markdown))
            .aiMode(report.getAiMode())
            .modelName(report.getModelName())
            .status(report.getStatus())
            .generatedAt(report.getGeneratedAt())
            .build();
    }

    private Map<String, Object> parseMetrics(String metricsJson) {
        if (StringUtils.isBlank(metricsJson)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(metricsJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            log.warn("Parse metrics json failed: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private List<WordCloudItemVO> parseWordCloud(String wordCloudJson) {
        if (StringUtils.isBlank(wordCloudJson)) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> items = objectMapper.readValue(wordCloudJson, new TypeReference<List<Map<String, Object>>>() {
            });
            if (CollectionUtils.isEmpty(items)) {
                return Collections.emptyList();
            }
            List<WordCloudItemVO> result = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String word = Objects.toString(item.get("word"), "");
                long weight = parseLong(item.get("weight"));
                if (StringUtils.isBlank(word) || weight <= 0) {
                    continue;
                }
                result.add(WordCloudItemVO.builder().word(word).weight(weight).build());
            }
            return result;
        } catch (Exception ex) {
            log.warn("Parse word cloud json failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String normalizeReportType(String periodType) {
        String safeType = StringUtils.defaultIfBlank(periodType, DAILY).toUpperCase(Locale.ROOT);
        if (!AnalyticsReportTypeEnum.valid(safeType)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "periodType only supports DAILY/WEEKLY");
        }
        return safeType;
    }

    private void assertEnabled() {
        if (!analyticsProperties.isEnabled()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "Analytics module is disabled");
        }
    }

    private boolean containsScopeType(List<SysRoleScope> scopes, String targetType) {
        if (CollectionUtils.isEmpty(scopes)) {
            return false;
        }
        return scopes.stream().anyMatch(scope -> StringUtils.equalsIgnoreCase(scope.getScopeType(), targetType));
    }

    private boolean isKnownScopeKind(String scopeKind) {
        return AnalyticsScopeKindEnum.STREET.getCode().equalsIgnoreCase(scopeKind)
            || AnalyticsScopeKindEnum.COMMUNITY.getCode().equalsIgnoreCase(scopeKind)
            || AnalyticsScopeKindEnum.COMPLEX.getCode().equalsIgnoreCase(scopeKind)
            || AnalyticsScopeKindEnum.PROPERTY_COMPANY.getCode().equalsIgnoreCase(scopeKind);
    }

    private ZoneId resolveZoneId() {
        try {
            return ZoneId.of(analyticsProperties.getReport().getZone());
        } catch (Exception ex) {
            return ZoneId.of("Asia/Shanghai");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Serialize analytics payload failed");
        }
    }

    private long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignore) {
                return 0L;
            }
        }
        return 0L;
    }

    private double defaultZero(Double value) {
        return value == null || Double.isNaN(value) || Double.isInfinite(value) ? 0D : value;
    }

    private double trend(double current, double previous) {
        if (previous <= 0D) {
            return current <= 0D ? 0D : 100D;
        }
        return roundDouble(((current - previous) / previous) * 100D, 2);
    }

    private double roundDouble(double value, int scale) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0D;
        }
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private record MetricSnapshot(
        long noticePublishedCount,
        long activityPublishedCount,
        long activitySignupCount,
        long repairCreatedCount,
        long repairFinishedCount,
        long repairReopenedCount,
        long repairUrgeCount,
        double repairAvgHandleMinutes
    ) {
    }
}

