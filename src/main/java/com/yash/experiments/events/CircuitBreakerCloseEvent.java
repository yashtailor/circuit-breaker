package com.yash.experiments.events;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CircuitBreakerCloseEvent {
    String topic;
}
