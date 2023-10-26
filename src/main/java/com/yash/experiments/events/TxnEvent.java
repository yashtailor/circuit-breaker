package com.yash.experiments.events;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TxnEvent {
    String txnId;
    String creditorAccountNm;
    String debtorAccountNm;
    Integer amount;
}
