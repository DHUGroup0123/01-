package com.team.team_work.memberA.user.entity;

public class UserAddress {
    private Long addressId;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long value) { addressId=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { userId=value; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String value) { receiverName=value; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String value) { receiverPhone=value; }
    public String getProvince() { return province; }
    public void setProvince(String value) { province=value; }
    public String getCity() { return city; }
    public void setCity(String value) { city=value; }
    public String getDistrict() { return district; }
    public void setDistrict(String value) { district=value; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String value) { detailAddress=value; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer value) { isDefault=value; }
}
