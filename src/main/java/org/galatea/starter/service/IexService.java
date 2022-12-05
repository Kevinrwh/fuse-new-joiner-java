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
  public List<IexHistoricalPrice> getHistoricalPricesForSymbols(
      final String symbol,
      final String range,
      final String date) throws Exception {

    // Initialize our result and stored result array
    List<IexHistoricalPrice> result = new ArrayList<IexHistoricalPrice>();
    List<IexHistoricalPriceDTO> storedHistoricalPrices = new ArrayList<IexHistoricalPriceDTO>();

    // No symbol provided
    if (StringUtils.isBlank(symbol)) {
      throw new EntityNotFoundException(symbol.getClass(), symbol);
    }

    // Both range & date are included
    if (StringUtils.isNotBlank(range) && StringUtils.isNotBlank(date)) {

      storedHistoricalPrices.addAll(fetchHistoricalPricesDate(symbol, date));

      // If there is no response stored, call the API and save it
      if(storedHistoricalPrices.isEmpty()) {

        List<IexHistoricalPrice> historicalPrices = iexClient.getHistoricalPricesForSymbolDateAndRange(symbol, range, date);

        if (!historicalPrices.isEmpty()) {
          result.add(historicalPrices.get(historicalPrices.size()-1));
          saveIexHistoricalPrices(new IexHistoricalPriceDTO(historicalPrices.get(historicalPrices.size()-1)));
        }

      } else { // Return the stored response
        result.add(new IexHistoricalPrice(storedHistoricalPrices.get(storedHistoricalPrices.size()-1)));
      }

    } else if (range == null && date == null) { // Return the default of range at 30 days

      // Initialize the start and end dates
      LocalDate end = LocalDate.now();
      LocalDate start = end.minusDays(30);

      // For each day, query the database or call the API and append to the result
      while (!start.isAfter(end)) {

        List <IexHistoricalPriceDTO> pricesForDay = fetchHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE)); // get for that day

        if (pricesForDay.isEmpty()) {

          List<IexHistoricalPrice> historicalPrices =
              iexClient.getHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE));

          if(!historicalPrices.isEmpty()) {
            result.add(historicalPrices.get(historicalPrices.size()-1));
            saveIexHistoricalPrices(new IexHistoricalPriceDTO(historicalPrices.get(historicalPrices.size()-1)));
          }

        } else {
          result.add(new IexHistoricalPrice(pricesForDay.get(pricesForDay.size()-1)));
        }

        // Increment to the next day
        start = start.plusDays(1);

      }

    } else if (range == null && date != null) { // A date was provided

      // Verify the date is not whitespace
      if (date.equals("")) {
        throw new HttpMessageNotReadableException("\"date\" is not allowed to be empty");
      }

      // Query the database or call the API and append the result
      storedHistoricalPrices.addAll(fetchHistoricalPricesDate(symbol, date));

      if (storedHistoricalPrices.isEmpty()) {

        List<IexHistoricalPrice> historicalPrices =
            iexClient.getHistoricalPricesDate(symbol, date);

        if (!historicalPrices.isEmpty()) {
          result.add((historicalPrices.get(historicalPrices.size()-1)));
          saveIexHistoricalPrices(new IexHistoricalPriceDTO(historicalPrices.get(historicalPrices.size()-1)));
        }
      } else {
        result.add(new IexHistoricalPrice(storedHistoricalPrices.get(storedHistoricalPrices.size()-1)));
      }

    } else if (date == null && range != null) { // A range was provided

      if (range.equals("")) {
        throw new HttpMessageNotReadableException("\"range\" is not allowed to be empty");
      }

      // Helper method to get a list of dates
      List<LocalDate> dates = getDates(range);

      LocalDate start = dates.get(0);
      LocalDate end = LocalDate.now();

      while (!start.isAfter(end)) {

        List <IexHistoricalPriceDTO> pricesForDay = fetchHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE));

        if (pricesForDay.isEmpty()) {

          List<IexHistoricalPrice> historicalPrices =
              iexClient.getHistoricalPricesDate(symbol, start.format(DateTimeFormatter.BASIC_ISO_DATE));

          if(!historicalPrices.isEmpty()) {
            result.add(historicalPrices.get(historicalPrices.size()-1));
            saveIexHistoricalPrices(new IexHistoricalPriceDTO(historicalPrices.get(historicalPrices.size()-1)));
          }

        } else {
          result.add(new IexHistoricalPrice(pricesForDay.get(pricesForDay.size()-1)));
        }

        start = start.plusDays(1);

      }
    }

    // Return the historical prices
    return result;

  }

  @Override
  public IexHistoricalPriceDTO saveIexHistoricalPrices(
      final IexHistoricalPriceDTO iexHistoricalPrices) {
    return historicalPricesRepository.save(iexHistoricalPrices);
  }

  @Override
  public List<IexHistoricalPriceDTO> fetchIexHistoricalPricesList() {
    return (List<IexHistoricalPriceDTO>)
        historicalPricesRepository.findAll();
  }

  @Override
  public List<IexHistoricalPriceDTO> fetchHistoricalPricesDate(
      final String symbol,
      final String date) {

    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);

    return historicalPricesRepository.findByDate(symbol, localDate);
  }

  /**
   * Get the list of dates within the range given the scenario.
   * @param range, a range that includes the dates we want
   * @return a list of dates
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