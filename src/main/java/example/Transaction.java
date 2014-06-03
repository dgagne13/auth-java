package example;

import java.math.BigDecimal;

public class Transaction {
  public enum TransactionType { Sale, Credit, DepositCollect, DepositHold, Void}
  protected Long authSequence;
  protected String transactionId;
  protected String jobCode;
  protected String invoice;
  protected BigDecimal amount;

  public Long getAuthSequence() { return authSequence; }
  public void setAuthSequence(Long authSequence) { this.authSequence = authSequence; }

  public String getTransactionId(){ return this.transactionId; }
  public void setTransactionId(String transactionId) { this.transactionId = transactionId;}

  public String getJobCode() { return jobCode; }
  public void setJobCode(String value) { this.jobCode = value; }

  public String getInvoice() { return invoice; }
  public void setInvoice(String value) { this.invoice = value; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }

}
