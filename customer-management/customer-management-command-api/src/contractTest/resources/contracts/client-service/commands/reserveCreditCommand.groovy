package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'reserveCredit'
    description "client-service asks customer-service to reserve credit for an order"
    input {
        triggeredBy('reserveCredit()')
    }

    outputMessage {
        sentTo('customerService')
        body([
                customerId: '11111111-1111-1111-1111-111111111111',
                orderId: 102,
                orderTotal: [amount: 12.34]
        ])
        headers {
            header('command_type', 'io.eventuate.customerservice.customermanagement.commandapi.ReserveCreditCommand')
        }
    }
}
