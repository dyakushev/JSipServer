package com.dyakushev;


import com.dyakushev.model.ents.RegisteredClient;
import com.dyakushev.service.RegisteredClientService;
import com.dyakushev.service.impl.RegisteredClientServiceImpl;
import com.dyakushev.service.impl.SipAccountServiceImpl;
import com.dyakushev.utils.DigestServerWWWAuthenticationHelper;
import com.dyakushev.utils.JSipServerHelper;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;


public class JSipServer implements SipListener {

    DigestServerWWWAuthenticationHelper dsam;
    DigestServerAuthenticationHelper dsamProxy;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;


    private static final String myAddress = "0.0.0.0";

    private static final int myPort = 5082;

    private AtomicLong counter = new AtomicLong();

    private ListeningPoint listeningPoint;

    private SipProvider sipProvider;

    private String transport = "udp";

    private JSipServerHelper jSipServerHelper = JSipServerHelper.getInstance(this);

    private SipAccountServiceImpl sipAccountServiceImpl = new SipAccountServiceImpl();
    private RegisteredClientService registeredClientService = new RegisteredClientServiceImpl();

    public RegisteredClientService getRegisteredClientService() {
        return registeredClientService;
    }


    public SipProvider getSipProvider() {
        return sipProvider;
    }


    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        //  System.out.println("\n\nRequest " + request.getMethod()
        //         + " received at " + sipStack.getStackName()
        //         + " with server transaction id " + serverTransactionId);

