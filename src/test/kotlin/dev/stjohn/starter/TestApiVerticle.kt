package dev.stjohn.starter

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class TestApiVerticle {

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(
        ApiVerticle(),
        testContext.succeeding<String> { _ -> testContext.completeNow() }
    )
  }

  @Test
  fun verticle_deployed(vertx: Vertx, testContext: VertxTestContext) {
    testContext.completeNow()
  }

  @Test
  fun getOrderBook(testContext: VertxTestContext) {
    Assertions.assertEquals(10, 10)
    testContext.completeNow()
  }

  @Test
  fun getOrderBookForNonExistentPair(vertx: Vertx, testContext: VertxTestContext) {
    // Issue an HTTP request
    // Assert that the response is 404

    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCUSD/orderbook")
        .compose { req -> req.send().map(HttpClientResponse::statusCode) }
        .onComplete(
            testContext.succeeding { statusCode ->
              testContext.verify {
                Assertions.assertEquals(404, statusCode)
                testContext.completeNow()
              }
            }
        )
  }

  @Test
  fun getOrderBookForPair(vertx: Vertx, testContext: VertxTestContext) {
    // Issue an HTTP request
    // Assert that the response is 404

    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCZAR/orderbook")
        .compose { req -> req.send() }
        .onComplete(
            testContext.succeeding { response ->
              testContext.verify {
                Assertions.assertEquals(200, response.statusCode())
                Assertions.assertEquals("application/json", response.getHeader("Content-Type"))
                response.handler({ body ->
                  Assertions.assertEquals("{\"bids\":[],\"asks\":[]}", body.toString())
                  testContext.completeNow()
                })
              }
            }
        )
  }

  // Submit limit order test
  @Test
  fun submitLimitOrderBid(vertx: Vertx, testContext: VertxTestContext) {
    val orderJson: String = """{"price":10000.0, "quantity":1.0, "side": "BID"}"""
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.POST, 8889, "localhost", "/BTCZAR/submitlimitorder")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.putHeader("Content-Length", orderJson.length.toString())
          request.write(orderJson)
          request.end()
        }
        .compose { req -> req.send() }
        .onComplete(
            testContext.succeeding { response ->
              testContext.verify {
                Assertions.assertEquals(200, response.statusCode())
                testContext.completeNow()
              }
            }
        )
  }

  // Submit limit order test
  @Test
  fun submitLimitOrderAsk(vertx: Vertx, testContext: VertxTestContext) {
    val orderJson: String = """{"price":10000.0, "quantity":1.0, "side": "ASK"}"""
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.POST, 8889, "localhost", "/BTCZAR/submitlimitorder")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.putHeader("Content-Length", orderJson.length.toString())
          request.write(orderJson)
          request.end()
        }
        .compose { req -> req.send() }
        .onComplete(
            testContext.succeeding { response ->
              testContext.verify {
                Assertions.assertEquals(200, response.statusCode())
                testContext.completeNow()
              }
            }
        )
  }

  // Simple Order matching
  @Test
  fun simpleOrderMatching(vertx: Vertx, testContext: VertxTestContext) {
    val ask: Order = Order(10000.0, 1.0, "ASK")
    val bid: Order = Order(10000.0, 1.0, "BID")
    val orderBook: OrderBook = OrderBook()

    orderBook.submitLimitOrder(ask)
    orderBook.submitLimitOrder(bid)

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.isEmpty())
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask)

    testContext.completeNow()
  }

  // Ensure order macthing to cheapest ask
  @Test
  fun higherBidLowerAskOrderMatching(vertx: Vertx, testContext: VertxTestContext) {
    val ask1: Order = Order(9000.0, 1.0, "ASK")
    val ask2: Order = Order(10000.0, 1.0, "ASK")
    val bid: Order = Order(10000.0, 1.0, "BID")

    val orderBook: OrderBook = OrderBook()

    orderBook.submitLimitOrder(ask1)
    orderBook.submitLimitOrder(ask2)
    orderBook.submitLimitOrder(bid)

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.first() == ask2)
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask1)

    testContext.completeNow()
  }

  // Ensure partial order matching
  @Test
  fun partialOrderMatching(vertx: Vertx, testContext: VertxTestContext) {
    val ask: Order = Order(10000.0, 1.0, "ASK")
    val bid: Order = Order(10000.0, 0.5, "BID")

    val orderBook: OrderBook = OrderBook()

    orderBook.submitLimitOrder(ask)
    orderBook.submitLimitOrder(bid)

    println(orderBook.getRecentTrades())

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.first().quantity == 0.5)
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask)
    Assertions.assertEquals(orderBook.getRecentTrades().first().executeQuantity, 0.5)
    testContext.completeNow()
  }

}
