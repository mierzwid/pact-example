# PACT & Integration Tests Examples

Tested on Java 8 OpenJDK, Docker Desktop for Mac 2.4

## Quick overview of the example

Run `gw test` to run all the tests - also PACT tests.

All the PACS files get stored in `pacts` directory.

Run `gw pactVerify` to verify PACTs generated during tests.

Run `gw runPactStubs` to start server with stubs based on generated PACTs in Docker container.

## Motivation

It is very rare to have applications that are not integrated with other systems. This also applies to AEM application development. In many cases, we don't have control over those other parties. Those APIs are maintained by other organizations or even companies. Sometimes they are not maintained at all. On the other hand, they might not exist at all when we start our delivery.

Those external APIs might be unstable, which can lead to unpredictable behavior of the entire system. It might be unclear where is the cause of a bug if it was introduced in the external API or our application? E2E and integration tests that we have in our project might fail from time to time without a straightforward reason (timeout, network issues, unstable environments, etc.).

We can use mocks of those systems, however, we won't have confidence if the integration works well. We can integrate directly, but then it is hard to distinguish where is the problem. Luckily, there are a few tools and ideas that are there to help.

## Anticorruption layer

As it might often happen, that not only the tools you use but the architecture you choose (if done consciously) and the way you structure your code is what can make the difference. The Anticorruption Layer is a pattern introduced in ["Domain Driven Desing" book by Eric Evans](https://www.amazon.com/dp/0321125215). In general, the idea goes: ["If your application needs to deal with a database or another application whose model is undesirable or inapplicable to the model you want within your own application, use an AnticorruptionLayer to translate to/from that model and yours."](http://wiki.c2.com/?AnticorruptionLayer)

Sometimes we need only a small piece of information that comes from an external system. The model used in the external app might be legacy or simply serving a different purpose. It is important to avoid polluting our application domain with that model. If we would let it go to our app internals, then any change in this external model might be a disaster to our domain logic. Additionally, if we deffer dealing with this model structure to some later time during execution, we might end up with hard to debug errors that raise at a later stage of request execution. It is like silently passing a `null` value to a method and waiting for an explosion at a later stage of processing.

What we can do about it? Firstly, **proper validation**. If you integrate with an external system, make sure that the data you read is 100% valid. If it is not, then indicate that raising an exception with a proper message explaining the situation in detail. The same goes for incoming requests, validate them before you will start processing anything.

Secondly, **make a clear distinction between your local domain representation and the external model**. Perform translation of data structure before you will pass it to internal logic. This way your code won't be based on the external model. Changes done to that model will affect only one place in your application - its entry point - the Anticorruption Layer.

## Integration tests with mock API?

Sometimes you want to perform integration tests of your application logic (all the layers of the app) without exercising external API. It might be tempting to use some stubs for that so you can run your application against them during tests. Imagine API client fetching currency rates:

```kotlin
class RateClientNbp(private val url: String = "http://api.nbp.pl") : RateClient {

    override fun getRate(code: Code) = try {
        val responseText = URL(requestUrl(code)).readText()
        Json.decodeFromString<ExchangeRates>(responseText).rates.first()
    } catch (e: FileNotFoundException) {
        throw NotFoundException()
    }
    ...
}
```

We try to mock the API to get `URL(requestUrl(id)).readText()` piece of code work in tests and enable assertions. There is a simpler solution to that. If you have integration with the API nicely encapsulated, instead of mocking external APIs, mock your view on this API, in this case, the entire RateClient used in the RateService:

```kotlin
interface RateClient {
    fun getRate(code: Code): Rate
}

class RateService(private val client: RateClient = RateClientNbp())
```

In many cases, your view on the API is much simpler than the API as a whole. Sometimes we need only one number to test our application. Leverage this in tests and mock your code only. Your tests will became independent from external systems (remember about [Anticorruption Layer]()), less vulnerable for external changes and easy to maintain! Imagine integration test for `RateService` performing `usdToEurRate` calculation on fetched rates:

```kotlin
@Test
fun shouldReturnUsdToEurRate() {
    // given
    val client = object : RateClient {
        override fun getRate(code: Code) =
            when (code) {
                Code.USD -> Rate.of(5.0)
                Code.EUR -> Rate.of(4.0)
                else -> throw Exception("It should never ask for different rates")
            }
    }
    val service = RateService(client)

    // when
    val usdToEurRate = service.usdToEurRate()

    // then
    assertEquals(1.25, usdToEurRate, 0.001)
}
```

We can use mocking libraries when needed or simply implement mocks in Java or Kotlin, like in the above example, to reduce complexity even more. We won't have to mock all the HTTP stuff, headers, content types, etc.

## UI tests with stubs?

Sometimes the external API is not there yet or we want to perform UI tests of our application without exercising external systems. In UI tests performance might be important. Additionally, we don't want data to change between requests to avoid false negatives. Also, when external API is unstable, we don't want our tests to fail because of other systems failing. Yes, those might be good use cases for stubs - mock API endpoints that mimic the original API.

### Stubs

We have some options to create stubs:
using DSL of a dedicated tool
or via recording requests.

[WireMock](http://wiremock.org/) is one example of a tool that does both and is available for the JVM platform.

Describing in DSL might be a good exercise that allows us to learn this external API. We will mock all the endpoints we have to use, all the nuances of the HTTP headers, etc. We will have a high degree of control of the mock, we can apply very specific request matching and model scenarios (needed for wizards, etc.).

[Recording requests](http://wiremock.org/docs/record-playback/) can be a faster way to get started. We simply run several requests against API proxied with WireMock. Those get recorded in JSON format and then can be easily replayed without interaction with the real API. We have less control here and might end up with some unnecessary stuff.

### Mock implementation

Still, stubbing external API might be a daunting task! There could be many endpoints, their contracts might be complicated and hard to model using stubbing DSL. Or we can end with thousands of requests recorded and re-recording them on a slight change in the API. Is there a simpler way?

Apparently, yes. We can take an approach similar to the one described in [Integration testing]() - create a mock implementation for our view of the API. In this case, it won't be that simple as before but still simpler than the API itself! Sometimes a hash map can easily represent a view on a very complex API. Imagine how reliable is an in-memory hash map compared to an external API!

```kotlin
class RateClientMock : RateClient {
    override fun getRate(code: Code) =
        when (code) {
            Code.USD -> Rate.of(3.82)
            Code.EUR -> Rate.of(4.674)
            Code.CHF -> Rate.of(3.483)
            else -> throw NotFoundException()
        }
}
```

Take the above example. We mocked only the part we are interested in. We skipped all boilerplate related to protocol, formats, etc. What is more, we could even add some simple state management when needed to mimic API even better. It might take an hour using TDD to write quite a reliable mock which will require little or no maintenance in comparison to many hours of stubbing or re-recording using WireMock.