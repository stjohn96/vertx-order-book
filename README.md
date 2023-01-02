# Simple InMemory Order Book Using Vertx + Kotlin

This project is a simple implementation of an in-memory order book using the Vertx framework and Kotlin. It exposes a REST API for placing and cancelling orders, as well as retrieving the current state of the order book and a list of recent trades.

## Prerequisites

-   JDK 1.8 or higher
-   Maven 3 or higher

## Running the API

1.  Clone the repository: `git clone https://github.com/stjohn96/vertx-order-book`
2.  Change into the project directory: `cd vertx-order-book`
3.  Run the API using Maven: `mvn clean compile exec:java`

The API will be running at [http://localhost:8889](http://localhost:8889/).

## Testing

`mvn test`

## Authentication

To authenticate a user, send a GET request to `http://localhost:8889/auth`. This will return a JSON Web Token (JWT) that must be included in the header of subsequent requests to the order book APIs. The header should have the following format:

Copy code

`Authorization: Bearer <JWT>`

## API Endpoints

### Auth

`GET /auth`

Generates a JSON Web Token (JWT) that is required for authenticating with the other endpoints.

### Get Order Book

`GET /:pair/orderbook`

Retrieves the current state of the order book for the specified pair.

#### Parameters

-   `pair`: The pair to retrieve the order book for (e.g. "BTCZAR")

#### Response

A JSON object with the following properties:

-   `bids`: A list of bid orders, sorted by price in descending order and then by timestamp
-   `asks`: A list of ask orders, sorted by price in ascending order and then by timestamp

### Place Limit Order

`POST /:pair/submitlimitorder`

Places a new limit order for the specified pair.

#### Parameters

-   `pair`: The pair to place the order for (e.g. "BTCZAR")

#### Request Body

A JSON object with the following properties:

-   `price`: The price of the order
-   `quantity`: The quantity of the order
-   `side`: The side of the order (either "BID" or "ASK")

#### Response

JSON object representing the created order, with the following properties:

-   `id`: The ID of the trade
-   `bid`: The bid order involved in the trade
-   `ask`: The ask order involved in the trade
-   `executePrice`: The price at which the trade was executed
-   `executeQuantity`: The quantity of the trade

### Get Trade History

`GET /:pair/tradehistory`

Retrieves a list of recent trades for the specified pair.

#### Parameters

-   `pair`: The pair to retrieve the trade history for (e.g. "BTCZAR")

#### Response

A list of JSON objects representing the recent trades, with the following properties:

-   `id`: The ID of the trade
-   `bid`: The bid order involved in the trade
-   `ask`: The ask order involved in the trade
-   `executePrice`: The price at which the trade was executed
-   `executeQuantity`: The quantity of the trade


### Cancel Order

`DELETE /:pair/cancelorder/:orderId`

Cancels an existing order for the specified pair and orderId.

#### Parameters

-   `pair`: The pair the order belongs to (e.g. "BTCZAR")
-   `orderId`: The ID of the order to cancel

#### Response

JSON object representing the canceled order, with the following properties:

-   `id`: The ID of the trade
-   `bid`: The bid order involved in the trade
-   `ask`: The ask order involved in the trade
-   `executePrice`: The price at which the trade was executed
-   `executeQuantity`: The quantity of the trade
