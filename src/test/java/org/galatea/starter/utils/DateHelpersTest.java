package org.galatea.starter.utils;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DateHelpersTest {
  @Test(expected = NullPointerException.class)
  public void testEmptyRange() throws Exception {
      List<LocalDate> resultDates = DateHelpers.getDates("");
  }

  @Test
  public void testRangeInDays() {

    List<LocalDate> result = DateHelpers.getDates("5d");
    LocalDate fiveDaysAgo = result.get(0);

    Assert.assertEquals(fiveDaysAgo, LocalDate.now().minusDays(5));

  }

  @Test
  public void testRangeInMonths() {

    List<LocalDate> result = DateHelpers.getDates("5M");
    LocalDate fiveMonthsAgo = result.get(0);

    Assert.assertEquals(fiveMonthsAgo, LocalDate.now().minusMonths(5));

  }

  @Test
  public void testRangeInYears() {

    List<LocalDate> result = DateHelpers.getDates("5Y");
    LocalDate fiveYearsAgo = result.get(0);

    Assert.assertEquals(fiveYearsAgo, LocalDate.now().minusYears(5));

  }

  @Test
  public void testRangeProvidedValues() {

    List<LocalDate> result = DateHelpers.getDates("max");
    LocalDate maxRangeDate = result.get(0);

    Assert.assertEquals(maxRangeDate, LocalDate.now().minusYears(15));

    result = DateHelpers.getDates("ytd");
    LocalDate ytdDate = result.get(0);

    Assert.assertEquals(ytdDate, LocalDate.of(2022, 1, 1));


  }
}
