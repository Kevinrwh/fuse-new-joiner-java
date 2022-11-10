package org.galatea.starter.service;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.entrypoint.exception.EntityNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;


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

  /** Get historical prices for the symbol, range, and date combination
   * @param symbol a valid symbol
   * @param range an optional range to check
   * @param date an optional date to check; has priority over range
   */
  public List<IexHistoricalPrices> getHistoricalPricesForSymbols(final String symbol, final String range, final String date) throws Exception{

    // Throw an error if an invalid symbol was provided
    if(StringUtils.isBlank(symbol)) {
      throw new EntityNotFoundException(symbol.getClass(), symbol);
    }

    // Call the endpoint based on input. Date has priority when both date and range are provided.
    if(StringUtils.isNotBlank(range) && StringUtils.isNotBlank(date)) {
      return iexClient.getHistoricalPricesForSymbolDateAndRange(symbol, range, date);
    } else if(range == null && date == null) {
      return iexClient.getHistoricalPricesForSymbol(symbol);
    } else if(StringUtils.isNotBlank(date)) {
       return iexClient.getHistoricalPricesDate(symbol, date);
    } else if(date == null && range != null) {
      if(range.equals("")) {
        throw new HttpMessageNotReadableException("\"range\" is not allowed to be empty");
      }
      return iexClient.getHistoricalPricesRange(symbol, range);
    } else if(range == null && date != null) { // No range but date is not Empty
      if(date.equals("")) {
        throw new HttpMessageNotReadableException("\"date\" is not allowed to be empty");
      }
      return iexClient.getHistoricalPricesDate(symbol, date);
    }

    return iexClient.getHistoricalPricesForSymbol(symbol);

  }
}
