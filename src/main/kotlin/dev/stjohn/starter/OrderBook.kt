package dev.stjohn.starter

import java.time.Instant
import java.util.PriorityQueue
import java.util.UUID

class OrderBook() {
  // Use a priority queue to store the orders sorted by price and timestamp
  private val bids = PriorityQueue<Order>(compareBy { it.price ; it.timestamp })
  private val asks = PriorityQueue<Order>(compareBy { it.price ; it.timestamp })

  // Use a hash map to store the orders indexed by their ID
  private val orders = HashMap<String, Order>()

  // Use a list to store the recent trades
  private val trades = ArrayList<Trade>()

  fun submitLimitOrder(order: Order) {
    // Check if the order can be immediately matched with an existing order

    // TODO: Remove .poll() and replace with a more efficient solution unnessary to remove from
    // queue until we know we have a match
    val otherOrder =
        if (order.side == "BID") {
          if (asks.isNotEmpty() && order.price >= asks.peek().price) asks.poll() else null
        } else {
          if (bids.isNotEmpty() && order.price <= bids.peek().price) bids.poll() else null
        }

    // If the order can be matched, execute the trade and add it to the list of recent trades
    if (otherOrder != null) {
      var tradeQuantity = otherOrder.quantity
      println("Placed Order $order")
      println("Placed Order $otherOrder")
      if (tradeQuantity > order.quantity) {
        tradeQuantity = order.quantity
        order.quantity -= tradeQuantity
        otherOrder.quantity -= tradeQuantity
        asks.add(otherOrder)
      } else {
        order.quantity -= tradeQuantity
      }
      if (order.side == "BID") {

        trades.add(
            Trade(
                bid = order,
                ask = otherOrder,
                executePrice = otherOrder.price,
                executeQuantity = tradeQuantity
            )
        )
      } else {
        trades.add(
            Trade(
                bid = otherOrder,
                ask = order,
                executePrice = otherOrder.price,
                executeQuantity = tradeQuantity
            )
        )
      }

      // If there is still remaining quantity for the original order, add it back to the order book
      if ((order.quantity - tradeQuantity) > 0) {
        orders[order.id] = order
        if (order.side == "BID") {
          bids.add(order)
        } else {
          asks.add(order)
        }
      }
    } else {
      // Otherwise, add the order to the data structure
      orders[order.id] = order
      if (order.side == "BID") {
        bids.add(order)
      } else {
        asks.add(order)
      }
    }
  }

  fun cancelOrder(orderId: String) {
    // Remove the order with the given ID from the data structure
    println("Cancel Order $orderId")
    println("Orders $orders")
    val order = orders.remove(orderId)
    print("Order $order")
    if (order != null) {
      if (order.side == "BID") {
        bids.remove(order)
      } else {
        asks.remove(order)
      }
    }
    println("Orders $orders")
  }

  fun getOrderBook(): OrderBookData {
    // Return the current state of the order book
    return OrderBookData(bids.toList(), asks.toList())
  }

  fun getRecentTrades(): List<Trade> {
    // Return the list of recent trades
    return trades
  }
}

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val price: Double,
    var quantity: Double,
    val side: String,
    val timestamp: Long = Instant.now().toEpochMilli()
)

data class Trade(
    val id: String = UUID.randomUUID().toString(),
    val bid: Order,
    val ask: Order,
    val executePrice: Double,
    val executeQuantity: Double,
    val timestamp: Long = Instant.now().toEpochMilli()
)

data class OrderBookData(val bids: List<Order>, val asks: List<Order>)
