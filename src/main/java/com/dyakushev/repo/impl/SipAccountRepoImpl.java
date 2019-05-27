package com.dyakushev.repo.impl;

import com.dyakushev.model.ents.SipAccount;
import com.dyakushev.repo.SipAccountRepo;
import com.dyakushev.utils.HibernateSessionFactoryUtil;
import org.hibernate.query.Query;

import java.util.List;

public class SipAccountRepoImpl implements SipAccountRepo {
    @Override
    public List<SipAccount> findSipAccount(String name) {
        Query query = HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("from SipAccount where username= :name");
        query.setParameter("name", name);
        return (List<SipAccount>) query.list();
    }
}
