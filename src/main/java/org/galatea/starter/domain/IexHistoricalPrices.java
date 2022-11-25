package org.galatea.starter.domain;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.galatea.starter.domain.IexHistoricalPrice;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class IexHistoricalPrices {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String symbol;
  private String range;
  private String date;
  private String historicalPrices;

  public IexHistoricalPrices(String symbol, String range, String date, List<IexHistoricalPrice> historicalPrices) {
    this.symbol = symbol;
    this.range = range;
    this.date = date;
    this.historicalPrices = historicalPrices.toString();
  }
}
