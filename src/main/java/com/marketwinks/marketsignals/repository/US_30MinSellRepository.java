package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_30minsells;

@Repository
public interface US_30MinSellRepository extends MongoRepository<us_30minsells, String> {
}