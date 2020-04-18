package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_dailybuys;

@Repository
public interface US_DailyBuyRepository extends MongoRepository<us_dailybuys, String> {
}