package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'authorizePayment'
    description "customer-service asks other-service to authorize payment for an order"
    input {
        triggeredBy('authorizePayment()')
    }

    outputMessage {
        sentTo('otherService')
        body([
                customerId: '11111111-1111-1111-1111-111111111111',
                orderId: 102,
                orderTotal: [amount: 12.34]
        ])
        headers {
            header('command_type', 'io.eventuate.otherservice.othersubdomain.commandapi.AuthorizePaymentCommand')
        }
    }
}
