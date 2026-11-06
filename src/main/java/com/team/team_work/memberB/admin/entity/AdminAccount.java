package com.team.team_work.memberB.admin.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
public class AdminAccount {
 public Long adminUserId;
 public String loginUserName;
 public String nickName;
 public Integer locked;
 public Integer credentialVersion;
 @JsonIgnore public String loginPassword;
}
