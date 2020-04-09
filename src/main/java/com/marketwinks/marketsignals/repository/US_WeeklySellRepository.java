package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_WeeklySell;

@Repository
public interface US_WeeklySellRepository extends MongoRepository<US_WeeklySell, String> {
}