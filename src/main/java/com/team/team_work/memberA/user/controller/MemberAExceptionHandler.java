package com.team.team_work.memberA.user.controller;
import com.team.team_work.common.ApiResponse;
import com.team.team_work.memberA.user.service.BusinessException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages="com.team.team_work.memberA")
public class MemberAExceptionHandler {
    private ResponseEntity<ApiResponse<Void>> error(int code,String message) {return ResponseEntity.status(code).body(new ApiResponse<>(code,message,null));}
    @ExceptionHandler(BusinessException.class) public ResponseEntity<ApiResponse<Void>> business(BusinessException e) {return error(e.getStatus(),e.getMessage());}
    @ExceptionHandler({MethodArgumentNotValidException.class,MethodArgumentTypeMismatchException.class,HttpMessageNotReadableException.class,ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> input(Exception e) {return error(400,"输入格式不正确，请检查账号、密码、手机号或地址长度");}
    @ExceptionHandler(DataAccessException.class) public ResponseEntity<ApiResponse<Void>> database(DataAccessException e) {return error(503,"数据库暂不可用，请稍后重试");}
}
