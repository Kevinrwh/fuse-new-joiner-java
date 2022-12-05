package org.galatea.starter.repository;

import feign.Param;
import java.time.LocalDate;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPriceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricalPricesRepository
    extends JpaRepository<IexHistoricalPriceDTO, Long> {

  /**
   * Retrieve historical prices matching the symbol and date.
   * @param symbol the symbol to check
   * @param date the date to check
   * @return a list of prices
   */
  @Query("select u from IexHistoricalPriceDTO u where u.symbol = :symbol AND u.date = :date")
  List<IexHistoricalPriceDTO> findByDate(
      @Param("symbol") String symbol,
      @Param("date") LocalDate date
  );

}
