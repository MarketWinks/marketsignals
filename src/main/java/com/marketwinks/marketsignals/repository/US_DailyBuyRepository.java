package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_DailyBuy;

@Repository
public interface US_DailyBuyRepository extends MongoRepository<US_DailyBuy, String> {
}