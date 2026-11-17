package de.chaos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImagePixel {
    private final int x;
    private final int y;
    private final String colourType;
    private final boolean weighted;
}
