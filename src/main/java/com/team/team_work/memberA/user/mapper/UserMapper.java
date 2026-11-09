package com.team.team_work.memberA.user.mapper;
import com.team.team_work.memberA.user.entity.UserAccount;
import org.apache.ibatis.annotations.*;
@Mapper
public interface UserMapper {
 UserAccount findByLogin(@Param("login") String login);
 UserAccount findById(@Param("id") Long id);
 UserAccount lockUser(@Param("id") Long id);
 int insertUser(UserAccount user);
 int updateProfile(UserAccount user);
 int updateAddress(@Param("id") Long id,@Param("address") String address);
}
