package org.example.gateway.config;

import lombok.Data;

@Data
public class JwtClaimDto {

    private String nickname;
    private String bio;
    private String url;
    private String occupation;
    private String location;
    private String email;
    private boolean emailVerified;
    private String profileImage;

}
