package org.galatea.starter.service;

import org.galatea.starter.domain.IexHistoricalPrice;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrices;

// Interface
public interface IexHistoricalPriceService {

  // Save operation
  IexHistoricalPrices saveIexHistoricalPrice(IexHistoricalPrices iexHistoricalPrices);

  // Read operation
  List<IexHistoricalPrices> fetchIexHistoricalPriceList();

}
