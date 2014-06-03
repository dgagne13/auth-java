package com.bluetarp.authorization.api.v1.service.impl;

import com.bluetarp.authorization.api.v1.service.BlueTarpAuthTerminal;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.IOException;

public class BlueTarpAuthTerminalImpl implements BlueTarpAuthTerminal {
  private static final int ONE_MINUTE_IN_MILLIS = 60000;

  private Long merchantNumber;
  private String merchantTerminalName;
  private String baseAuthUrl;
  private String authHeader;

  public BlueTarpAuthTerminalImpl(Long merchantNumber, String merchantTerminalName, String clientKey, String baseAuthUrl) {
    this.merchantNumber = merchantNumber;
    this.merchantTerminalName = merchantTerminalName;
    if (!baseAuthUrl.endsWith("/")) {
      baseAuthUrl += "/";
    }
    this.baseAuthUrl = baseAuthUrl + merchantNumber;
    this.authHeader = "Bearer " + clientKey;
  }

  public String sendLookupRequest(String requestUrl){
    HttpClient authClient = buildHttpClient();

    HttpGet request = new HttpGet(baseAuthUrl + requestUrl);
    request.setHeader("Authorization", authHeader);

    System.out.println("Sending GET to: " + request.getURI().toString());

    String response = null;
    try {
      response = authClient.execute(request, new BasicResponseHandler());
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      authClient.getConnectionManager().shutdown();
    }

    return response;
  }

  public String sendAuthRequest(String requestXML){
    HttpClient authClient = buildHttpClient();
    HttpEntity entity = new StringEntity(requestXML, ContentType.TEXT_XML);
    HttpPost request = new HttpPost(baseAuthUrl);
    request.setHeader("Authorization", authHeader);
    request.setEntity(entity);

    String response = null;
    try {
      response = authClient.execute(request, new BasicResponseHandler());
    } catch (IOException e) {
      //log an error
      e.printStackTrace();
    } finally {
      authClient.getConnectionManager().shutdown();
    }

    return response;
  }

  public Long getMerchantNumber() {return  merchantNumber; }
  public String getMerchantTerminalName() { return merchantTerminalName; }

  protected HttpClient buildHttpClient() {
    HttpClient httpClient = new DefaultHttpClient();

    HttpParams params = httpClient.getParams();
    params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, ONE_MINUTE_IN_MILLIS);
    params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, ONE_MINUTE_IN_MILLIS);
    params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");

    return httpClient;
  }
}
