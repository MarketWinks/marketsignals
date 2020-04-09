package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_5MinBuy;

@Repository
public interface US_5MinBuyRepository extends MongoRepository<US_5MinBuy, String> {
}