package example;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;

public class BlueTarpAuthorizationService {
  private final BlueTarpAuthTerminal blueTarpAuthTerminal;
  private static final String SALE_XML = "" +
      "<bt:sale>" +
      "<bt:amount>%.2f</bt:amount>" +
      "<bt:job-code>%s</bt:job-code>" +
      "<bt:invoice>%s</bt:invoice>" +
      "</bt:sale>";
  private static final String CREDIT_XML = "" +
      "<bt:credit>" +
      "<bt:amount>%.2f</bt:amount>" +
      "<bt:job-code>%s</bt:job-code>" +
      "<bt:invoice>%s</bt:invoice>" +
      "</bt:credit>";
  private static final String HOLD_XML = "" +
      "<bt:deposit-hold>" +
      "<bt:amount>%.2f</bt:amount>" +
      "<bt:job-code>%s</bt:job-code>" +
      "<bt:invoice>%s</bt:invoice>" +
      "</bt:deposit-hold>";
  private static final String VOID_XML = "" +
      "<bt:void>" +
      "<bt:auth-seq>%d</bt:auth-seq>" +
      "</bt:void>";
  private static final String COLLECT_XML = "" +
      "<bt:deposit-collect>" +
      "<bt:amount>%.2f</bt:amount>" +
      "<bt:auth-seq>%d</bt:auth-seq>" +
      "<bt:job-code>%s</bt:job-code>" +
      "<bt:invoice>%s</bt:invoice>" +
      "</bt:deposit-collect>";
  private static final String AUTH_REQUEST_XML = "" +
      "<?xml version='1.0' encoding='utf-8'?>" +
      "<bt:bluetarp-authorization xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://api.bluetarp.com/ns/1.0 https://api.bluetarp.com/v1/Authorization.xsd\" xmlns:bt=\"http://api.bluetarp.com/ns/1.0\">" +
      "<bt:authorization-request>" +
      "<bt:merchant-number>%d</bt:merchant-number>" +
      "<bt:client-id>%s</bt:client-id>" +
      "<bt:client-username>%s</bt:client-username>" +
      "<bt:transaction-id>%s</bt:transaction-id>" +
      "<bt:purchaser-with-token>" +
      "<bt:token>%s</bt:token>" +
      "</bt:purchaser-with-token>" +
      "%s" +
      "</bt:authorization-request>" +
      "</bt:bluetarp-authorization>";
  public BlueTarpAuthorizationService(BlueTarpAuthTerminal blueTarpAuthTerminal){
    this.blueTarpAuthTerminal = blueTarpAuthTerminal;
  }

  public AuthorizationResponse runSaleWithToken(String clientUserName, Purchaser purchaser, Transaction sale) {
    String authRequestXML = buildRequestXML(clientUserName, purchaser, sale, buildSaleXML(sale));
    return sendAuthRequest(authRequestXML);
  }

  public AuthorizationResponse runCreditWithToken(String clientUserName, Purchaser purchaser, Transaction credit) {
    String authRequestXML = buildRequestXML(clientUserName, purchaser, credit, buildCreditXML(credit));
    return sendAuthRequest(authRequestXML);
  }

  public AuthorizationResponse runDepositHoldWithToken(String clientUserName, Purchaser purchaser, Transaction depositHold) {
    String authRequestXML = buildRequestXML(clientUserName, purchaser, depositHold, buildDepositHoldXML(depositHold));
    return sendAuthRequest(authRequestXML);
  }

  public AuthorizationResponse runDepositCollectWithToken(String clientUserName, Purchaser purchaser, Transaction depositCollect) {
    String authRequestXML = buildRequestXML(clientUserName, purchaser, depositCollect, buildDepositCollectXML(depositCollect));
    return sendAuthRequest(authRequestXML);
  }

  public AuthorizationResponse voidTransactionWithToken(String clientUserName, Purchaser purchaser, Transaction voidRequest) {
    String authRequestXML = buildRequestXML(clientUserName, purchaser, voidRequest, buildVoidXML(voidRequest));
    return sendAuthRequest(authRequestXML);
  }

  private String buildSaleXML(Transaction sale) {
    return String.format(SALE_XML, sale.getAmount(), sale.getJobCode(), sale.getInvoice());
  }

  private String buildCreditXML(Transaction credit) {
    return String.format(CREDIT_XML, credit.getAmount(), credit.getJobCode(), credit.getInvoice());
  }

  private String buildDepositHoldXML(Transaction depositHold) {
    return String.format(HOLD_XML, depositHold.getAmount(), depositHold.getJobCode(), depositHold.getInvoice());
  }

  private String buildDepositCollectXML(Transaction depositCollect) {
    return String.format(COLLECT_XML, depositCollect.getAmount(), depositCollect.getAuthSequence(), depositCollect.getJobCode(), depositCollect.getInvoice());
  }

  private String buildVoidXML(Transaction voidAuth) {
    return String.format(VOID_XML, voidAuth.getAuthSequence());
  }

  private String buildRequestXML(String clientUserName, Purchaser purchaser, Transaction transaction, String transactionXML) {
    return String.format(AUTH_REQUEST_XML, blueTarpAuthTerminal.getMerchantNumber(), blueTarpAuthTerminal.getMerchantTerminalName(),
        clientUserName, transaction.getTransactionId(), purchaser.getToken(), transactionXML);
  }

  private AuthorizationResponse sendAuthRequest(String authRequestXML) {
    System.out.println("Sending XML to integration:\n" + authRequestXML);
    String response = blueTarpAuthTerminal.sendAuthRequest(authRequestXML);
    AuthorizationResponse authorizationResponse = null;
    if(response != null) {
      System.out.println("Received XML response:\n" + response);
      authorizationResponse = new AuthorizationResponse();
      try {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(response.getBytes("UTF-8"))));
        XPath xPath = XPathFactory.newInstance().newXPath();
        authorizationResponse.setCode(xPath.evaluate("//bluetarp-authorization/authorization-response/code/text()",doc));
        authorizationResponse.setMessage(xPath.evaluate("//bluetarp-authorization/authorization-response/message/text()",doc));
        authorizationResponse.setTransactionId(xPath.evaluate("//bluetarp-authorization/authorization-response/transactionId/text()",doc));
        authorizationResponse.setAuthSeq(Long.valueOf(xPath.evaluate("//bluetarp-authorization/authorization-response/auth-seq/text()",doc)));
      } catch (Exception e){
        e.printStackTrace();
        System.out.println("Could not parse response");
      }

    }

    return authorizationResponse;
  }
}
