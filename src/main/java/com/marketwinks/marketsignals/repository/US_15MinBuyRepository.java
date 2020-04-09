package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_15MinBuy;

@Repository
public interface US_15MinBuyRepository extends MongoRepository<US_15MinBuy, String> {
}