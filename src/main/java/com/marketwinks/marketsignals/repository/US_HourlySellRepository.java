package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_HourlySell;

@Repository
public interface US_HourlySellRepository extends MongoRepository<US_HourlySell, String> {
}