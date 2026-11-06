package com.team.team_work.memberB.admin.controller;
import com.team.team_work.memberB.admin.dto.ApiResult;
import com.team.team_work.memberB.admin.service.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
@RestControllerAdvice(basePackages="com.team.team_work.memberB")
public class ApiErrors {
 @ExceptionHandler(BusinessException.class)public ResponseEntity<ApiResult> business(BusinessException e){return ResponseEntity.status(e.status).body(new ApiResult(e.status,e.getMessage(),null));}
 @ExceptionHandler({org.springframework.web.bind.MissingRequestHeaderException.class,org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})public ResponseEntity<ApiResult> input(Exception e){return ResponseEntity.badRequest().body(new ApiResult(400,"请求参数缺失或格式错误",null));}
 @ExceptionHandler(DataIntegrityViolationException.class)public ResponseEntity<ApiResult> conflict(Exception e){return ResponseEntity.status(409).body(new ApiResult(409,"数据已存在或关联约束不满足",null));}
 @ExceptionHandler(MaxUploadSizeExceededException.class)public ResponseEntity<ApiResult> tooLarge(Exception e){return ResponseEntity.status(413).body(new ApiResult(413,"上传文件过大",null));}
}
