package com.team.team_work.service;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
@Component public class CommerceLocks {
 private final JdbcTemplate jdbc;
 public CommerceLocks(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public void user(Long id){jdbc.queryForObject("SELECT user_id FROM tb_mall_user WHERE user_id=? FOR UPDATE",Long.class,id);}
}
