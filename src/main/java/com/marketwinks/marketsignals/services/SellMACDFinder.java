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
import com.marketwinks.marketsignals.model.us_15minsells;
import com.marketwinks.marketsignals.model.us_30minsells;
import com.marketwinks.marketsignals.model.us_5minsells;
import com.marketwinks.marketsignals.model.us_dailysells;
import com.marketwinks.marketsignals.model.us_hourlysells;
import com.marketwinks.marketsignals.model.us_monthlysells;
import com.marketwinks.marketsignals.model.us_weeklysells;
import com.marketwinks.marketsignals.repository.UK_LSE_15MinSellRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_30MinSellRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_5MinSellRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_DailySellRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_HourlySellRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_MonthlySellRepository;
import com.marketwinks.marketsignals.repository.UK_LSE_WeeklySellRepository;
import com.marketwinks.marketsignals.repository.US_15MinSellRepository;
import com.marketwinks.marketsignals.repository.US_30MinSellRepository;
import com.marketwinks.marketsignals.repository.US_5MinSellRepository;
import com.marketwinks.marketsignals.repository.US_DailySellRepository;
import com.marketwinks.marketsignals.repository.US_HourlySellRepository;
import com.marketwinks.marketsignals.repository.US_MonthlySellRepository;
import com.marketwinks.marketsignals.repository.US_WeeklySellRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@RestController
@RequestMapping("/baseURL")
public class SellMACDFinder {

	@Autowired
	private US_MonthlySellRepository monthlysellsRepository;

	@Autowired
	private US_WeeklySellRepository weeklysellsRepository;

	@Autowired
	private US_DailySellRepository dailysellsRepository;

	@Autowired
	private US_HourlySellRepository hourlySellRepository;

	@Autowired
	private US_5MinSellRepository __5MinSellRepository;

	@Autowired
	private US_15MinSellRepository __15MinSellRepository;

	@Autowired
	private US_30MinSellRepository __30MinSellRepository;

	@Autowired
	private UK_LSE_5MinSellRepository UK_LSE__5MinSellRepository;

	@Autowired
	private UK_LSE_15MinSellRepository UK_LSE__15MinSellRepository;

	@Autowired
	private UK_LSE_30MinSellRepository UK_LSE__30MinSellRepository;

	@Autowired
	private UK_LSE_HourlySellRepository UK_LSE__HourlySellRepository;

	@Autowired
	private UK_LSE_DailySellRepository UK_LSE__DailySellRepository;

	@Autowired
	private UK_LSE_WeeklySellRepository UK_LSE__WeeklySellRepository;

	@Autowired
	private UK_LSE_MonthlySellRepository UK_LSE__MonthlySellRepository;

	// uk 5 mins, 15 mins, 30 mins, hourly, daily, weekly, monthly remaining

