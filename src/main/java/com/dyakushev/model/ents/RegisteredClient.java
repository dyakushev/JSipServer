package com.dyakushev.model.ents;

import com.dyakushev.model.base.AbstractRegisteredClient;

import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class RegisteredClient extends AbstractRegisteredClient {

    private int expireTime;
    private Header contactHeader;
    private Header toHeader;
    private Header fromHeader;
    private String callId;
    private String fromTag;

    public RegisteredClient() {
    }

    public void setContactHeader(Header contactHeader) {
        this.contactHeader = contactHeader;
    }

    public void setToHeader(Header toHeader) {
        this.toHeader = toHeader;
    }

    public void setFromHeader(Header fromHeader) {
        this.fromHeader = fromHeader;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setFromTag(String fromTag) {
        this.fromTag = fromTag;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public ContactHeader getContactHeader() {
        return (ContactHeader) contactHeader;
    }


    public String getContactDisplayName() {
        return getContactHeader().getAddress().getDisplayName();
    }

    public String getContactAddress() {
        return getContactHeader().getAddress().getURI().toString();
    }

    public long getRegisterLeftTime() {
        long timeLeft = getExpireTime() - ChronoUnit.SECONDS.between(getRegisteredAt(), LocalDateTime.now());
        return timeLeft > 0 ? timeLeft : 0;
    }

    @Override
    public String toString() {
        return "RegisteredClient{" +
                "registrationTime=" + getRegisteredAt() +
                ", expireTime=" + getRegisterLeftTime() +
                ", lastRegistrationTime=" + getLastRegisteredAt() +
                ", Contact name=" + getContactDisplayName() +
                ", Contact address=" + getContactAddress() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RegisteredClient that = (RegisteredClient) o;

        if (contactHeader != null ? !contactHeader.equals(that.contactHeader) : that.contactHeader != null)
            return false;
        if (toHeader != null ? !toHeader.equals(that.toHeader) : that.toHeader != null) return false;
        if (fromHeader != null ? !fromHeader.equals(that.fromHeader) : that.fromHeader != null) return false;
        if (callId != null ? !callId.equals(that.callId) : that.callId != null) return false;
        return fromTag != null ? fromTag.equals(that.fromTag) : that.fromTag == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (contactHeader != null ? contactHeader.hashCode() : 0);
        result = 31 * result + (toHeader != null ? toHeader.hashCode() : 0);
        result = 31 * result + (fromHeader != null ? fromHeader.hashCode() : 0);
        result = 31 * result + (callId != null ? callId.hashCode() : 0);
        result = 31 * result + (fromTag != null ? fromTag.hashCode() : 0);
        return result;
    }
}
