package org.galatea.starter.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice.Local;
import org.apache.commons.lang3.StringUtils;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.entrypoint.exception.EntityNotFoundException;
import org.galatea.starter.repository.HistoricalPricesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService implements IexHistoricalPriceService {

  @NonNull
  private IexClient iexClient;

  @Autowired
  private HistoricalPricesRepository historicalPricesRepository;

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
   * Store values for quicker retrieval.
   * @param symbol a valid symbol
   * @param range an optional range to check
   * @param date an optional date to check; has priority over range
   */
  public List<IexHistoricalPrice> getHistoricalPricesForSymbols(
      final String symbol,
      final String range,
      final String date) throws Exception {

    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();
//    List<IexHistoricalPrices> hps = new ArrayList<IexHistoricalPrices>();
    List<IexHistoricalPrices> storedHistoricalPrices = new ArrayList<IexHistoricalPrices>();

    // No symbol provided
    if (StringUtils.isBlank(symbol)) {
      throw new EntityNotFoundException(symbol.getClass(), symbol);
    }

    // Retrieve for date if both date and range are provided
    if (StringUtils.isNotBlank(range) && StringUtils.isNotBlank(date)) {

      // Fetch stored responses
      storedHistoricalPrices.addAll(fetchHistoricalPricesDate(symbol, date));

      // If there aren't any, call the API
      if(storedHistoricalPrices.isEmpty()) {

        List<IexHistoricalPrice> historicalPrices = iexClient.getHistoricalPricesForSymbolDateAndRange(symbol, range, date);

        // Save the response based on whether there were any historical prices
        if (!historicalPrices.isEmpty()) {
          result.add(historicalPrices.get(0));
          saveIexHistoricalPrices(new IexHistoricalPrices(result.get(0)));
        }

      } else { // Return stored responses
        result.add(new IexHistoricalPrice(storedHistoricalPrices.get(0)));
      }

    } else if (range == null && date == null) { // Default is query for a range of 30 days

      // Determine the start and end dates for our range
      LocalDate end = LocalDate.now();
      LocalDate start = end.minusDays(30);

      // For each day, try to use a stored historical price or call the API for a new one.
      while (!start.isAfter(end)) {

        List <IexHistoricalPrices> pricesForDay = fetchHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE)); // get for that day

        if (pricesForDay.isEmpty()) {

          List<IexHistoricalPrice> historicalPrices =
              iexClient.getHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE));

          // Save the API response appropriately
          if(!historicalPrices.isEmpty()) {
            result.add(historicalPrices.get(0));
            saveIexHistoricalPrices(new IexHistoricalPrices(historicalPrices.get(0)));
          }

        } else { // use a stored value for this day
          result.add(new IexHistoricalPrice(pricesForDay.get(0)));
        }

        // Go to the next date
        start = start.plusDays(1);

      }

    } else if (range == null && date != null) { // Query for specified date

      // Return an error if the date was not entered
      if (date.equals("")) {
        throw new HttpMessageNotReadableException("\"date\" is not allowed to be empty");
      }

      // Fetch the date's stored response, if any.
      storedHistoricalPrices.addAll(fetchHistoricalPricesDate(symbol, date));

      // If there isn't, make an API call
      if (storedHistoricalPrices.isEmpty()) {

        List<IexHistoricalPrice> historicalPrices =
            iexClient.getHistoricalPricesDate(symbol, date);

        // Save the response appropriately
        if (!historicalPrices.isEmpty()) {
          result.add((historicalPrices.get(0)));
          saveIexHistoricalPrices(new IexHistoricalPrices(historicalPrices.get(0)));
        }
      } else { // Use stored value for that day
        result.add(new IexHistoricalPrice(storedHistoricalPrices.get(0)));
      }

    } else if (date == null && range != null) { // Query for specified range

      if (range.equals("")) {
        throw new HttpMessageNotReadableException("\"range\" is not allowed to be empty");
      }


      // Use a list of dates in the range to query the data
      List<LocalDate> dates = getDates(range);
      LocalDate start = dates.get(0);
      LocalDate end = LocalDate.now();

      // For each day, use a stored value or call the API
      while (!start.isAfter(end)) {

        // Query for a stored response for this day
        List <IexHistoricalPrices> pricesForDay = fetchHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE));

        // Not stored response. Call the API.
        if (pricesForDay.isEmpty()) {

          List<IexHistoricalPrice> historicalPrices =
              iexClient.getHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE));

          // Store the response appropriately
          if(!historicalPrices.isEmpty()) {
            result.add(historicalPrices.get(0));
            saveIexHistoricalPrices(new IexHistoricalPrices(historicalPrices.get(0)));
          }

        } else { // Use a stored response
          result.add(new IexHistoricalPrice(pricesForDay.get(0)));
        }

        start = start.plusDays(1);

      }
    }

    // Return the response or an empty list
    return result;

  }

  @Override
  public IexHistoricalPrices saveIexHistoricalPrices(
      final IexHistoricalPrices iexHistoricalPrices) {
    return historicalPricesRepository.save(iexHistoricalPrices);
  }

  @Override
  public List<IexHistoricalPrices> fetchIexHistoricalPricesList() {
    return (List<IexHistoricalPrices>)
        historicalPricesRepository.findAll();
  }

  @Override
  public List<IexHistoricalPrices> fetchHistoricalPricesDate(
      final String symbol,
      final String date) {

    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);

    return historicalPricesRepository.findByDate(symbol, localDate);
  }

  /**
   * Get the list of dates within the range given the scenario.
   * @param range
   * @return a list of dates within the range
   */
  public List<LocalDate> getDates(String range) {
    LocalDate end = LocalDate.now();
    List<LocalDate> totalDates = new ArrayList<>();
    LocalDate start = end.minusDays(30); // default

    // Reassign start value if a range was entered
    if (range.equalsIgnoreCase("max")) {
      start = end.minusYears(15);
    } else if (range.equalsIgnoreCase("5y")) {
      start = end.minusYears(5);
    } else if (range.equalsIgnoreCase("2y")) {
      start = end.minusYears(2);
    } else if (range.equalsIgnoreCase("1y")) {
      start = end.minusYears(1);
    } else if (range.equalsIgnoreCase("ytd")) {
      // to do
    } else if (range.equalsIgnoreCase("6m")) {
      start = end.minusMonths(6);
    } else if(range.equalsIgnoreCase("3m")) {
      start = end.minusMonths(3);
    } else if(range.equalsIgnoreCase("1m") || range.equalsIgnoreCase("1mm")) {
      start = end.minusMonths(1);
    } else if(range.equalsIgnoreCase("5d")) {
      start = end.minusDays(5);
    }

    while(!start.isAfter(end)) {
      totalDates.add(start);
      start = start.plusDays(1);
    }

    return totalDates;
  }
}