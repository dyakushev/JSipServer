package com.dyakushev.service.impl;

import com.dyakushev.model.ents.SipAccount;
import com.dyakushev.repo.SipAccountRepo;
import com.dyakushev.repo.impl.SipAccountRepoImpl;
import com.dyakushev.service.SipAccountService;

import java.util.List;

public class SipAccountServiceImpl implements SipAccountService {
    private SipAccountRepo sipAccountRepo = new SipAccountRepoImpl();


    public SipAccount findSipAccount(String name) {
        List<SipAccount> list = (List<SipAccount>) sipAccountRepo.findSipAccount(name);
        if (list.isEmpty()) {
            return null;
        } else
            return list.get(0);
    }
}
