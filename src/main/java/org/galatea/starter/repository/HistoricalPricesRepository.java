package org.galatea.starter.repository;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPrices;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

// Interface extending CrudRepository
public interface HistoricalPricesRepository
    extends CrudRepository<IexHistoricalPrices, Long> {
}
