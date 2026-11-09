package com.team.team_work.memberA.user.dto;
import com.team.team_work.memberA.user.entity.UserAccount;
public final class UserViews {
 private UserViews() { }
 public record Profile(String userId,String loginName,String nickname,String phone,String introduceSign) {
  public static Profile of(UserAccount u){return new Profile(u.getUserId().toString(),u.getLoginName(),u.getNickname(),u.getLoginName(),u.getIntroduceSign());}
 }
 public record Address(String addressId,String address,boolean isDefault) { }
}
