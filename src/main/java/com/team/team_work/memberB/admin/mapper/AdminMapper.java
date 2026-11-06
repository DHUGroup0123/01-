package com.team.team_work.memberB.admin.mapper;
import com.team.team_work.memberB.admin.entity.*;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper
public interface AdminMapper {
 AdminAccount byLogin(String login);
 AdminAccount byId(long id);
 int insertAdmin(AdminAccount account);
 int profile(@Param("id")long id,@Param("nickName")String nickName);
 int password(@Param("id")long id,@Param("password")String password);
 int guard();
 String receipt(@Param("actor")long actor,@Param("key")String key);
 String fingerprint(@Param("actor")long actor,@Param("key")String key);
 int remember(@Param("actor")long actor,@Param("key")String key,@Param("fingerprint")String fingerprint,@Param("result")String result);
 List<Member> members(@Param("q")String q,@Param("offset")int offset,@Param("size")int size);
 long memberCount(String q);
 int lockMember(@Param("id")long id,@Param("locked")int locked);
 Member member(long id);
 List<Carousel> carousels();
 Carousel carousel(long id);
 int insertCarousel(Carousel item);
 int updateCarousel(Carousel item);
 int deleteCarousel(long id);
 List<HomeConfig> configs();
 HomeConfig config(long id);
 int insertConfig(HomeConfig item);
 int updateConfig(HomeConfig item);
 int deleteConfig(long id);
}
