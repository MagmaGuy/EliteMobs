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
            //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
            //outputStrm.writeObject(object);
            //ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            //ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            //return objInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
