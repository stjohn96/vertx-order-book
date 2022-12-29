package dev.stjohn.starter
import java.util.PriorityQueue

class OrderBook(){
  // Use a priority queue to store the orders sorted by price
  private val bids = PriorityQueue<Order>(compareBy { it.price })
  private val asks = PriorityQueue<Order>(compareBy { it.price })

  // Use a hash map to store the orders indexed by their ID
  private val orders = HashMap<String, Order>()

  // Use a list to store the recent trades
  private val trades = ArrayList<Trade>()

  fun submitLimitOrder(order: Order) {
      // Check if the order can be immediately matched with an existing order
      val otherOrder = if (order.isBid) {
          if (asks.isNotEmpty() && order.price >= asks.peek().price) asks.poll() else null
      } else {
          if (bids.isNotEmpty() && order.price <= bids.peek().price) bids.poll() else null
      }

      // If the order can be matched, execute the trade and add it to the list of recent trades
      if (otherOrder != null) {
          val trade = Trade(order, otherOrder)
          trades.add(trade)
      } else {
          // Otherwise, add the order to the data structure
          orders[order.id] = order
          if (order.isBid) {
              bids.add(order)
          } else {
              asks.add(order)
          }
      }
  }

  fun cancelOrder(orderId: String) {
      // Remove the order with the given ID from the data structure
      val order = orders.remove(orderId)
      if (order != null) {
          if (order.isBid) {
              bids.remove(order)
          } else {
              asks.remove(order)
          }
      }
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

data class Order(val id: String, val price: Double, val quantity: Double, val isBid: Boolean)
data class Trade(val bid: Order, val ask: Order)
data class OrderBookData(val bids: List<Order>, val asks: List<Order>)
