package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_weeklysells;

@Repository
public interface US_WeeklySellRepository extends MongoRepository<us_weeklysells, String> {
}