package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'customerCreatedEvent'
    description "publishes CustomerCreatedEvent when a customer is created"
    input {
        triggeredBy('customerCreatedEvent()')
    }

    outputMessage {
        sentTo('io.eventuate.customerservice.customermanagement.domain.Customer')
        body([
                name       : 'Fred',
                creditLimit: [amount: 15.00]
        ])
        headers {
            header('event-aggregate-type', 'io.eventuate.customerservice.customermanagement.domain.Customer')
            header('event-type', 'io.eventuate.customerservice.customermanagement.domain.CustomerCreatedEvent')
            header('event-aggregate-id', '11111111-1111-1111-1111-111111111111')
        }
    }
}
