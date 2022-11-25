package org.galatea.starter.repository;

import org.galatea.starter.domain.IexHistoricalPrice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository

// Interface extending CrudRepository
public interface HistoricalPriceRepository
    extends CrudRepository<IexHistoricalPrice, Long> {
}
