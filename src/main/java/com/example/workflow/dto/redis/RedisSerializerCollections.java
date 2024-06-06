package com.example.workflow.dto.redis;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class RedisSerializerCollections {

    private static class RolePermissionSerializer implements RedisSerializer<RedisSerializers.RolePermission>{

        @Override
        public byte[] serialize(RedisSerializers.RolePermission antPaths) throws SerializationException {
            return antPaths == null?new byte[0]:antPaths.toByteArray();
        }

        @Override
        public RedisSerializers.RolePermission deserialize(byte[] bytes) throws SerializationException {
            try {
                return bytes == null || bytes.length == 0 ?null:RedisSerializers.RolePermission.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final RedisSerializer<RedisSerializers.RolePermission> ROLE_PERMISSION_REDIS_SERIALIZER = new RolePermissionSerializer();
}
