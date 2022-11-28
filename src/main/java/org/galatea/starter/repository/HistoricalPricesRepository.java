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

// Interface extending CrudRepository
public interface HistoricalPricesRepository
    extends JpaRepository<IexHistoricalPrices, Long> {

//  List<IexHistoricalPrices> findByDate(String symbol, String date);
//
//  List<IexHistoricalPrices> findByRange(String symbol, String range);
//
//  List<IexHistoricalPrices> findBySymbol(String symbol);

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

//  @Query("select a from Article a where a.creationDateTime <= :creationDateTime")
//  List<Article> findAllWithCreationDateTimeBefore(
//      @Param("creationDateTime") Date creationDateTime);
}
