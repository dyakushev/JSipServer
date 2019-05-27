package com.dyakushev.service;

import com.dyakushev.model.base.AbstractSipAccount;
import com.dyakushev.model.ents.SipAccount;

public interface SipAccountService {
    SipAccount findSipAccount(String name);
}
