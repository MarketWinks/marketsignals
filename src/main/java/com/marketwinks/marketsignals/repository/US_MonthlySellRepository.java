package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_monthlysells;

@Repository
public interface US_MonthlySellRepository extends MongoRepository<us_monthlysells, String> {
}