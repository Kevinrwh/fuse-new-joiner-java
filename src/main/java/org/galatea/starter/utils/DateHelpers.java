package org.galatea.starter.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPriceDTO;

public class DateHelpers {

  /**
   *
   * @param range
   * @return
   * @throws NullPointerException
   */
  public static List<LocalDate> getDates(String range) throws NullPointerException {

    if (range == null) {
      throw new NullPointerException("\"range\" is not allowed to be empty");
    }

    LocalDate start = getFirstDateInRange(range);

    return getListOfDates(start, LocalDate.now());

  }

  private static LocalDate getFirstDateInRange(String range) {

    if (range.equalsIgnoreCase("max")) {
      return LocalDate.now().minusYears(15);
    } else if (range.equalsIgnoreCase("ytd")) {
      return  LocalDate.of(LocalDate.now().getYear(), 1, 1);
    }

    return getDateInCustomRange(range.toLowerCase());

  }

  private static LocalDate getDateInCustomRange(String range) {

    int digitPrefix = Integer.parseInt(range.substring(0, range.length()-1));
    LocalDate result = LocalDate.now();

    if (range.endsWith("y")) {
      result = result.minusYears(digitPrefix);
    } else if (range.endsWith("m")) {
      result = result.minusMonths(digitPrefix);
    } else if (range.endsWith("d")) {
      result = result.minusDays(digitPrefix);
    }

    return result;

  }

  private static List<LocalDate> getListOfDates(LocalDate start, LocalDate end) {

    List<LocalDate> totalDates = new ArrayList<>();

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
