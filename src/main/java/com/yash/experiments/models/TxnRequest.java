package com.yash.experiments.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxnRequest {
    @JsonProperty("creditor")
    String creditorAccountNm;

    @JsonProperty("debtor")
    String debtorAccountNm;

    @JsonProperty("amount")
    Integer amount;
}
