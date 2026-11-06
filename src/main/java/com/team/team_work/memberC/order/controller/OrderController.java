package com.team.team_work.memberC.order.controller;

import com.team.team_work.memberC.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/memberC/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/create")
    public Map<String, Object> create(@RequestParam Long userId, @RequestParam Integer count) {
        Map<String, Object> res = new HashMap<>();
        try {
            String orderNo = orderService.createOrder(userId, count);
            res.put("code", 200);
            res.put("msg", "下单成功！");
            res.put("orderNo", orderNo);
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", e.getMessage());
        }
        return res;
    }

    @GetMapping("/detail")
    public Map<String, Object> detail(@RequestParam Long currentUserId) {
        Map<String, Object> res = new HashMap<>();
        try {
            String msg = orderService.getOrderDetail(currentUserId);
            res.put("code", 200);
            res.put("msg", msg);
        } catch (Exception e) {
            res.put("code", 403);
            res.put("msg", e.getMessage());
        }
        return res;
    }
}
