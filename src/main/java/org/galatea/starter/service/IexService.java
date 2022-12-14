package org.galatea.starter.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPriceDTO;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.entrypoint.exception.EntityNotFoundException;
import org.galatea.starter.repository.HistoricalPricesRepository;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.galatea.starter.utils.DateHelpers;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService implements IexHistoricalPriceService {

  @NonNull
  private IexClient iexClient;

  private final HistoricalPricesRepository historicalPricesRepository;

  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols() {
    return iexClient.getAllSymbols();
  }

  /**
   * Get the last traded price for each Symbol that is passed in.
   *
   * @param symbols the list of symbols to get a last traded price for.
   * @return a list of last traded price objects for each Symbol that is passed in.
   */
  public List<IexLastTradedPrice> getLastTradedPriceForSymbols(final List<String> symbols) {
    if (CollectionUtils.isEmpty(symbols)) {
      return Collections.emptyList();
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]));
    }
  }

  /** Get historical prices for the symbol.
   *
   * Store values for quicker retrieval.
   * @param symbol a symbol of reference
   * @param range a range of dates
   * @param date a specific date
   */
  public List<IexHistoricalPrice> getHistoricalPrices(
      final String symbol,
      final String range,
      final String date) {

    // Validate the symbol
    symbolChecker(symbol);

    return getResponseBasedOnQuery(symbol, range, date);

  }

  @Override
  public IexHistoricalPriceDTO saveIexHistoricalPrices(
      final IexHistoricalPriceDTO iexHistoricalPrices) {
    return historicalPricesRepository.save(iexHistoricalPrices);
  }

  @Override
  public List<IexHistoricalPriceDTO> fetchIexHistoricalPricesList() {
    return historicalPricesRepository.findAll();
  }

  @Override
  public List<IexHistoricalPriceDTO> fetchHistoricalPricesDate(
      final String symbol,
      final String date) {

    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);

    return historicalPricesRepository.findByDate(symbol, localDate);

  }

  /**
   * Get a historical prices result based on query parameters.
   *
   * @param symbol, an IEX symbol to check historical prices for
   * @param range, a range to check prices within
   * @param date, a specific date to check historical prices
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getResponseBasedOnQuery(String symbol, String range, String date) {

    if (dateWasEntered(range, date)) {

      return getResultsByADate(symbol, date);

    }

    return getResultsByARange(symbol, range);

  }

  /**
   * Determines if we should get a response by a date.
   *
   * @param range, an optional range
   * @param date, a specified date
   * @return true if a date was provided
   */
  private boolean dateWasEntered(String range, String date) {

    return (StringUtils.isNotBlank(range) && StringUtils.isNotBlank(date))
        || (range == null && date != null);

  }

  /**
   * Get the historical prices when a date is provided.
   *
   * @param symbol, a symbol of reference
   * @param date, a specific date to check
   * @return, a list of historical prices
   */
  private List<IexHistoricalPrice> getResultsByADate(String symbol, String date) {

    // Validate the date before looking up a result
    dateChecker(date);

    // Get the result
    return getStoredOrNewPrices(symbol, date);

  }

  /**
   * Use stored historical prices or get new ones from the client.
   *
   * @param symbol, a symbol to look up
   * @param date, a date of reference
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getStoredOrNewPrices(String symbol, String date) {

    if(getStoredPrices(symbol, date).isEmpty()) {
      return getClientResponseForADate(symbol, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE));
    }

    List<IexHistoricalPrice> result = new ArrayList<>();
    result.add(new IexHistoricalPrice(getStoredPrices(symbol, date).get(0)));

    return result;

  }

  /**
   * Get historical prices when a range is provided.
   *
   * @param symbol, a symbol of reference
   * @param range, a range to check within
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getResultsByARange(String symbol, String range) {

    range = rangeChecker(range);

    LocalDate start = DateHelpers.getDates(range).get(0);
    LocalDate end = LocalDate.now();

    return getPricesForDates(symbol, start, end);

  }

  /**
   * Get the historical price for each date within the range.
   *
   * @param symbol, a symbol of reference
   * @param start, a start date at the beginning of the range
   * @param end, an end date at the end of the range
   * @return, a list of historical prices within the range
   */
  private List<IexHistoricalPrice> getPricesForDates(String symbol, LocalDate start, LocalDate end) {

    List<IexHistoricalPrice> result = new ArrayList<>();

    while(!start.isAfter(end)) {

      result.addAll(getStoredOrNewPrices(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE)));

      start = start.plusDays(1);
    }

    return result;
  }

  /**
   * Get stored prices for a given date.
   *
   * @param symbol, a symbol of reference
   * @param date, a specific date
   * @return a list of stored historical prices
   */
  private List<IexHistoricalPriceDTO> getStoredPrices(String symbol, String date) {

    List<IexHistoricalPriceDTO> storedHistoricalPrices = fetchHistoricalPricesDate(symbol, date);

    if (storedHistoricalPrices.size() > 1) {

      return getMostRecentPriceInAList(storedHistoricalPrices);

    }

    return storedHistoricalPrices;

  }

  /**
   * Get the most recent historical price in a list of stored historical prices.
   *
   * @param storedHistoricalPrices, a list of stored historical prices
   * @return a list including the most recently stored price
   */
  private List<IexHistoricalPriceDTO> getMostRecentPriceInAList(List<IexHistoricalPriceDTO> storedHistoricalPrices) {

    IexHistoricalPriceDTO mostRecentPrice = DateHelpers.getMostRecentPrice(storedHistoricalPrices);

    storedHistoricalPrices.clear();
    storedHistoricalPrices.add(mostRecentPrice);

    return storedHistoricalPrices;
  }

  /**
   * Store a price for a later retrieval.
   * Calls the saveIexHistoricalPrice helper method
   *
   * @param historicalPrices, a list of historical prices
   * @return a saved historical price
   */
  private IexHistoricalPrice storeAHistoricalPrice(List<IexHistoricalPrice> historicalPrices) {

    IexHistoricalPrice historicalPrice = historicalPrices.get(historicalPrices.size()-1);
    saveIexHistoricalPrices(new IexHistoricalPriceDTO(historicalPrice));

    return historicalPrice;

  }

  /**
   * Get a new response for a specific date.
   *
   * @param symbol, a symbol of reference
   * @param date, a date of reference
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getClientResponseForADate(String symbol, LocalDate date) {

    List<IexHistoricalPrice> result = new ArrayList<>();

    List<IexHistoricalPrice> historicalPrices = iexClient
        .getHistoricalPricesDate(symbol, date.format(DateTimeFormatter.BASIC_ISO_DATE));

    if (!historicalPrices.isEmpty()) {

      result.add(storeAHistoricalPrice(historicalPrices));

    }

    return result;
  }

  /**
   * Validates the symbol passed in.
   *
   * @param symbol, a symbol to validate
   */
  private void symbolChecker(String symbol) throws EntityNotFoundException {

    // Symbol validation
    if (StringUtils.isBlank(symbol)) {

      throw new EntityNotFoundException(symbol.getClass(), symbol);

    }

  }

  /**
   * Validates the date passed in.
   * Throws an exception if the date is whitespace.
   *
   * @param date
   * @throws HttpMessageNotReadableException
   */
  private void dateChecker(String date) throws HttpMessageNotReadableException {

    // Verify the date is not whitespace
    if (date.equals("")) {

      throw new HttpMessageNotReadableException("\"date\" is not allowed to be empty");

    }

  }

  /**
   * Validates a range passed in.
   * Throws an exception if the range is whitespace.
   *
   * @param range to check
   * @return the range
   */
  private String rangeChecker(String range) {

    if (range == null) {

      range = "30d";

    } else if (range.equals("")) {

      throw new HttpMessageNotReadableException("\"range\" is not allowed to be empty");

    }

    return range;

  }

}