	@RequestMapping(value = "/findMarketSignals/MACD/Monthly/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDMonthlySELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 5 && counter - sell_counter >= 2 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1.5) {
					// TODO: experimental: signal average*1.5 maintained

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_monthlysells monthlysells = new us_monthlysells();
		monthlysells.setMonth(new java.util.Date().getMonth());
		monthlysells.setYear(new java.util.Date().getYear());
		monthlysells.setCompany(company);
		monthlysells.setIndicator("LIVE");
		monthlysells.setConfidence_level(confidence_level);
		monthlysells.setLastSellEvent(sell_opportunity);
		monthlysells.setLastSellPrice(0.0);
		monthlysells.setLastEvent(last_opportunity);
		monthlysells.setLastEventSell(isLastEventSell);
		monthlysells.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_monthlysells saveresult = monthlysellsRepository.insert(monthlysells);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Weekly/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDWeeklySELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 8 && counter - sell_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1.5) {
					// TODO: experimental: signal average*1.5 maintained

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_weeklysells weeklysells = new us_weeklysells();
		weeklysells.setMonth(new java.util.Date().getMonth());
		weeklysells.setYear(new java.util.Date().getYear());
		weeklysells.setCompany(company);
		weeklysells.setIndicator("LIVE");
		weeklysells.setConfidence_level(confidence_level);
		weeklysells.setLastSellEvent(sell_opportunity);
		weeklysells.setLastSellPrice(0.0);
		weeklysells.setLastEvent(last_opportunity);
		weeklysells.setLastEventSell(isLastEventSell);
		weeklysells.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_weeklysells saveresult = weeklysellsRepository.insert(weeklysells);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Daily/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDDailySELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 10 && counter - sell_counter >= 15 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1.5) {
					// TODO: experimental: signal average*1.5 maintained

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_dailysells dailysells = new us_dailysells();
		dailysells.setMonth(new java.util.Date().getMonth());
		dailysells.setYear(new java.util.Date().getYear());
		dailysells.setCompany(company);
		dailysells.setIndicator("LIVE");
		dailysells.setConfidence_level(confidence_level);
		dailysells.setLastSellEvent(sell_opportunity);
		dailysells.setLastSellPrice(0.0);
		dailysells.setLastEvent(last_opportunity);
		dailysells.setLastEventSell(isLastEventSell);
		dailysells.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_dailysells saveresult = dailysellsRepository.insert(dailysells);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Hourly/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACDHourlySELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 5 && counter - sell_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0 && Math.abs(macdData.get(i).getSignal()) > signal_average) {
					// TODO: experimental: signal average*1.5 not maintained for hourly

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_hourlysells hourlySell = new us_hourlysells();
		hourlySell.setMonth(new java.util.Date().getMonth());
		hourlySell.setYear(new java.util.Date().getYear());
		hourlySell.setCompany(company);
		hourlySell.setIndicator("LIVE");
		hourlySell.setConfidence_level(confidence_level);
		hourlySell.setLastSellEvent(sell_opportunity);
		hourlySell.setLastSellPrice(0.0);
		hourlySell.setLastEvent(last_opportunity);
		hourlySell.setLastEventSell(isLastEventSell);
		hourlySell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_hourlysells saveresult = hourlySellRepository.insert(hourlySell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_30Min/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACD30MinSELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 5 && counter - sell_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1) {
					// TODO: experimental: signal average*1.5 maintained not here

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_30minsells __30MinSell = new us_30minsells();
		__30MinSell.setMonth(new java.util.Date().getMonth());
		__30MinSell.setYear(new java.util.Date().getYear());
		__30MinSell.setCompany(company);
		__30MinSell.setIndicator("LIVE");
		__30MinSell.setConfidence_level(confidence_level);
		__30MinSell.setLastSellEvent(sell_opportunity);
		__30MinSell.setLastSellPrice(0.0);
		__30MinSell.setLastEvent(last_opportunity);
		__30MinSell.setLastEventSell(isLastEventSell);
		__30MinSell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_30minsells saveresult = __30MinSellRepository.insert(__30MinSell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_15Min/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACD15MinSELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 5 && counter - sell_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1) {
					// TODO: experimental: signal average*1.5 maintained not here

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_15minsells __15MinSell = new us_15minsells();
		__15MinSell.setMonth(new java.util.Date().getMonth());
		__15MinSell.setYear(new java.util.Date().getYear());
		__15MinSell.setCompany(company);
		__15MinSell.setIndicator("LIVE");
		__15MinSell.setConfidence_level(confidence_level);
		__15MinSell.setLastSellEvent(sell_opportunity);
		__15MinSell.setLastSellPrice(0.0);
		__15MinSell.setLastEvent(last_opportunity);
		__15MinSell.setLastEventSell(isLastEventSell);
		__15MinSell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_15minsells saveresult = __15MinSellRepository.insert(__15MinSell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_5Min/SELL/USEq/{company}", method = RequestMethod.GET)
	public boolean findMACD5MinSELLSignals(@PathVariable String company) {

		boolean execution_result = false;

		String apiKey = "50M3AP1K3Y";
		int timeout = 3000;
		int size = 0;

		double signal_average = 0.0;

		java.time.LocalDateTime buy_opportunity = null;
		java.time.LocalDateTime sell_opportunity = null;
		java.time.LocalDateTime last_opportunity = null;

		// TODO experimenatal: to reduce repeated buy signals
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
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
				if (counter > 5 && counter - sell_counter >= 5 && i < size - 1 && macdData.get(i + 1).getHist() > 0
						&& macdData.get(i).getHist() < 0 && macdData.get(i).getSignal() > 0
						&& macdData.get(i).getMacd() > 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average * 1) {
					// TODO: experimental: signal average*1.5 maintained not here

					sell_counter = counter;

					sell_opportunity = macdData.get(i).getDateTime();
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && macdData.get(i + 1).getHist() < 0 && macdData.get(i).getHist() > 0
						&& macdData.get(i).getSignal() < 0 && macdData.get(i).getMacd() < 0
						&& Math.abs(macdData.get(i).getSignal()) > signal_average) {

					buy_opportunity = macdData.get(i).getDateTime();
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
				}

			}

		} catch (AlphaVantageException e) {
			System.out.println("something went wrong");
			return execution_result;
		}

		for (int i = 0; i < event.size() - 1; i++) {
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		us_5minsells __5MinSell = new us_5minsells();
		__5MinSell.setMonth(new java.util.Date().getMonth());
		__5MinSell.setYear(new java.util.Date().getYear());
		__5MinSell.setCompany(company);
		__5MinSell.setIndicator("LIVE");
		__5MinSell.setConfidence_level(confidence_level);
		__5MinSell.setLastSellEvent(sell_opportunity);
		__5MinSell.setLastSellPrice(0.0);
		__5MinSell.setLastEvent(last_opportunity);
		__5MinSell.setLastEventSell(isLastEventSell);
		__5MinSell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		us_5minsells saveresult = __5MinSellRepository.insert(__5MinSell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_5Min/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACD5MinsSELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();

		double sell_price = 0.00;
		double current_hist = 0.00;
		double current_price = 0.00;
		String current_time = null;

		
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

				if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}

				if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
								Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
					

					
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_5minsells UK_LSE_5MinSell = new uk_lse_5minsells();
		UK_LSE_5MinSell.setMonth(new java.util.Date().getMonth());
		UK_LSE_5MinSell.setYear(new java.util.Date().getYear());
		UK_LSE_5MinSell.setCompany(company);
		UK_LSE_5MinSell.setIndicator("LIVE");
		UK_LSE_5MinSell.setConfidence_level(confidence_level);
		UK_LSE_5MinSell.setLastSellEvent(sell_opportunity);
		UK_LSE_5MinSell.setLastSellPrice(sell_price);
		UK_LSE_5MinSell.setLastEvent(last_opportunity);
		UK_LSE_5MinSell.setLastEventSell(isLastEventSell);
		UK_LSE_5MinSell.setLastEventPrice(0.0);
		UK_LSE_5MinSell.setLasttradedprice(sell_price);



		

		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_5minsells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_5minsells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_5MinSell.isLastEventSell() == true) {
			uk_lse_5minsells saveresult = UK_LSE__5MinSellRepository.insert(UK_LSE_5MinSell);

		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;

		

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_15Min/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACD15MinsSELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();
		
		double sell_price = 0.00;
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


					if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
							&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
							&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
									* 1.5) {
					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}


					if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
									Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
						

					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_15minsells UK_LSE_15MinSell = new uk_lse_15minsells();
		UK_LSE_15MinSell.setMonth(new java.util.Date().getMonth());
		UK_LSE_15MinSell.setYear(new java.util.Date().getYear());
		UK_LSE_15MinSell.setCompany(company);
		UK_LSE_15MinSell.setIndicator("LIVE");
		UK_LSE_15MinSell.setConfidence_level(confidence_level);
		UK_LSE_15MinSell.setLastSellEvent(sell_opportunity);
		UK_LSE_15MinSell.setLastSellPrice(sell_price);
		UK_LSE_15MinSell.setLastEvent(last_opportunity);
		UK_LSE_15MinSell.setLastEventSell(isLastEventSell);
		UK_LSE_15MinSell.setLastEventPrice(0.0);
		UK_LSE_15MinSell.setLasttradedprice(sell_price);

		
		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_15minsells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_15minsells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_15MinSell.isLastEventSell() == true) {
			uk_lse_15minsells saveresult = UK_LSE__15MinSellRepository.insert(UK_LSE_15MinSell);

		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;




	}

	@RequestMapping(value = "/findMarketSignals/MACD/_30Min/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACD30MinsSELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();
		
		double sell_price = 0.00;
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


				if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {

					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}


					if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
									Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
						
						
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_30minsells UK_LSE_30MinSell = new uk_lse_30minsells();
		UK_LSE_30MinSell.setMonth(new java.util.Date().getMonth());
		UK_LSE_30MinSell.setYear(new java.util.Date().getYear());
		UK_LSE_30MinSell.setCompany(company);
		UK_LSE_30MinSell.setIndicator("LIVE");
		UK_LSE_30MinSell.setConfidence_level(confidence_level);
		UK_LSE_30MinSell.setLastSellEvent(sell_opportunity);
		UK_LSE_30MinSell.setLastSellPrice(sell_price);
		UK_LSE_30MinSell.setLastEvent(last_opportunity);
		UK_LSE_30MinSell.setLastEventSell(isLastEventSell);
		UK_LSE_30MinSell.setLastEventPrice(0.0);
		UK_LSE_30MinSell.setLasttradedprice(sell_price);

		
		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_30minsells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_30minsells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_30MinSell.isLastEventSell() == true) {
			uk_lse_30minsells saveresult = UK_LSE__30MinSellRepository.insert(UK_LSE_30MinSell);

		}
		// TO DO price need to be populated


		execution_result = true;
		return execution_result;


	}

	@RequestMapping(value = "/findMarketSignals/MACD/Hourly/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDHourlySELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();
		
		double sell_price = 0.00;
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


				if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {
					
					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}


					if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
									Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
						
						
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_hourlysells UK_LSE_HourlySell = new uk_lse_hourlysells();
		UK_LSE_HourlySell.setMonth(new java.util.Date().getMonth());
		UK_LSE_HourlySell.setYear(new java.util.Date().getYear());
		UK_LSE_HourlySell.setCompany(company);
		UK_LSE_HourlySell.setIndicator("LIVE");
		UK_LSE_HourlySell.setConfidence_level(confidence_level);
		UK_LSE_HourlySell.setLastSellEvent(sell_opportunity);
		UK_LSE_HourlySell.setLastSellPrice(sell_price);
		UK_LSE_HourlySell.setLastEvent(last_opportunity);
		UK_LSE_HourlySell.setLastEventSell(isLastEventSell);
		UK_LSE_HourlySell.setLastEventPrice(0.0);
		UK_LSE_HourlySell.setLasttradedprice(sell_price);

		
		// this block is to avoid duplicate signals to get inserted
		List<uk_lse_hourlysells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_hourlysells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_HourlySell.isLastEventSell() == true) {

			uk_lse_hourlysells saveresult = UK_LSE__HourlySellRepository.insert(UK_LSE_HourlySell);

		}
		// TO DO price need to be populated

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Daily/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDDailySELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();
		
		double sell_price = 0.00;
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


					if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
							&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
							&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
									* 1.5) {
					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}


