package example;

import java.util.List;

public class CustomerResponse {

  protected List<Customer> customers;
  protected Boolean morePages;

  public Boolean isMorePages() { return morePages; }
  public void setMorePages(Boolean value) { this.morePages = value; }

  public List<Customer> getCustomers() { return customers; }
  public void setCustomers(List<Customer> customers) { this.customers = customers; }

}
