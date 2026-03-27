package xxqqyyy.community.modules.analytics.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.analytics.entity.BizAnalyticsReport;

/**
 * Analytics report mapper.
 */
@Mapper
public interface BizAnalyticsReportMapper {

    BizAnalyticsReport selectByUnique(
        @Param("reportType") String reportType,
        @Param("reportDate") LocalDate reportDate,
        @Param("scopeKey") String scopeKey
    );

    BizAnalyticsReport selectLatest(
        @Param("reportType") String reportType,
        @Param("scopeKey") String scopeKey
    );

    long countPage(
        @Param("reportType") String reportType,
        @Param("scopeKey") String scopeKey
    );

    List<BizAnalyticsReport> selectPage(
        @Param("reportType") String reportType,
        @Param("scopeKey") String scopeKey,
        @Param("offset") long offset,
        @Param("size") long size
    );

    int upsert(BizAnalyticsReport report);
}

