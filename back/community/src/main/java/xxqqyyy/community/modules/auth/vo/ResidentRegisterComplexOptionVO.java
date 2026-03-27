package xxqqyyy.community.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Resident register complex option.
 */
@Data
@Builder
@Schema(description = "Resident register complex option")
public class ResidentRegisterComplexOptionVO {

    @Schema(description = "Complex org id")
    private Long complexOrgId;

    @Schema(description = "Complex org name")
    private String complexOrgName;

    @Schema(description = "Community org id")
    private Long communityOrgId;

    @Schema(description = "Community org name")
    private String communityOrgName;
}
