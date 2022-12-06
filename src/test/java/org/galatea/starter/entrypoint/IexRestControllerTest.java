package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexHistoricalPriceDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.galatea.starter.utils.DateHelpers;


@RequiredArgsConstructor
@Slf4j
// We need to do a full application start up for this one, since we want the feign clients to be instantiated.
// It's possible we could do a narrower slice of beans, but it wouldn't save that much test run time.
@SpringBootTest
// this gives us the MockMvc variable
@AutoConfigureMockMvc
// we previously used WireMockClassRule for consistency with ASpringTest, but when moving to a dynamic port
// to prevent test failures in concurrent builds, the wiremock server was created too late and feign was
// already expecting it to be running somewhere else, resulting in a connection refused
@AutoConfigureWireMock(port = 0, files = "classpath:/wiremock")
// Use this runner since we want to parameterize certain tests.
// See runner's javadoc for more usage.
@RunWith(JUnitParamsRunner.class)
public class IexRestControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testGetSymbolsEndpoint() throws Exception {
    MvcResult result = this.mvc.perform(
        // note that we were are testing the fuse REST end point here, not the IEX end point.
        // the fuse end point in turn calls the IEX end point, which is WireMocked for this test.
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/iex/symbols?token=DUMMY_TOKEN")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        // some simple validations, in practice I would expect these to be much more comprehensive.
        .andExpect(jsonPath("$[0].symbol", is("A")))
        .andExpect(jsonPath("$[1].symbol", is("AA")))
        .andExpect(jsonPath("$[2].symbol", is("AAAU")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPrice() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=DUMMY_TOKEN&symbols=FB")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-lastTradedPrice.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("FB")))
        .andExpect(jsonPath("$[0].price").value(new BigDecimal("186.3011")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPriceEmpty() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=DUMMY_TOKEN&symbols=")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(Collections.emptyList())))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPrice() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?token=DUMMY_TOKEN&symbol=AAPL&date=20190220")
                // This URL will be hit by the MockMvc client. The result is configured in the file
                // src/test/resources/wiremock/mappings/mapping-historicalPrice.json
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].date").value("2019-02-20"))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceEmptySymbol() throws Exception{

        MvcResult result = this.mvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .get("/iex/historicalPrice?token=DUMMY_TOKEN&symbol=")
                    // This URL will be hit by the MockMvc client. The result is configured in the file
                    // src/test/resources/wiremock/mappings/mapping-historicalPriceEmptySymbol.json
                    .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andReturn();

  }

  @Test
  public void testGetHistoricalPriceEmptyRange() throws Exception{
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?token=DUMMY_TOKEN&symbol=AAPL&range=")
                // This URL will be hit by the MockMvc client. The result is configured in the file
                // src/test/resources/wiremock/mappings/mapping-historicalPriceEmptyRange.json
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceEmptyDate() throws Exception{
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?token=DUMMY_TOKEN&symbol=AAPL&date=")
                // This URL will be hit by the MockMvc client. The result is configured in the file
                // src/test/resources/wiremock/mappings/mapping-historicalPriceEmptyDate.json
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  public void testGetHistoricalPriceBothDateAndRange() throws Exception{
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrice?token=DUMMY_TOKEN&symbol=AAPL&range=max&date=20190220")
                // This URL will be hit by the MockMvc client. The result is configured in the file
                // src/test/resources/wiremock/mappings/mapping-historicalPriceBothDateAndRange.json
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("AAPL")))
        .andExpect(jsonPath("$[0].date", is("2019-02-20")))
        .andReturn();
  }

  @Test
  public void testLastInstanceOfDate() throws Exception {

    // Initialize historical price data
    BigDecimal close = BigDecimal.valueOf(43.0075);
    BigDecimal high = BigDecimal.valueOf(43.33);
    BigDecimal low = BigDecimal.valueOf(42.7475);
    BigDecimal open = BigDecimal.valueOf(42.7975);
    String symbol = "AAPL";
    Integer volume = 104457448;
    LocalDate date = LocalDate.parse("2019-02-20");

    // To store a couple instances to compare via timestamp
    List<IexHistoricalPriceDTO> testList = new ArrayList<>();

    IexHistoricalPriceDTO firstPrice = new IexHistoricalPriceDTO(new IexHistoricalPrice(
        close, high, low, open, symbol, volume, date
    ));

    // Wait 1 second before initializing the next
    Thread.sleep(1000);

    IexHistoricalPriceDTO secondPrice = new IexHistoricalPriceDTO(new IexHistoricalPrice(
        close, high, low, open, symbol, 104457449, date
    ));

    // Add both to compare
    testList.add(firstPrice);
    testList.add(secondPrice);

    // Get the volume data from the most recent price using the helper method
    IexHistoricalPriceDTO result = DateHelpers.getMostRecentPrice(testList);
    String vol = result.getVolume().toString();

    Assert.assertTrue(testList.size() == 2);
    Assert.assertEquals(vol,"104457449");
    Assert.assertTrue(testList.get(0).getTimestamp().compareTo(result.getTimestamp()) < 0);
  }

}
