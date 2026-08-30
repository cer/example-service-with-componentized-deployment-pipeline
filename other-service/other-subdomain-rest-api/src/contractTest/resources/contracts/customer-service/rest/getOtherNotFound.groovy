package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "returns 404 when the other does not exist"
    request {
        method GET()
        url '/others/unknown'
    }
    response {
        status NOT_FOUND()
    }
}
