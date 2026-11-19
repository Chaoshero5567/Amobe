package de.chaos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImagePixel {
    private final double x;
    private final double y;
    private final String colourType;
    private final boolean weighted;
    private final double weight;
}
