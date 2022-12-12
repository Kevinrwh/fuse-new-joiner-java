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

  /** Get historical prices for the symbol, range, and date combination.
   * If a range is provided, get a list of dates in that range and
   * query them independently to build a result of responses.
   * Use stored results when available. If there are more than one instance
   * of historical prices for a date, use the most recent one.
   * Store values for quicker retrieval.
   * @param symbol a symbol to reference
   * @param range a range to check prices for
   * @param date a date to check prices for; has priority to range if both are included
   */
  public List<IexHistoricalPrice> getHistoricalPrices(
      final String symbol,
      final String range,
      final String date) throws Exception {

    // Symbol validation
    if (StringUtils.isBlank(symbol)) {
      throw new EntityNotFoundException(symbol.getClass(), symbol);
    }

    return getResponseForQuery(symbol, range, date);

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
   * @param symbol, an IEX symbol to check historical prices for
   * @param range, a range to check prices within
   * @param date, a specific date to check historical prices
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getResponseForQuery(String symbol, String range, String date) {

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();

    // Return the response based on the query
    if (StringUtils.isNotBlank(range) && StringUtils.isNotBlank(date)) { // Date > Priority

      result.addAll(getResultsForDateAndRange(symbol, range, date));

    } else if (range == null && date == null) { // Return the default (range)

      result.addAll(getResultsForSymbol(symbol));

    } else if (range == null && date != null) { // Return by date

      result.addAll(getResultsForDate(symbol, date));

    } else if (date == null && range != null) { // Return by range

      result.addAll(getResultsForRange(symbol, range));

    }

    return result;
  }

  /**
   * Get the historical prices result for when the query is for a symbol and a date.
   * @param symbol, a symbol of reference
   * @param date, a specific date to check
   * @return, a list of historical prices
   */
  private List<IexHistoricalPrice> getResultsForDate(String symbol, String date) {

    // Verify the date is not whitespace
    if (date.equals("")) {
      throw new HttpMessageNotReadableException("\"date\" is not allowed to be empty");
    }

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();
    List<IexHistoricalPriceDTO> storedHistoricalPrices = new ArrayList<>();

    // Query the database or call the API and append the result
    storedHistoricalPrices.addAll(getStoredPricesForDate(symbol, date));

    if (storedHistoricalPrices.isEmpty()) {
      result.addAll(getClientResponseForDate(symbol, LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE)));
    } else {
      result.add(new IexHistoricalPrice(storedHistoricalPrices.get(0)));
    }

    return result;

  }

  /**
   * Get the historical prices for when the query includes a symbol, date, and range.
   * @param symbol, a symbol of reference
   * @param range, a range to check within
   * @param date, a specific date
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getResultsForDateAndRange(String symbol, String range, String date) {

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();
    List<IexHistoricalPriceDTO> storedHistoricalPrices = new ArrayList<IexHistoricalPriceDTO>();

    storedHistoricalPrices.addAll(getStoredPricesForDate(symbol, date));

    // If there is no response stored, call the API and save it
    if(storedHistoricalPrices.isEmpty()) {

      result.addAll(getClientResponseForRangeAndDate(symbol, range, date));

    } else { // Return the stored response

      result.add(new IexHistoricalPrice(storedHistoricalPrices.get(0)));

    }

    return result;
  }

  /**
   * Get the historical prices result for when the query is a symbol and range.
   * @param symbol, a symbol of reference
   * @param range, a range to check within
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getResultsForRange(String symbol, String range) {

    if (range.equals("")) {
      throw new HttpMessageNotReadableException("\"range\" is not allowed to be empty");
    }

    LocalDate start;
    LocalDate end = LocalDate.now();

    try {

      start = DateHelpers.getDates(range).get(0);

    } catch(Exception e) {

      throw new RuntimeException(e);

    }

    return getPricesForDatesInRange(symbol, start, end);

  }

  /**
   * Get the historical prices result for when the query is just the symbol. By default,
   * this returns a range of 30 days.
   * @param symbol, a symbol of reference
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getResultsForSymbol(String symbol) {

    // Initialize the start and end dates
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(30);

    return getPricesForDatesInRange(symbol, start, end);

  }

  /**
   * Get stored prices for a given date.
   * @param symbol, a symbol of reference
   * @param date, a specific date
   * @return a list of stored historical prices
   */
  private List<IexHistoricalPriceDTO> getStoredPricesForDate(String symbol, String date) {

    List<IexHistoricalPriceDTO> storedHistoricalPrices = fetchHistoricalPricesDate(symbol, date);

    if(storedHistoricalPrices.size() > 1) {
      return getMostRecentPriceInList(storedHistoricalPrices);
    }

    return storedHistoricalPrices;

  }

  /**
   * Get the historical price for each date within the range.
   * @param symbol, a symbol of reference
   * @param start, a start date at the beginning of the range
   * @param end, an end date at the end of the range
   * @return, a list of historical prices within the range
   */
  private List<IexHistoricalPrice> getPricesForDatesInRange(String symbol, LocalDate start, LocalDate end) {

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();

    while(!start.isAfter(end)) {
      List <IexHistoricalPriceDTO> pricesForDay = fetchHistoricalPricesDate(
          symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE)); // get for that day

      result.addAll(addDatePriceToRange(pricesForDay, symbol, start));

      start = start.plusDays(1);
    }

    return result;
  }

  /**
   * Get the most recent historical price in a list of stored historical prices
   * @param storedHistoricalPrices
   * @return a list of stored historical prices
   */
  private List<IexHistoricalPriceDTO> getMostRecentPriceInList(List<IexHistoricalPriceDTO> storedHistoricalPrices) {

    IexHistoricalPriceDTO mostRecentPrice = DateHelpers.getMostRecentPrice(storedHistoricalPrices);
    storedHistoricalPrices.clear();
    storedHistoricalPrices.add(mostRecentPrice);

    return storedHistoricalPrices;
  }

  /**
   * Add a date's historical price to a growing list regarding a range.
   * @param pricesForDay, a list historical prices stored for that date
   * @param symbol, a symbol of reference
   * @param date, a day of reference
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> addDatePriceToRange(List<IexHistoricalPriceDTO> pricesForDay, String symbol, LocalDate date) {

    List<IexHistoricalPrice> result = new ArrayList<>();

    if(pricesForDay.size() > 1) {
      pricesForDay = getMostRecentPriceInList(pricesForDay);
    }

    if(pricesForDay.isEmpty()) {
      result.addAll(getClientResponseForDate(symbol, date));
    } else {
      result.add(new IexHistoricalPrice(pricesForDay.get(0)));
    }

    return result;

  }

  /**
   * Save for retrieval and return a historical price.
   * @param historicalPrices, a list of historical prices
   * @return a historical price
   */
  private IexHistoricalPrice saveAndReturnHistoricalPrice(List<IexHistoricalPrice> historicalPrices) {

    IexHistoricalPrice historicalPrice = historicalPrices.get(historicalPrices.size()-1);
    saveIexHistoricalPrices(new IexHistoricalPriceDTO(historicalPrice));
    return historicalPrice;

  }

  /**
   * Call the client API for a historical prices result regarding a symbol and a date.
   * @param symbol, a symbol of reference
   * @param date, a date of reference
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getClientResponseForDate(String symbol, LocalDate date) {

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();

    List<IexHistoricalPrice> historicalPrices = iexClient
        .getHistoricalPricesDate(symbol, date.format(DateTimeFormatter.BASIC_ISO_DATE));

    if (!historicalPrices.isEmpty()) {
      result.add(saveAndReturnHistoricalPrice(historicalPrices));
    }

    return result;
  }

  /**
   * Call the client API for a historical prices result regarding a syhmbol, range, and date.
   * @param symbol, a symbol to check
   * @param range, a range of reference
   * @param date, a date of reference
   * @return a list of historical prices
   */
  private List<IexHistoricalPrice> getClientResponseForRangeAndDate(String symbol, String range, String date) {

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();

    List<IexHistoricalPrice> historicalPrices = iexClient
        .getHistoricalPricesForSymbolDateAndRange(symbol, range, date);

    if (!historicalPrices.isEmpty()) {
      result.add(saveAndReturnHistoricalPrice(historicalPrices));
    }

    return result;
  }

}