package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.UK_LSE_MonthlyBuy;

@Repository
public interface UK_LSE_MonthlyBuyRepository extends MongoRepository<UK_LSE_MonthlyBuy, String> {
}