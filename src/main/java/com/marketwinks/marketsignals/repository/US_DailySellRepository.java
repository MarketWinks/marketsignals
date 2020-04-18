package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_dailysells;

@Repository
public interface US_DailySellRepository extends MongoRepository<us_dailysells, String> {
}