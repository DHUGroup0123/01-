package com.team.team_work.memberA.user.service;
import com.team.team_work.memberA.user.dto.*;
import com.team.team_work.memberA.user.entity.*;
import com.team.team_work.memberA.user.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class UserService {
    private final UserMapper mapper;
    private final PasswordHasher passwords;
    private final String dummyHash;
    public UserService(UserMapper mapper,PasswordHasher passwords) {
        this.mapper=mapper;this.passwords=passwords;dummyHash=passwords.hash(UUID.randomUUID().toString());
    }
    @Transactional
    public UserViews.Profile register(UserRequests.Credentials input) {
        UserAccount user=new UserAccount();
        user.setLoginName(input.loginName().toLowerCase(Locale.ROOT));
        user.setNickname(user.getLoginName());user.setStatus(0);user.setPasswordHash(passwords.hash(input.password()));
        try {mapper.insertUser(user);} catch(DuplicateKeyException e) {throw new BusinessException(409,"账号已存在，请更换账号");}
        return UserViews.Profile.of(user);
    }
    public UserAccount login(UserRequests.Credentials input) {
        UserAccount user=mapper.findByLogin(input.loginName().toLowerCase(Locale.ROOT));
        boolean valid=passwords.matches(input.password(),user==null?dummyHash:user.getPasswordHash());
        if(!valid || user==null) throw new BusinessException(401,"账号或密码不正确");
        if(user.getStatus()!=0) throw new BusinessException(403,"账号已被禁用，请联系管理员");
        return user;
    }
    public UserAccount active(Long id) {
        UserAccount user=mapper.findById(id);
        requireActive(user);return user;
    }
    private void requireActive(UserAccount user) {
        if(user==null) throw new BusinessException(401,"请重新登录");
        if(user.getStatus()!=0) throw new BusinessException(403,"账号已被禁用，请联系管理员");
    }
    @Transactional
    public UserViews.Profile updateProfile(Long id,UserRequests.Profile input) {
        UserAccount user=mapper.lockUser(id);requireActive(user);
        user.setNickname(input.nickname().trim());user.setPhone(input.phone()==null?null:input.phone().trim());
        mapper.updateProfile(user);return UserViews.Profile.of(user);
    }
    public List<UserViews.Address> addresses(Long id) {return mapper.addresses(id).stream().map(UserViews.Address::of).toList();}
    @Transactional
    public UserViews.Address saveAddress(Long userId,Long id,UserRequests.Address input) {
        requireActive(mapper.lockUser(userId)); // Serializes default-address changes across requests/processes.
        List<UserAddress> before=mapper.addresses(userId);
        UserAddress address=id==null?new UserAddress():mapper.address(userId,id);
        if(address==null) throw new BusinessException(404,"收货地址不存在");
        if(id==null && before.size()>=20) throw new BusinessException(409,"最多保存20条收货地址");
        address.setUserId(userId);address.setReceiverName(input.receiverName().trim());address.setReceiverPhone(input.receiverPhone());
        address.setProvince(input.province().trim());address.setCity(input.city().trim());address.setDistrict(input.district().trim());address.setDetailAddress(input.detailAddress().trim());
        address.setIsDefault(input.isDefault() || before.isEmpty()?1:0);
        if(address.getIsDefault()==1) mapper.clearDefaults(userId);
        if(id==null) mapper.insertAddress(address); else mapper.updateAddress(address);
        ensureDefault(userId);
        return UserViews.Address.of(mapper.address(userId,address.getAddressId()));
    }
    @Transactional
    public void deleteAddress(Long userId,Long id) {
        requireActive(mapper.lockUser(userId));
        if(mapper.deleteAddress(userId,id)!=1) throw new BusinessException(404,"收货地址不存在");
        ensureDefault(userId);
    }
    private void ensureDefault(Long id) {
        List<UserAddress> addresses=mapper.addresses(id);
        if(!addresses.isEmpty() && addresses.stream().noneMatch(a->a.getIsDefault()==1)) mapper.setDefault(id,addresses.get(0).getAddressId());
    }
}
