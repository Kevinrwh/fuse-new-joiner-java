package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class IexHistoricalPriceDTO {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private Integer volume;
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  /**
   * Constructor for our data transfer object
   * @param historicalPrice a historical price to store
   */
  public IexHistoricalPriceDTO(final IexHistoricalPrice historicalPrice) {

    this.close = historicalPrice.getClose();
    this.high = historicalPrice.getHigh();
    this.low = historicalPrice.getLow();
    this.open = historicalPrice.getOpen();
    this.symbol = historicalPrice.getSymbol();
    this.volume = historicalPrice.getVolume();
    this.date = historicalPrice.getDate();

  }

}
