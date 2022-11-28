package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class IexHistoricalPrices {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String range;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private Integer volume;
  @JsonFormat(pattern="yyyy-MM-dd")
  private LocalDate date;

  public IexHistoricalPrices(IexHistoricalPrice hp) {

    this.close = hp.getClose();
    this.high = hp.getHigh();
    this.low = hp.getLow();
    this.open = hp.getOpen();
    this.symbol = hp.getSymbol();
    this.volume = hp.getVolume();
    this.date = hp.getDate();
    this.range = "1D";
  }

  public IexHistoricalPrices(IexHistoricalPrice hp, String range) {
    this.close = hp.getClose();
    this.high = hp.getHigh();
    this.low = hp.getLow();
    this.open = hp.getOpen();
    this.symbol = hp.getSymbol();
    this.volume = hp.getVolume();
    this.date = hp.getDate();
    this.range = range;
  }
}
