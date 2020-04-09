package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.UK_LSE_5MinBuy;

@Repository
public interface UK_LSE_5MinBuyRepository extends MongoRepository<UK_LSE_5MinBuy, String> {
}