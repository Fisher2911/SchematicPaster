package io.github.fisher2911.schematicpaster.command;

import io.github.fisher2911.fisherlib.command.BaseCommand;
import io.github.fisher2911.fisherlib.command.CommandSenderType;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class SchematicBaseCommand extends BaseCommand<SchematicUser, SchematicPasterPlugin, SchematicBaseCommand> {

    public SchematicBaseCommand(SchematicPasterPlugin plugin, @Nullable SchematicBaseCommand parent, String name, @Nullable String dynamicArgs, @Nullable String permission, CommandSenderType senderType, int minArgs, int maxArgs, Map<String, SchematicBaseCommand> subCommands) {
        super(plugin, parent, name, dynamicArgs, permission, senderType, minArgs, maxArgs, subCommands, plugin.getSchematicSettings());
    }

    public SchematicBaseCommand(SchematicPasterPlugin plugin, @Nullable SchematicBaseCommand parent, String name, @Nullable String permission, CommandSenderType senderType, int minArgs, int maxArgs, Map<String, SchematicBaseCommand> subCommands) {
        super(plugin, parent, name, permission, senderType, minArgs, maxArgs, subCommands, plugin.getSchematicSettings());
    }

}
