package org.galatea.starter.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPriceDTO;

public class DateHelpers {
  public static List<LocalDate> getDates(String range) {
    LocalDate end = LocalDate.now();
    List<LocalDate> totalDates = new ArrayList<>();
    LocalDate start = end.minusDays(30); // default

    // Reassign start value if a range was entered
    if (range.equalsIgnoreCase("max")) {
      start = end.minusYears(15);
    } else if (range.equalsIgnoreCase("5y")) {
      start = end.minusYears(5);
    } else if (range.equalsIgnoreCase("2y")) {
      start = end.minusYears(2);
    } else if (range.equalsIgnoreCase("1y")) {
      start = end.minusYears(1);
    } else if (range.equalsIgnoreCase("ytd")) {
      // to do
    } else if (range.equalsIgnoreCase("6m")) {
      start = end.minusMonths(6);
    } else if(range.equalsIgnoreCase("3m")) {
      start = end.minusMonths(3);
    } else if(range.equalsIgnoreCase("1m") || range.equalsIgnoreCase("1mm")) {
      start = end.minusMonths(1);
    } else if(range.equalsIgnoreCase("5d")) {
      start = end.minusDays(5);
    }

    while(!start.isAfter(end)) {
      totalDates.add(start);
      start = start.plusDays(1);
    }

    return totalDates;
  }

  /**
   * Find the most recent price in the list of historical prices given their timestamps
   * @param storedHistoricalPrices
   * @return the most recent historical price
   */
  public static IexHistoricalPriceDTO getMostRecentPrice(
      List<IexHistoricalPriceDTO> storedHistoricalPrices) {
    int mostRecentIndex = 0;
    int i;
    int len = storedHistoricalPrices.size();

    // Replace the most recent index if a later timestamp is found
    for (i = 0; i < len; i++) {
      if (storedHistoricalPrices.get(i).getTimestamp()
          .compareTo(storedHistoricalPrices.get(mostRecentIndex)
              .getTimestamp()) > 0) {
        mostRecentIndex = i;
      }
    }

    // Return the latest instance of historical price for this date
    return storedHistoricalPrices.get(mostRecentIndex);
  }
}
