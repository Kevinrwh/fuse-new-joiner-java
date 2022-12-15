package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IexHistoricalPrice {

  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private Integer volume;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  public IexHistoricalPrice(BigDecimal close, BigDecimal high, BigDecimal low, BigDecimal open,
      String symbol, Integer volume, LocalDate date) {

    this.close = close;
    this.high = high;
    this.low = low;
    this.open = open;
    this.symbol = symbol;
    this.volume = volume;
    this.date = date;

  }

  /**
   * Constructor for the returned data from the repository.
   * @param hp a stored historical price
   */
  public IexHistoricalPrice(final IexHistoricalPriceDTO hp) {
    this.close = hp.getClose();
    this.high = hp.getHigh();
    this.low = hp.getLow();
    this.open = hp.getOpen();
    this.symbol = hp.getSymbol();
    this.volume = hp.getVolume();
    this.date = hp.getDate();
  }

}