package com.team.team_work.memberA.user.mapper;
import com.team.team_work.memberA.user.entity.*;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    UserAccount findByLogin(@Param("login") String login);
    UserAccount findById(@Param("id") Long id);
    UserAccount lockUser(@Param("id") Long id);
    int insertUser(UserAccount user);
    int updateProfile(UserAccount user);
    List<UserAddress> addresses(@Param("userId") Long userId);
    UserAddress address(@Param("userId") Long userId,@Param("id") Long id);
    int insertAddress(UserAddress address);
    int updateAddress(UserAddress address);
    int deleteAddress(@Param("userId") Long userId,@Param("id") Long id);
    int clearDefaults(@Param("userId") Long userId);
    int setDefault(@Param("userId") Long userId,@Param("id") Long id);
}
