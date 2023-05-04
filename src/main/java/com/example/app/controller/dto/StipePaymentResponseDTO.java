package com.example.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.app.service.stripe.StripePaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Map;

@Data
public class StipePaymentResponseDTO {

    private String message;
    private Map<String, Object> chargeJson;

    public StipePaymentResponseDTO(StripePaymentStatus paymentStatus, String message) throws JsonProcessingException {
        this.message = message;
        this.chargeJson = new ObjectMapper().readValue(paymentStatus.getCharge().toJson(),
                new TypeReference<Map<String, Object>>() {});
    }

    @JsonAnyGetter
    public Map<String, Object> getChargeJson(){
        return this.chargeJson;
    }
}
