package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrices;

// Interface
public interface IexHistoricalPriceService {

  /**
   * Save a historical price.
   * @param iexHistoricalPrices a historical price to save
   * @return the historical price
   */
  IexHistoricalPrices saveIexHistoricalPrices(IexHistoricalPrices iexHistoricalPrices);

  /**
   * Save a historical price.
   * @return the list of historical prices
   */
  List<IexHistoricalPrices> fetchIexHistoricalPricesList();

  /**
   * fetch a historical price given a date.
   * @param symbol a symbol to check
   * @param date a date to check
   * @return the historical price
   */
  List<IexHistoricalPrices> fetchHistoricalPricesDate(String symbol, String date);
}
