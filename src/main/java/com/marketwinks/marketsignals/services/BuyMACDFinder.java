package com.marketwinks.marketsignals.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.patriques.AlphaVantageConnector;
import org.patriques.TechnicalIndicators;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.MACD;
import org.patriques.output.technicalindicators.data.MACDData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.marketwinks.marketsignals.model.uk_lse_15minbuys;
import com.marketwinks.marketsignals.model.uk_lse_15minsells;
import com.marketwinks.marketsignals.model.uk_lse_30minbuys;
import com.marketwinks.marketsignals.model.uk_lse_30minsells;
import com.marketwinks.marketsignals.model.uk_lse_5minbuys;
import com.marketwinks.marketsignals.model.uk_lse_5minsells;
import com.marketwinks.marketsignals.model.uk_lse_dailybuys;
import com.marketwinks.marketsignals.model.uk_lse_dailysells;
import com.marketwinks.marketsignals.model.uk_lse_hourlybuys;
import com.marketwinks.marketsignals.model.uk_lse_hourlysells;
import com.marketwinks.marketsignals.model.uk_lse_monthlybuys;
import com.marketwinks.marketsignals.model.uk_lse_monthlysells;
import com.marketwinks.marketsignals.model.uk_lse_weeklybuys;
import com.marketwinks.marketsignals.model.uk_lse_weeklysells;
import com.marketwinks.marketsignals.model.us_15minbuys;
import com.marketwinks.marketsignals.model.us_30minbuys;
import com.marketwinks.marketsignals.model.us_5minbuys;
import com.marketwinks.marketsignals.model.us_dailybuys;
import com.marketwinks.marketsignals.model.us_hourlybuys;
import com.marketwinks.marketsignals.model.us_monthlybuys;
import com.marketwinks.marketsignals.model.us_weeklybuys;
import com.marketwinks.marketsignals.repository.UK_LSE_15MinBuyRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_30MinBuyRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_5MinBuyRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_DailyBuyRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_HourlyBuyRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_MonthlyBuyRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_WeeklyBuyRepository;
import com.marketwinks.marketsignals.repository.US_15MinBuyRepository;
import com.marketwinks.marketsignals.repository.US_30MinBuyRepository;
import com.marketwinks.marketsignals.repository.US_5MinBuyRepository;
import com.marketwinks.marketsignals.repository.US_DailyBuyRepository;
import com.marketwinks.marketsignals.repository.US_HourlyBuyRepository;
import com.marketwinks.marketsignals.repository.US_MonthlyBuyRepository;
import com.marketwinks.marketsignals.repository.US_WeeklyBuyRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@RestController
@RequestMapping("/baseURL")
public class BuyMACDFinder {

	@Autowired
	private US_MonthlyBuyRepository monthlybuysRepository;

	@Autowired
	private US_WeeklyBuyRepository weeklybuysRepository;

	@Autowired
	private US_DailyBuyRepository dailybuysRepository;

	@Autowired
	private US_HourlyBuyRepository hourlyBuyRepository;

	@Autowired
	private US_5MinBuyRepository __5MinBuyRepository;

	@Autowired
	private US_15MinBuyRepository __15MinBuyRepository;

	@Autowired
	private US_30MinBuyRepository __30MinBuyRepository;

	@Autowired
	private UK_LSE_5MinBuyRepository UK_LSE__5MinBuyRepository;

	@Autowired
	private UK_LSE_15MinBuyRepository UK_LSE__15MinBuyRepository;

	@Autowired
	private UK_LSE_30MinBuyRepository UK_LSE__30MinBuyRepository;

	@Autowired
	private UK_LSE_HourlyBuyRepository UK_LSE__HourlyBuyRepository;

	@Autowired
	private UK_LSE_DailyBuyRepository UK_LSE__DailyBuyRepository;

	@Autowired
	private UK_LSE_WeeklyBuyRepository UK_LSE__WeeklyBuyRepository;

	@Autowired
	private UK_LSE_MonthlyBuyRepository UK_LSE__MonthlyBuyRepository;

