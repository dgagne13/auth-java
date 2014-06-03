package com.bluetarp.authorization.api.v1.service.impl;

import com.bluetarp.authorization.api.v1.model.*;
import com.bluetarp.authorization.api.v1.service.BlueTarpAuthTerminal;
import com.bluetarp.authorization.api.v1.service.BlueTarpLookupService;
import org.apache.http.client.utils.URIBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class BlueTarpLookupServiceImpl implements BlueTarpLookupService {
  private final BlueTarpAuthTerminal blueTarpAuthTerminal;
  private static final String CUSTOMER_LOOKUP_URL = "/customers";
  private static final String TRANSACTION_LOOKUP_URL="/transactions";
  private static final String INCLUDE_INACTIVE_PARAM = "include-inactive";
  private static final String PAGE_PARAM = "page";

  public BlueTarpLookupServiceImpl(BlueTarpAuthTerminal blueTarpAuthTerminal){
    this.blueTarpAuthTerminal = blueTarpAuthTerminal;
  }

  @Override
  public CustomerResponse findAll(Long page, boolean includeInactive) {
    String uri = buildCustomerURI(null, null, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  @Override
  public CustomerResponse findByBlueTarpIdentifier(Long bluetarpIdentifier, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("bluetarp-cid", bluetarpIdentifier.toString(), page,includeInactive);
    return sendCustomerLookup(uri);
  }

  @Override
  public CustomerResponse findByMerchantIdentifier(String merchantIdentifier, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("merchant-cid", merchantIdentifier, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  @Override
  public CustomerResponse findByCustomerName(String customerName, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("cname", customerName, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  @Override
  public CustomerResponse findByCustomerPhone(String phoneNumber, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("phone", phoneNumber, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  @Override
  public List<Transaction> findVoidableTransactions() {
    return sendTransactionLookup(TRANSACTION_LOOKUP_URL + "/void");

  }

  @Override
  public List<Transaction> findOpenDepositHolds() {
    return sendTransactionLookup(TRANSACTION_LOOKUP_URL + "/deposit");
  }


  private String buildCustomerURI(String queryName, String queryValue, Long page, boolean includeInactive){
    String customerURI = CUSTOMER_LOOKUP_URL;
    URIBuilder uriBuilder = new URIBuilder();
    uriBuilder.setPath(CUSTOMER_LOOKUP_URL);
    if(queryName != null && queryValue != null) {
      uriBuilder.addParameter(queryName, queryValue);
    }
    if (page != null) {
      uriBuilder.addParameter(PAGE_PARAM, page.toString());
    }
    if (includeInactive) {
      uriBuilder.addParameter("include-inactive", "true");
    }

    try {
      customerURI = uriBuilder.build().toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    return customerURI;
  }

  private CustomerResponse sendCustomerLookup(String customerUri) {
    String responseXML = blueTarpAuthTerminal.sendLookupRequest(customerUri);

    if (responseXML == null) {
      return null;
    }

    CustomerResponse response = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(BlueTarpAuthorization.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      StringReader stringReader = new StringReader(responseXML);
      JAXBElement<BlueTarpAuthorization> blueTarpAuthorizationJAXBElement = unmarshaller.unmarshal(new StreamSource(stringReader), BlueTarpAuthorization.class);
      BlueTarpAuthorization blueTarpAuthorization = blueTarpAuthorizationJAXBElement.getValue();
      if (blueTarpAuthorization != null) {
        response = blueTarpAuthorization.getCustomerResponse();
      }
    } catch (JAXBException jxbe){
      jxbe.printStackTrace();
    }

    return response;
  }

  private List<Transaction> sendTransactionLookup(String transactionUri) {
    String responseXML = blueTarpAuthTerminal.sendLookupRequest(transactionUri);

    if (responseXML == null) {
      return null;
    }

    List<Transaction> transactions = Collections.EMPTY_LIST;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(BlueTarpAuthorization.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      StringReader stringReader = new StringReader(responseXML);
      JAXBElement<BlueTarpAuthorization> blueTarpAuthorizationJAXBElement = unmarshaller.unmarshal(new StreamSource(stringReader), BlueTarpAuthorization.class);
      BlueTarpAuthorization blueTarpAuthorization = blueTarpAuthorizationJAXBElement.getValue();
      if (blueTarpAuthorization != null) {
        TransactionResponse response = blueTarpAuthorization.getTransactionResponse();
        if ( response != null) {
          transactions = response.getTransactions().getTransaction();
        }
      }
    } catch (JAXBException jxbe){
      jxbe.printStackTrace();
    }

    return transactions;
  }

}
