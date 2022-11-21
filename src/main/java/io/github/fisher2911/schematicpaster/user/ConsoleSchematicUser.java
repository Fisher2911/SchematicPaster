package io.github.fisher2911.schematicpaster.user;

import io.github.fisher2911.fisherlib.world.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ConsoleSchematicUser implements SchematicUser {

    public static final UUID CONSOLE_ID = UUID.randomUUID();

    protected ConsoleSchematicUser() {}

    @Override
    public UUID getId() {
        return CONSOLE_ID;
    }

    @Override
    public String getName() {
        return "console";
    }

    @Override
    public @Nullable Player getPlayer() {
        return null;
    }

    @Override
    public @Nullable OfflinePlayer getOfflinePlayer() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
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
        return Bukkit.getConsoleSender().hasPermission(permission);
    }

    @Override
    public Map<Integer, ItemStack> getInventory() {
        return Collections.emptyMap();
    }

    @Override
    public @Nullable WorldPosition getPosition() {
        return null;
    }

    @Override
    public void onJoin(Player player) {

    }

    @Override
    public void onQuit() {

    }

    @Override
    public Set<Integer> getCurrentTasks() {
        return Collections.emptySet();
    }

    @Override
    public void addTask(int id) {

    }

    @Override
    public void removeTask(int id) {

    }

}
