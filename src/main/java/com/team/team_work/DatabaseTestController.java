package com.team.team_work;

import com.team.team_work.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseTestController {
    private final JdbcTemplate jdbcTemplate;
    public DatabaseTestController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }
    @GetMapping("/test-db")
    public ApiResponse<Integer> testDatabase() {
        return ApiResponse.success(jdbcTemplate.queryForObject("SELECT 1", Integer.class));
    }
}
