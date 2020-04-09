package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.UK_LSE_30MinBuy;

@Repository
public interface UK_LSE_30MinBuyRepository extends MongoRepository<UK_LSE_30MinBuy, String> {
}