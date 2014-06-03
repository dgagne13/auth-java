package example;

import java.util.List;

public class Customer {

  protected Long blueTarpIdentifier;
  protected String name;
  protected String merchantIdentifier;
  protected Double availableCredit;
  protected Double creditLimit;
  protected List<Purchaser> purchasers;

  public Long getBlueTarpIdentifier() { return blueTarpIdentifier;}
  public void setBlueTarpIdentifier(Long blueTarpIdentifier) { this.blueTarpIdentifier = blueTarpIdentifier; }

  public String getName() { return name; }
  public void setName(String value) { this.name = value; }

  public String getMerchantIdentifier() { return merchantIdentifier; }
  public void setMerchantIdentifier(String value) { this.merchantIdentifier = value; }

  public Double getAvailableCredit() { return availableCredit; }
  public void setAvailableCredit(Double value) { this.availableCredit = value; }

  public Double getCreditLimit() { return creditLimit; }
  public void setCreditLimit(Double value) { this.creditLimit = value; }

  public List<Purchaser> getPurchasers() { return purchasers; }
  public void setPurchasers(List<Purchaser> purchasers) { this.purchasers = purchasers; }

}
