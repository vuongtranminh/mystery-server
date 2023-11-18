package com.vuong.app.redis.serializer;

import com.google.protobuf.Message;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ProtobufSerializer<T extends Message> implements RedisSerializer<T> {

    private static final ConcurrentHashMap<Class<?>, Method> methodCache = new ConcurrentHashMap<Class<?>, Method>();

    private final Class<T> clazz;

    public ProtobufSerializer(Class<T> clazz){
        this.clazz= clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return (t == null) ? null : ((Message) t).toByteArray();
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        T t = null;
        if (bytes != null) {
            try {
                t = parseFrom(clazz, bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    /**
     * Create a new {@code Message.Builder} instance for the given class.
     * <p>This method uses a ConcurrentHashMap for caching method lookups.
     */
    private T parseFrom(Class<? extends Message> clazz,byte[] bytes) throws Exception {
        Method method = methodCache.get(clazz);
        if (method == null) {
            method = clazz.getMethod("parseFrom", byte[].class);
            methodCache.put(clazz, method);
        }
        return (T) method.invoke(clazz, bytes);
    }
}
