package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_5minsells;

@Repository
public interface US_5MinSellRepository extends MongoRepository<us_5minsells, String> {
}