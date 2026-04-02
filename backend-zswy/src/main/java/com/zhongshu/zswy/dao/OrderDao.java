package com.zhongshu.zswy.dao;

import com.zhongshu.zswy.entity.ZhongshuOrder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager em;

    public void save(ZhongshuOrder order) {
        em.persist(order);
    }

    /**
     * JS 语义：localStorage 的 orders 通过 unshift() 让最新订单排在最前，
     * 然后用 find() 找到第一条匹配 order_no/contact 的记录。
     */
    public ZhongshuOrder findByOrderNoOrContact(String input) {
        return em.createQuery(
                        "select o from ZhongshuOrder o where o.orderNo=:v or o.contact=:v order by o.payTime desc",
                        ZhongshuOrder.class
                )
                .setParameter("v", input)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<ZhongshuOrder> listAllOrders() {
        return em.createQuery(
                        "select o from ZhongshuOrder o order by o.payTime desc",
                        ZhongshuOrder.class
                )
                .getResultList();
    }
}

