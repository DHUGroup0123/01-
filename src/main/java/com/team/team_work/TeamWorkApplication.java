package com.team.team_work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.mybatis.spring.annotation.MapperScan({"com.team.team_work.dao","com.team.team_work.memberA.user.mapper","com.team.team_work.memberA.mall.mapper"})
@SpringBootApplication
public class TeamWorkApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamWorkApplication.class, args);
	}
}