	@RequestMapping(value = "/findMarketSignals/MACD/Monthly/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDMonthlyBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("MONTHLY SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.MONTHLY, TimePeriod.of(10), SeriesType.CLOSE,
					null, null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 2 here and counter>5 since its
				// monthly
				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1.5) {
					// TODO: experimental: signal average*1.5 maintained

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_monthlybuys monthlybuys = new us_monthlybuys();
		monthlybuys.setMonth(new java.util.Date().getMonth());
		monthlybuys.setYear(new java.util.Date().getYear());
		monthlybuys.setCompany(company);
		monthlybuys.setIndicator("LIVE");
		monthlybuys.setConfidence_level(confidence_level);
		monthlybuys.setLastBuyEvent(buy_opportunity);
		monthlybuys.setLastBuyPrice(0.0);
		monthlybuys.setLastEvent(last_opportunity);
		monthlybuys.setLastEventBuy(isLastEventBuy);
		monthlybuys.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_monthlybuys saveresult = monthlybuysRepository.insert(monthlybuys);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Weekly/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDWeeklyBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("WEEKLY SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.WEEKLY, TimePeriod.of(10), SeriesType.CLOSE,
					null, null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 5 here and counter>8 since its
				// monthly
				if (counter > 8 && counter - buy_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1.5) {
					// TODO: experimental: signal average*1.5 maintained

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_weeklybuys weeklybuys = new us_weeklybuys();
		weeklybuys.setMonth(new java.util.Date().getMonth());
		weeklybuys.setYear(new java.util.Date().getYear());
		weeklybuys.setCompany(company);
		weeklybuys.setIndicator("LIVE");
		weeklybuys.setConfidence_level(confidence_level);
		weeklybuys.setLastBuyEvent(buy_opportunity);
		weeklybuys.setLastBuyPrice(0.0);
		weeklybuys.setLastEvent(last_opportunity);
		weeklybuys.setLastEventBuy(isLastEventBuy);
		weeklybuys.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_weeklybuys saveresult = weeklybuysRepository.insert(weeklybuys);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Daily/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDDailyBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("DAILY SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.DAILY, TimePeriod.of(10), SeriesType.CLOSE, null,
					null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 10 here and counter>15 since its
				// daily
				if (counter > 10 && counter - buy_counter >= 15 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1.5) {
					// TODO: experimental: signal average*1.5 maintained

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_dailybuys dailybuys = new us_dailybuys();
		dailybuys.setMonth(new java.util.Date().getMonth());
		dailybuys.setYear(new java.util.Date().getYear());
		dailybuys.setCompany(company);
		dailybuys.setIndicator("LIVE");
		dailybuys.setConfidence_level(confidence_level);
		dailybuys.setLastBuyEvent(buy_opportunity);
		dailybuys.setLastBuyPrice(0.0);
		dailybuys.setLastEvent(last_opportunity);
		dailybuys.setLastEventBuy(isLastEventBuy);
		dailybuys.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_dailybuys saveresult = dailybuysRepository.insert(dailybuys);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Hourly/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDHourlyBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("HOURLY SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.SIXTY_MIN, TimePeriod.of(10), SeriesType.CLOSE,
					null, null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 10 here and counter>15 since its
				// hourly
				if (counter > 5 && counter - buy_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0 && Math.abs(macdData.get(i).getSignal()) > signal_average) {
					// TODO: experimental: signal average*1.5 not maintained for hourly

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_hourlybuys hourlyBuy = new us_hourlybuys();
		hourlyBuy.setMonth(new java.util.Date().getMonth());
		hourlyBuy.setYear(new java.util.Date().getYear());
		hourlyBuy.setCompany(company);
		hourlyBuy.setIndicator("LIVE");
		hourlyBuy.setConfidence_level(confidence_level);
		hourlyBuy.setLastBuyEvent(buy_opportunity);
		hourlyBuy.setLastBuyPrice(0.0);
		hourlyBuy.setLastEvent(last_opportunity);
		hourlyBuy.setLastEventBuy(isLastEventBuy);
		hourlyBuy.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_hourlybuys saveresult = hourlyBuyRepository.insert(hourlyBuy);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_30Min/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACD30MinBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("30MIN SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.THIRTY_MIN, TimePeriod.of(10), SeriesType.CLOSE,
					null, null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 2 here and counter>5 since its
				// monthly
				if (counter > 5 && counter - buy_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1) {
					// TODO: experimental: signal average*1.5 maintained not here

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_30minbuys __30MinBuy = new us_30minbuys();
		__30MinBuy.setMonth(new java.util.Date().getMonth());
		__30MinBuy.setYear(new java.util.Date().getYear());
		__30MinBuy.setCompany(company);
		__30MinBuy.setIndicator("LIVE");
		__30MinBuy.setConfidence_level(confidence_level);
		__30MinBuy.setLastBuyEvent(buy_opportunity);
		__30MinBuy.setLastBuyPrice(0.0);
		__30MinBuy.setLastEvent(last_opportunity);
		__30MinBuy.setLastEventBuy(isLastEventBuy);
		__30MinBuy.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_30minbuys saveresult = __30MinBuyRepository.insert(__30MinBuy);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_15Min/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACD15MinBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("15MIN SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.FIFTEEN_MIN, TimePeriod.of(10), SeriesType.CLOSE,
					null, null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 2 here and counter>5 since its
				// monthly
				if (counter > 5 && counter - buy_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1) {
					// TODO: experimental: signal average*1.5 maintained not here

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_15minbuys __15MinBuy = new us_15minbuys();
		__15MinBuy.setMonth(new java.util.Date().getMonth());
		__15MinBuy.setYear(new java.util.Date().getYear());
		__15MinBuy.setCompany(company);
		__15MinBuy.setIndicator("LIVE");
		__15MinBuy.setConfidence_level(confidence_level);
		__15MinBuy.setLastBuyEvent(buy_opportunity);
		__15MinBuy.setLastBuyPrice(0.0);
		__15MinBuy.setLastEvent(last_opportunity);
		__15MinBuy.setLastEventBuy(isLastEventBuy);
		__15MinBuy.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_15minbuys saveresult = __15MinBuyRepository.insert(__15MinBuy);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_5Min/BUY/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACD5MinBUYSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(apiConnector);

		try {
			System.out.println("5MIN SIGNALS:");

			// TODO Timeperiod is noise controller
			MACD response = technicalIndicators.macd(company, Interval.FIVE_MIN, TimePeriod.of(10), SeriesType.CLOSE,
					null, null, null);
			Map<String, String> metaData = response.getMetaData();
			System.out.println("Symbol: " + metaData.get("1: Symbol"));
			System.out.println("Indicator: " + metaData.get("2: Indicator"));

			List<MACDData> macdData = response.getData();
			size = macdData.size();
			for (int i = size - 1, counter = 1; i >= 0; i--, counter++) {
				/*
				 * System.out.println("date:           " + macdData.get(i).getDateTime());
				 * System.out.println("MACD Histogram: " + macdData.get(i).getHist());
				 * System.out.println("MACD Signal:    " + macdData.get(i).getSignal());
				 * System.out.println("MACD:           " + macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1) + Math.abs(macdData.get(i).getSignal())) / counter;

				// TODO experimental: the difference is set to 2 here and counter>5 since its
				// monthly
				if (counter > 5 && counter - buy_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() < 0
						&& macdData.get(i).getHist() > 0 && macdData.get(i).getSignal() < 0
						&& macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1) {
					// TODO: experimental: signal average*1.5 maintained not here

					buy_counter = counter;

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() > 0 && macdData.get(i).getHist() < 0
						&& macdData.get(i).getSignal() > 0 && macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_5minbuys __5MinBuy = new us_5minbuys();
		__5MinBuy.setMonth(new java.util.Date().getMonth());
		__5MinBuy.setYear(new java.util.Date().getYear());
		__5MinBuy.setCompany(company);
		__5MinBuy.setIndicator("LIVE");
		__5MinBuy.setConfidence_level(confidence_level);
		__5MinBuy.setLastBuyEvent(buy_opportunity);
		__5MinBuy.setLastBuyPrice(0.0);
		__5MinBuy.setLastEvent(last_opportunity);
		__5MinBuy.setLastEventBuy(isLastEventBuy);
		__5MinBuy.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_5minbuys saveresult = __5MinBuyRepository.insert(__5MinBuy);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_15Min/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACD15MinsBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();
		
		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;

		
		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_15mins/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}
				
				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}


			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_15minbuys UK_LSE_15MinBuy = new uk_lse_15minbuys();
		UK_LSE_15MinBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_15MinBuy.setYear(new java.util.Date().getYear());
		UK_LSE_15MinBuy.setCompany(company);
		UK_LSE_15MinBuy.setIndicator("LIVE");
		UK_LSE_15MinBuy.setConfidence_level(confidence_level);
		UK_LSE_15MinBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_15MinBuy.setLastBuyPrice(buy_price);
		UK_LSE_15MinBuy.setLastEvent(last_opportunity);
		UK_LSE_15MinBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_15MinBuy.setLastEventPrice(0.0);
		UK_LSE_15MinBuy.setLasttradedprice(buy_price);

		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_15minbuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_15minbuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_15minbuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_15minsells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_15minbuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_15minsells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_15MinBuy.isLastEventBuy() == true) {
			uk_lse_15minbuys saveresult = UK_LSE__15MinBuyRepository.insert(UK_LSE_15MinBuy);

		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_30Min/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACD30MinsBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;

		
		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_30mins/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}
				
				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}


			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_30minbuys UK_LSE_30MinBuy = new uk_lse_30minbuys();
		UK_LSE_30MinBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_30MinBuy.setYear(new java.util.Date().getYear());
		UK_LSE_30MinBuy.setCompany(company);
		UK_LSE_30MinBuy.setIndicator("LIVE");
		UK_LSE_30MinBuy.setConfidence_level(confidence_level);
		UK_LSE_30MinBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_30MinBuy.setLastBuyPrice(buy_price);
		UK_LSE_30MinBuy.setLastEvent(last_opportunity);
		UK_LSE_30MinBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_30MinBuy.setLastEventPrice(0.0);
		UK_LSE_30MinBuy.setLasttradedprice(buy_price);
		
		
		
		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_30minbuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_30minbuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_30minbuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_30minsells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_30minbuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_30minsells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_30MinBuy.isLastEventBuy() == true) {
			uk_lse_30minbuys saveresult = UK_LSE__30MinBuyRepository.insert(UK_LSE_30MinBuy);


		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;

		

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Hourly/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDHourlyBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();
		
		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;


		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_hourly/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;
				
				System.out.println(key);
				System.out.println(Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())));
				
				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}
				
				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}


			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_hourlybuys UK_LSE_HourlyBuy = new uk_lse_hourlybuys();
		UK_LSE_HourlyBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_HourlyBuy.setYear(new java.util.Date().getYear());
		UK_LSE_HourlyBuy.setCompany(company);
		UK_LSE_HourlyBuy.setIndicator("LIVE");
		UK_LSE_HourlyBuy.setConfidence_level(confidence_level);
		UK_LSE_HourlyBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_HourlyBuy.setLastBuyPrice(buy_price);
		UK_LSE_HourlyBuy.setLastEvent(last_opportunity);
		UK_LSE_HourlyBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_HourlyBuy.setLastEventPrice(0.0);
		UK_LSE_HourlyBuy.setLasttradedprice(buy_price);
		
		
		
		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_hourlybuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_hourlybuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_hourlybuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_hourlysells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_hourlybuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_hourlysells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_HourlyBuy.isLastEventBuy() == true) {
			
			uk_lse_hourlybuys saveresult = UK_LSE__HourlyBuyRepository.insert(UK_LSE_HourlyBuy);



		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;

		

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_5Min/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACD5MinsBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;
		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_5mins/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}

			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		// ALL CALCS OVER - NOW ACTIONS

		uk_lse_5minbuys UK_LSE_5MinBuy = new uk_lse_5minbuys();
		UK_LSE_5MinBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_5MinBuy.setYear(new java.util.Date().getYear());
		UK_LSE_5MinBuy.setCompany(company);
		UK_LSE_5MinBuy.setIndicator("LIVE");
		UK_LSE_5MinBuy.setConfidence_level(confidence_level);
		UK_LSE_5MinBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_5MinBuy.setLastBuyPrice(buy_price);
		UK_LSE_5MinBuy.setLastEvent(last_opportunity);
		UK_LSE_5MinBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_5MinBuy.setLastEventPrice(0.0);
		UK_LSE_5MinBuy.setLasttradedprice(buy_price);

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_5minbuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_5minbuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_5minbuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_5minsells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_5minbuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_5minsells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_5MinBuy.isLastEventBuy() == true) {
			uk_lse_5minbuys saveresult = UK_LSE__5MinBuyRepository.insert(UK_LSE_5MinBuy);
		}
		// TO DO price need to be populated

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Weekly/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDWeeklyBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;

		
		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_weekly/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}
				
				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}


			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_weeklybuys UK_LSE_WeeklyBuy = new uk_lse_weeklybuys();
		UK_LSE_WeeklyBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_WeeklyBuy.setYear(new java.util.Date().getYear());
		UK_LSE_WeeklyBuy.setCompany(company);
		UK_LSE_WeeklyBuy.setIndicator("LIVE");
		UK_LSE_WeeklyBuy.setConfidence_level(confidence_level);
		UK_LSE_WeeklyBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_WeeklyBuy.setLastBuyPrice(buy_price);
		UK_LSE_WeeklyBuy.setLastEvent(last_opportunity);
		UK_LSE_WeeklyBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_WeeklyBuy.setLastEventPrice(0.0);
		UK_LSE_WeeklyBuy.setLasttradedprice(buy_price);

		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_weeklybuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_weeklybuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_weeklybuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_weeklysells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_weeklybuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_weeklysells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_WeeklyBuy.isLastEventBuy() == true) {
			uk_lse_weeklybuys saveresult = UK_LSE__WeeklyBuyRepository.insert(UK_LSE_WeeklyBuy);


		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;



	}

	@RequestMapping(value = "/findMarketSignals/MACD/Monthly/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDMonthlyBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();
		
		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;


		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_monthly/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}
				
				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}


			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_monthlybuys UK_LSE_MonthlyBuy = new uk_lse_monthlybuys();
		UK_LSE_MonthlyBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_MonthlyBuy.setYear(new java.util.Date().getYear());
		UK_LSE_MonthlyBuy.setCompany(company);
		UK_LSE_MonthlyBuy.setIndicator("LIVE");
		UK_LSE_MonthlyBuy.setConfidence_level(confidence_level);
		UK_LSE_MonthlyBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_MonthlyBuy.setLastBuyPrice(buy_price);
		UK_LSE_MonthlyBuy.setLastEvent(last_opportunity);
		UK_LSE_MonthlyBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_MonthlyBuy.setLastEventPrice(0.0);
		UK_LSE_MonthlyBuy.setLasttradedprice(buy_price);



		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_monthlybuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_monthlybuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_monthlybuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_monthlysells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_monthlybuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_monthlysells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_MonthlyBuy.isLastEventBuy() == true) {
			uk_lse_monthlybuys saveresult = UK_LSE__MonthlyBuyRepository.insert(UK_LSE_MonthlyBuy);

		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;



	}

	@RequestMapping(value = "/findMarketSignals/MACD/Daily/BUY/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDDailyBUYSignals_UK(@PathVariable String company) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(company);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		double buy_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;

		
		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_daily/macd/read/" + company;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
					buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}
				
				if (i == 0) {
					current_hist = Float.parseFloat(criterialoopObject.get("MACD_Hist").toString());
					current_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					current_time = key;

				}


			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_dailybuys UK_LSE_DailyBuy = new uk_lse_dailybuys();
		UK_LSE_DailyBuy.setMonth(new java.util.Date().getMonth());
		UK_LSE_DailyBuy.setYear(new java.util.Date().getYear());
		UK_LSE_DailyBuy.setCompany(company);
		UK_LSE_DailyBuy.setIndicator("LIVE");
		UK_LSE_DailyBuy.setConfidence_level(confidence_level);
		UK_LSE_DailyBuy.setLastBuyEvent(buy_opportunity);
		UK_LSE_DailyBuy.setLastBuyPrice(buy_price);
		UK_LSE_DailyBuy.setLastEvent(last_opportunity);
		UK_LSE_DailyBuy.setLastEventBuy(isLastEventBuy);
		UK_LSE_DailyBuy.setLastEventPrice(0.0);
		UK_LSE_DailyBuy.setLasttradedprice(buy_price);


		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_dailybuys> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastBuyEvent").is(buy_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_dailybuys.class);

			Query querytomarkexpired = new Query();
			querytomarkexpired.addCriteria(Criteria.where("company").is(company));
			querytomarkexpired.addCriteria(Criteria.where("indicator").is("LIVE"));
			Update updatetomarkexpired = new Update();
			updatetomarkexpired.set("indicator", "EXPIRED");
			updatetomarkexpired.set("expirytime", current_time);
			updatetomarkexpired.set("expiryprice", current_price);

			if (current_hist < 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_dailybuys.class);
			} else if (current_hist > 0) {
				mongoTemplate.updateMulti(querytomarkexpired, updatetomarkexpired, uk_lse_dailysells.class);

			}

			// FOR LTP UPDATE
			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("company").is(company));
			
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("lasttradedprice", current_price);

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_dailybuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_dailysells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_DailyBuy.isLastEventBuy() == true) {
			uk_lse_dailybuys saveresult = UK_LSE__DailyBuyRepository.insert(UK_LSE_DailyBuy);


		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;



	}

	@RequestMapping(value = "/runtestmethod/{cpny}", method = RequestMethod.GET)
	public boolean testmethod(@PathVariable String cpny) {

		boolean execution_result = false;
		int timeout = 3000;
		int size = 0;

		System.out.println(cpny);

		double signal_average = 0.0;

		// java.time.LocalDateTime buy_opportunity = null;
		// java.time.LocalDateTime sell_opportunity = null;
		// java.time.LocalDateTime last_opportunity = null;

		String buy_opportunity = null;
		String sell_opportunity = null;
		String last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int buy_counter = 0;
		int no_of_buys = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventBuy = false;
		List<String> event = new ArrayList<>();

		String feedURLFull = "https://markettechnicals-api.herokuapp.com/uk_lse_5mins/macd/read/" + cpny;

		HttpGet request = null;
		String url = feedURLFull;
		String content = null;
		JSONArray criteriaObject = null;

		try {

			HttpClient client = HttpClientBuilder.create().build();
			request = new HttpGet(url);

			request.addHeader("User-Agent", "Apache HTTPClient");
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			HttpEntity entity = response.getEntity();
			try {
				content = EntityUtils.toString(entity);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println(content);

			JSONParser parser = new JSONParser();
			org.json.simple.JSONObject jsonresult = null;
			try {
				jsonresult = (org.json.simple.JSONObject) parser.parse(content);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				criteriaObject = (JSONArray) jsonresult.get("macdjson");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(criteriaObject.get(0));
			// System.out.println(criteriaObject.get(1));

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

		try {
			// System.out.println("MONTHLY SIGNALS:");

			List macdData = criteriaObject;
			size = macdData.size();
			for (int i = size - 2, counter = 1; i >= 0; i--, counter++) {
				// for (int i = 0, counter = 1; i <= size - 1; i++, counter++) {

				// System.out.println(macdData.get(i));

				String loopobj = macdData.get(i).toString();
				String loopnextobj = macdData.get(i + 1).toString();

				JSONParser loopparser = new JSONParser();
				JSONParser loopnextparser = new JSONParser();

				org.json.simple.JSONObject jsonloopresult = (org.json.simple.JSONObject) loopparser
						.parse(macdData.get(i).toString());

				org.json.simple.JSONObject jsonloopnextresult = (org.json.simple.JSONObject) loopnextparser
						.parse(macdData.get(i + 1).toString());

				String key = null;

				String keynext = null;

				for (Iterator iterator = jsonloopresult.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
				}

				for (Iterator iteratornext = jsonloopnextresult.keySet().iterator(); iteratornext.hasNext();) {
					keynext = (String) iteratornext.next();
				}

				JSONObject criterialoopObject = (JSONObject) jsonloopresult.get(key);
				// System.out.println(key);
				// System.out.println(criterialoopObject.get("MACD_Signal"));
				// System.out.println(criterialoopObject.get("MACD_Hist"));
				// System.out.println(criterialoopObject.get("Price"));
				// System.out.println(criterialoopObject.get("MACD"));

				JSONObject criterialoopnextObject = (JSONObject) jsonloopnextresult.get(keynext);
				// System.out.println(keynext);
				// System.out.println(criterialoopnextObject.get("MACD_Signal"));
				// System.out.println(criterialoopnextObject.get("MACD_Hist"));
				// System.out.println(criterialoopnextObject.get("Price"));
				// System.out.println(criterialoopnextObject.get("MACD"));

				/*
				 * already commented System.out.println("date:           " +
				 * macdData.get(i).getDateTime()); System.out.println("MACD Histogram: " +
				 * macdData.get(i).getHist()); System.out.println("MACD Signal:    " +
				 * macdData.get(i).getSignal()); System.out.println("MACD:           " +
				 * macdData.get(i).getMacd());
				 * 
				 */

				signal_average = (signal_average * (counter - 1)
						+ Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()))) / counter;

				if (counter > 5 && counter - buy_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					buy_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;
				//	buy_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventBuy = true;
					last_opportunity = buy_opportunity;
					no_of_buys++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					buy_opportunity = key;

					sell_opportunity = buy_opportunity;
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventBuy = false;
					last_opportunity = sell_opportunity;
				}

			}

		} catch (Exception e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("BUY") && event.get(i + 1).equals("BUY")) {
				signal_lapse++;
			}
		}

		if (no_of_buys != 0) {
			confidence_level = (no_of_buys - signal_lapse) * 100 / no_of_buys;
		}

		System.out.println("Confidence level:" + confidence_level);

		// Monthlybuys monthlybuys = new Monthlybuys();
		// monthlybuys.setMonth(new java.util.Date().getMonth());
		// monthlybuys.setYear(new java.util.Date().getYear());
		// monthlybuys.setCompany(company);
		// monthlybuys.setIndicator("LIVE");
		// monthlybuys.setConfidence_level(confidence_level);
		// monthlybuys.setLastBuyEvent(buy_opportunity);
		// monthlybuys.setLastBuyPrice(0.0);
		// monthlybuys.setLastEvent(last_opportunity);
		// monthlybuys.setLastEventBuy(isLastEventBuy);
		// monthlybuys.setLastEventPrice(0.0);
		//
		// // TO DO price need to be populated
		// Monthlybuys saveresult = monthlybuysRepository.insert(monthlybuys);
		//
		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/changeMACDtoLIVE", method = RequestMethod.GET)
	public boolean changeIndicatorMACDtoLIVE() {

		boolean execution_result = false;

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytoupdateLTP = new Query();
			querytoupdateLTP.addCriteria(Criteria.where("indicator").is("MACD"));
			Update updatetoupdateLTP = new Update();
			updatetoupdateLTP.set("indicator", "LIVE");

			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_5minbuys.class);
			mongoTemplate.updateMulti(querytoupdateLTP, updatetoupdateLTP, uk_lse_5minsells.class);

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			mongoClient.close();
		}

		execution_result = true;
		return execution_result;

	}

}
