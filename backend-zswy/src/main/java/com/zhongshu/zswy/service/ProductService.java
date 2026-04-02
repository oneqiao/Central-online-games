package com.zhongshu.zswy.service;

import com.zhongshu.zswy.dao.CardCodeDao;
import com.zhongshu.zswy.dao.ProductDao;
import com.zhongshu.zswy.entity.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductDao productDao;
    private final CardCodeDao cardCodeDao;

    public ProductService(ProductDao productDao, CardCodeDao cardCodeDao) {
        this.productDao = productDao;
        this.cardCodeDao = cardCodeDao;
    }

    /**
     * 返回给前端：商品 + 当前 available 库存数量。
     */
    public List<ProductCardView> list(String category, String keyword) {
        List<Product> ps = productDao.listProducts(category, keyword);
        List<ProductCardView> out = new ArrayList<>();
        for (Product p : ps) {
            long stock = cardCodeDao.countAvailableByProductId(p.getId());
            out.add(new ProductCardView(
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getDesc(),
                    p.getOfficialLink(),
                    stock
            ));
        }
        return out;
    }

    public record ProductCardView(
            Long id,
            String name,
            String category,
            java.math.BigDecimal price,
            String desc,
            String officialLink,
            long stockCount
    ) {
    }
}

