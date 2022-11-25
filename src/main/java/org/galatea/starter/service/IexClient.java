package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Feign Declarative REST Client to access endpoints from the Free and Open IEX API to get market
 * data. See https://iextrading.com/developer/docs/
 */
@FeignClient(name = "IEX", url = "${spring.rest.iexBasePath}")
public interface IexClient {

  /**
   * Get a list of all stocks supported by IEX. See https://iextrading.com/developer/docs/#symbols.
   * As of July 2019 this returns almost 9,000 symbols, so maybe don't call it in a loop.
   *
   * @return a list of all of the stock symbols supported by IEX.
   */
  @GetMapping("/ref-data/symbols?token=${spring.rest.iexToken}")
  List<IexSymbol> getAllSymbols();

  /**
   * Get the last traded price for each stock symbol passed in. See https://iextrading.com/developer/docs/#last.
   *
   * @param symbols stock symbols to get last traded price for.
   * @return a list of the last traded price for each of the symbols passed in.
   */
  @GetMapping("/tops/last?token=${spring.rest.iexToken}")
  List<IexLastTradedPrice> getLastTradedPriceForSymbols(@RequestParam("symbols") String[] symbols);

  /**
   * Get the historical price for each stock passed in. See
   * https://iexcloud.io/docs/api/#historical-prices
   *
   * @param symbol stock symbols to get historical prices for.
   * @param range an optional range to check
   * @param date an optional date
   * @return a list of the historical prices for each of the symbols passed in.
   */
  @GetMapping("/stock/{symbol}/chart/{range}/{date}?chartByDay=True&token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesForSymbolDateAndRange(@PathVariable(name="symbol", required = false) String symbol,
      @PathVariable(name= "range", required = false) String range,
      @PathVariable(name = "date", required = false) String date);

  @GetMapping("/stock/chart?token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPrices();

  @GetMapping("/stock/{symbol}/chart?token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesForSymbol(@PathVariable(name="symbol") String symbol);

  @GetMapping("/stock/{symbol}/chart/{range}?token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesRange(@PathVariable(name="symbol") String symbol,
      @PathVariable(name="range") String range);

  @GetMapping("/stock/{symbol}/chart/date/{date}?chartByDay=True&token=${spring.rest.iexToken}")
  List<IexHistoricalPrice> getHistoricalPricesDate(@PathVariable(name="symbol") String symbol,
      @PathVariable(name="date") String date);

}
