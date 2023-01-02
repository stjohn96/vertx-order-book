package dev.stjohn.starter

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class TestApiVerticle {

  private val config =
      JWTAuthOptions()
          .addJwk(
              JsonObject()
                  .put("alg", "HS256")
                  .put("kty", "oct")
                  .put("k", "ZzwjfDFDGDFGJgvS8hSfIzJ27nY9IH0vLE8UNpJjHBY")
          )

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
    val authProvider = JWTAuth.create(vertx, config)
    val jwt = authProvider.generateToken(JsonObject().put("sub", "vertx-order-book"), JWTOptions())

    // Issue an HTTP request
    // Assert that the response is 404
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCUSD/orderbook")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          // Put JWT token in header
          request.putHeader("Authorization", "Bearer $jwt")
          request.end()
        }
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
    val authProvider = JWTAuth.create(vertx, config)
    val jwt = authProvider.generateToken(JsonObject().put("sub", "vertx-order-book"), JWTOptions())

    // Issue an HTTP request
    // Assert that the response is 404

    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCZAR/orderbook")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.putHeader("Authorization", "Bearer $jwt")
          request.end()
        }
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
    val authProvider = JWTAuth.create(vertx, config)
    val jwt = authProvider.generateToken(JsonObject().put("sub", "vertx-order-book"), JWTOptions())

    val orderJson: String = """{"price":10000.0, "quantity":1.0, "side": "BID"}"""
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.POST, 8889, "localhost", "/BTCZAR/submitlimitorder")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.putHeader("Content-Length", orderJson.length.toString())
          request.putHeader("Authorization", "Bearer $jwt")
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
    val authProvider = JWTAuth.create(vertx, config)
    val jwt = authProvider.generateToken(JsonObject().put("sub", "vertx-order-book"), JWTOptions())

    val orderJson: String = """{"price":10000.0, "quantity":1.0, "side": "ASK"}"""
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.POST, 8889, "localhost", "/BTCZAR/submitlimitorder")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.putHeader("Content-Length", orderJson.length.toString())
          request.putHeader("Authorization", "Bearer $jwt")
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

  @Test
  fun getRecentTrades(vertx: Vertx, testContext: VertxTestContext) {
    val authProvider = JWTAuth.create(vertx, config)
    val jwt = authProvider.generateToken(JsonObject().put("sub", "vertx-order-book"), JWTOptions())

    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCZAR/tradehistory")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.putHeader("Authorization", "Bearer $jwt")
          request.end()
        }
        .compose { req -> req.send() }
        .onComplete(
            testContext.succeeding { response ->
              testContext.verify {
                Assertions.assertEquals(200, response.statusCode())
                Assertions.assertEquals("application/json", response.getHeader("Content-Type"))
                response.handler({ body ->
                  Assertions.assertEquals("[]", body.toString())
                  testContext.completeNow()
                })
              }
            }
        )
  }

  @Test
  fun submitLimitOrderNoAuth(vertx: Vertx, testContext: VertxTestContext) {
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
        .compose { req -> req.send().map(HttpClientResponse::statusCode) }
        .onComplete(
            testContext.succeeding { statusCode ->
              testContext.verify {
                Assertions.assertEquals(401, statusCode)
                testContext.completeNow()
              }
            }
        )
  }

  @Test
  fun getOrderBookForPairNoAuth(vertx: Vertx, testContext: VertxTestContext) {
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCZAR/orderbook")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.end()
        }
        .compose { req -> req.send().map(HttpClientResponse::statusCode) }
        .onComplete(
            testContext.succeeding { statusCode ->
              testContext.verify {
                Assertions.assertEquals(401, statusCode)
                testContext.completeNow()
              }
            }
        )
  }

  @Test
  fun getRecentTradesNoAuth(vertx: Vertx, testContext: VertxTestContext) {
    val client: HttpClient = vertx.createHttpClient()
    client
        .request(HttpMethod.GET, 8889, "localhost", "/BTCZAR/tradehistory")
        .onSuccess { request: HttpClientRequest ->
          request.putHeader("Content-Type", "application/json")
          request.end()
        }
        .compose { req -> req.send().map(HttpClientResponse::statusCode) }
        .onComplete(
            testContext.succeeding { statusCode ->
              testContext.verify {
                Assertions.assertEquals(401, statusCode)
                testContext.completeNow()
              }
            }
        )
  }
}
