package com.bluetarp.authorization.api.v1.service;

import com.bluetarp.authorization.api.v1.model.*;
import com.bluetarp.authorization.api.v1.model.Void;

public interface BlueTarpAuthorizationService {
  AuthorizationResponse runSaleWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, Sale sale);
  AuthorizationResponse runSaleWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, Sale sale);
  AuthorizationResponse runCreditWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, Credit credit);
  AuthorizationResponse runCreditWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, Credit credit);
  AuthorizationResponse runDepositHoldWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, DepositHold depositHold);
  AuthorizationResponse runDepositHoldWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, DepositHold depositHold);
  AuthorizationResponse runDepositCollectWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, DepositCollect depositCollect);
  AuthorizationResponse runDepositCollectWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithToken, DepositCollect depositCollect);
  AuthorizationResponse voidTransactionWithCard(String clientUserName, String transactionId, PurchaserWithCard purchaserWithCard, Void voidRequest);
  AuthorizationResponse voidTransactionWithToken(String clientUserName, String transactionId, PurchaserWithToken purchaserWithtoken, Void voidRequest);
}
