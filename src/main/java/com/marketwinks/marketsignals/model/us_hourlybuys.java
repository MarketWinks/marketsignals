package com.marketwinks.marketsignals.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

//Entity
//@Document(collection = "HourlyBuy")
public class us_hourlybuys {

	@Id
	public ObjectId _id;

	private int month;
	private int year;

	private String company;

	private String indicator;
	private double confidence_level;

	private java.time.LocalDateTime lastBuyEvent;
	private double lastBuyPrice;

	private java.time.LocalDateTime lastEvent;
	private boolean isLastEventBuy;
	private double lastEventPrice;

	// ObjectId needs to be converted to string
	public String get_id() {
		return _id.toHexString();
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public double getConfidence_level() {
		return confidence_level;
	}

	public void setConfidence_level(double confidence_level) {
		this.confidence_level = confidence_level;
	}

	public java.time.LocalDateTime getLastBuyEvent() {
		return lastBuyEvent;
	}

	public void setLastBuyEvent(java.time.LocalDateTime lastBuyEvent) {
		this.lastBuyEvent = lastBuyEvent;
	}

	public double getLastBuyPrice() {
		return lastBuyPrice;
	}

	public void setLastBuyPrice(double lastBuyPrice) {
		this.lastBuyPrice = lastBuyPrice;
	}

	public java.time.LocalDateTime getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(java.time.LocalDateTime lastEvent) {
		this.lastEvent = lastEvent;
	}

	public boolean isLastEventBuy() {
		return isLastEventBuy;
	}

	public void setLastEventBuy(boolean isLastEventBuy) {
		this.isLastEventBuy = isLastEventBuy;
	}

	public double getLastEventPrice() {
		return lastEventPrice;
	}

	public void setLastEventPrice(double lastEventPrice) {
		this.lastEventPrice = lastEventPrice;
	}

}
