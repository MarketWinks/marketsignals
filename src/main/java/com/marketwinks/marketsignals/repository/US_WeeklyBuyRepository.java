package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_WeeklyBuy;

@Repository
public interface US_WeeklyBuyRepository extends MongoRepository<US_WeeklyBuy, String> {
}