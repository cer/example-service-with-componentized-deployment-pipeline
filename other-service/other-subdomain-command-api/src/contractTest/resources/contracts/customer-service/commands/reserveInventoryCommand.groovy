package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'reserveInventory'
    description "customer-service asks other-service to reserve inventory for an order"
    input {
        triggeredBy('reserveInventory()')
    }

    outputMessage {
        sentTo('otherService')
        body([
                orderId: 102
        ])
        headers {
            header('command_type', 'io.eventuate.otherservice.othersubdomain.commandapi.ReserveInventoryCommand')
        }
    }
}
