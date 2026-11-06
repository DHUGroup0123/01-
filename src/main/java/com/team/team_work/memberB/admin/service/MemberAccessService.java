package com.team.team_work.memberB.admin.service;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import org.springframework.stereotype.Service;
/** Member A should call this at login and on authenticated requests, including existing sessions. */
@Service
public class MemberAccessService {
 private final AdminMapper mapper;
 public MemberAccessService(AdminMapper mapper){this.mapper=mapper;}
 public void requireActive(long userId){var member=mapper.member(userId);if(member==null || member.lockedFlag!=0)throw new BusinessException(403,"会员不存在或已被禁用");}
}
