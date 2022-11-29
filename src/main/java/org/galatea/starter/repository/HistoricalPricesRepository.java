package org.galatea.starter.repository;

import feign.Param;
import java.time.LocalDate;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricalPricesRepository
    extends JpaRepository<IexHistoricalPrices, Long> {

  /**
   * Retrieve historical prices matching the symbol and date.
   * @param symbol the symbol to check
   * @param date the date to check
   * @return a list of prices
   */
  @Query("select u from IexHistoricalPrices u where u.symbol = :symbol AND u.date = :date")
  List<IexHistoricalPrices> findByDate(
      @Param("symbol") String symbol,
      @Param("date") LocalDate date
  );

  /**
   * Retrieve historical prices matching the symbol and range.
   * @param symbol the symbol to check
   * @param range the range to check
   * @return a list of prices
   */
  @Query("select u from IexHistoricalPrices u where u.symbol = :symbol AND u.range = :range")
  List<IexHistoricalPrices> findByRange(
      @Param("symbol") String symbol,
      @Param("range") String range
  );

  /**
   * Retrieve historical prices matching the symbol with a default range of 30d.
   * @param symbol the symbol to check
   * @param range the default value of 30d
   * @return a list of prices
   */
  @Query("select u from IexHistoricalPrices u where u.symbol = :symbol and u.range = :range")
  List<IexHistoricalPrices> findBySymbol(
      @Param("symbol") String symbol,
      @Param("range") String range
  );

}
