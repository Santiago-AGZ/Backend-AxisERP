package com.axiserp.auth.ports.input;

import com.axiserp.auth.application.dto.response.UserInfoResponse;

public interface GetUserInfoUseCase {

    UserInfoResponse getUserInfo(String userId);
}
