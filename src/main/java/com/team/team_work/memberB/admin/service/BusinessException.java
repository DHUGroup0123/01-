package com.team.team_work.memberB.admin.service;
public class BusinessException extends RuntimeException {
 public final int status;
 public BusinessException(int status,String message){super(message);this.status=status;}
 public static BusinessException bad(String message){return new BusinessException(400,message);}
}
