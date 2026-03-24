package xxqqyyy.community.modules.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 字典数据实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictData extends BaseAuditEntity {

    private String dictTypeCode;

    private String dictLabel;

    private String dictValue;

    private Integer sort;

    private Integer status;
}
