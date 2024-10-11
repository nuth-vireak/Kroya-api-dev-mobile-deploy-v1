package com.kshrd.kroya_api.payload.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoRequest {
    private String email;
    private String userName;
    private String phoneNumber;
    private String address;
}
