package org.galatea.starter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
//  private String symbol;
////  private String range; // default is 30
//
//  private String date;
//
//  private String range;
//  private String hp;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private Integer volume;
  private LocalDate date;
  private String range;

  public IexHistoricalPrices(IexHistoricalPrice hp) {

    this.close = hp.getClose();
    this.high = hp.getHigh();
    this.low = hp.getLow();
    this.open = hp.getOpen();
    this.symbol = hp.getSymbol();
    this.volume = hp.getVolume();
    this.date = hp.getDate();

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
