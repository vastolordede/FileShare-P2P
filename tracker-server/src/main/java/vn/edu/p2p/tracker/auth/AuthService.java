package vn.edu.p2p.tracker.auth;

import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LoginResponse;
import vn.edu.p2p.common.dto.LogoutRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request, String remoteIp) throws AuthException;

    void logout(LogoutRequest request) throws AuthException;
}
