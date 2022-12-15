package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPriceDTO;

// Interface
public interface IexHistoricalPriceService {

  /**
   * Save a historical price.
   * @param iexHistoricalPrices a historical price to save
   * @return the historical price
   */
  IexHistoricalPriceDTO saveIexHistoricalPrices(IexHistoricalPriceDTO iexHistoricalPrices);

  /**
   * Save a historical price.
   * @return the list of historical prices
   */
  List<IexHistoricalPriceDTO> fetchIexHistoricalPricesList();

  /**
   * fetch a historical price given a date.
   * @param symbol a symbol to check
   * @param date a date to check
   * @return the historical price
   */
  List<IexHistoricalPriceDTO> fetchHistoricalPricesDate(String symbol, String date);
}
