package de.chaos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ImageResult {
    final String baseName;
    final List<ImagePixel> pixels;
    final StringBuilder output;

}
