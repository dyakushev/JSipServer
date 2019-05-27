package com.dyakushev.model.base;

import java.time.LocalDateTime;

public abstract class AbstractRegisteredClient extends AbstractEntity {

    protected static Long registeredClientId = 0L;
    protected LocalDateTime registeredAt;
    protected LocalDateTime lastRegisteredAt;


    public AbstractRegisteredClient() {
        this.registeredAt = LocalDateTime.now();
        this.lastRegisteredAt = LocalDateTime.now();
        id = ++registeredClientId;
    }

    public void setLastRegisteredAt(LocalDateTime lastRegisteredAt) {
        this.lastRegisteredAt = lastRegisteredAt;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public LocalDateTime getLastRegisteredAt() {
        return lastRegisteredAt;
    }


}
