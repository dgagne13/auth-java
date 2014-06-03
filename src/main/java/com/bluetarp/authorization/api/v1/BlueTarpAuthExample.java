package com.bluetarp.authorization.api.v1;

import com.bluetarp.authorization.api.v1.model.*;
import com.bluetarp.authorization.api.v1.model.Void;
import com.bluetarp.authorization.api.v1.service.BlueTarpAuthTerminal;
import com.bluetarp.authorization.api.v1.service.BlueTarpAuthorizationService;
import com.bluetarp.authorization.api.v1.service.BlueTarpLookupService;
import com.bluetarp.authorization.api.v1.service.impl.BlueTarpAuthTerminalImpl;
import com.bluetarp.authorization.api.v1.service.impl.BlueTarpAuthorizationServiceImpl;
import com.bluetarp.authorization.api.v1.service.impl.BlueTarpLookupServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class BlueTarpAuthExample {
  private static String integrationUrl = "https://integration.bluetarp.com/auth/v1/";
  private static Long exampleMerchantNumber = 7L;
  private static String exampleTerminalName = "example-terminal-1";
  private static String exampleClientKey = "wLn1zexQS3dzIGx3bG2U2M";

  public static void main(String[] args) {
    System.out.println("Creating an auth terminal");
    BlueTarpAuthTerminal authorizationTerminal = new BlueTarpAuthTerminalImpl(exampleMerchantNumber, exampleTerminalName, exampleClientKey, integrationUrl);

    System.out.println("Initializing lookup service");
    BlueTarpLookupService lookupService = new BlueTarpLookupServiceImpl(authorizationTerminal);

    System.out.println("Getting first page of all active customers");
    CustomerResponse response = lookupService.findAll(1L, false);
    printCustomerList(response);
    PurchaserWithToken examplePurchaser = extractPurchaserFromList(response);

    if (response != null && response.isMorePages()) {
      System.out.println("Getting second page of all active customers");
      response = lookupService.findAll(2L, false);
      printCustomerList(response);
    }

    System.out.println("Getting first page of all customers (with inactive)");
    System.out.println("Inactive customers available for informational purposes only. (Not for running auths)");
    response = lookupService.findAll(1L, true);
    printCustomerList(response);

    /*
     * Example bluetarp id, name, merchant id, phone are used to demonstrate lookup functionality
     * These may or may not result in customers being returned.
     */
    Long exampleCid = 6308L;
    System.out.println("Looking for active customers with BlueTarp cid " + exampleCid + " (un-paged)");
    response = lookupService.findByBlueTarpIdentifier(exampleCid, null, false);
    printCustomerList(response);

    String partName = "E";
    System.out.println("Looking for active customers with " + partName + " in the name (page 1)");
    response = lookupService.findByCustomerName(partName, 1L, false);
    printCustomerList(response);

    String merchId ="123";
    System.out.println("Looking for active customers with merchant id" + merchId + " (un-paged)");
    response = lookupService.findByMerchantIdentifier(merchId, null, false);
    printCustomerList(response);

    String phone = "1234567890";
    System.out.println("Looking for active customers with phone" + phone + " (un-paged)");
    response = lookupService.findByMerchantIdentifier(merchId, null, false);
    printCustomerList(response);

    if(examplePurchaser != null){
      BlueTarpAuthorizationService authService = new BlueTarpAuthorizationServiceImpl(authorizationTerminal);
      BigDecimal OneDollar = new BigDecimal("1.00");

      System.out.println("Getting authorization for a $1.00 sale");
      Sale sale = new Sale();
      sale.setAmount(OneDollar);
      sale.setInvoice("example-inv-1");
      sale.setJobCode("auth-example");

      UUID transactionId = UUID.randomUUID();

      AuthorizationResponse authResponse = authService.runSaleWithToken("user1", transactionId.toString(), examplePurchaser, sale);
      printAuthResponse(authResponse);

      System.out.println("Looking up voidable transactions:");
      List<Transaction> transactions = lookupService.findVoidableTransactions();
      printTransactionList(transactions);

      Long voidableAuthSeq = extractAuthSequenceFromList(transactions);
      if (voidableAuthSeq != null) {
        System.out.println("Voiding auth " + voidableAuthSeq);
        Void voidAuth = new Void();
        voidAuth.setAuthSeq(voidableAuthSeq);
        authResponse = authService.voidTransactionWithToken("user1", transactionId.toString(), examplePurchaser, voidAuth);
        printAuthResponse(authResponse);
      }

      System.out.println("Getting authorization for a $1.00 credit");
      Credit credit = new Credit();
      credit.setAmount(OneDollar);
      credit.setInvoice("example-inv-2");
      credit.setJobCode("auth-example");

      transactionId = UUID.randomUUID();

      authResponse = authService.runCreditWithToken("user1", transactionId.toString(), examplePurchaser, credit);
      printAuthResponse(authResponse);

      System.out.println("Getting authorization for a $1.00 hold");
      DepositHold hold = new DepositHold();
      hold.setAmount(OneDollar);
      hold.setInvoice("example-inv-3");
      hold.setJobCode("auth-example");

      transactionId = UUID.randomUUID();

      authResponse = authService.runDepositHoldWithToken("user1", transactionId.toString(), examplePurchaser, hold);
      printAuthResponse(authResponse);

      System.out.println("Looking up open deposit holds:");
      transactions = lookupService.findOpenDepositHolds();
      printTransactionList(transactions);

      Long collectableAuthSeq = extractAuthSequenceFromList(transactions);
      if (collectableAuthSeq != null) {
        System.out.println("Collecting a deposit");
        DepositCollect collect = new DepositCollect();
        collect.setAuthSeq(collectableAuthSeq);
        collect.setAmount(OneDollar);
        collect.setInvoice("example-inv-4");
        collect.setJobCode("auth-example");

        authResponse = authService.runDepositCollectWithToken("user1", transactionId.toString(), examplePurchaser, collect);
        printAuthResponse(authResponse);
      }

    }
  }

  private static void printCustomerList(CustomerResponse response){
    if(response != null){
      List<Customer> customers = response.getCustomers().getCustomer();
      if(customers.isEmpty()){
        System.out.println("No customers found");
      } else {
        System.out.println("Lookup returned the following customers: ");
        for(Customer c : customers) {
           printCustomer(c);
        }
      }
    }
  }

  private static void printCustomer(Customer customer){
    System.out.println("Name: " + customer.getName() + "  BlueTarp Id: " + customer.getNumber() + "  Merchant Id: " + customer.getMerchantIdentifier());
    if (customer.getAvailableCredit() != null) {
       System.out.println("Available credit: " + customer.getAvailableCredit());
    }
    System.out.println("Purchasers:");
    for(Purchaser p : customer.getPurchasers().getPurchaser()) {
      System.out.println("Name: " + p.getName() + "  Token: " + p.getToken());
    }
    System.out.println("-----");
  }

  private static PurchaserWithToken extractPurchaserFromList(CustomerResponse response){
    if(response != null) {
      List<Customer> customers = response.getCustomers().getCustomer();
      //If customers are returned, find any purchaser and return it.
      for(Customer c : customers) {
        for(Purchaser p: c.getPurchasers().getPurchaser()){
          PurchaserWithToken purchaser = new PurchaserWithToken();
          purchaser.setToken(p.getToken());
          return purchaser;
        }
      }
    }

    return null;
  }

  private static void printTransactionList(List<Transaction> transactions) {
    System.out.println("Transactions:");
    if (transactions.isEmpty()) {
      System.out.println("none");
    } else {
      for (Transaction t : transactions) {
        System.out.println("Invoice no.: " + t.getInvoice() + "  Auth Sequence: " + t.getAuthseqNumber() + "  Amount: "  + t.getAmount());
      }
    }
    System.out.println("----");
  }

  private static Long extractAuthSequenceFromList(List<Transaction> transactions){
    if(transactions.isEmpty()) {
      return null;
    } else {
      return transactions.iterator().next().getAuthseqNumber();
    }
  }

  private static void printAuthResponse(AuthorizationResponse response) {
    if (response != null) {
      if("00".equals(response.getCode())) {
        System.out.println("Success! Transaction " + response.getTransactionId() + " was assigned auth sequence " + response.getAuthSeq());
      } else {
        System.out.println("Transaction not authorized. Reason: " + response.getMessage());
      }
    } else {
      System.out.println("Failed to run auth");
    }
  }

}
