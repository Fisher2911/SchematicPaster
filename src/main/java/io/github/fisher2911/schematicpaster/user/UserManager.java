package io.github.fisher2911.schematicpaster.user;

import io.github.fisher2911.fisherlib.user.CoreUserManager;
import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager implements CoreUserManager<SchematicUser> {

    private final SchematicPasterPlugin plugin;
    private final Map<UUID, SchematicUser> users;

    public UserManager(SchematicPasterPlugin plugin, Map<UUID, SchematicUser> users) {
        this.plugin = plugin;
        this.users = users;
    }

    @Nullable
    public SchematicUser forceGet(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) return SchematicUserImpl.CONSOLE;
        if (sender instanceof final Player player) {
            final UUID uuid = player.getUniqueId();
            return this.forceGet(uuid);
        }
        return null;
    }

    @Override
    public Optional<SchematicUser> get(UUID uuid) {
        return Optional.ofNullable(this.users.get(uuid));
    }

    @Override
    public SchematicUser forceGet(UUID uuid) {
        return this.users.get(uuid);
    }

    public void add(Player player) {
        this.users.put(player.getUniqueId(), new SchematicUserImpl(player, new HashSet<>()));
    }

    public void remove(UUID uuid) {
        this.users.remove(uuid);
    }

}
