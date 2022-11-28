package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrices;

// Interface
public interface IexHistoricalPriceService {

  // Save operation
  IexHistoricalPrices saveIexHistoricalPrices(IexHistoricalPrices iexHistoricalPrices);

  // Read operation
  List<IexHistoricalPrices> fetchIexHistoricalPricesList();

  public List<IexHistoricalPrices> fetchHistoricalPricesDate(String symbol, String date);

  public List<IexHistoricalPrices> fetchHistoricalPricesRange(String symbol, String range);

  public List<IexHistoricalPrices> fetchHistoricalPricesSymbol(String symbol);
}
