# PACT & Integration Tests Examples

Tested on Java 8 OpenJDK, Docker Desktop for Mac 2.4

## Quick overview of the example

Run `gw test` to run all the tests - also PACT tests.

All the PACS files get stored in `pacts` directory.

Run `gw pactVerify` to verify PACTs generated during tests.

Run `gw runPactStubs` to start server with stubs based on generated PACTs in Docker container.

## Motivation

It is very rare to have an application that is not integrated with other systems. This also applies to AEM application
development. In many cases, we don't have control over those other parties. Such APIs are maintained by other teams or
even companies. Sometimes they are not maintained at all. On the other hand, they might not exist when we start our
delivery.

Those external APIs might be unstable, which can lead to unpredictable behavior of the entire system. It can be unclear
what is the root cause of a bug: if it was introduced in the external API or our application. E2E and integration tests
that we have in our project might fail from time to time without a straightforward reason (timeout, network issues,
unstable environments, etc.).

We can use [stubs](https://martinfowler.com/articles/mocksArentStubs.html) for those systems. However, then we don't
have confidence if the integration works well. We can integrate directly, but then it is hard to distinguish where the
problem is. Luckily, there are a few good options to explore.

All the code samples and configuration in Gradle can be found in the
[pact-example](https://github.com/mierzwid/pact-example) project on GitHub.

## Anti-corruption layer

As it often happens, not only the tools you use, but the architecture you choose (if done consciously), and the way you
structure your code, is what makes the difference. The Anti-corruption Layer is a pattern introduced in
["Domain Driven Design" book by Eric Evans](https://www.amazon.com/dp/0321125215). In general, the idea goes: "If your
application needs to deal with a database or another application whose model is undesirable or inapplicable to the model
you want within your own application, use an AnticorruptionLayer to translate to/from that model and yours."
[[wiki.c2.com]](http://wiki.c2.com/?AnticorruptionLayer)

Sometimes we need only a small piece of information that comes from an external system. The model used in the external
app might be legacy or simply serving a different purpose. It is important to avoid polluting our application domain
with that model. If we would let it go to our app internals, then any change in this external model might be a disaster
to our domain logic. Additionally, if we defer dealing with this model structure to some later time during execution, we
might end up with hard to debug errors raised at a later stage of request execution. It is like silently passing a
`null` value to a method and waiting for an explosion at a later stage of processing.

What can we do about it? Firstly, **proper validation**. If you integrate with an external system, make sure that the
data you read is 100% valid. If it is not, then indicate that by raising an exception with a proper message explaining
the situation in detail. The same goes for incoming requests, validate them before you start processing anything.

Secondly, **make a clear distinction between your local domain representation and the external model**. Perform
translation of data structure before you pass it to internal logic. This way your code won't be based on the external
model. Changes done to that model will affect only one place in your application - its entry point - the Anti-corruption
Layer.

## Integration tests with stubs?

Sometimes you want to perform integration tests of your application logic (all the layers of the app) without invoking
external API. It might be tempting to use some stubs for that so you can run your application against them during tests.
Imagine API client fetching currency rates (a specified currency to Polish złoty - PLN):

```kotlin
interface RateClient {
    fun getCurrencyToPlnRate(code: Code): Rate
}

class RateClientNbp(private val url: String = "http://api.nbp.pl") : RateClient {

    override fun getCurrencyToPlnRate(code: Code): Rate {
        return try {
            val responseText = URL(requestUrl(code)).readText()
            Json { ignoreUnknownKeys = true }
                .decodeFromString<RatesNbp>(responseText)
                .rates
                .map { Rate.of(it.mid) }
                .first()
        } catch (e: FileNotFoundException) {
            throw NotFoundException()
        }
    }
    ...
}
```

We try to stub the API to get `URL(requestUrl(code)).readText()` piece of code work in tests and enable assertions.
There is a simpler solution to that. If you have integration with the API nicely encapsulated, instead of stubbing
external APIs, stub your view on this API. In this case, the entire RateClient used in the RateService:

```kotlin
class RateService(private val client: RateClient = RateClientNbp()) {

    fun usdToEurRate(): Rate {
        val usd = client.getCurrencyToPlnRate(Code.USD)
        val eur = client.getCurrencyToPlnRate(Code.EUR)
        return usd / eur
    }
}
```

In many cases, your view on the API is much simpler than the API as a whole. Sometimes we need only one number to test
our application. Leverage this in tests and stub your code only. Your tests will became independent of other systems
(remember about [Anti-corruption Layer](#anti-corruption-layer)), less vulnerable to external changes and easy to
maintain! Imagine integration test for `RateService` performing `usdToEurRate` calculation on fetched rates:

```kotlin


@Test
fun shouldReturnUsdToEurRate() {
    // given
    val service = RateService(object : RateClient {
        override fun getCurrencyToPlnRate(code: Code) =
            when (code) {
                Code.USD -> Rate.of(5.0)
                Code.EUR -> Rate.of(4.0)
                else -> throw Exception("It should never ask for different rates")
            }
    })

    // when
    val usdToEurRate = service.usdToEurRate()

    // then
    assertEquals(Rate.of(1.25), usdToEurRate)
}
```

We can use mocking libraries like [mockito](https://site.mockito.org/) when needed or simply implement stubs in Java or
Kotlin, like in the above example, to reduce complexity even more. We won't have to stub all the HTTP stuff, headers,
content types, etc.

## UI tests with stubs?

Sometimes the external API is not there yet, or we want to perform UI tests of our application without calling external
systems (we use [AET](https://github.com/Cognifide/aet) @ Cognifide). In UI tests performance might be important.
Additionally, we don't want data to change between requests to avoid false negatives. Also, when external API is
unstable, we don't want our tests to fail because of other systems failing. Yes, those might be good use cases for
stubbed API endpoints that mimic the original API.

### Stubbed API

We have two options to create stubbed API:

- using DSL of a dedicated tool
- or via recording requests.

[WireMock](http://wiremock.org/) is one example of a tool that does both and is available for the JVM platform.

Describing in DSL might be a good exercise that allows us to learn this external API. We will stub all the endpoints we
have to use, all the nuances of the HTTP headers, etc. We will have a high degree of control of the stub, we can apply
very specific request matching and model complex user journeys.

[Recording requests](http://wiremock.org/docs/record-playback/) can be a faster way to get started. We simply run
several requests against an API proxied with WireMock. Those get recorded in JSON format and can be easily replayed
without interaction with the real API. We have less control here and might end up with some unnecessary stuff.

### AEM stubs

What about a situation, when we work in a restricted environment, where we cannot deploy any stubbing solution and there
is only an AEM instance that we have access to? There is a foxy solution to that created @ Cognifide:
[AEM stubs](https://github.com/Cognifide/aem-stubs). Simply speaking it is a package with WireMock that installs on AEM
and allows you to create stubbed endpoints directly on AEM.

### Stubbed view of the API

Still, stubbing external API might be a daunting task! There could be many endpoints, their contracts might be
complicated and hard to model using stubbing DSL. This is especially difficult in any transactional APIs, e.g.
supporting multi-step forms, where one request depends on another. Additionally, we can end with thousands of requests
recorded and re-recording them on a slight change in the API. Is there a simpler way?

Apparently, yes. We can take an approach similar to the one described in
["Integration tests with stubs?"](#integration-tests-with-stubs) - create stubs for our view of the API. Sometimes a
simple map structure can easily represent a view on a very complex API. Imagine, how reliable and fast an in-memory
hash map is, compared to an external API!

```kotlin
class RateClientStub : RateClient {
    override fun getCurrencyToPlnRate(code: Code) =
        when (code) {
            Code.USD -> Rate.of(3.82)
            Code.EUR -> Rate.of(4.674)
            Code.CHF -> Rate.of(3.483)
            else -> throw NotFoundException()
        }
}
```

Take the above example. We stubbed only the part we are interested in. We skipped all boilerplate related to protocol,
formats, etc. What is more, we could even add some simple state management when needed. It might take an hour using
[TDD](https://martinfowler.com/bliki/TestDrivenDevelopment.html) to write quite a reliable stub which will require
little maintenance in comparison to many hours of stubbing or re-recording using WireMock.

## E2E testing with real API

OK, but we need to finally connect to the real API and verify how everything works together, don't we? Yes, but before
that, I can see one more clear and valuable step to be done -
[contract tests](https://martinfowler.com/bliki/ContractTest.html).

### Contract tests

Contract tests help us to make a clear distinction between problems introduced in our application, and those introduced
by an external API - strictly related to the particular integration. A well-known contract test tool is
[PACT](https://pact.io/). Please see five-slides presentation
[How PACT works](https://pactflow.io/how-pact-works/?utm_source=ossdocs&utm_campaign=getting_started#slide-1) to have
good visualization of it.

#### Define contract

Firstly, we define a contract between our application and the API (the way we will actually use it - since we probably
won't use all its endpoints and properties) and run tests against this contract. Have a look at example of contract
definition in DSL:

```kotlin
@Pact(provider = "nbp", consumer = "demo")
fun nbpEurPact(builder: PactDslWithProvider): RequestResponsePact? {
    return builder
        .given("euro rate")
        .uponReceiving("a request for json data")
        .path("/api/exchangerates/rates/A/EUR")
        .method("GET")
        .query("format=json")
        .willRespondWith()
        .status(200)
        .body(
            PactDslJsonBody()
                .stringValue("code", "EUR")
                .array("rates")
                .`object`()
                .numberType("mid", 4.38)
                .closeObject()
                .closeArray()
        )
        .toPact()
}
```

Please notice, that this contract does not specify every JSON property which is returned by the real endpoint. It only
focuses on those properties that are important from our (the API consumer) point of view. Here is an example of the full
response from the API that the contract applies to where you can see that there are other properties like `table` that
won't be validated:

```json
// 20201105130212
// http://api.nbp.pl/api/exchangerates/rates/A/EUR?format=json

{
  "table": "A",
  "currency": "euro",
  "code": "EUR",
  "rates": [
    {
      "no": "217/A/NBP/2020",
      "effectiveDate": "2020-11-05",
      "mid": 4.5233
    }
  ]
}
```

Having this `nbpEurPact` contract we can run tests against it and verify how our application will work:

```kotlin{numberLines: true}
@Test
@PactVerification(value = ["nbp"], fragment = "nbpEurPact")
fun shouldFetchEURRatesPACT() {
    //given
    val client = RateClientNbp(provider.url)

    //when
    val rate = client.getCurrencyToPlnRate(Code.EUR)

    //then
    assertEquals(Rate.of(4.38), rate)
}
```

The 2nd line defines which fragment of the contract (`nbpEurPact`) we verify against the provider (`nbp`). As a result
of executing it, we will get a JSON file with all fragments of our contract:

```json
{
  "provider": {
    "name": "nbp"
  },
  "consumer": {
    "name": "demo"
  },
  "interactions": [
    {
      "description": "a request for json data",
      "request": {
        "method": "GET",
        "path": "/api/exchangerates/rates/A/EUR",
        "query": {
          "format": [
            "json"
          ]
        }
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json; charset=UTF-8"
        },
        "body": {
          "code": "EUR",
          "rates": [
            {
              "mid": 4.38,
            }
          ],
          "currency": "euro",
          "table": "A"
        },
        "matchingRules": {
          "body": {
            "$.rates[0].mid": {
              "matchers": [
                {
                  "match": "number"
                }
              ],
              "combine": "AND"
            }
          },
          "header": {
            "Content-Type": {
              "matchers": [
                {
                  "match": "regex",
                  "regex": "application/json(;\\s?charset=[\\w\\-]+)?"
                }
              ],
              "combine": "AND"
            }
          }
        }
      },
      "providerStates": [
        {
          "name": "euro rate"
        }
      ]
    }
}
```

What is worth mentioning, data returned by this stub based on the contract won't change from test to test. Additionally,
we exercise our application against a real HTTP server, we send headers, receive a response. What is more, this is only
the first part.

#### Verify contract

Secondly, we verify if the real, provided API also fulfills the contract. In the
[example project](https://github.com/mierzwid/pact-example), there is a Gradle task `pactVerify` that will take all
contracts stored in the `pacts` directory and run them against the API. In this case, it will be the National Bank of
Poland: http://api.nbp.pl.

Have a look back into the contract file in JSON and notice that it captured information about the request - not only the
response:

```json
"request": {
        "method": "GET",
        "path": "/api/exchangerates/rates/A/EUR",
        "query": {
          "format": [
            "json"
          ]
        }
```

This way, the contract can be verified both ways and the `pactVerify` task can perform the same requests that were
performed by our application. Also, the response is validated via matching rules, so even the rate of USD against PLN
will change, validation will still pass as long the API will return a valid number:

```json
"matchingRules": {
          "body": {
            "$.rates[0].mid": {
              "matchers": [
                {
                  "match": "number"
                }
              ],
```

#### Engage API provider

If the API is "alive", and there is a team developing or maintaining it, the next step is to engage that team in the
contract verification. We can share contracts with the API team using a contract broker (e.g.
[PACT Flow](https://pactflow.io/)). This way, they could **incorporate your contracts** (namely, your expectations
related to the API as the API consumer) **into their daily development lifecycle** to verify if the API still supports
your needs after introducing any changes.

**This can make a difference and vastly improve your collaboration.**

#### Stubs based on contract tests

Everything looks very promising. My initial question was, could I use those contracts to set up stubs used in UI testing
in our integration environment? The answer is yes. One way is to use Docker to run a PACT stub server that will consume
JSON contracts. In the [example project](https://github.com/mierzwid/pact-example) you can run the `runPactStubs` task
to run a Docker container serving those stubs.

#### When to use contract tests?

The short answer is simple: when we want to verify the contract between API consumer and API provider. The full answer
is a little longer. Contract tests should focus on contract verification. It should validate required HTTP headers and
parameters, the structure of the request and response, fields in the JSON document. However, **please don't test
complicated scenarios of interactions, or the application logic itself, using contract tests!** It is because you can
end up with many contract tests that are hard to maintain. The effort will outweigh the value you'll get out
of it. Because of that, **be cautious about using contract-based stubs for UI tests!**

#### How frequently to run contract tests?

Include consumer verification in your regular test suite and run it on each build to see if your application still
works with the contract.

On the other hand, it is not your responsibility to verify the provided API frequently. The `pactVerify` task could be
run just before STAGE deployment or nightly to see if there were any changes introduced in the API.

However, if API providers use published contracts to verify their changes as described in
["Engage API provider"](#engage-api-provider), it would be valuable if they run verification on every change.

### Direct integration

Finally, we need to come to the point when we can verify our system E2E with the real API. I believe it has to happen
frequently, so we can stay confident the changes we introduce are correct. On the other hand, it cannot slow down our
daily development.

My advice is to have the INTEGRATION environment work against a stubbed view of the API for UI tests and manual testing.
This way our development team won't get stuck when the external API is down.

Then, to have the STAGE environment use the real API. Additionally, make sure to run contract verification just before
deploying a new version of your application to STAGE, and a sensible set of E2E tests just after the deployment, to see
how everything works together.

## Conclusions

Integration testing is a complex problem. It is good to add a dose of common sense to our work every time we need to
make some decisions in this field. Remember, you have three valuable allies that can help:

1. you can **stub your view of the external API instead of stubbing the entire API** - both for UI & integration tests,
2. you can have a watchdog in form of **contract tests** to validate integration without exposure to issues related to
   real API,
3. you can **integrate directly with the real API on STAGE and higher environments** to stay confident entire system
   serves its purpose.

**Please, don't lose time debugging issues created by other teams.** You have enough of your problems.

All the code samples and configuration in Gradle can be found in the
[pact-example](https://github.com/mierzwid/pact-example) project on GitHub.
