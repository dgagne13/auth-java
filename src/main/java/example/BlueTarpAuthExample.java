package example;

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
    BlueTarpAuthTerminal authorizationTerminal = new BlueTarpAuthTerminal(exampleMerchantNumber, exampleTerminalName, exampleClientKey, integrationUrl);

    System.out.println("Initializing lookup service");
    BlueTarpLookupService lookupService = new BlueTarpLookupService(authorizationTerminal);

    System.out.println("Getting first page of all active customers");
    CustomerResponse response = lookupService.findAll(1L, false);
    printCustomerList(response);
    Purchaser examplePurchaser = extractPurchaserFromList(response);

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
      BlueTarpAuthorizationService authService = new BlueTarpAuthorizationService(authorizationTerminal);
      BigDecimal OneDollar = new BigDecimal("1.00");

      System.out.println("Getting authorization for a $1.00 sale");
      Transaction sale = new Transaction();
      sale.setAmount(OneDollar);
      sale.setInvoice("example-inv-1");
      sale.setJobCode("auth-example");
      sale.setTransactionId(UUID.randomUUID().toString());

      AuthorizationResponse authResponse = authService.runSaleWithToken("user1", examplePurchaser, sale);
      printAuthResponse(authResponse);

      System.out.println("Looking up voidable transactions:");
      List<Transaction> transactions = lookupService.findVoidableTransactions();
      printTransactionList(transactions);

      Long voidableAuthSeq = extractAuthSequenceFromList(transactions);
      if (voidableAuthSeq != null) {
        System.out.println("Voiding auth " + voidableAuthSeq);
        Transaction voidAuth = new Transaction();
        voidAuth.setAuthSequence(voidableAuthSeq);
        authResponse = authService.voidTransactionWithToken("user1", examplePurchaser, voidAuth);
        printAuthResponse(authResponse);
      }

      System.out.println("Getting authorization for a $1.00 credit");
      Transaction credit = new Transaction();
      credit.setAmount(OneDollar);
      credit.setInvoice("example-inv-2");
      credit.setJobCode("auth-example");
      credit.setTransactionId(UUID.randomUUID().toString());

      authResponse = authService.runCreditWithToken("user1", examplePurchaser, credit);
      printAuthResponse(authResponse);

      System.out.println("Getting authorization for a $1.00 hold");
      Transaction hold = new Transaction();
      hold.setAmount(OneDollar);
      hold.setInvoice("example-inv-3");
      hold.setJobCode("auth-example");
      hold.setTransactionId(UUID.randomUUID().toString());

      authResponse = authService.runDepositHoldWithToken("user1", examplePurchaser, hold);
      printAuthResponse(authResponse);

      System.out.println("Looking up open deposit holds:");
      transactions = lookupService.findOpenDepositHolds();
      printTransactionList(transactions);

      Long collectableAuthSeq = extractAuthSequenceFromList(transactions);
      if (collectableAuthSeq != null) {
        System.out.println("Collecting a deposit");
        Transaction collect = new Transaction();
        collect.setAuthSequence(collectableAuthSeq);
        collect.setAmount(OneDollar);
        collect.setInvoice("example-inv-4");
        collect.setJobCode("auth-example");
        collect.setTransactionId(UUID.randomUUID().toString());

        authResponse = authService.runDepositCollectWithToken("user1", examplePurchaser, collect);
        printAuthResponse(authResponse);
      }

    }
  }

  private static void printCustomerList(CustomerResponse response){
    if(response != null){
      List<Customer> customers = response.getCustomers();
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
    System.out.println("Name: " + customer.getName() + "  BlueTarp Id: " + customer.getBlueTarpIdentifier() + "  Merchant Id: " + customer.getMerchantIdentifier());
    System.out.println("Purchasers:");
    for(Purchaser p : customer.getPurchasers()) {
      System.out.println("Name: " + p.getName() + "  Token: " + p.getToken());
    }
    System.out.println("-----");
  }

  private static Purchaser extractPurchaserFromList(CustomerResponse response){
    if(response != null) {
      List<Customer> customers = response.getCustomers();
      //If customers are returned, find any purchaser and return it.
      for(Customer c : customers) {
        for(Purchaser p: c.getPurchasers()){
          Purchaser purchaser = new Purchaser();
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
        System.out.println("Invoice no.: " + t.getInvoice() + "  Auth Sequence: " + t.getAuthSequence() + "  Amount: "  + t.getAmount());
      }
    }
    System.out.println("----");
  }

  private static Long extractAuthSequenceFromList(List<Transaction> transactions){
    if(transactions.isEmpty()) {
      return null;
    } else {
      return transactions.iterator().next().getAuthSequence();
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
