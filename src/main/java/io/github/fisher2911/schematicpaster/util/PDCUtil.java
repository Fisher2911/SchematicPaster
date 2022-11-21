package io.github.fisher2911.schematicpaster.util;

import io.github.fisher2911.schematicpaster.SchematicPasterPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PDCUtil {

    private static final SchematicPasterPlugin PLUGIN = SchematicPasterPlugin.getPlugin(SchematicPasterPlugin.class);

    public static final NamespacedKey SCHEMATIC_BUILDER_ID = new NamespacedKey(PLUGIN, "schematic_builder_id");
    public static final NamespacedKey PASTING_SCHEMATIC_IDS = new NamespacedKey(PLUGIN, "pasting_schematic_ids");

    public static boolean setSchematicBuilderId(ItemStack itemStack, String id) {
        if (itemStack == null) return false;
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;
        itemMeta.getPersistentDataContainer().set(SCHEMATIC_BUILDER_ID, PersistentDataType.STRING, id);
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    @Nullable
    public static String getSchematicBuilderId(ItemStack itemStack) {
        if (itemStack == null) return null;
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;
        return itemMeta.getPersistentDataContainer().get(SCHEMATIC_BUILDER_ID, PersistentDataType.STRING);
    }

    public static void setChunksPastingSchematics(PersistentDataContainer container, Collection<Integer> ids) {
        final int[] idArray = new int[ids.size()];
        int i = 0;
        for (final int id : ids) {
            idArray[i] = id;
            i++;
        }
        container.set(PASTING_SCHEMATIC_IDS, PersistentDataType.INTEGER_ARRAY, idArray);
    }

    public static int[] getChunksPastingSchematics(PersistentDataContainer container) {
        if (!container.has(PASTING_SCHEMATIC_IDS, PersistentDataType.INTEGER_ARRAY)) {
            return new int[0];
        }
        return container.get(PASTING_SCHEMATIC_IDS, PersistentDataType.INTEGER_ARRAY);
    }

    public static void addChunkPastingSchematic(PersistentDataContainer container, int id) {
        final int[] ids = getChunksPastingSchematics(container);
        final Set<Integer> newIds = new HashSet<>();
        for (int i : ids) {
            newIds.add(i);
        }
        newIds.add(id);
        setChunksPastingSchematics(container, newIds);
    }

    public static void removeChunkPastingSchematics(PersistentDataContainer container, int id) {
        final Set<Integer> ids = new HashSet<>();
        for (int i : getChunksPastingSchematics(container)) {
            if (i != id) {
                ids.add(i);
            }
        }
        setChunksPastingSchematics(container, ids);
    }

}
