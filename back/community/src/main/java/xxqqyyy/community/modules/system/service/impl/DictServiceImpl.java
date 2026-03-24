package xxqqyyy.community.modules.system.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.system.dto.DictDataCreateRequest;
import xxqqyyy.community.modules.system.dto.DictDataPageQuery;
import xxqqyyy.community.modules.system.dto.DictDataUpdateRequest;
import xxqqyyy.community.modules.system.dto.DictTypeCreateRequest;
import xxqqyyy.community.modules.system.dto.DictTypePageQuery;
import xxqqyyy.community.modules.system.dto.DictTypeUpdateRequest;
import xxqqyyy.community.modules.system.entity.SysDictData;
import xxqqyyy.community.modules.system.entity.SysDictType;
import xxqqyyy.community.modules.system.mapper.SysDictDataMapper;
import xxqqyyy.community.modules.system.mapper.SysDictTypeMapper;
import xxqqyyy.community.modules.system.service.DictService;
import xxqqyyy.community.modules.system.vo.DictDataVO;
import xxqqyyy.community.modules.system.vo.DictOptionVO;
import xxqqyyy.community.modules.system.vo.DictTypeVO;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 字典管理服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;

    @Override
    public PageResult<DictTypeVO> pageType(DictTypePageQuery query) {
        long total = sysDictTypeMapper.countPage(query);
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<DictTypeVO> records = sysDictTypeMapper.selectPage(query, offset, query.getSize())
            .stream()
            .map(this::toDictTypeVO)
            .toList();
        return PageResult.<DictTypeVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public DictTypeVO getTypeById(Long id) {
        return toDictTypeVO(requireDictType(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createType(DictTypeCreateRequest request) {
        if (sysDictTypeMapper.countByCode(request.getDictCode(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "字典编码已存在");
        }
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysDictType dictType = new SysDictType();
        dictType.setDictCode(request.getDictCode());
        dictType.setDictName(request.getDictName());
        dictType.setStatus(request.getStatus());
        dictType.setCreateBy(principal.getUserId());
        dictType.setUpdateBy(principal.getUserId());
        sysDictTypeMapper.insert(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(DictTypeUpdateRequest request) {
        SysDictType dictType = requireDictType(request.getId());
        if (sysDictTypeMapper.countByCode(request.getDictCode(), request.getId()) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "字典编码已存在");
        }
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dictType.setDictCode(request.getDictCode());
        dictType.setDictName(request.getDictName());
        dictType.setStatus(request.getStatus());
        dictType.setUpdateBy(principal.getUserId());
        sysDictTypeMapper.update(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDictType dictType = requireDictType(id);
        long refCount = sysDictDataMapper.countByTypeCode(dictType.getDictCode());
        if (refCount > 0) {
            throw new BizException(ErrorCode.CONFLICT, "字典类型下存在字典数据，不能删除");
        }
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        sysDictTypeMapper.logicalDelete(id, principal.getUserId());
    }

    @Override
    public PageResult<DictDataVO> pageData(DictDataPageQuery query) {
        long total = sysDictDataMapper.countPage(query);
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<DictDataVO> records = sysDictDataMapper.selectPage(query, offset, query.getSize())
            .stream()
            .map(this::toDictDataVO)
            .toList();
        return PageResult.<DictDataVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public DictDataVO getDataById(Long id) {
        return toDictDataVO(requireDictData(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createData(DictDataCreateRequest request) {
        requireDictTypeByCode(request.getDictTypeCode());
        if (sysDictDataMapper.countByTypeAndValue(request.getDictTypeCode(), request.getDictValue(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "同一字典类型下字典值已存在");
        }
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysDictData dictData = new SysDictData();
        dictData.setDictTypeCode(request.getDictTypeCode());
        dictData.setDictLabel(request.getDictLabel());
        dictData.setDictValue(request.getDictValue());
        dictData.setSort(request.getSort());
        dictData.setStatus(request.getStatus());
        dictData.setCreateBy(principal.getUserId());
        dictData.setUpdateBy(principal.getUserId());
        sysDictDataMapper.insert(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateData(DictDataUpdateRequest request) {
        SysDictData dictData = requireDictData(request.getId());
        requireDictTypeByCode(request.getDictTypeCode());
        if (sysDictDataMapper.countByTypeAndValue(request.getDictTypeCode(), request.getDictValue(), request.getId()) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "同一字典类型下字典值已存在");
        }
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dictData.setDictTypeCode(request.getDictTypeCode());
        dictData.setDictLabel(request.getDictLabel());
        dictData.setDictValue(request.getDictValue());
        dictData.setSort(request.getSort());
        dictData.setStatus(request.getStatus());
        dictData.setUpdateBy(principal.getUserId());
        sysDictDataMapper.update(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(Long id) {
        requireDictData(id);
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        sysDictDataMapper.logicalDelete(id, principal.getUserId());
    }

    @Override
    public List<DictOptionVO> listOptions(String dictCode) {
        requireDictTypeByCode(dictCode);
        return sysDictDataMapper.selectOptionsByTypeCode(dictCode)
            .stream()
            .map(item -> DictOptionVO.builder()
                .label(item.getDictLabel())
                .value(item.getDictValue())
                .sort(item.getSort())
                .build())
            .toList();
    }

    private SysDictType requireDictType(Long id) {
        SysDictType dictType = sysDictTypeMapper.selectById(id);
        if (dictType == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "字典类型不存在");
        }
        return dictType;
    }

    private SysDictType requireDictTypeByCode(String dictCode) {
        SysDictType dictType = sysDictTypeMapper.selectByCode(dictCode);
        if (dictType == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "字典类型不存在");
        }
        return dictType;
    }

    private SysDictData requireDictData(Long id) {
        SysDictData dictData = sysDictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "字典数据不存在");
        }
        return dictData;
    }

    private DictTypeVO toDictTypeVO(SysDictType dictType) {
        return DictTypeVO.builder()
            .id(dictType.getId())
            .dictCode(dictType.getDictCode())
            .dictName(dictType.getDictName())
            .status(dictType.getStatus())
            .createTime(dictType.getCreateTime())
            .build();
    }

    private DictDataVO toDictDataVO(SysDictData dictData) {
        return DictDataVO.builder()
            .id(dictData.getId())
            .dictTypeCode(dictData.getDictTypeCode())
            .dictLabel(dictData.getDictLabel())
            .dictValue(dictData.getDictValue())
            .sort(dictData.getSort())
            .status(dictData.getStatus())
            .createTime(dictData.getCreateTime())
            .build();
    }
}
