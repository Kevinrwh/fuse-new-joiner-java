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

  /**
   * Constructor for the returned data from the repository.
   * @param hp a stored historical price
   */
  public IexHistoricalPrice(final IexHistoricalPrices hp) {
    this.close = hp.getClose();
    this.high = hp.getHigh();
    this.low = hp.getLow();
    this.open = hp.getOpen();
    this.symbol = hp.getSymbol();
    this.volume = hp.getVolume();
    this.date = hp.getDate();
  }

}