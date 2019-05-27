package com.dyakushev.model.ents;

import com.dyakushev.model.base.AbstractSipAccount;

import javax.persistence.*;

@Entity
@Table(name = "sip_account")
public class SipAccount extends AbstractSipAccount {
    private String domain;
    private String password;
    private String username;


    public SipAccount() {
    }

    public SipAccount(String username, String password, String domain) {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
