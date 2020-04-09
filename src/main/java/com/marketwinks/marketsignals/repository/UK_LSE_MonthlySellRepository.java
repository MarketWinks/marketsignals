package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.UK_LSE_MonthlySell;

@Repository
public interface UK_LSE_MonthlySellRepository extends MongoRepository<UK_LSE_MonthlySell, String> {
}