package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_hourlybuys;

@Repository
public interface US_HourlyBuyRepository extends MongoRepository<us_hourlybuys, String> {
}