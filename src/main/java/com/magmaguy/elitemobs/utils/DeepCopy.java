package com.magmaguy.elitemobs.utils;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class DeepCopy {

    /**
     * Makes a deep copy of any Java object that is passed.
     */
    public static Object copyObject(Serializable object) {
        try {
            return SerializationUtils.clone(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
