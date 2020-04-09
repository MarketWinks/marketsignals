package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_DailySell;

@Repository
public interface US_DailySellRepository extends MongoRepository<US_DailySell, String> {
}