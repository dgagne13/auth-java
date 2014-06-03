package com.bluetarp.authorization.api.v1.service;

import com.bluetarp.authorization.api.v1.model.Customer;
import com.bluetarp.authorization.api.v1.model.CustomerResponse;
import com.bluetarp.authorization.api.v1.model.Transaction;

import java.util.List;

public interface BlueTarpLookupService {
  CustomerResponse findAll(Long page, boolean includeInactive);
  CustomerResponse findByBlueTarpIdentifier(Long bluetarpIdentifier, Long page, boolean includeInactive);
  CustomerResponse findByMerchantIdentifier(String merchantIdentifier, Long page, boolean includeInactive);
  CustomerResponse findByCustomerName(String customerName, Long page, boolean includeInactive);
  CustomerResponse findByCustomerPhone(String phoneNumber, Long page, boolean includeInactive);
  List<Transaction> findVoidableTransactions();
  List<Transaction> findOpenDepositHolds();
}
