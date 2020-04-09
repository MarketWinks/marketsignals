package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_30MinBuy;

@Repository
public interface US_30MinBuyRepository extends MongoRepository<US_30MinBuy, String> {
}