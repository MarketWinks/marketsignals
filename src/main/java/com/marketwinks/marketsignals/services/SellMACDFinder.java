package com.marketwinks.marketsignals.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.patriques.AlphaVantageConnector;
import org.patriques.TechnicalIndicators;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.MACD;
import org.patriques.output.technicalindicators.data.MACDData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.marketwinks.marketsignals.model.US_15MinSell;
import com.marketwinks.marketsignals.model.US_30MinSell;
import com.marketwinks.marketsignals.model.US_5MinSell;
import com.marketwinks.marketsignals.model.US_DailySell;
import com.marketwinks.marketsignals.model.US_HourlySell;
import com.marketwinks.marketsignals.model.US_MonthlySell;
import com.marketwinks.marketsignals.model.US_WeeklySell;
import com.marketwinks.marketsignals.repository.US_15MinSellRepository;
import com.marketwinks.marketsignals.repository.US_30MinSellRepository;
import com.marketwinks.marketsignals.repository.US_5MinSellRepository;
import com.marketwinks.marketsignals.repository.US_DailySellRepository;
import com.marketwinks.marketsignals.repository.US_HourlySellRepository;
import com.marketwinks.marketsignals.repository.US_MonthlySellRepository;
import com.marketwinks.marketsignals.repository.US_WeeklySellRepository;

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

	@RequestMapping(value = "/findMarketSignals/MACD/Monthly/SELL/{company}", method = RequestMethod.GET)
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

		US_MonthlySell monthlysells = new US_MonthlySell();
		monthlysells.setMonth(new java.util.Date().getMonth());
		monthlysells.setYear(new java.util.Date().getYear());
		monthlysells.setCompany(company);
		monthlysells.setIndicator("MACD");
		monthlysells.setConfidence_level(confidence_level);
		monthlysells.setLastSellEvent(sell_opportunity);
		monthlysells.setLastSellPrice(0.0);
		monthlysells.setLastEvent(last_opportunity);
		monthlysells.setLastEventSell(isLastEventSell);
		monthlysells.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_MonthlySell saveresult = monthlysellsRepository.insert(monthlysells);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Weekly/SELL/{company}", method = RequestMethod.GET)
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

		US_WeeklySell weeklysells = new US_WeeklySell();
		weeklysells.setMonth(new java.util.Date().getMonth());
		weeklysells.setYear(new java.util.Date().getYear());
		weeklysells.setCompany(company);
		weeklysells.setIndicator("MACD");
		weeklysells.setConfidence_level(confidence_level);
		weeklysells.setLastSellEvent(sell_opportunity);
		weeklysells.setLastSellPrice(0.0);
		weeklysells.setLastEvent(last_opportunity);
		weeklysells.setLastEventSell(isLastEventSell);
		weeklysells.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_WeeklySell saveresult = weeklysellsRepository.insert(weeklysells);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Daily/SELL/{company}", method = RequestMethod.GET)
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

		US_DailySell dailysells = new US_DailySell();
		dailysells.setMonth(new java.util.Date().getMonth());
		dailysells.setYear(new java.util.Date().getYear());
		dailysells.setCompany(company);
		dailysells.setIndicator("MACD");
		dailysells.setConfidence_level(confidence_level);
		dailysells.setLastSellEvent(sell_opportunity);
		dailysells.setLastSellPrice(0.0);
		dailysells.setLastEvent(last_opportunity);
		dailysells.setLastEventSell(isLastEventSell);
		dailysells.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_DailySell saveresult = dailysellsRepository.insert(dailysells);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/Hourly/SELL/{company}", method = RequestMethod.GET)
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

		US_HourlySell hourlySell = new US_HourlySell();
		hourlySell.setMonth(new java.util.Date().getMonth());
		hourlySell.setYear(new java.util.Date().getYear());
		hourlySell.setCompany(company);
		hourlySell.setIndicator("MACD");
		hourlySell.setConfidence_level(confidence_level);
		hourlySell.setLastSellEvent(sell_opportunity);
		hourlySell.setLastSellPrice(0.0);
		hourlySell.setLastEvent(last_opportunity);
		hourlySell.setLastEventSell(isLastEventSell);
		hourlySell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_HourlySell saveresult = hourlySellRepository.insert(hourlySell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_30Min/SELL/{company}", method = RequestMethod.GET)
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

		US_30MinSell __30MinSell = new US_30MinSell();
		__30MinSell.setMonth(new java.util.Date().getMonth());
		__30MinSell.setYear(new java.util.Date().getYear());
		__30MinSell.setCompany(company);
		__30MinSell.setIndicator("MACD");
		__30MinSell.setConfidence_level(confidence_level);
		__30MinSell.setLastSellEvent(sell_opportunity);
		__30MinSell.setLastSellPrice(0.0);
		__30MinSell.setLastEvent(last_opportunity);
		__30MinSell.setLastEventSell(isLastEventSell);
		__30MinSell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_30MinSell saveresult = __30MinSellRepository.insert(__30MinSell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_15Min/SELL/{company}", method = RequestMethod.GET)
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

		US_15MinSell __15MinSell = new US_15MinSell();
		__15MinSell.setMonth(new java.util.Date().getMonth());
		__15MinSell.setYear(new java.util.Date().getYear());
		__15MinSell.setCompany(company);
		__15MinSell.setIndicator("MACD");
		__15MinSell.setConfidence_level(confidence_level);
		__15MinSell.setLastSellEvent(sell_opportunity);
		__15MinSell.setLastSellPrice(0.0);
		__15MinSell.setLastEvent(last_opportunity);
		__15MinSell.setLastEventSell(isLastEventSell);
		__15MinSell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_15MinSell saveresult = __15MinSellRepository.insert(__15MinSell);

		execution_result = true;
		return execution_result;

	}

	@RequestMapping(value = "/findMarketSignals/MACD/_5Min/SELL/{company}", method = RequestMethod.GET)
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

		US_5MinSell __5MinSell = new US_5MinSell();
		__5MinSell.setMonth(new java.util.Date().getMonth());
		__5MinSell.setYear(new java.util.Date().getYear());
		__5MinSell.setCompany(company);
		__5MinSell.setIndicator("MACD");
		__5MinSell.setConfidence_level(confidence_level);
		__5MinSell.setLastSellEvent(sell_opportunity);
		__5MinSell.setLastSellPrice(0.0);
		__5MinSell.setLastEvent(last_opportunity);
		__5MinSell.setLastEventSell(isLastEventSell);
		__5MinSell.setLastEventPrice(0.0);

		// TO DO price need to be populated
		US_5MinSell saveresult = __5MinSellRepository.insert(__5MinSell);

		execution_result = true;
		return execution_result;

	}

}