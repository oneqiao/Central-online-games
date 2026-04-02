package com.zhongshu.zswy.dao;

import com.zhongshu.zswy.entity.MerchantUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantUserDao {

    @PersistenceContext
    private EntityManager em;

    public MerchantUser findByPhone(String phone) {
        return em.createQuery(
                        "select u from MerchantUser u where u.phone=:phone",
                        MerchantUser.class
                )
                .setParameter("phone", phone)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public MerchantUser findByPhoneAndPassword(String phone, String password) {
        return em.createQuery(
                        "select u from MerchantUser u where u.phone=:phone and u.password=:password",
                        MerchantUser.class
                )
                .setParameter("phone", phone)
                .setParameter("password", password)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public void save(MerchantUser user) {
        em.persist(user);
    }
}

