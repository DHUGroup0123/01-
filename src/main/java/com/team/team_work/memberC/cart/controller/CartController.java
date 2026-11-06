package com.team.team_work.memberC.cart.controller;

import com.team.team_work.memberC.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/memberC/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/add")
    public Map<String, Object> add(@RequestParam Long userId, @RequestParam Integer count) {
        Map<String, Object> res = new HashMap<>();
        try {
            res.put("code", 200);
            res.put("msg", cartService.addToCart(userId, count));
        } catch (Exception e) {
            res.put("code", 500);
            res.put("msg", e.getMessage());
        }
        return res;
    }

    @GetMapping("/delete")
    public Map<String, Object> delete(@RequestParam Long currentUserId) {
        Map<String, Object> res = new HashMap<>();
        try {
            res.put("code", 200);
            res.put("msg", cartService.deleteCartItem(currentUserId));
        } catch (Exception e) {
            res.put("code", 403);
            res.put("msg", e.getMessage());
        }
        return res;
    }
}
