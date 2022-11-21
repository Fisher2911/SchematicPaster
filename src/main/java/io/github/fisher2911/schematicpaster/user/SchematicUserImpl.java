package io.github.fisher2911.schematicpaster.user;

import io.github.fisher2911.fisherlib.world.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SchematicUserImpl implements SchematicUser {

    public static ConsoleSchematicUser CONSOLE = new ConsoleSchematicUser();

    private final UUID uuid;
    private final String name;
    @Nullable
    private Player player;
    private final Set<Integer> currentTasks;

    public SchematicUserImpl(Player player, Set<Integer> currentTasks) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.currentTasks = currentTasks;
    }

    public SchematicUserImpl(UUID uuid, String name, Set<Integer> currentTasks) {
        this.uuid = uuid;
        this.name = name;
        this.player = Bukkit.getPlayer(uuid);
        this.currentTasks = currentTasks;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getId() {
        return this.uuid;
    }

    @Override
    public @Nullable Player getPlayer() {
        return this.player;
    }

    @Override
    public @Nullable OfflinePlayer getOfflinePlayer() {
        if (this.player == null) return Bukkit.getOfflinePlayer(this.uuid);
        return this.player;
    }

    @Override
    public boolean isOnline() {
        return this.player != null;
    }

    @Override
    public void takeMoney(double amount) {

    }

    @Override
    public void addMoney(double amount) {

    }

    @Override
    public double getMoney() {
        return 0;
    }

    @Override
    public boolean hasPermission(String permission) {
        if (this.player == null) return false;
        return this.player.hasPermission(permission);
    }

    @Override
    public Map<Integer, ItemStack> getInventory() {
        if (this.player == null || !this.player.isOnline()) return Collections.emptyMap();
        final Map<Integer, ItemStack> inventoryMap = new HashMap<>();
        final Inventory inventory = this.player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventoryMap.put(0, inventory.getItem(slot));
        }
        return inventoryMap;
    }

    @Override
    public @Nullable WorldPosition getPosition() {
        if (this.player == null) return null;
        return WorldPosition.fromLocation(this.player.getLocation());
    }

    @Override
    public void onJoin(Player player) {
        this.player = player;
    }

    @Override
    public void onQuit() {
        this.player = null;
    }

    @Override
    public Set<Integer> getCurrentTasks() {
        return currentTasks;
    }

    @Override
    public void addTask(int id) {
        this.currentTasks.add(id);
    }

    @Override
    public void removeTask(int id) {
        this.currentTasks.remove(id);
    }

}
