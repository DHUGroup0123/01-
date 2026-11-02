package com.team.team_work.memberA.user.dto;
import jakarta.validation.constraints.*;

public final class UserRequests {
    private UserRequests() { }
    public record Credentials(
        @NotBlank @Pattern(regexp="[a-zA-Z0-9_]{4,32}",message="账号需为4至32位字母、数字或下划线") String loginName,
        @NotBlank @Size(min=8,max=72,message="密码长度需为8至72位") String password,
        @NotBlank @Size(max=8) String captcha) { }
    public record Profile(
        @NotBlank @Size(max=40) String nickname,
        @Pattern(regexp="^$|1[3-9][0-9]{9}",message="手机号格式不正确") String phone) { }
    public record Address(
        @NotBlank @Size(max=40) String receiverName,
        @NotBlank @Pattern(regexp="1[3-9][0-9]{9}",message="收货手机号格式不正确") String receiverPhone,
        @NotBlank @Size(max=40) String province,
        @NotBlank @Size(max=40) String city,
        @NotBlank @Size(max=40) String district,
        @NotBlank @Size(max=200) String detailAddress,
        @NotNull Boolean isDefault) { }
}
