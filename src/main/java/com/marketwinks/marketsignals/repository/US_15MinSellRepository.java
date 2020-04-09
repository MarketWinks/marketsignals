package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.US_15MinSell;

@Repository
public interface US_15MinSellRepository extends MongoRepository<US_15MinSell, String> {
}