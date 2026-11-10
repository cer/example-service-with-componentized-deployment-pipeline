package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'reserveCreditSuccessReply'
    description "customer-service tells client-service that it reserved the credit"
    input {
        triggeredBy('reserveCreditSuccessReply()')
    }

    outputMessage {
        sentTo('reserveCreditReply')
        body([])
        headers {
            header('reply_type', 'io.eventuate.customerservice.customermanagement.commandapi.CustomerCreditReserved')
            header('reply_outcome-type', 'SUCCESS')
        }
    }
}
