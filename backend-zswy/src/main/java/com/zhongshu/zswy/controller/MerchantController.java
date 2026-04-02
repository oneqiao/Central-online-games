package com.zhongshu.zswy.controller;

import com.zhongshu.zswy.service.MerchantService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private static final String SESSION_KEY = "merchant_phone";

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    private void requireLogin(HttpSession session) {
        if (session.getAttribute(SESSION_KEY) == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public java.util.Map<String, String> register(@RequestBody RegisterReq req) {
        merchantService.register(req.phone(), req.password());
        return java.util.Map.of("message", "注册成功，请登录");
    }

    @PostMapping("/login")
    public java.util.Map<String, String> login(@RequestBody LoginReq req, HttpSession session) {
        var u = merchantService.authenticate(req.phone(), req.password());
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账号或密码错误");
        }
        session.setAttribute(SESSION_KEY, u.getPhone());
        return java.util.Map.of("message", "登录成功");
    }

    @PostMapping("/logout")
    public java.util.Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return java.util.Map.of("message", "已退出商家中心");
    }

    @GetMapping("/dashboard")
    public MerchantService.MerchantDashboard dashboard(HttpSession session) {
        requireLogin(session);
        return merchantService.dashboard();
    }

    @PostMapping("/cards/add")
    @ResponseStatus(HttpStatus.CREATED)
    public java.util.Map<String, String> addCards(HttpSession session, @RequestBody AddCardsReq req) {
        requireLogin(session);
        List<String> codes = splitCodes(req.cardsText());
        merchantService.addCards(req.productId(), codes);
        return java.util.Map.of("message", "添加成功");
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public java.util.Map<String, String> withdraw(HttpSession session, @RequestBody WithdrawReq req) {
        requireLogin(session);
        merchantService.applyWithdraw(req.amount(), req.method(), req.account());
        return java.util.Map.of("message", "提现申请已提交");
    }

    /**
     * 对应：前端 merExportBtn 导出所需的订单列表
     */
    @GetMapping("/orders")
    public List<MerchantService.OrderRow> orders(HttpSession session) {
        requireLogin(session);
        return merchantService.dashboard().orders();
    }

    private static List<String> splitCodes(String cardsText) {
        if (cardsText == null || cardsText.isBlank()) return List.of();
        return Arrays.stream(cardsText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public record RegisterReq(String phone, String password) {
    }

    public record LoginReq(String phone, String password) {
    }

    public record AddCardsReq(Long productId, String cardsText) {
    }

    public record WithdrawReq(BigDecimal amount, String method, String account) {
    }
}

