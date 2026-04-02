package com.zhongshu.zswy.config;

import com.zhongshu.zswy.dao.MerchantUserDao;
import com.zhongshu.zswy.dao.ProductDao;
import com.zhongshu.zswy.entity.MerchantUser;
import com.zhongshu.zswy.entity.Product;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 启动时初始化固定商品（1-7）和默认商家账号。
 * 对齐前端 zswy.html 的 FIXED_PRODUCTS 与 initStorage() 行为；
 * 但不预置 card_code（方案B：卡密池由商家中心添加卡密生成）。
 */
@Component
public class InitialDataLoader implements ApplicationRunner {

    private final ProductDao productDao;
    private final MerchantUserDao merchantUserDao;

    public InitialDataLoader(ProductDao productDao, MerchantUserDao merchantUserDao) {
        this.productDao = productDao;
        this.merchantUserDao = merchantUserDao;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productDao.countProducts() <= 0) {
            seedProducts();
        }

        seedDefaultMerchantUser();
    }

    private void seedProducts() {
        List<Product> fixed = List.of(
                product(1L, "一键解除PUBG机器码-（卡密一次性使用）", "解码", new BigDecimal("5.00"),
                        "PUBG硬件封禁一键解除，卡密自动发", "#"),
                product(2L, "BMW解码专业解码", "解码", new BigDecimal("5.00"),
                        "BMW解码专业解码，稳定秒解", "#"),
                product(3L, "玄武磐石云甲好游【单次】", "磐石", new BigDecimal("5.00"),
                        "玄武磐石云甲联合卡，单次使用", "#"),
                product(4L, "HT解码|1次卡", "解码", new BigDecimal("5.00"),
                        "HT硬件解码，一次永久生效", "#"),
                product(5L, "【HT】备用", "备用", new BigDecimal("4.50"),
                        "HT备用通道，稳定备用卡密", "#"),
                product(6L, "好游|12H卡", "好游", new BigDecimal("10.00"),
                        "好游12小时时长卡，畅玩无忧", "#"),
                product(7L, "键过磐石次卡", "磐石", new BigDecimal("1.50"),
                        "磐石一键过机器码，次卡实惠", "#")
        );
        for (Product p : fixed) {
            productDao.save(p);
        }
    }

    private void seedDefaultMerchantUser() {
        String phone = "18912891897";
        MerchantUser exist = merchantUserDao.findByPhone(phone);
        if (exist != null) return;

        MerchantUser u = new MerchantUser();
        u.setPhone(phone);
        u.setPassword("1"); // 与前端 localStorage 初始账号保持一致（待确认是否要做哈希）
        u.setUsername("创始人");
        merchantUserDao.save(u);
    }

    private static Product product(Long id, String name, String category, BigDecimal price, String desc, String officialLink) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setDesc(desc);
        p.setOfficialLink(officialLink);
        return p;
    }
}

