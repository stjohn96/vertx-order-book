package dev.stjohn.starter
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import com.google.gson.Gson
import kotlin.contracts.contract
import dev.stjohn.starter.OrderBook

class ApiVerticle : AbstractVerticle() {
  private val orderBooks = HashMap<String, OrderBook>()

  override fun start(promise: Promise<Void>) {

    val router = Router.router(vertx);

    router.get("/").handler { req ->
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!")
    }

    router.get("/:pair/orderbook").handler(this@ApiVerticle::getOrderBook)
    router.post("/:pair/submitlimitorder").handler(this@ApiVerticle::submitLimitOrder)
    router.get("/:pair/tradehistory").handler(this@ApiVerticle::getTradeHistory)

    // Init pairs
    orderBooks.put("BTCZAR", OrderBook())
    orderBooks.put("ETHZAR", OrderBook())
    orderBooks.put("LTCZAR", OrderBook())

    vertx.createHttpServer().requestHandler(router).listen(8889) { http ->
      if (http.succeeded()) {
        promise.complete()
        println("HTTP API server started on port 8889")
      } else {
        promise.fail(http.cause());
      }
    }
  }

  private fun getOrderBook(context: RoutingContext) {
    val pair = context.pathParam("pair")
    val orderBook = orderBooks.get(pair)

    // Check if pair is not in orderbooks return 404
    if (orderBook == null){
      context.response().statusCode = 404
      context.response().end()
      return
    }

    context.response().statusCode = 200
    context.response().putHeader("Content-Type", "application/json")
    context.response().end(Gson().toJson(orderBook?.getOrderBook()))
  }

  private fun submitLimitOrder(context: RoutingContext) {
    val pair = context.pathParam("pair")
    val orderBook = orderBooks.get(pair)

    // Check if pair is not in orderbooks return 404
    if (orderBook == null){
      context.response().statusCode = 404
      context.response().end()
      return
    }

    context.request().bodyHandler { body ->
      val order = Gson().fromJson(body.toString(), Order::class.java)

      orderBook.submitLimitOrder(order)

      context.response().statusCode = 200
      context.response().putHeader("Content-Type", "application/json")
      context.response().end()
    }
  }

  private fun getTradeHistory(context: RoutingContext) {
    val pair = context.pathParam("pair")
    val orderBook = orderBooks.get(pair)

    // Check if pair is not in orderbooks return 404
    if (orderBook == null){
      context.response().statusCode = 404
      context.response().end()
      return
    }

    context.response().statusCode = 200
    context.response().putHeader("Content-Type", "application/json")
    context.response().end(Gson().toJson(orderBook.getRecentTrades()))
  }
}



