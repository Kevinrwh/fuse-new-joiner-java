package org.galatea.starter.entrypoint;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPriceDTO;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.service.IexHistoricalPriceService;
import org.galatea.starter.service.IexService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
@Validated
@RestController
@RequiredArgsConstructor
public class IexRestController {

  private final IexHistoricalPriceService iexHistoricalPriceService;

  @NonNull
  private IexService iexService;

  /**
   * Exposes an endpoint to get all symbols available on IEX.
   *
   * @return a list of all IexStockSymbols.
   */
  @GetMapping(value = "${mvc.iex.getAllSymbolsPath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public List<IexSymbol> getAllStockSymbols() {
    return iexService.getAllSymbols();
  }

  /**
   * Get the last traded price for each of the symbols passed in.
   *
   * @param symbols list of symbols to get last traded price for.
   * @return a List of IexLastTradedPrice objects for the given symbols.
   */
  @GetMapping(value = "${mvc.iex.getLastTradedPricePath}", produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public List<IexLastTradedPrice> getLastTradedPrice(
      @RequestParam(value = "symbols") final List<String> symbols) {

    return iexService.getLastTradedPriceForSymbols(symbols);
  }

  /** Get historical prices for the symbol.
  * @param symbol a symbol to find historical prices for
   * @param range an optional range to check
   * @param date an optional date to check
   */
  @GetMapping(value = "${mvc.iex.getHistoricalPricesPath}", produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public List<IexHistoricalPrice> getHistoricalPrices(
      @RequestParam(value = "symbol", required = false) final String symbol,
      @RequestParam(value = "range", required = false) final String range,
      @RequestParam(value = "date", required = false) final String date) throws Exception {

    return iexService.getHistoricalPrices(symbol, range, date);

  }

  /**
   * Return a list of historical prices.
   * @return a stored historical price
   */
  @GetMapping(value = "/iexHistoricalPrices")
  public List<IexHistoricalPriceDTO> fetchIexHistoricalPricesList() {
    return iexHistoricalPriceService.fetchIexHistoricalPricesList();
  }

}
