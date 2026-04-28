package io.eventuate.customerservice.customermanagement.restapi;

public class ReserveCreditResponse {

    private String status;

    public ReserveCreditResponse() {
    }

    public ReserveCreditResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
