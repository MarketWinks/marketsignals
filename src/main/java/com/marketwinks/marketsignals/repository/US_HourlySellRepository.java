package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_hourlysells;

@Repository
public interface US_HourlySellRepository extends MongoRepository<us_hourlysells, String> {
}