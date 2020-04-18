package com.marketwinks.marketsignals.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

//Entity
//@Document(collection = "_15MinSell")
public class us_15minsells {

	@Id
	public ObjectId _id;

	private int month;
	private int year;

	private String company;

	private String indicator;
	private double confidence_level;

	private java.time.LocalDateTime lastSellEvent;
	private double lastSellPrice;

	private java.time.LocalDateTime lastEvent;
	private boolean isLastEventSell;
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

	public java.time.LocalDateTime getLastSellEvent() {
		return lastSellEvent;
	}

	public void setLastSellEvent(java.time.LocalDateTime lastSellEvent) {
		this.lastSellEvent = lastSellEvent;
	}

	public double getLastSellPrice() {
		return lastSellPrice;
	}

	public void setLastSellPrice(double lastSellPrice) {
		this.lastSellPrice = lastSellPrice;
	}

	public java.time.LocalDateTime getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(java.time.LocalDateTime lastEvent) {
		this.lastEvent = lastEvent;
	}

	public boolean isLastEventSell() {
		return isLastEventSell;
	}

	public void setLastEventSell(boolean isLastEventSell) {
		this.isLastEventSell = isLastEventSell;
	}

	public double getLastEventPrice() {
		return lastEventPrice;
	}

	public void setLastEventPrice(double lastEventPrice) {
		this.lastEventPrice = lastEventPrice;
	}

}
