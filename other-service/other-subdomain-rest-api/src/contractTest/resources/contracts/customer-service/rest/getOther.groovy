package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "returns the details of an existing other"
    request {
        method GET()
        url '/others/other-1'
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                otherId: 'other-1',
                name: 'Fred'
        )
    }
}
