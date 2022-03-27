package me.elijuh.soup.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class MathUtil {
    public boolean chance(double chance) {
        return ThreadLocalRandom.current().nextDouble(100) < chance;
    }
}
