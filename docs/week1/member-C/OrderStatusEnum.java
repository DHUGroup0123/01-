package com.example.mall.common;

/**
 * 订单状态枚举类（成员C设计）
 */
public enum OrderStatusEnum {

    ORDER_PRE_PAY(0, "待支付"),
    ORDER_PAID(1, "已支付"),
    ORDER_PACKAGED(2, "配货完成"),
    ORDER_EXPRESS(3, "出库成功"),
    ORDER_SUCCESS(4, "交易成功"),
    ORDER_CLOSED_BY_USER(-1, "用户取消"),
    ORDER_CLOSED_BY_EXPIRED(-2, "超时关闭"),
    ORDER_CLOSED_BY_ADMIN(-3, "商家关闭");

    private final int status;
    private final String desc;

    OrderStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() { return status; }
    public String getDesc() { return desc; }
}