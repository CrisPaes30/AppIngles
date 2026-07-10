package com.englishmemory.dto.response;

import com.englishmemory.enums.CefrLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CefrProgressResponse {

    private CefrLevel level;
    private String levelDescription;
    private long totalWords;
    private double averageMastery;
}
