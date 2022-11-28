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

  @Query("select u from IexHistoricalPrices u where u.symbol = :symbol AND u.date = :date")
  List<IexHistoricalPrices> findByDate(
      @Param("symbol") String symbol,
      @Param("date") LocalDate date
  );

  @Query("select u from IexHistoricalPrices u where u.symbol = :symbol AND u.range = :range")
  List<IexHistoricalPrices> findByRange(
      @Param("symbol") String symbol,
      @Param("range") String range
  );

  @Query("select u from IexHistoricalPrices u where u.symbol = :symbol and u.range = :range")
  List<IexHistoricalPrices> findBySymbol(
      @Param("symbol") String symbol,
      @Param("range") String range
  );

}
