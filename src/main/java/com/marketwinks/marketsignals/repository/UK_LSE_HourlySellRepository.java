package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.UK_LSE_HourlySell;

@Repository
public interface UK_LSE_HourlySellRepository extends MongoRepository<UK_LSE_HourlySell, String> {
}