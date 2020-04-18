package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_5minbuys;

@Repository
public interface US_5MinBuyRepository extends MongoRepository<us_5minbuys, String> {
}