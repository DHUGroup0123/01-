package com.team.team_work.memberA.mall.controller;

import com.team.team_work.common.ApiResponse;
import com.team.team_work.memberA.mall.mapper.EnvironmentMapper;
import com.team.team_work.memberA.mall.service.EnvironmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

/** Retained environment and temporary admin entry points; user and mall routes live in MallController. */
@Controller
public class WeekOneController {
    private final EnvironmentService service;
    private final EnvironmentMapper mapper;
    public WeekOneController(EnvironmentService service, EnvironmentMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    @GetMapping("/api/environment")
    @ResponseBody
    public ApiResponse<Map<String, Object>> environment() {
        return ApiResponse.success(service.inspect());
    }
}
