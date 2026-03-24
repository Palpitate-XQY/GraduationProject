package xxqqyyy.community.modules.org.service;

import java.util.List;
import xxqqyyy.community.modules.org.dto.ComplexPropertyRelCreateRequest;
import xxqqyyy.community.modules.org.vo.ComplexPropertyRelVO;

/**
 * 小区物业关联服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface ComplexPropertyRelService {

    void create(ComplexPropertyRelCreateRequest request);

    void delete(Long relId);

    List<ComplexPropertyRelVO> listByComplex(Long complexOrgId);
}

