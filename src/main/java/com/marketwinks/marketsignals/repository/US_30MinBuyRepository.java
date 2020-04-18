package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_30minbuys;

@Repository
public interface US_30MinBuyRepository extends MongoRepository<us_30minbuys, String> {
}