package com.zhongshu.zswy.controller;

import com.zhongshu.zswy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 对应：页面 submitPayBtn -> processDelivery
     */
    @PostMapping("/pay")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderService.PayResult pay(@RequestBody PayRequest req) {
        return orderService.pay(req.productId(), req.contact(), req.payType());
    }

    /**
     * 对应：页面 queryOrderBtn
     */
    @GetMapping("/query")
    public OrderService.OrderQueryResult query(@RequestParam String input) {
        return orderService.queryByInput(input);
    }

    public record PayRequest(Long productId, String contact, String payType) {
    }
}

