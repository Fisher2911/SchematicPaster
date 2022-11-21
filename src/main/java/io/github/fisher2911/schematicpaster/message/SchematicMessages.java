package io.github.fisher2911.schematicpaster.message;

import io.github.fisher2911.fisherlib.message.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SchematicMessages {

    private static final Map<String, Message> messages = new HashMap<>();

    public static final Message PLACED_SCHEMATIC = path("placed-schematic");
    public static final Message NO_PERMISSION_TO_PLACE_SCHEMATIC = path("no-permission-to-place-schematic");
    public static final Message SCHEMATIC_BUILD_COMPLETE = path("schematic-build-complete");
    public static final Message MAX_TASKS = path("max-tasks");
    public static final Message SCHEMATIC_ITEM_NOT_FOUND = path("schematic-item-not-found");
    public static final Message PLAYER_NOT_FOUND = path("player-not-found");
    public static final Message GAVE_ITEM = path("gave-item");
    public static final Message GIVEN_ITEM = path("given-item");

    public static Collection<Message> values() {
        return messages.values();
    }

    private static Message path(String path) {
        final Message message = Message.path(path);
        messages.put(message.getConfigPath(), message);
        return message;
    }

}
