package com.stephanofer.networkplatform.paper.config;

import java.util.Map;

public interface ConfigTypeAdapter<T> {

    Map<Object, Object> serialize(T object);

    T deserialize(Map<Object, Object> serialized);
}
