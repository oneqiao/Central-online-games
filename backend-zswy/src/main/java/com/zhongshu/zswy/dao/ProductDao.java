package com.zhongshu.zswy.dao;

import com.zhongshu.zswy.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDao {

    @PersistenceContext
    private EntityManager em;

    public List<Product> listProducts(String category, String keyword) {
        StringBuilder jpql = new StringBuilder("select p from Product p where 1=1 ");
        if (category != null && !"all".equals(category)) jpql.append(" and p.category=:category ");
        if (keyword != null && !keyword.isBlank()) jpql.append(" and lower(p.name) like :kw ");

        var q = em.createQuery(jpql.toString(), Product.class);
        if (category != null && !"all".equals(category)) q.setParameter("category", category);
        if (keyword != null && !keyword.isBlank()) q.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
        return q.getResultList();
    }

    public Product findById(Long id) {
        return em.find(Product.class, id);
    }

    public long countProducts() {
        return em.createQuery("select count(p) from Product p", Long.class).getSingleResult();
    }

    public void save(Product product) {
        em.persist(product);
    }
}