					if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
									Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
						
						
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_dailysells UK_LSE_DailySell = new uk_lse_dailysells();
		UK_LSE_DailySell.setMonth(new java.util.Date().getMonth());
		UK_LSE_DailySell.setYear(new java.util.Date().getYear());
		UK_LSE_DailySell.setCompany(company);
		UK_LSE_DailySell.setIndicator("LIVE");
		UK_LSE_DailySell.setConfidence_level(confidence_level);
		UK_LSE_DailySell.setLastSellEvent(sell_opportunity);
		UK_LSE_DailySell.setLastSellPrice(sell_price);
		UK_LSE_DailySell.setLastEvent(last_opportunity);
		UK_LSE_DailySell.setLastEventSell(isLastEventSell);
		UK_LSE_DailySell.setLastEventPrice(0.0);
		UK_LSE_DailySell.setLasttradedprice(sell_price);

		List<uk_lse_dailysells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_dailysells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_DailySell.isLastEventSell() == true) {

			uk_lse_dailysells saveresult = UK_LSE__DailySellRepository.insert(UK_LSE_DailySell);

		}
		// TO DO price need to be populated

		execution_result = true;
		return execution_result;


	}

	@RequestMapping(value = "/findMarketSignals/MACD/Weekly/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDWeeklySELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();
		
		double sell_price = 0.00;
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


				if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
						&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
						&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
						&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
						&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
								* 1.5) {
					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}


					if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
									Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
						
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_weeklysells UK_LSE_WeeklySell = new uk_lse_weeklysells();
		UK_LSE_WeeklySell.setMonth(new java.util.Date().getMonth());
		UK_LSE_WeeklySell.setYear(new java.util.Date().getYear());
		UK_LSE_WeeklySell.setCompany(company);
		UK_LSE_WeeklySell.setIndicator("LIVE");
		UK_LSE_WeeklySell.setConfidence_level(confidence_level);
		UK_LSE_WeeklySell.setLastSellEvent(sell_opportunity);
		UK_LSE_WeeklySell.setLastSellPrice(sell_price);
		UK_LSE_WeeklySell.setLastEvent(last_opportunity);
		UK_LSE_WeeklySell.setLastEventSell(isLastEventSell);
		UK_LSE_WeeklySell.setLastEventPrice(0.0);
		UK_LSE_WeeklySell.setLasttradedprice(sell_price);


		
		List<uk_lse_weeklysells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_weeklysells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_WeeklySell.isLastEventSell() == true) {

			uk_lse_weeklysells saveresult = UK_LSE__WeeklySellRepository.insert(UK_LSE_WeeklySell);

		}
		// TO DO price need to be populated

				
		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Monthly/SELL/UKEq/{company}", method = RequestMethod.GET)
	public boolean findMACDMonthlySELLSignals_UK(@PathVariable String company) {

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
		int sell_counter = 0;
		int no_of_sells = 0;
		int signal_lapse = 0;
		double confidence_level = 0.0;
		boolean isLastEventSell = false;
		List<String> event = new ArrayList<>();
		
		double sell_price = 0.00;
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


					if (counter > 5 && counter - sell_counter >= 2 && i < size - 1
							&& Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) > 0
							&& Math.abs(Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average
									* 1.5) {
					sell_counter = counter;

					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;
					sell_price = Float.parseFloat(criterialoopObject.get("Price").toString());
					System.out.println("SELL OPPORTUNITY happened on:" + sell_opportunity);
					event.add("SELL");
					isLastEventSell = true;
					last_opportunity = sell_opportunity;
					no_of_sells++;
				}


					if (i < size - 1 && Float.parseFloat(criterialoopnextObject.get("MACD_Hist").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Hist").toString()) > 0
							&& Float.parseFloat(criterialoopObject.get("MACD_Signal").toString()) < 0
							&& Float.parseFloat(criterialoopObject.get("MACD").toString()) < 0 && Math.abs(
									Float.parseFloat(criterialoopObject.get("MACD_Signal").toString())) > signal_average) {
						
						
					//
					// String str = key;
					//
					// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
					// HH:mm");
					// LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

					sell_opportunity = key;

					buy_opportunity = sell_opportunity;
					System.out.println("BUY OPPORTUNITY happened on:" + buy_opportunity);
					event.add("BUY");
					isLastEventSell = false;
					last_opportunity = buy_opportunity;
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
			if (event.get(i).equals("SELL") && event.get(i + 1).equals("SELL")) {
				signal_lapse++;
			}
		}

		if (no_of_sells != 0) {
			confidence_level = (no_of_sells - signal_lapse) * 100 / no_of_sells;
		}

		System.out.println("Confidence level:" + confidence_level);

		uk_lse_monthlysells UK_LSE_MonthlySell = new uk_lse_monthlysells();
		UK_LSE_MonthlySell.setMonth(new java.util.Date().getMonth());
		UK_LSE_MonthlySell.setYear(new java.util.Date().getYear());
		UK_LSE_MonthlySell.setCompany(company);
		UK_LSE_MonthlySell.setIndicator("LIVE");
		UK_LSE_MonthlySell.setConfidence_level(confidence_level);
		UK_LSE_MonthlySell.setLastSellEvent(sell_opportunity);
		UK_LSE_MonthlySell.setLastSellPrice(sell_price);
		UK_LSE_MonthlySell.setLastEvent(last_opportunity);
		UK_LSE_MonthlySell.setLastEventSell(isLastEventSell);
		UK_LSE_MonthlySell.setLastEventPrice(0.0);
		UK_LSE_MonthlySell.setLasttradedprice(sell_price);
		
		List<uk_lse_monthlysells> queryresult_querytostopduplicate = new ArrayList<>();

		MongoClient mongoClient = MongoClients.create(
				"mongodb+srv://marketwinks:L9sS6oOAk1sHL0yi@aws-eu-west1-cluster-tszuq.mongodb.net/marketwinksdbprod?retryWrites=true");
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "marketwinksdbprod");

		try {

			Query querytostopduplicate = new Query();
			querytostopduplicate.addCriteria(Criteria.where("company").is(company));
			
			querytostopduplicate.addCriteria(Criteria.where("lastSellEvent").is(sell_opportunity));
			queryresult_querytostopduplicate = mongoTemplate.find(querytostopduplicate, uk_lse_monthlysells.class);

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

		if (queryresult_querytostopduplicate.size() == 0 && UK_LSE_MonthlySell.isLastEventSell() == true) {

			uk_lse_monthlysells saveresult = UK_LSE__MonthlySellRepository.insert(UK_LSE_MonthlySell);

		}
		// TO DO price need to be populated

				
		execution_result = true;
		return execution_result;

	}

}