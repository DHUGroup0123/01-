package com.team.team_work.memberC.cart.service;


import com.team.team_work.memberC.cart.entity.CartItem;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    // 加入购物车：校验上限
    public String addToCart(Long userId, Integer count) {
        if (count > 5) {
            throw new RuntimeException("加入失败：单个商品最多买 5 件！");
        }
        return "加入购物车成功，数量：" + count;
    }

    // 删除购物车：防越权
    public String deleteCartItem(Long currentUserId) {
        // 假设数据库里这条购物车记录属于用户 1001
        Long ownerUserId = 1001L;
        if (!ownerUserId.equals(currentUserId)) {
            throw new RuntimeException("越权警告：不能删除别人的购物车！");
        }
        return "删除成功！";
    }
}
