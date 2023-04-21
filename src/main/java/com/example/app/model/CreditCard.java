package com.example.app.model;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class CreditCard {

    @Pattern(regexp =
            "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}" +
                    "|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]" +
                    "{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131" +
                    "|1800|35\\d{3})\\d{11})$")
    private String number;
    @Length(min = 4, max = 4)
    private String expirationYear;
    @Length(min = 1, max = 2)
    private String expirationMonth;
    @Length(min = 3, max = 3)
    private String cvc;
}
