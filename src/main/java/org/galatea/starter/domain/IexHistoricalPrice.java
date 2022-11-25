package org.galatea.starter.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.NoArgsConstructor;

//@Entity
//@NoArgsConstructor
//@AllArgsConstructor
@Data
@Builder

// Class
public class IexHistoricalPrice {

//  @Id
//  @GeneratedValue(strategy = GenerationType.AUTO)
//  private Long id;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private Integer volume;
  private String date;

}