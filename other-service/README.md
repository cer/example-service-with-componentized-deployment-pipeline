# other-service

`other-service` exists only to support contract testing of the customer service's modules. It stands in for a separate service that the `customer-management` subdomain collaborates with. It has no domain logic of its own, and it is not built into an image or deployed.

It owns the contracts that the customer service's consumer-side contract tests run against. Each of its projects defines contracts with [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) and packages them as a stubs jar:

| Project | Contracts | Consumed by |
|---|---|---|
| `other-subdomain-event-publishing` | `events/otherEvent.groovy` — the `OtherEvent` domain event | `CustomerManagementEventConsumerContractTest` in `customer-management-event-subscribers` |
| `other-subdomain-rest-api` | `rest/getOther.groovy`, `rest/getOtherNotFound.groovy` — `GET` of an other resource | `OtherServiceProxyContractTest` in `customer-management-other-service-proxy` |
| `other-subdomain-command-api` | `commands/reserveInventoryCommand.groovy`, `replies/inventoryReservedReply.groovy` — the saga's `reserveInventory` step | The contract tests generated in `customer-management-sagas`, and `ReplyHandlersTest` |

The contracts are under `src/contractTest/resources/contracts/customer-service/`, named for the consumer.

## How the stubs reach the consumer modules

The root `settings.gradle` includes this project as a composite build with `includeBuild 'other-service'`. `build.gradle` exposes each project's stubs jar on a `contractStubs` configuration, and a consumer module depends on it with `targetConfiguration = 'contractStubs'`. No Maven repository is involved.

`other-subdomain-rest-api` and `other-subdomain-command-api` contain contracts only. They disable `generateContractTests` and `contractTest`, because `other-service` implements neither the REST API nor a command handler, so their contracts are checked only by the consumer-side tests. `other-subdomain-event-publishing` also verifies its contract on the provider side, against `BaseClass`, which publishes `OtherEvent`.

## In generated services

The skeleton generator copies `other-service` into a generated service only when the service applies a feature whose module has a consumer-side contract test: `event-subscribers`, `other-service-proxy` or `sagas`. `skeleton.yaml` declares this with `paths: [other-service]` on each of those features. The generated consumer modules then test against `other-service`'s placeholder collaboration, not against the contracts of the service they really collaborate with.
