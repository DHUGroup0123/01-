package com.team.team_work.memberA.mall.controller;

import com.team.team_work.common.ApiResponse;
import com.team.team_work.memberA.mall.mapper.EnvironmentMapper;
import com.team.team_work.memberA.mall.service.EnvironmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

/** First-week entry points. Authentication and operations are second-week tasks. */
@Controller
public class WeekOneController {
    private final EnvironmentService service;
    private final EnvironmentMapper mapper;
    public WeekOneController(EnvironmentService service, EnvironmentMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    @GetMapping({"/", "/index"})
    public String home(Model model) {
        model.addAttribute("environment", service.inspect());
        model.addAttribute("products", mapper.findPreviewProducts());
        return "memberA/mall/index";
    }
    @GetMapping({"/admin", "/admin/", "/admin/login"})
    public String admin(Model model) {
        model.addAttribute("environment", service.inspect());
        return "memberA/mall/admin-start";
    }
    @GetMapping({"/login", "/register"})
    public String accountStart() { return "memberA/mall/account-start"; }
    @GetMapping("/api/environment")
    @ResponseBody
    public ApiResponse<Map<String, Object>> environment() {
        return ApiResponse.success(service.inspect());
    }
}
