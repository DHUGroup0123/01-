package com.team.team_work;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseTestController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test-db")
    public String testDatabase() {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );

        return "MySQL连接成功，查询结果：" + result;
    }
}
