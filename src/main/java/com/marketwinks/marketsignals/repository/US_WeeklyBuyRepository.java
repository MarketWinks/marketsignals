package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_weeklybuys;

@Repository
public interface US_WeeklyBuyRepository extends MongoRepository<us_weeklybuys, String> {
}