package me.elijuh.soup.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtil {

    public <T> T nullOrDef(T object, T def) {
        return object == null ? def : object;
    }
}
