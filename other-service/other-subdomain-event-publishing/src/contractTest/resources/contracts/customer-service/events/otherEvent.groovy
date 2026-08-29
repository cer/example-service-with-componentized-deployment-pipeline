package contracts;

org.springframework.cloud.contract.spec.Contract.make {
    label 'otherEvent'
    input {
        triggeredBy('otherEvent()')
    }

    outputMessage {
        sentTo('io.eventuate.otherservice.othersubdomain.domain.OtherAggregate')
        body([
                orderId: 101
        ])
        headers {
            header('event-aggregate-type', 'io.eventuate.otherservice.othersubdomain.domain.OtherAggregate')
            header('event-type', 'io.eventuate.otherservice.othersubdomain.domain.OtherEvent')
            header('event-aggregate-id', '99')
        }
    }
}
