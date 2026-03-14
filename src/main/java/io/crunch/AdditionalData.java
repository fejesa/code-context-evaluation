package io.crunch;

import lombok.Value;

@Value
public class AdditionalData {

    int expectedWaitTimeSeconds;

    int queueSize;

    int totalSlots;

    double throughputRatio;

    double offsetRatio;

    int maxWaitTimeSeconds;
}
