package xyz.tomsoz.pluginBase.common.redis;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Payload {
    @Nullable
    @Expose
    private Notification notification;

    /**
     * Returns an empty cross-server message payload.
     *
     * @return an empty payload
     */
    @NotNull
    public static Payload empty() {
        return new Payload();
    }

    /**
     * Returns a payload containing a message and a recipient
     *
     * @param target  the player to send the message to, null to broadcast
     * @param message the message to send
     * @return a payload containing the message
     */
    @NotNull
    public static Payload withNotification(@Nullable UUID target, @NotNull Component message) {
        final Payload payload = new Payload();
        payload.notification = new Notification(target, message);
        return payload;
    }

    public Optional<Notification> getNotification() {
        return Optional.ofNullable(notification);
    }
}
