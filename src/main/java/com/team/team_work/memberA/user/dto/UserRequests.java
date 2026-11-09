package com.team.team_work.memberA.user.dto;
import jakarta.validation.constraints.*;
public final class UserRequests {
 private UserRequests() { }
 public record Credentials(@NotBlank @Pattern(regexp="1[3-9][0-9]{9}",message="请使用11位手机号") String loginName,@NotBlank @Size(min=6,max=72) String password,@NotBlank @Size(max=8) String captcha) { }
 public record Profile(@NotBlank @Size(max=40) String nickname,@Pattern(regexp="^$|1[3-9][0-9]{9}") String phone,@Size(max=100) String introduceSign) { }
 public record Address(@NotBlank @Size(max=100) String address) { }
}
