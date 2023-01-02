package dev.stjohn.starter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OrderBookTests {

  @Test
  fun simpleOrderMatching() {
    val ask: Order = Order(price = 10000.0, quantity = 1.0, side = "ASK")
    val bid: Order = Order(price = 10000.0, quantity = 1.0, side = "BID")
    val orderBook: OrderBook = OrderBook()

    orderBook.submitLimitOrder(ask)
    orderBook.submitLimitOrder(bid)

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.isEmpty())
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask)
  }

  // Ensure order macthing to cheapest ask
  @Test
  fun higherBidLowerAskOrderMatching() {
    val ask1: Order = Order(price = 9000.0, quantity = 1.0, side = "ASK")
    val ask2: Order = Order(price = 10000.0, quantity = 1.0, side = "ASK")
    val bid: Order = Order(price = 10000.0, quantity = 1.0, side = "BID")

    val orderBook: OrderBook = OrderBook()

    orderBook.submitLimitOrder(ask1)
    orderBook.submitLimitOrder(ask2)
    orderBook.submitLimitOrder(bid)

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.first() == ask2)
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask1)
  }

  // Ensure partial order matching
  @Test
  fun partialOrderMatching() {
    val orderBook: OrderBook = OrderBook()
    val ask: Order = Order(price = 10000.0, quantity = 1.0, side = "ASK")
    val bid: Order = Order(price = 10000.0, quantity = 0.5, side = "BID")

    orderBook.submitLimitOrder(ask)
    orderBook.submitLimitOrder(bid)

    println(orderBook.getRecentTrades())

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.first().quantity == 0.5)
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask)
    Assertions.assertEquals(orderBook.getRecentTrades().first().executeQuantity, 0.5)
  }

  // Simple Order matching
  @Test
  fun timestampOrderMatching() {
    val orderBook: OrderBook = OrderBook()
    val ask1: Order = Order(price = 10000.0, quantity = 1.0, side = "ASK")
    // Sleep for 100ms to ensure timestamp is different
    Thread.sleep(100)
    val ask2: Order = Order(price = 10000.0, quantity = 1.0, side = "ASK")
    val bid: Order = Order(price = 10000.0, quantity = 1.0, side = "BID")

    orderBook.submitLimitOrder(ask1)
    orderBook.submitLimitOrder(ask2)
    orderBook.submitLimitOrder(bid)

    Assertions.assertTrue(orderBook.getOrderBook().bids.isEmpty())
    Assertions.assertTrue(orderBook.getOrderBook().asks.first() == ask2)
    Assertions.assertEquals(orderBook.getRecentTrades().first().bid, bid)
    Assertions.assertEquals(orderBook.getRecentTrades().first().ask, ask1)
  }

  @Test
  fun cancelOrder() {
    val orderBook: OrderBook = OrderBook()
    val order = Order(price = 10.0, quantity = 5.0, side = "BID")

    orderBook.submitLimitOrder(order)
    orderBook.cancelOrder(order.id)

    val orderBookData = orderBook.getOrderBook()
    Assertions.assertTrue(orderBookData.bids.isEmpty())
    Assertions.assertTrue(orderBookData.asks.isEmpty())
  }

  @Test
  fun getRecentTrades() {
    val orderBook: OrderBook = OrderBook()

    // Test adding two trades to the list
    val bid1 = Order(price = 10.0, quantity = 5.0, side = "BID")
    val ask1 = Order(price = 10.0, quantity = 5.0, side = "ASK")
    val bid2 = Order(price = 11.0, quantity = 2.0, side = "BID")
    val ask2 = Order(price = 9.0, quantity = 2.0, side = "ASK")
    orderBook.submitLimitOrder(ask1)
    orderBook.submitLimitOrder(bid1)
    orderBook.submitLimitOrder(ask2)
    orderBook.submitLimitOrder(bid2)

    val recentTrades = orderBook.getRecentTrades()

    Assertions.assertEquals(recentTrades.size, 2)
    Assertions.assertEquals(recentTrades[0].executePrice, 10.0)
    Assertions.assertEquals(recentTrades[0].executeQuantity, 5.0)
    Assertions.assertEquals(recentTrades[0].bid.id, bid1.id)
    Assertions.assertEquals(recentTrades[0].ask.id, ask1.id)
    Assertions.assertEquals(recentTrades[1].executePrice, 9.0)
    Assertions.assertEquals(recentTrades[1].executeQuantity, 2.0)
    Assertions.assertEquals(recentTrades[1].bid.id, bid2.id)
    Assertions.assertEquals(recentTrades[1].ask.id, ask2.id)
  }
}
