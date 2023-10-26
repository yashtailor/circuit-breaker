package com.yash.experiments.models;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Data
public class CircuitBreakerStatus {
    String name;
    String status;
    CircuitBreakerError error;
}
