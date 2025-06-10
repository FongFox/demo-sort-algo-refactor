package com.nhs2304.demosortalgo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Data model representing one sorting history record.
 */
public class HistoryEntry {
    private final SimpleStringProperty algorithm;
    private final SimpleStringProperty inputArray;
    private final SimpleStringProperty outputArray;
    private final SimpleStringProperty startTime;
    private final SimpleStringProperty formattedDuration;
    private final SimpleIntegerProperty swapCount;
    private final SimpleIntegerProperty iValue;

    public HistoryEntry(
            String algorithm,
            String inputArray,
            String outputArray,
            long durationMillis,
            int swapCount,
            String startTime,
            int iValue
    ) {
        this.algorithm = new SimpleStringProperty(algorithm);
        this.inputArray = new SimpleStringProperty(inputArray);
        this.outputArray = new SimpleStringProperty(outputArray);
        this.formattedDuration = new SimpleStringProperty(formatDuration(durationMillis));
        this.swapCount = new SimpleIntegerProperty(swapCount);
        this.startTime = new SimpleStringProperty(startTime);
        this.iValue = new SimpleIntegerProperty(iValue);
    }

    @JsonCreator
    public HistoryEntry(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("inputArray") String inputArray,
            @JsonProperty("outputArray") String outputArray,
            @JsonProperty("formattedDuration") String formattedDuration,
            @JsonProperty("swapCount") int swapCount,
            @JsonProperty("startTime") String startTime,
            @JsonProperty("ivalue") int iValue) {
        this.algorithm = new SimpleStringProperty(algorithm);
        this.inputArray = new SimpleStringProperty(inputArray);
        this.outputArray = new SimpleStringProperty(outputArray);
        this.formattedDuration = new SimpleStringProperty(formattedDuration);
        this.swapCount = new SimpleIntegerProperty(swapCount);
        this.startTime = new SimpleStringProperty(startTime);
        this.iValue = new SimpleIntegerProperty(iValue);
    }

    public String getAlgorithm() {
        return algorithm.get();
    }

    public String getInputArray() {
        return inputArray.get();
    }

    public String getOutputArray() {
        return outputArray.get();
    }

    public String getStartTime() {
        return startTime.get();
    }

    public String getFormattedDuration() {
        return formattedDuration.get();
    }

    public int getSwapCount() {
        return swapCount.get();
    }

    public int getIValue() {
        return iValue.get();
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long ms = millis % 1000;
        return seconds + "s " + ms + "ms";
    }
}
