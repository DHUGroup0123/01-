package com.team.team_work.memberA.user.entity;

public class UserAccount {
    private Long userId;
    private String loginName;
    private String passwordHash;
    private String nickname;
    private String phone;
    private Integer status;
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { userId=value; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String value) { loginName=value; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String value) { passwordHash=value; }
    public String getNickname() { return nickname; }
    public void setNickname(String value) { nickname=value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone=value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status=value; }
}
