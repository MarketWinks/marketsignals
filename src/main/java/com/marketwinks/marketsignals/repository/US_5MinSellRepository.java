package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_5MinSell;

@Repository
public interface US_5MinSellRepository extends MongoRepository<US_5MinSell, String> {
}