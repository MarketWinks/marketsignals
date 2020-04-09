package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_HourlyBuy;

@Repository
public interface US_HourlyBuyRepository extends MongoRepository<US_HourlyBuy, String> {
}