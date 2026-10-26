package com.team.team_work.memberA.mall.service;

import com.team.team_work.memberA.mall.mapper.EnvironmentMapper;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EnvironmentService {
    private final EnvironmentMapper mapper;
    private final Environment environment;
    public EnvironmentService(EnvironmentMapper mapper, Environment environment) {
        this.mapper = mapper;
        this.environment = environment;
    }
    public Map<String, Object> inspect() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("projectName", "01电商系统");
        data.put("stage", "第一周：需求分析与环境搭建");
        data.put("springBootVersion", SpringBootVersion.getVersion());
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("databaseMode", environment.matchesProfiles("demo") ? "H2 临时演示" : "MySQL");
        data.put("schemaVersion", mapper.schemaVersion());
        data.put("categoryCount", mapper.countCategories());
        data.put("productCount", mapper.countProducts());
        return data;
    }
}
