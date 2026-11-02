package com.team.team_work.memberA.user.dto;
import com.team.team_work.memberA.user.entity.*;

public final class UserViews {
    private UserViews() { }
    public record Profile(String userId,String loginName,String nickname,String phone) {
        public static Profile of(UserAccount user) {return new Profile(user.getUserId().toString(),user.getLoginName(),user.getNickname(),user.getPhone());}
    }
    public record Address(String addressId,String receiverName,String receiverPhone,String province,String city,String district,String detailAddress,boolean isDefault) {
        public static Address of(UserAddress a) {return new Address(a.getAddressId().toString(),a.getReceiverName(),a.getReceiverPhone(),a.getProvince(),a.getCity(),a.getDistrict(),a.getDetailAddress(),a.getIsDefault()==1);}
    }
}
