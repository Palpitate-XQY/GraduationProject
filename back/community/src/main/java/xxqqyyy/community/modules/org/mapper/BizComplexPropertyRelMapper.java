package xxqqyyy.community.modules.org.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.org.entity.BizComplexPropertyRel;

/**
 * 小区物业关系 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizComplexPropertyRelMapper {

    int insert(BizComplexPropertyRel rel);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    List<BizComplexPropertyRel> selectByComplexOrgId(@Param("complexOrgId") Long complexOrgId);

    List<BizComplexPropertyRel> selectByPropertyCompanyOrgId(@Param("propertyCompanyOrgId") Long propertyCompanyOrgId);

    long countActiveByPropertyAndComplex(@Param("propertyCompanyOrgId") Long propertyCompanyOrgId, @Param("complexOrgId") Long complexOrgId);
}
