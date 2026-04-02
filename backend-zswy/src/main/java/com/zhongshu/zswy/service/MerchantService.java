package com.zhongshu.zswy.service;

import com.zhongshu.zswy.dao.CardCodeDao;
import com.zhongshu.zswy.dao.MerchantUserDao;
import com.zhongshu.zswy.dao.OrderDao;
import com.zhongshu.zswy.dao.ProductDao;
import com.zhongshu.zswy.dao.WithdrawDao;
import com.zhongshu.zswy.entity.CardCode;
import com.zhongshu.zswy.entity.MerchantUser;
import com.zhongshu.zswy.entity.Product;
import com.zhongshu.zswy.entity.Withdraw;
import com.zhongshu.zswy.entity.ZhongshuOrder;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class MerchantService {

    private final MerchantUserDao merchantUserDao;
    private final OrderDao orderDao;
    private final CardCodeDao cardCodeDao;
    private final ProductDao productDao;
    private final WithdrawDao withdrawDao;

    public MerchantService(MerchantUserDao merchantUserDao, OrderDao orderDao, CardCodeDao cardCodeDao, ProductDao productDao, WithdrawDao withdrawDao) {
        this.merchantUserDao = merchantUserDao;
        this.orderDao = orderDao;
        this.cardCodeDao = cardCodeDao;
        this.productDao = productDao;
        this.withdrawDao = withdrawDao;
    }

    @Transactional
    public void register(String phone, String password) {
        if (phone == null || phone.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("请填写完整");
        }
        String p = phone.trim();
        String pwd = password.trim();

        MerchantUser exist = merchantUserDao.findByPhone(p);
        if (exist != null) {
            throw new IllegalStateException("手机号已注册");
        }

        MerchantUser u = new MerchantUser();
        u.setPhone(p);
        u.setPassword(pwd);
        u.setUsername(p); // HTML 登录注册用 phone 作为 username
        merchantUserDao.save(u);
    }

    public MerchantUser authenticate(String phone, String password) {
        if (phone == null || phone.isBlank() || password == null) return null;
        return merchantUserDao.findByPhoneAndPassword(phone.trim(), password.trim());
    }

    /**
     * 对应页面 renderMerchantDashboard 的统计区域与两张表数据。
     */
    public MerchantDashboard dashboard() {
        List<ZhongshuOrder> orders = orderDao.listAllOrders();

        long totalCards = 0;
        // HTML 里是按池里商品统计；这里按 product 表统计所有商品的 available 卡密数量
        List<Product> allProducts = productDao.listProducts("all", null);
        for (Product p : allProducts) {
            totalCards += cardCodeDao.countAvailableByProductId(p.getId());
        }

        BigDecimal totalRevenue = orders.stream()
                .map(ZhongshuOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Withdraw> withdraws = withdrawDao.listAllWithdraws();
        BigDecimal withdrawSumExcludingRejected = withdraws.stream()
                .filter(w -> !"rejected".equalsIgnoreCase(w.getStatus()))
                .map(Withdraw::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableBalance = totalRevenue.subtract(withdrawSumExcludingRejected);

        List<OrderRow> orderRows = orders.stream()
                .map(o -> new OrderRow(
                        o.getOrderNo(),
                        o.getProductName(),
                        o.getAmount(),
                        o.getPayType(),
                        o.getContact(),
                        o.getPayTime(),
                        o.getCardCode()
                ))
                .toList();

        return new MerchantDashboard(
                orders.size(),
                totalRevenue,
                totalCards,
                availableBalance,
                orderRows,
                withdraws
        );
    }

    @Transactional
    public void addCards(Long productId, List<String> codes) {
        if (productId == null) throw new IllegalArgumentException("商品ID不能为空");
        if (codes == null || codes.isEmpty()) throw new IllegalArgumentException("卡密不能为空");

        List<CardCode> toPersist = new ArrayList<>();
        for (String raw : codes) {
            String code = raw == null ? null : raw.trim();
            if (code == null || code.isEmpty()) continue;

            CardCode c = new CardCode();
            c.setProductId(productId);
            c.setCode(code);
            c.setStatus("available");
            toPersist.add(c);
        }

        if (toPersist.isEmpty()) throw new IllegalArgumentException("卡密不能为空");
        cardCodeDao.saveAll(toPersist);
    }

    @Transactional
    public void applyWithdraw(BigDecimal amount, String method, String account) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("无效金额");
        if (!"alipay".equals(method) && !"wechat".equals(method)) throw new IllegalArgumentException("提现方式不正确");
        if (account == null || account.isBlank()) throw new IllegalArgumentException("请输入收款账号");

        MerchantDashboard db = dashboard();
        if (amount.compareTo(db.availableBalance()) > 0) throw new IllegalStateException("金额超限");

        Withdraw w = new Withdraw();
        w.setAmount(amount);
        w.setMethod(method);
        w.setAccount(account.trim());
        w.setStatus("pending");
        w.setApplyTime(Instant.now());
        withdrawDao.save(w);
    }

    public record MerchantDashboard(
            int totalOrders,
            BigDecimal totalRevenue,
            long totalCards,
            BigDecimal availableBalance,
            List<OrderRow> orders,
            List<Withdraw> withdraws
    ) {
    }

    public record OrderRow(
            @JsonProperty("order_no") String orderNo,
            @JsonProperty("product_name") String productName,
            BigDecimal amount,
            @JsonProperty("pay_type") String payType,
            String contact,
            @JsonProperty("pay_time") Instant payTime,
            @JsonProperty("card_code") String cardCode
    ) {
    }
}

