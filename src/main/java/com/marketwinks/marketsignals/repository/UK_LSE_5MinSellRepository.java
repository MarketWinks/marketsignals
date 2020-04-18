package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.uk_lse_5minsells;

@Repository
public interface UK_LSE_5MinSellRepository extends MongoRepository<uk_lse_5minsells, String> {
}