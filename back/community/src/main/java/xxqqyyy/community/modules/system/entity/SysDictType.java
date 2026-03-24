package xxqqyyy.community.modules.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 字典类型实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseAuditEntity {

    private String dictCode;

    private String dictName;

    private Integer status;
}
