package xxqqyyy.community.modules.auth.service;

import java.util.List;
import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.dto.ResidentRegisterRequest;
import xxqqyyy.community.modules.auth.dto.ResetPasswordByCodeRequest;
import xxqqyyy.community.modules.auth.dto.SendResetCodeRequest;
import xxqqyyy.community.modules.auth.dto.UpdatePasswordRequest;
import xxqqyyy.community.modules.auth.vo.CurrentUserVO;
import xxqqyyy.community.modules.auth.vo.LoginVO;
import xxqqyyy.community.modules.auth.vo.ResidentRegisterComplexOptionVO;
import xxqqyyy.community.modules.auth.vo.SendResetCodeVO;

/**
 * Authentication service.
 */
public interface AuthService {

    LoginVO login(LoginRequest request, String ip, String userAgent);

    CurrentUserVO me();

    void logout(String token);

    void updatePassword(UpdatePasswordRequest request);

    void registerResident(ResidentRegisterRequest request);

    List<ResidentRegisterComplexOptionVO> residentRegisterComplexOptions();

    SendResetCodeVO sendResetCode(SendResetCodeRequest request);

    void resetPasswordByCode(ResetPasswordByCodeRequest request);
}