        jSipServerHelper.printMessage(requestEvent.getRequest());
        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            processCancel(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.REGISTER)) {

            processRegister(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.SUBSCRIBE)) {

            //  System.out.println("Subscribe message");

        } else {
            processInDialogRequest(requestEvent, serverTransactionId);
        }
    }

    public void processResponse(ResponseEvent responseEvent) {
        ClientTransaction ct = responseEvent.getClientTransaction();
        Response response = responseEvent.getResponse();
        ServerTransaction st = (ServerTransaction) ct.getApplicationData();
        try {

            Response otherResponse = messageFactory.createResponse(response.getStatusCode(), st.getRequest());
            if (response.getStatusCode() == 200 && ct.getRequest().getMethod().equals("INVITE")) {
                Address address = addressFactory.createAddress("B2BUA <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                ToHeader toHeader = (ToHeader) otherResponse.getHeader(ToHeader.NAME);
                if (toHeader.getTag() == null) toHeader.setTag(new Long(counter.getAndIncrement()).toString());
                otherResponse.addHeader(contactHeader);
            }
            st.sendResponse(otherResponse);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Process the ACK request, forward it to the other leg.
     */
    public void processAck(RequestEvent requestEvent,
                           ServerTransaction serverTransaction) {
        try {
            Dialog dialog = serverTransaction.getDialog();
            jSipServerHelper.printMessage("b2bua: got an ACK! ");
            jSipServerHelper.printMessage("Dialog State = " + dialog.getState());
            Dialog otherDialog = (Dialog) dialog.getApplicationData();
            Request request = otherDialog.createAck(otherDialog.getLocalSeqNumber());
            otherDialog.sendAck(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Process the invite request.
     */


    public void processInvite(RequestEvent requestEvent,
                              ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            jSipServerHelper.printMessage("b2bua: got an Invite sending Trying");
            ServerTransaction st = requestEvent.getServerTransaction();
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            Dialog dialog = st.getDialog();
            ContactHeader contact = (ContactHeader) request.getHeader(ContactHeader.NAME);

            SipURI contactUri = (SipURI) contact.getAddress().getURI();
            ToHeader to = (ToHeader) request.getHeader(ToHeader.NAME);
            SipURI toUri = (SipURI) to.getAddress().getURI();
            FromHeader from = (FromHeader) request.getHeader(FromHeader.NAME);
            dsamProxy = new DigestServerAuthenticationHelper();
            SipURI fromUri = (SipURI) from.getAddress().getURI();
            if (!registeredClientService.isRegistered(request)) {
                jSipServerHelper.printMessage("User " + fromUri + " is not registered.");

                if (!dsamProxy.doAuthenticatePlainTextPassword(request, "pass")) {
                    jSipServerHelper.printMessage("");
                    jSipServerHelper.printMessage("Authentication failed");
                    jSipServerHelper.printMessage("");
                    Response challengeResponse = messageFactory.createResponse(
                            Response.PROXY_AUTHENTICATION_REQUIRED, request);
                    dsamProxy.generateChallenge(headerFactory, challengeResponse, "nist.gov");
                    st.sendResponse(challengeResponse);
                    jSipServerHelper.printMessage(challengeResponse);

                    return;
                }
                // registrar.put(fromUri.getUser(), contactUri);
            }
            Response response = messageFactory.createResponse(Response.TRYING, request);
            jSipServerHelper.printMessage(response);
            st.sendResponse(response);


            if (registeredClientService.isRegistered(request)) {
                jSipServerHelper.printMessage("User is not registered.");
                throw new RuntimeException("User is not registered ");
            } else {
          /*      ClientTransaction otherLeg = call(target);
                otherLeg.setApplicationData(st);
                st.setApplicationData(otherLeg);
                dialog.setApplicationData(otherLeg.getDialog());
                otherLeg.getDialog().setApplicationData(dialog);*/
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            //junit.framework.TestCase.fail("Exit JVM");
        }
    }

    /**
     * Process the any in dialog request - MESSAGE, BYE, INFO, UPDATE.
     */
    public void processInDialogRequest(RequestEvent requestEvent,
                                       ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        Dialog dialog = requestEvent.getDialog();
        System.out.println("local party = " + dialog.getLocalParty());
        try {
            System.out.println("b2bua:  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());

            Dialog otherLeg = (Dialog) dialog.getApplicationData();
            Request otherBye = otherLeg.createRequest(request.getMethod());
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(otherBye);
            clientTransaction.setApplicationData(serverTransactionId);
            serverTransactionId.setApplicationData(clientTransaction);
            otherLeg.sendRequest(clientTransaction);

        } catch (Exception ex) {
            ex.printStackTrace();
            //junit.framework.TestCase.fail("Exit JVM");

        }
    }


    public void processRegister(RequestEvent requestEvent,
                                ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        ServerTransaction st = requestEvent.getServerTransaction();
        try {
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);

            }

            int timeOut = JSipServerHelper.getExpiredTime(request);
            if (registeredClientService.isRegistered(request)) {


                if (timeOut == 0) {
                    //getRegistrar().remove(fromUri.getUser());
                    registeredClientService.removeClient(request);

                    //jSipServerHelper.clearRegistration(fromUri);
                    Response response = this.messageFactory.createResponse(200, request);
                    // ServerTransaction serverTransaction = sipProvider.getNewServerTransaction(request);
                    jSipServerHelper.printMessage(response);
                    st.sendResponse(response);

                    return;
                } else {

                    //   jSipServerHelper.updateExpiredTimer(request);
                    RegisteredClient registeredClient = registeredClientService.getClient(request);
                    if (registeredClient != null) {
                        registeredClient.setExpireTime(timeOut);
                    }
                    Response response = this.messageFactory.createResponse(200, request);
                    // ServerTransaction serverTransaction = sipProvider.getNewServerTransaction(request);
                    st.sendResponse(response);
                    jSipServerHelper.printMessage(response);
                    return;

                }
            }
            dsam = new DigestServerWWWAuthenticationHelper();


            if (!dsam.doAuthenticatePlainTextPassword(sipAccountServiceImpl, request)) {

                Response challengeResponse = messageFactory.createResponse(
                        Response.UNAUTHORIZED, request);
                dsam.generateChallenge(headerFactory, challengeResponse, "test.ru");
                st.sendResponse(challengeResponse);
                jSipServerHelper.printMessage(challengeResponse);
                return;
            }
            registeredClientService.addClient(request);
            Response response = this.messageFactory.createResponse(200, request);
            // ServerTransaction serverTransaction = sipProvider.getNewServerTransaction(request);
            st.sendResponse(response);
            jSipServerHelper.printMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processCancel(RequestEvent requestEvent,
                              ServerTransaction serverTransactionId) {
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = "
                + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
    }

    public void init() {

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.ERROR);
        console.activateOptions();
        console.setTarget("System.out");
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console);
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "B2BUA");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootmedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootmelog.txt");
        properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("sipStack = " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            //   junit.framework.TestCase.fail("Exit JVM");
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            this.listeningPoint = sipStack.createListeningPoint("0.0.0.0",
                    myPort, transport);

            JSipServer listener = this;

            sipProvider = sipStack.createSipProvider(listeningPoint);
            System.out.println("ws provider " + sipProvider);
            sipProvider.addSipListener(listener);


            //sipProvider.getSipStack().stop();


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void stopServer() {

    }


    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        if (transactionTerminatedEvent.isServerTransaction())
            System.out.println("Transaction terminated event recieved"
                    + transactionTerminatedEvent.getServerTransaction());
        else
            System.out.println("Transaction terminated "
                    + transactionTerminatedEvent.getClientTransaction());

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog terminated event recieved");
        Dialog d = dialogTerminatedEvent.getDialog();
        System.out.println("Local Party = " + d.getLocalParty());

    }

    public ClientTransaction call(SipURI destination) {
        try {

            String fromName = "B2BUA";
            String fromSipAddress = "here.com";
            String fromDisplayName = "B2BUA";

            String toSipAddress = "there.com";
            String toUser = "Target";
            String toDisplayName = "Target";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, new Long(counter.getAndIncrement()).toString());

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // create Request URI
            SipURI requestURI = destination;

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            String ipAddress = listeningPoint.getIPAddress();
            ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                    sipProvider.getListeningPoint(transport).getPort(),
                    transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = "127.0.0.1";

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(listeningPoint.getPort());
            contactUrl.setLrParam();

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // You can add extension headers of your own making
            // to the outgoing SIP request.
            // Add the extension header.
            Header extensionHeader = headerFactory.createHeader("My-Header",
                    "my header value");
            request.addHeader(extensionHeader);

            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            request.setContent(contents, contentTypeHeader);
            // You can add as many extension headers as you
            // want.

            extensionHeader = headerFactory.createHeader("My-Other-Header",
                    "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);

            System.out.println("inviteTid = " + inviteTid);

            // send the request out.

            inviteTid.sendRequest();

            return inviteTid;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }


}
