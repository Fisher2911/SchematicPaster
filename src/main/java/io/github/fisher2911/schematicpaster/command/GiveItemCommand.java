package io.github.fisher2911.schematicpaster.command;

import io.github.fisher2911.fisherlib.command.CommandSenderType;
import io.github.fisher2911.fisherlib.util.NumberUtil;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import io.github.fisher2911.schematicpaster.message.SchematicMessages;
import io.github.fisher2911.schematicpaster.schematic.SchematicBuilderManager;
import io.github.fisher2911.schematicpaster.user.SchematicUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiveItemCommand extends SchematicBaseCommand {

    private final SchematicBuilderManager schematicBuilderManager;

    public GiveItemCommand(SchematicPasterPlugin plugin, @Nullable SchematicBaseCommand parent, Map<String, SchematicBaseCommand> subCommands) {
        super(plugin, parent, "give", "<id> [amount] [player]", SchematicPermissions.ADMIN_GIVE_PERMISSION, CommandSenderType.PLAYER, 1, 3, subCommands);
        this.schematicBuilderManager = plugin.getSchematicBuilderManager();
    }

    @Override
    public void execute(SchematicUser user, String[] args, String[] previousArgs) {
        final String id = args[0];
        if (args.length == 1) {
            this.schematicBuilderManager.giveItem(user, user, id, 1);
            return;
        }
        final Integer amount = NumberUtil.integerValueOf(args[1]);
        if (amount == null) {
            this.sendHelp(user);
            return;
        }
        final SchematicUser target;
        if (args.length == 3) {
            final Player player = this.plugin.getServer().getPlayer(args[2]);
            if (player == null) {
                target = null;
            } else {
                target = this.plugin.getUserManager().forceGet(player);
            }
            if (target == null) {
                this.messageHandler.sendMessage(
                        user,
                        SchematicMessages.PLAYER_NOT_FOUND
                );
                return;
            }
        } else {
            target = user;
        }
        this.schematicBuilderManager.giveItem(user, target, id, amount);
    }

    @Override
    public @Nullable List<String> getTabs(SchematicUser user, String[] args, String[] previousArgs, boolean defaultTabIsNull) {
        List<String> tabs = super.getTabs(user, args, previousArgs, defaultTabIsNull);
        if (tabs == null) tabs = new ArrayList<>();
        if (args.length != 1) return tabs.isEmpty() && defaultTabIsNull ? null : tabs;
        final String arg = args[0];
        for (String id : this.schematicBuilderManager.getBuilderIds()) {
            if (id.startsWith(arg)) {
                tabs.add(id);
            }
        }
        return tabs;
    }

}
