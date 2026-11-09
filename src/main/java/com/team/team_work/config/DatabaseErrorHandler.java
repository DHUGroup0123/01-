package com.team.team_work.config;

import com.team.team_work.common.ApiResponse;
import com.team.team_work.memberA.mall.controller.WeekOneController;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/** Database diagnostics expose no JDBC URL, username, password, or stack trace. */
@ControllerAdvice(assignableTypes = {WeekOneController.class, com.team.team_work.DatabaseTestController.class})
public class DatabaseErrorHandler {
    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> unavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            new ApiResponse<>(503, "数据库尚未就绪，请按运行说明检查连接并执行初始化。", null));
    }
}
