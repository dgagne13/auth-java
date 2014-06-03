package com.bluetarp.authorization.api.v1.service.impl;

import com.bluetarp.authorization.api.v1.model.*;
import com.bluetarp.authorization.api.v1.model.Void;
import com.bluetarp.authorization.api.v1.service.BlueTarpAuthorizationService;
import com.bluetarp.authorization.api.v1.service.BlueTarpAuthTerminal;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class BlueTarpAuthorizationServiceImpl implements BlueTarpAuthorizationService {
  private final BlueTarpAuthTerminal blueTarpAuthTerminal;

  public BlueTarpAuthorizationServiceImpl(BlueTarpAuthTerminal blueTarpAuthTerminal){
    this.blueTarpAuthTerminal = blueTarpAuthTerminal;
  }

  @Override
  public AuthorizationResponse runSaleWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, Sale sale) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaser(purchaserWithCard);
    authorizationRequest.setSale(sale);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runSaleWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, Sale sale) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaserWithToken(purchaserWithToken);
    authorizationRequest.setSale(sale);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runCreditWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, Credit credit) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaser(purchaserWithCard);
    authorizationRequest.setCredit(credit);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runCreditWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, Credit credit) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaserWithToken(purchaserWithToken);
    authorizationRequest.setCredit(credit);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runDepositHoldWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, DepositHold depositHold) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaser(purchaserWithCard);
    authorizationRequest.setDepositHold(depositHold);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runDepositHoldWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, DepositHold depositHold) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaserWithToken(purchaserWithToken);
    authorizationRequest.setDepositHold(depositHold);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runDepositCollectWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, DepositCollect depositCollect) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaser(purchaserWithCard);
    authorizationRequest.setDepositCollect(depositCollect);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse runDepositCollectWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, DepositCollect depositCollect) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaserWithToken(purchaserWithToken);
    authorizationRequest.setDepositCollect(depositCollect);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse voidTransactionWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, Void voidRequest) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaser(purchaserWithCard);
    authorizationRequest.setVoid(voidRequest);
    return sendAuthorizationRequest(authorizationRequest);
  }

  @Override
  public AuthorizationResponse voidTransactionWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, Void voidRequest) {
    AuthorizationRequest authorizationRequest = buildAuthorizationRequest(clientUserName, transactionId);
    authorizationRequest.setPurchaserWithToken(purchaserWithToken);
    authorizationRequest.setVoid(voidRequest);
    return sendAuthorizationRequest(authorizationRequest);
  }

  private AuthorizationRequest buildAuthorizationRequest(String clientUserName, String transactionId){
    AuthorizationRequest authorizationRequest = new AuthorizationRequest();
    authorizationRequest.setClientId(blueTarpAuthTerminal.getMerchantTerminalName());
    authorizationRequest.setMerchantNumber(blueTarpAuthTerminal.getMerchantNumber());
    authorizationRequest.setClientUsername(clientUserName);
    authorizationRequest.setTransactionId(transactionId);

    return authorizationRequest;
  }

  private AuthorizationResponse sendAuthorizationRequest(AuthorizationRequest authorizationRequest) {
    AuthorizationResponse authorizationResponse = null;
    BlueTarpAuthorization blueTarpAuthorization = new BlueTarpAuthorization();
    blueTarpAuthorization.setAuthorizationRequest(authorizationRequest);
    String authRequestXML = marshal(blueTarpAuthorization);
    if (authRequestXML != null) {
      String response = blueTarpAuthTerminal.sendAuthRequest(authRequestXML);
      BlueTarpAuthorization btAuthResponse = unmarshal(response);
      if (btAuthResponse != null) {
        authorizationResponse = btAuthResponse.getAuthorizationResponse();
      }
    }

    return authorizationResponse;
  }

  private String marshal(BlueTarpAuthorization blueTarpAuthorization) {
    String authRequestXML = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(BlueTarpAuthorization.class);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ObjectFactory objectFactory = new ObjectFactory();
      marshaller.marshal(objectFactory.createBluetarpAuthorization(blueTarpAuthorization), outputStream);
      authRequestXML = new String(outputStream.toByteArray(), "UTF-8");
    } catch (JAXBException jxbe) {
      jxbe.printStackTrace();
    } catch (UnsupportedEncodingException uee) {
      uee.printStackTrace();
    }

    return authRequestXML;

  }

  private BlueTarpAuthorization unmarshal(String responseXML) {
    if (responseXML == null) {
      return null;
    }

    BlueTarpAuthorization blueTarpAuthorization = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(BlueTarpAuthorization.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      StringReader stringReader = new StringReader(responseXML);
      JAXBElement<BlueTarpAuthorization> blueTarpAuthorizationJAXBElement = unmarshaller.unmarshal(new StreamSource(stringReader), BlueTarpAuthorization.class);
      blueTarpAuthorization = blueTarpAuthorizationJAXBElement.getValue();
    } catch (JAXBException jxbe){
      jxbe.printStackTrace();
    }

    return blueTarpAuthorization;
  }
}
