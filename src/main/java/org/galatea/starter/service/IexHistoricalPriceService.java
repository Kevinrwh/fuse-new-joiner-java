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
  public List<IexHistoricalPrices> fetchHistoricalPricesDate(String symbol, String date);

  /**
   * fetch a historical price given a range.
   * @param symbol a symbol to check
   * @param range a range to check
   * @return the historical price
   */
  public List<IexHistoricalPrices> fetchHistoricalPricesRange(String symbol, String range);

  /**
   * fetch a historical price given a range.
   * @param symbol a symbol to check
   * @return the historical price
   */
  public List<IexHistoricalPrices> fetchHistoricalPricesSymbol(String symbol);
}
