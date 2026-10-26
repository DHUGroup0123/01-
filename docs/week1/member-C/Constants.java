package com.example.mall.common;

/ 交易模块常量类
 /
public class Constants {
    // 购物车中单个商品的最大购买数量
    public static final int SHOPPING_CART_ITEM_LIMIT_NUMBER = 5;
    
    // 购物车中商品的最大种类数量
    public static final int SHOPPING_CART_ITEM_TOTAL_NUMBER = 13;
    
    // 前台订单列表分页条数
    public static final int ORDER_SEARCH_PAGE_LIMIT = 3;
    
    // Session中保存用户信息的Key
    public static final String MALL_USER_SESSION_KEY = "mallUser";
    
    // 商品上架状态
    public static final int SELL_STATUS_UP = 0;
    // 商品下架状态
    public static final int SELL_STATUS_DOWN = 1;
}