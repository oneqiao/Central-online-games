package com.zhongshu.zswy.dao;

import com.zhongshu.zswy.entity.Withdraw;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WithdrawDao {

    @PersistenceContext
    private EntityManager em;

    public void save(Withdraw w) {
        em.persist(w);
    }

    public List<Withdraw> listAllWithdraws() {
        return em.createQuery(
                        "select w from Withdraw w order by w.applyTime desc",
                        Withdraw.class
                )
                .getResultList();
    }
}

