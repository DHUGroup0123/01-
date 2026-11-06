package com.team.team_work.memberB.admin.service;
import jakarta.servlet.http.HttpSession;
public record AdminSession(long id,int credentialVersion) {
 public static final String KEY="memberB.admin";
 public static AdminSession current(HttpSession session){
  if(session==null || !(session.getAttribute(KEY) instanceof AdminSession admin))throw new BusinessException(401,"请先登录后台");return admin;
 }
}
