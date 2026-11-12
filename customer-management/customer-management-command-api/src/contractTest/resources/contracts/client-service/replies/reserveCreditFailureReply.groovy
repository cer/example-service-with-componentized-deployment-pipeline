package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'reserveCreditFailureReply'
    description "customer-service tells client-service that it could not reserve the credit"
    input {
        triggeredBy('reserveCreditFailureReply()')
    }

    outputMessage {
        sentTo('reserveCreditReply')
        body([])
        headers {
            header('reply_type', 'io.eventuate.customerservice.customermanagement.commandapi.CustomerNotFound')
            header('reply_outcome-type', 'FAILURE')
        }
    }
}
