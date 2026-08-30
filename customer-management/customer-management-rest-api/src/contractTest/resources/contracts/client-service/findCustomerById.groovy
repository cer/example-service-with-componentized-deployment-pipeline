package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "returns the details of an existing customer"
    request {
        method GET()
        url '/customers/11111111-1111-1111-1111-111111111111'
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                customerId: '11111111-1111-1111-1111-111111111111',
                name: 'Fred',
                creditLimit: [amount: 15.00]
        )
    }
}
