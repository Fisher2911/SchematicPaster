package io.github.fisher2911.schematicpaster.command;

import io.github.fisher2911.fisherlib.command.CommandSenderType;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicCommand extends SchematicBaseCommand implements TabExecutor, TabCompleter {

    public SchematicCommand(SchematicPasterPlugin plugin, Map<String, SchematicBaseCommand> subCommands) {
        super(plugin, null, "schematicpaster", null, CommandSenderType.ANY, -1, -1, subCommands);
        this.addSubCommand(new GiveItemCommand(plugin, this, new HashMap<>()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        this.handleArgs(sender, args, new String[0]);
        return true;
    }

    @Override
    public void execute(SchematicUser user, String[] args, String[] previous) {
        this.sendHelp(user);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final SchematicUser user = this.plugin.getUserManager().forceGet(sender);
        if (user == null) return null;
        return this.getTabs(user, args, new String[0], false);
    }

}
