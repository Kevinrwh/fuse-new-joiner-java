package org.galatea.starter.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.galatea.starter.domain.IexHistoricalPriceDTO;

public class DateHelpers {

  /**
   * Gets the list of dates within the range.
   * Calls the helper method getListOfDates
   *
   * @param range, a range of reference
   * @return a list of dates
   * @throws NullPointerException
   */
  public static List<LocalDate> getDates(String range) throws NullPointerException {

    if (range == null || range.equalsIgnoreCase("")) {
      throw new NullPointerException("\"range\" is not allowed to be empty");
    }

    LocalDate start = getFirstDateInRange(range);

    return getListOfDates(start, LocalDate.now());

  }

  /**
   * Get the first date in a range.
   *
   * @param range, a range of reference
   * @return a date
   */
  private static LocalDate getFirstDateInRange(String range) {

    if (range.equalsIgnoreCase("max")) {
      return LocalDate.now().minusYears(15);
    } else if (range.equalsIgnoreCase("ytd")) {
      return  LocalDate.of(LocalDate.now().getYear(), 1, 1);
    }

    return getDateInCustomRange(range.toLowerCase());

  }

  /**
   * Parse the range to get the first date in a custom range.
   *
   * @param range, a range of reference
   * @return a date
   */
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

  /**
   * Helper method to get a list of dates in the range.
   *
   * @param start, a starting date
   * @param end, an end date
   * @return
   */
  private static List<LocalDate> getListOfDates(LocalDate start, LocalDate end) {

    List<LocalDate> totalDates = new ArrayList<>();

    while(!start.isAfter(end)) {
      totalDates.add(start);
      start = start.plusDays(1);
    }

    return totalDates;

  }

  /**
   * Return the most recent historical price for a date.
   *
   * @param storedHistoricalPrices, a list of stored historical prices
   * @return the most recent historical price
   */
  public static IexHistoricalPriceDTO getMostRecentPrice(
      List<IexHistoricalPriceDTO> storedHistoricalPrices) {

    int mostRecentIndex = 0;

    for (int i = 0; i < storedHistoricalPrices.size(); i++) {

      if (updateRecentPrice(storedHistoricalPrices, i, mostRecentIndex)) {
        mostRecentIndex = i;
      }

    }

    return storedHistoricalPrices.get(mostRecentIndex);
  }

  /**
   * Update the recent price index if a more recent price is found.
   *
   * @param storedHistoricalPrices, a list of stored historical prices
   * @param currentIndex, an index of the current price
   * @param mostRecentIndex, an index of the running most recent price
   * @return true if the currentIndex holds a more recent price
   */
  private static boolean updateRecentPrice(List<IexHistoricalPriceDTO> storedHistoricalPrices, int currentIndex, int mostRecentIndex) {

    if (storedHistoricalPrices.get(currentIndex).getTimestamp()
        .compareTo(storedHistoricalPrices.get(mostRecentIndex)
            .getTimestamp()) > 0) {

      return true;

    }

    return false;

  }

}
