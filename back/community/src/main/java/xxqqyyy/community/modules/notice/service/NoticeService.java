package xxqqyyy.community.modules.notice.service;

import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.notice.dto.NoticeCreateRequest;
import xxqqyyy.community.modules.notice.dto.NoticePageQuery;
import xxqqyyy.community.modules.notice.dto.NoticeUpdateRequest;
import xxqqyyy.community.modules.notice.vo.NoticeVO;

/**
 * 公告服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface NoticeService {

    PageResult<NoticeVO> managePage(NoticePageQuery query);

    NoticeVO detail(Long noticeId);

    void create(NoticeCreateRequest request);

    void update(NoticeUpdateRequest request);

    void delete(Long noticeId);

    void publish(Long noticeId);

    void recall(Long noticeId);

    PageResult<NoticeVO> residentPage(NoticePageQuery query);

    NoticeVO residentDetail(Long noticeId);
}

