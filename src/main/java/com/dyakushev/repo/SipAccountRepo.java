package com.dyakushev.repo;

import com.dyakushev.model.base.AbstractSipAccount;

import java.util.List;

public interface SipAccountRepo {
    List<? extends AbstractSipAccount> findSipAccount(String name);
}
