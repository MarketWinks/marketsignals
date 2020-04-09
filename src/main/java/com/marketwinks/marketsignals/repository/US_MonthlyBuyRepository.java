package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_MonthlyBuy;

@Repository
public interface US_MonthlyBuyRepository extends MongoRepository<US_MonthlyBuy, String> {
}