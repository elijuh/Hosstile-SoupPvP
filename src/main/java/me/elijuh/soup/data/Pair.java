package me.elijuh.soup.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair <X, Y> {
    private final X x;
    private final Y y;
}
