package com.zhongshu.zswy.service;

import com.zhongshu.zswy.dao.CardCodeDao;
import com.zhongshu.zswy.dao.OrderDao;
import com.zhongshu.zswy.dao.ProductDao;
import com.zhongshu.zswy.entity.CardCode;
import com.zhongshu.zswy.entity.Product;
import com.zhongshu.zswy.entity.ZhongshuOrder;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;

@Service
public class OrderService {

    private final ProductDao productDao;
    private final CardCodeDao cardCodeDao;
    private final OrderDao orderDao;

    // HTML 端的正则校验逻辑（手机号/邮箱）
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    public OrderService(ProductDao productDao, CardCodeDao cardCodeDao, OrderDao orderDao) {
        this.productDao = productDao;
        this.cardCodeDao = cardCodeDao;
        this.orderDao = orderDao;
    }

    /**
     * 对应页面的 submitPayBtn -> processDelivery
     */
    @Transactional
    public PayResult pay(Long productId, String contact, String payType) {
        if (contact == null || contact.isBlank()) {
            throw new IllegalArgumentException("请填写手机号或邮箱");
        }
        String c = contact.trim();
        if (!EMAIL.matcher(c).matches() && !PHONE.matcher(c).matches()) {
            throw new IllegalArgumentException("请填写正确的手机号或邮箱");
        }

        if (!"wechat".equals(payType) && !"alipay".equals(payType)) {
            throw new IllegalArgumentException("支付方式不正确");
        }

        Product p = productDao.findById(productId);
        if (p == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        CardCode code = cardCodeDao.findAndLockAvailableOne(productId);
        if (code == null) {
            throw new IllegalStateException("库存不足");
        }

        // 对应 JS：shift() 从池移除后，等价于标记 used
        code.setStatus("used");

        // Java 等价生成：ZSNY + 时间戳 + 随机数
        String orderNo = generateOrderNo();

        ZhongshuOrder o = new ZhongshuOrder();
        o.setOrderNo(orderNo);
        o.setProductName(p.getName());
        o.setAmount(p.getPrice());
        o.setPayType(payType);
        o.setContact(c);
        o.setCardCode(code.getCode());
        o.setPayTime(Instant.now());

        orderDao.save(o);

        return new PayResult(orderNo, code.getCode());
    }

    public OrderQueryResult queryByInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("请输入订单号、手机号或邮箱");
        }

        ZhongshuOrder o = orderDao.findByOrderNoOrContact(input.trim());
        if (o == null) {
            throw new IllegalStateException("未找到订单");
        }

        return new OrderQueryResult(o.getOrderNo(), o.getCardCode(), o.getContact());
    }

    private String generateOrderNo() {
        long now = System.currentTimeMillis();
        int rand = (int) (Math.random() * 10000);
        return "ZSNY" + now + rand;
    }

    public record PayResult(
            @JsonProperty("order_no") String orderNo,
            @JsonProperty("card_code") String cardCode
    ) {}

    public record OrderQueryResult(
            @JsonProperty("order_no") String orderNo,
            @JsonProperty("card_code") String cardCode,
            String contact
    ) {}
}

