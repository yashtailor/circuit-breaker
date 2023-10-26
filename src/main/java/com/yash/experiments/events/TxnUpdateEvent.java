package com.yash.experiments.events;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TxnUpdateEvent {
    String txnId;
    String status;
}
