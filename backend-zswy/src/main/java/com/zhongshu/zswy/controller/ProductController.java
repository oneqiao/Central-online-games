package com.zhongshu.zswy.controller;

import com.zhongshu.zswy.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 对应：页面 searchGoodsBtn / keywordInput(Enter) / 分类点击
     */
    @GetMapping
    public List<ProductService.ProductCardView> list(
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(required = false) String keyword
    ) {
        return productService.list(category, keyword);
    }
}

