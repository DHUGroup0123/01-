package com.team.team_work.memberB.admin.dto;
public record ApiResult(int resultCode, String message, Object data) {
 public static ApiResult ok(Object data) { return new ApiResult(200,"操作成功",data); }
}
