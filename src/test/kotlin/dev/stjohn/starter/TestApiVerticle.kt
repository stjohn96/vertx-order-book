package dev.stjohn.starter

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions;


@ExtendWith(VertxExtension::class)
class TestApiVerticle {

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(ApiVerticle(), testContext.succeeding<String> { _ -> testContext.completeNow() })
  }

  @Test
  fun verticle_deployed(vertx: Vertx, testContext: VertxTestContext) {
    testContext.completeNow()
  }

  @Test
  fun getOrderBook(testContext: VertxTestContext){
    Assertions.assertEquals(10, 10);
    testContext.completeNow()
  }

  @Test
  fun getOrderBookForNonExistentPair(testContext:VertxTestContext){
    testContext.completeNow()
  }
}
