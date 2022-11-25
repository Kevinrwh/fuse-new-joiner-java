package org.galatea.starter.service;

import org.galatea.starter.domain.IexHistoricalPrices;
import java.util.List;

// Interface
public interface IexService {

  // Save operation
  IexHistoricalPrices saveIexHistoricalPrices(IexHistoricalPrices iexHistoricalPrices);

  // Read operation
  List<IexHistoricalPrices> fetchIexHistoricalPricesList();

  // Update operation
//  IexHistoricalPrices updateIexHistoricalPrices(IexHistoricalPrices iexHistoricalPrices,
//      Long iexHistoricalPricesId);

  // Delete operation
//  void deleteIexHistoricalPricesById(Long iexHistoricalPricesId);
}
