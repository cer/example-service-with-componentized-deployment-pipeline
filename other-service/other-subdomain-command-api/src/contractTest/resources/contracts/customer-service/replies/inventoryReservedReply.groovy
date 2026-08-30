package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'inventoryReserved'
    description "other-service tells customer-service that inventory was reserved"
    input {
        triggeredBy('inventoryReserved()')
    }

    outputMessage {
        sentTo('reserveInventoryReply')
        body([])
        headers {
            header('reply_type', 'io.eventuate.otherservice.othersubdomain.commandapi.InventoryReserved')
            header('reply_outcome-type', 'SUCCESS')
        }
    }
}
