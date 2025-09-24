package com.example.hong.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class HateSpeechResponseDto {

    @JsonProperty("is_hate_speech")
    private boolean isHateSpeech;

    @JsonProperty("predicted_labels")
    private List<String> predictedLabels;
}