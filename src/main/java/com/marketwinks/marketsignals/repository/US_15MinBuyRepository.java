package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_15minbuys;

@Repository
public interface US_15MinBuyRepository extends MongoRepository<us_15minbuys, String> {
}