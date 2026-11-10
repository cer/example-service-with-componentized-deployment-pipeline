package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'authorizePaymentFailureReply'
    description "other-service tells customer-service that payment was declined"
    input {
        triggeredBy('authorizePaymentFailureReply()')
    }

    outputMessage {
        sentTo('authorizePaymentReply')
        body([])
        headers {
            header('reply_type', 'io.eventuate.otherservice.othersubdomain.commandapi.PaymentDeclined')
            header('reply_outcome-type', 'FAILURE')
        }
    }
}
