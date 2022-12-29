package dev.stjohn.starter

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.http.bodyAwait
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
    .onComplete(testContext.succeeding { response ->
        testContext.verify {
            Assertions.assertEquals(200, response.statusCode())
            Assertions.assertEquals("application/json", response.getHeader("Content-Type"))
            response.handler({ body ->
                Assertions.assertEquals("{\"bids\":[],\"asks\":[]}", body.toString())
                testContext.completeNow()
            })
        }
    })
  }
}
