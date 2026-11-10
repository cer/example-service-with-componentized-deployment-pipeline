package contracts

org.springframework.cloud.contract.spec.Contract.make {
    label 'authorizePaymentSuccessReply'
    description "other-service tells customer-service that payment was authorized"
    input {
        triggeredBy('authorizePaymentSuccessReply()')
    }

    outputMessage {
        sentTo('authorizePaymentReply')
        body([])
        headers {
            header('reply_type', 'io.eventuate.otherservice.othersubdomain.commandapi.PaymentAuthorized')
            header('reply_outcome-type', 'SUCCESS')
        }
    }
}
