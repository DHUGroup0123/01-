package com.team.team_work.memberC.order.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public String createOrder(Long userId, Integer count) {
        int currentStock = 2; // 模拟库存 2 件
        if (count > currentStock) {
            throw new RuntimeException("库存不足：当前仅剩 " + currentStock + " 件！");
        }
        return "ORD" + System.currentTimeMillis();
    }

    public String getOrderDetail(Long currentUserId) {
        Long orderOwnerUserId = 1001L; // 模拟订单属于 1001
        if (!orderOwnerUserId.equals(currentUserId)) {
            throw new RuntimeException("越权警告：不能查看他人的订单！");
        }
        return "订单详情获取成功，订单归属正确！";
    }
}
