package io.github.fisher2911.schematicpaster.util;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class DirectionUtil {

    public static int getFacing(BlockFace facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    public static int getFacing(Player player) {
        return getFacing(player.getFacing());
    }


}
