package com.marketwinks.marketsignals.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.marketwinks.marketsignals.model.us_monthlybuys;

@Repository
public interface US_MonthlyBuyRepository extends MongoRepository<us_monthlybuys, String> {
}