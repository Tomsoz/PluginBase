package xyz.tomsoz.pluginBase.common.serialization;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class BaseGsonCodec extends BaseCodec {
    private static final Logger LOGGER = Logger.getLogger(BaseGsonCodec.class.getName());
    protected Gson gson;
    protected Gson baseGson;
    private final Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    private final Encoder encoder = in -> {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try (ByteBufOutputStream os = new ByteBufOutputStream(out)) {
            Class<?> clazz = in.getClass();

            try {
                String json = gson.toJson(in, clazz);
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
                byte[] typeBytes = clazz.getName().getBytes(StandardCharsets.UTF_8);

                os.writeInt(jsonBytes.length);
                os.write(jsonBytes);

                os.writeInt(typeBytes.length);
                os.write(typeBytes);

                return os.buffer();
            } catch (Exception e) {
                LOGGER.severe("Failed to encode object of type " + clazz.getName() + ": " + e.getMessage());
                throw e;
            }
        } catch (IOException e) {
            out.release();
            throw e;
        } catch (Exception e) {
            out.release();
            throw new IOException("Encoding failed", e);
        }
    };

    private final Decoder<Object> decoder = (buf, state) -> {
        try (ByteBufInputStream is = new ByteBufInputStream(buf)) {
            int jsonLen = is.readInt();
            byte[] jsonBytes = new byte[jsonLen];
            is.readFully(jsonBytes);
            String json = new String(jsonBytes, StandardCharsets.UTF_8);

            int typeLen = is.readInt();
            byte[] typeBytes = new byte[typeLen];
            is.readFully(typeBytes);
            String type = new String(typeBytes, StandardCharsets.UTF_8);

            try {
                Class<?> clazz = getClassFromType(type);
                return gson.fromJson(json, clazz);
            } catch (Exception e) {
                LOGGER.severe("Failed to decode object of type " + type + " from JSON: " + json + ". Error: " + e.getMessage());
                throw e;
            }
        }
    };

    public Class<?> getClassFromType(String name) {
        Class<?> clazz = classMap.get(name);
        if (clazz == null) {
            try {
                clazz = Class.forName(name);
                classMap.put(name, clazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find class named " + name, e);
            }
        }
        return clazz;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (gson.getClass().getClassLoader() != null) {
            return gson.getClass().getClassLoader();
        }
        return super.getClassLoader();
    }
}
