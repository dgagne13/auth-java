package example;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlueTarpLookupService {
  private final BlueTarpAuthTerminal blueTarpAuthTerminal;
  private static final String CUSTOMER_LOOKUP_URL = "/customers";
  private static final String TRANSACTION_LOOKUP_URL="/transactions";
  private static final String INCLUDE_INACTIVE_PARAM = "include-inactive";
  private static final String PAGE_PARAM = "page";

  public BlueTarpLookupService(BlueTarpAuthTerminal blueTarpAuthTerminal){
    this.blueTarpAuthTerminal = blueTarpAuthTerminal;
  }

  public CustomerResponse findAll(Long page, boolean includeInactive) {
    String uri = buildCustomerURI(null, null, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  public CustomerResponse findByBlueTarpIdentifier(Long bluetarpIdentifier, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("bluetarp-cid", bluetarpIdentifier.toString(), page,includeInactive);
    return sendCustomerLookup(uri);
  }

  public CustomerResponse findByMerchantIdentifier(String merchantIdentifier, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("merchant-cid", merchantIdentifier, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  public CustomerResponse findByCustomerName(String customerName, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("cname", customerName, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  public CustomerResponse findByCustomerPhone(String phoneNumber, Long page, boolean includeInactive) {
    String uri = buildCustomerURI("phone", phoneNumber, page,includeInactive);
    return sendCustomerLookup(uri);
  }

  public List<Transaction> findVoidableTransactions() {
    return sendTransactionLookup(TRANSACTION_LOOKUP_URL + "/void");

  }

  public List<Transaction> findOpenDepositHolds() {
    return sendTransactionLookup(TRANSACTION_LOOKUP_URL + "/deposit");
  }


  private String buildCustomerURI(String queryName, String queryValue, Long page, boolean includeInactive){
    StringBuilder customerURI = new StringBuilder(CUSTOMER_LOOKUP_URL);
    if(queryName != null || page != null || includeInactive){
      customerURI.append("?");
      if(queryName != null && queryValue != null) {
        customerURI.append(String.format("%s=%s", queryName, queryValue));
        if(page != null || includeInactive){
          customerURI.append("&");
        }
      }
      if (page != null) {
        customerURI.append(String.format("%s=%s",PAGE_PARAM, page.toString()));
        if(includeInactive){
          customerURI.append("&");
        }
      }
      if (includeInactive) {
        customerURI.append(String.format("%s=%s",INCLUDE_INACTIVE_PARAM, "true"));
      }
    }


    return customerURI.toString();
  }

  private CustomerResponse sendCustomerLookup(String customerUri) {
    String responseXML = blueTarpAuthTerminal.sendLookupRequest(customerUri);

    if (responseXML == null) {
      return null;
    }
    System.out.println("Received XML response:\n" + responseXML);
    System.out.println("--------------");
    System.out.println("Parse with your favorite xml library...getting only first customer, purchaser for the example");
    CustomerResponse customerResponse = new CustomerResponse();
    ArrayList<Customer> customers = new ArrayList<Customer>();
    customerResponse.setCustomers(customers);
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(responseXML.getBytes("UTF-8"))));
      XPath xPath = XPathFactory.newInstance().newXPath();
      customerResponse.setMorePages(Boolean.valueOf(xPath.evaluate("//bluetarp-authorization/customer-response/more-pages", doc)));
      Node node = (Node)xPath.evaluate("//bluetarp-authorization/customer-response/customers/customer[1]", doc, XPathConstants.NODE);
      if(node != null) {
        Customer c = new Customer();
        customers.add(c);
        c.setBlueTarpIdentifier(Long.valueOf(xPath.evaluate(".//number/text()",node)));
        c.setName(xPath.evaluate(".//name/text()",node));
        c.setMerchantIdentifier(xPath.evaluate(".//merchant-identifier/text()",node));
        List<Purchaser> purchasers = new ArrayList<Purchaser>();
        c.setPurchasers(purchasers);
        Node purchaserNode = (Node)xPath.evaluate(".//purchasers/purchaser[1]", node, XPathConstants.NODE);
        if(purchaserNode != null) {
          Purchaser p = new Purchaser();
          purchasers.add(p);
          p.setName(xPath.evaluate(".//name/text()", purchaserNode));
          p.setToken(xPath.evaluate(".//token/text()",purchaserNode));
        }
      }
    } catch (Exception e){
      e.printStackTrace();
      System.out.println("Could not parse response");
    }
    return customerResponse;
  }

  private List<Transaction> sendTransactionLookup(String transactionUri) {
    List<Transaction> transactions = Collections.EMPTY_LIST;
    String responseXML = blueTarpAuthTerminal.sendLookupRequest(transactionUri);
    if(responseXML != null) {
      transactions = new ArrayList<Transaction>();
      System.out.println("Received XML response:\n" + responseXML);
      System.out.println("--------------");
      System.out.println("Parse with your favorite xml library...getting only first transaction for the example");
      try {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(responseXML.getBytes("UTF-8"))));
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node)xPath.evaluate("//bluetarp-authorization/transaction-response/transactions/transaction[1]", doc, XPathConstants.NODE);
        if(node != null) {
          Transaction t = new Transaction();
          t.setAuthSequence(Long.valueOf(xPath.evaluate(".//auth-seq/text()",node)));
          t.setAmount(new BigDecimal(xPath.evaluate(".//amount/text()",node)));
          t.setInvoice(xPath.evaluate(".//invoice/text()",node));
          transactions.add(t);
        }
      } catch (Exception e){
        e.printStackTrace();
        System.out.println("Could not parse response");
      }

    }



    return transactions;
  }

}
