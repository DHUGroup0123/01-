package com.team.team_work.memberA.user.service;
import com.team.team_work.memberA.user.dto.*;
import com.team.team_work.memberA.user.entity.UserAccount;
import com.team.team_work.memberA.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.*;
@Service
public class UserService {
 private final UserMapper mapper;private final PasswordHasher passwords;private final String dummyHash;
 public UserService(UserMapper mapper,PasswordHasher passwords){this.mapper=mapper;this.passwords=passwords;dummyHash=passwords.hash(UUID.randomUUID().toString());}
 @Transactional(isolation=Isolation.SERIALIZABLE)
 public UserViews.Profile register(UserRequests.Credentials input){
  if(input.password().length()<8)throw new BusinessException(400,"新密码至少需要8位");
  if(mapper.findByLogin(input.loginName())!=null)throw new BusinessException(409,"手机号已注册");
  UserAccount u=new UserAccount();u.setLoginName(input.loginName());u.setNickname(input.loginName());u.setPasswordHash(passwords.hash(input.password()));u.setStatus(0);u.setAddress("");u.setIntroduceSign("");mapper.insertUser(u);return UserViews.Profile.of(u);
 }
 public UserAccount login(UserRequests.Credentials input){UserAccount u=mapper.findByLogin(input.loginName());boolean valid=passwords.matches(input.password(),u==null?dummyHash:u.getPasswordHash());if(!valid||u==null)throw new BusinessException(401,"手机号或密码不正确");requireActive(u);return u;}
 public UserAccount active(Long id){UserAccount u=mapper.findById(id);requireActive(u);return u;}
 private void requireActive(UserAccount u){if(u==null)throw new BusinessException(401,"请重新登录");if(u.getStatus()!=0)throw new BusinessException(403,"账号已被禁用，请联系管理员");}
 @Transactional public UserViews.Profile updateProfile(Long id,UserRequests.Profile input){UserAccount u=mapper.lockUser(id);requireActive(u);if(input.phone()!=null&&!input.phone().isBlank()&&!input.phone().equals(u.getLoginName()))throw new BusinessException(400,"登录手机号不可修改");u.setNickname(input.nickname().trim());u.setIntroduceSign(input.introduceSign()==null?u.getIntroduceSign():input.introduceSign().trim());mapper.updateProfile(u);return UserViews.Profile.of(u);}
 public List<UserViews.Address> addresses(Long id){UserAccount u=active(id);return u.getAddress()==null||u.getAddress().isBlank()?List.of():List.of(new UserViews.Address(id.toString(),u.getAddress(),true));}
 @Transactional public UserViews.Address saveAddress(Long userId,Long id,UserRequests.Address input){UserAccount u=mapper.lockUser(userId);requireActive(u);if(id!=null&&!id.equals(userId))throw new BusinessException(404,"收货信息不存在");if(id==null&&u.getAddress()!=null&&!u.getAddress().isBlank())throw new BusinessException(409,"已保存收货信息，请选择编辑");mapper.updateAddress(userId,input.address().trim());return new UserViews.Address(userId.toString(),input.address().trim(),true);}
 @Transactional public void deleteAddress(Long userId,Long id){UserAccount u=mapper.lockUser(userId);requireActive(u);if(!userId.equals(id)||u.getAddress()==null||u.getAddress().isBlank())throw new BusinessException(404,"收货信息不存在");mapper.updateAddress(userId,"");}
}
