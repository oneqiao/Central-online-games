package com.zhongshu.zswy.dao;

import com.zhongshu.zswy.entity.CardCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CardCodeDao {

    @PersistenceContext
    private EntityManager em;

    public long countAvailableByProductId(Long productId) {
        return em.createQuery(
                        "select count(c) from CardCode c where c.productId=:pid and c.status='available'",
                        Long.class
                )
                .setParameter("pid", productId)
                .getSingleResult();
    }

    /**
     * 分配一条可用卡密：在同一事务内锁定，避免并发重复发货。
     */
    public CardCode findAndLockAvailableOne(Long productId) {
        List<CardCode> list = em.createQuery(
                        "select c from CardCode c where c.productId=:pid and c.status='available' order by c.id asc",
                        CardCode.class
                )
                .setParameter("pid", productId)
                .setMaxResults(1)
                .getResultList();

        if (list.isEmpty()) return null;
        CardCode c = list.get(0);
        em.lock(c, LockModeType.PESSIMISTIC_WRITE);
        return c;
    }

    public void saveAll(List<CardCode> codes) {
        for (CardCode c : codes) {
            em.persist(c);
        }
    }
}

