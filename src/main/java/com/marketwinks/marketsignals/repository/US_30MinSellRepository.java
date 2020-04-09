package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_30MinSell;

@Repository
public interface US_30MinSellRepository extends MongoRepository<US_30MinSell, String> {
}