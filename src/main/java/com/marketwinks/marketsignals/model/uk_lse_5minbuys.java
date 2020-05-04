package com.marketwinks.marketsignals.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

//Entity
//@Document(collection = "_5MinBuy")
public class uk_lse_5minbuys {

	@Id
	public ObjectId _id;

	private int month;
	private int year;

	private String company;

	private String indicator;
	private double confidence_level;

	private String lastBuyEvent;
	private double lastBuyPrice;

	private String lastEvent;
	private boolean isLastEventBuy;
	private double lastEventPrice;

	private double lasttradedprice;

	private String expirytime;
	private double expiryprice;

	private double targetlowrisk;
	private double targethighrisk;
	private double stoplosslowrisk;
	private double stoplosshighrisk;

	private int likes;

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

	public String getLastBuyEvent() {
		return lastBuyEvent;
	}

	public void setLastBuyEvent(String buy_opportunity) {
		this.lastBuyEvent = buy_opportunity;
	}

	public double getLastBuyPrice() {
		return lastBuyPrice;
	}

	public void setLastBuyPrice(double lastBuyPrice) {
		this.lastBuyPrice = lastBuyPrice;
	}

	public String getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(String last_opportunity) {
		this.lastEvent = last_opportunity;
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

	/**
	 * @return the lasttradedprice
	 */
	public double getLasttradedprice() {
		return lasttradedprice;
	}

	/**
	 * @param lasttradedprice
	 *            the lasttradedprice to set
	 */
	public void setLasttradedprice(double lasttradedprice) {
		this.lasttradedprice = lasttradedprice;
	}

	/**
	 * @return the expirytime
	 */
	public String getExpirytime() {
		return expirytime;
	}

	/**
	 * @param expirytime
	 *            the expirytime to set
	 */
	public void setExpirytime(String expirytime) {
		this.expirytime = expirytime;
	}

	/**
	 * @return the expiryprice
	 */
	public double getExpiryprice() {
		return expiryprice;
	}

	/**
	 * @param expiryprice
	 *            the expiryprice to set
	 */
	public void setExpiryprice(double expiryprice) {
		this.expiryprice = expiryprice;
	}

	/**
	 * @return the targetlowrisk
	 */
	public double getTargetlowrisk() {
		return targetlowrisk;
	}

	/**
	 * @param targetlowrisk
	 *            the targetlowrisk to set
	 */
	public void setTargetlowrisk(double targetlowrisk) {
		this.targetlowrisk = targetlowrisk;
	}

	/**
	 * @return the targethighrisk
	 */
	public double getTargethighrisk() {
		return targethighrisk;
	}

	/**
	 * @param targethighrisk
	 *            the targethighrisk to set
	 */
	public void setTargethighrisk(double targethighrisk) {
		this.targethighrisk = targethighrisk;
	}

	/**
	 * @return the stoplosslowrisk
	 */
	public double getStoplosslowrisk() {
		return stoplosslowrisk;
	}

	/**
	 * @param stoplosslowrisk
	 *            the stoplosslowrisk to set
	 */
	public void setStoplosslowrisk(double stoplosslowrisk) {
		this.stoplosslowrisk = stoplosslowrisk;
	}

	/**
	 * @return the stoplosshighrisk
	 */
	public double getStoplosshighrisk() {
		return stoplosshighrisk;
	}

	/**
	 * @param stoplosshighrisk
	 *            the stoplosshighrisk to set
	 */
	public void setStoplosshighrisk(double stoplosshighrisk) {
		this.stoplosshighrisk = stoplosshighrisk;
	}

	/**
	 * @return the likes
	 */
	public int getLikes() {
		return likes;
	}

	/**
	 * @param likes
	 *            the likes to set
	 */
	public void setLikes(int likes) {
		this.likes = likes;
	}

}
