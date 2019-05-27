package com.dyakushev.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import com.dyakushev.model.ents.SipAccount;
import com.dyakushev.service.impl.SipAccountServiceImpl;
import gov.nist.core.InternalErrorHandler;

import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * Implements the HTTP digest authentication method server side functionality.
 *
 * @author M. Ranganathan
 * @author Marc Bednarek
 */

public class DigestServerWWWAuthenticationHelper {

    private MessageDigest messageDigest;

    public static final String DEFAULT_ALGORITHM = "MD5";
    public static final String DEFAULT_SCHEME = "Digest";


    /**
     * to hex converter
     */
    private static final char[] toHex = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Default constructor.
     *
     * @throws NoSuchAlgorithmException
     */
    public DigestServerWWWAuthenticationHelper()
            throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
    }

    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            c[pos++] = toHex[(b[i] >> 4) & 0x0F];
            c[pos++] = toHex[b[i] & 0x0f];
        }
        return new String(c);
    }

    /**
     * Generate the challenge string.
     *
     * @return a generated nonce.
     */
    private String generateNonce() {
        // Get the time of day and run MD5 over it.
        Date date = new Date();
        long time = date.getTime();
        Random rand = new Random();
        long pad = rand.nextLong();
        String nonceString = (new Long(time)).toString()
                + (new Long(pad)).toString();
        byte mdbytes[] = messageDigest.digest(nonceString.getBytes());
        // Convert the mdbytes array into a hex string.
        return toHexString(mdbytes);
    }

    public void generateChallenge(HeaderFactory headerFactory, Response response, String realm) {
        try {

            WWWAuthenticateHeader wwwAuthenticateHeader = headerFactory.createWWWAuthenticateHeader(DEFAULT_SCHEME);
            wwwAuthenticateHeader.setParameter("realm", realm);
            wwwAuthenticateHeader.setParameter("nonce", generateNonce());
            wwwAuthenticateHeader.setParameter("opaque", "");
            wwwAuthenticateHeader.setParameter("stale", "FALSE");
            wwwAuthenticateHeader.setParameter("algorithm", DEFAULT_ALGORITHM);
            response.setHeader(wwwAuthenticateHeader);
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }

    }

    /**
     * Authenticate the inbound request.
     *
     * @param request        - the request to authenticate.
     * @param hashedPassword -- the MD5 hashed string of username:realm:plaintext password.
     * @return true if authentication succeded and false otherwise.
     */
    public boolean doAuthenticateHashedPassword(Request request, String hashedPassword) {
        ProxyAuthorizationHeader authHeader = (ProxyAuthorizationHeader) request.getHeader(WWWAuthenticateHeader.NAME);
        if (authHeader == null) return false;
        String realm = authHeader.getRealm();
        String username = authHeader.getUsername();

        if (username == null || realm == null) {
            return false;
        }

        String nonce = authHeader.getNonce();
        URI uri = authHeader.getURI();
        if (uri == null) {
            return false;
        }


        String A2 = request.getMethod().toUpperCase() + ":" + uri.toString();
        String HA1 = hashedPassword;


        byte[] mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);

        String cnonce = authHeader.getCNonce();
        String KD = HA1 + ":" + nonce;
        if (cnonce != null) {
            KD += ":" + cnonce;
        }
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = toHexString(mdbytes);
        String response = authHeader.getResponse();


        return mdString.equals(response);
    }


    /**
     * Authenticate the inbound request given plain text password.
     *
     * @param request - the request to authenticate.
     * @return true if authentication succeded and false otherwise.
     */


    public boolean doAuthenticatePlainTextPassword(SipAccountServiceImpl sipAccountServiceImpl, Request request) {
        AuthorizationHeader authHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);

        if (authHeader == null) return false;
        String realm = authHeader.getRealm();
        String username = authHeader.getUsername();

        if (username == null || realm == null) {
            return false;
        }
        SipAccount sipAccount = sipAccountServiceImpl.findSipAccount(username);
        if (sipAccount == null)
            return false;
        if (!realm.equals(sipAccount.getDomain())) {
            return false;
        }

        String pass = sipAccount.getPassword();


        String nonce = authHeader.getNonce();
        URI uri = authHeader.getURI();
        if (uri == null) {
            return false;
        }


        String A1 = username + ":" + realm + ":" + pass;
        String A2 = request.getMethod().toUpperCase() + ":" + uri.toString();
        byte mdbytes[] = messageDigest.digest(A1.getBytes());
        String HA1 = toHexString(mdbytes);


        mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);

        String cnonce = authHeader.getCNonce();
        String KD = HA1 + ":" + nonce;
        if (cnonce != null) {
            KD += ":" + cnonce;
        }
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = toHexString(mdbytes);
        String response = authHeader.getResponse();
        return mdString.equals(response);

    }

}