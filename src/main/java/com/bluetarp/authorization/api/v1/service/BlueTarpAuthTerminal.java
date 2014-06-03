package com.bluetarp.authorization.api.v1.service;

public interface BlueTarpAuthTerminal {
  public String sendLookupRequest(String requestUrl);
  public String sendAuthRequest(String requestXML);
  public String getMerchantTerminalName();
  public Long getMerchantNumber();
}
