package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_MonthlySell;

@Repository
public interface US_MonthlySellRepository extends MongoRepository<US_MonthlySell, String> {
}