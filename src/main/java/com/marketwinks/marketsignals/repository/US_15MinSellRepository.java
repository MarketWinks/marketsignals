package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_15minsells;

@Repository
public interface US_15MinSellRepository extends MongoRepository<us_15minsells, String> {
}