package org.galatea.starter.utils;

import java.time.LocalDate;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DateHelpersTest {
  @Test(expected = NullPointerException.class)
  public void testEmptyRange() throws Exception {
      List<LocalDate> resultDates = DateHelpers.getDates("");
  }
}
