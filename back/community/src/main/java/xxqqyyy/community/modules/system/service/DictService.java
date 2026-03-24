package xxqqyyy.community.modules.system.service;

import java.util.List;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.system.dto.DictDataCreateRequest;
import xxqqyyy.community.modules.system.dto.DictDataPageQuery;
import xxqqyyy.community.modules.system.dto.DictDataUpdateRequest;
import xxqqyyy.community.modules.system.dto.DictTypeCreateRequest;
import xxqqyyy.community.modules.system.dto.DictTypePageQuery;
import xxqqyyy.community.modules.system.dto.DictTypeUpdateRequest;
import xxqqyyy.community.modules.system.vo.DictDataVO;
import xxqqyyy.community.modules.system.vo.DictOptionVO;
import xxqqyyy.community.modules.system.vo.DictTypeVO;

/**
 * 字典管理服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface DictService {

    PageResult<DictTypeVO> pageType(DictTypePageQuery query);

    DictTypeVO getTypeById(Long id);

    void createType(DictTypeCreateRequest request);

    void updateType(DictTypeUpdateRequest request);

    void deleteType(Long id);

    PageResult<DictDataVO> pageData(DictDataPageQuery query);

    DictDataVO getDataById(Long id);

    void createData(DictDataCreateRequest request);

    void updateData(DictDataUpdateRequest request);

    void deleteData(Long id);

    List<DictOptionVO> listOptions(String dictCode);
}
