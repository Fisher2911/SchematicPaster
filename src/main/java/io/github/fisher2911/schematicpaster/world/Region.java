package io.github.fisher2911.schematicpaster.world;

import io.github.fisher2911.fisherlib.world.ChunkPos;
import io.github.fisher2911.fisherlib.world.Position;

import java.util.Collection;
import java.util.UUID;

public interface Region {

    UUID getWorld();
    Position getMin();
    Position getMax();

    /**
     *
     * @param position
     * @return if the position is in the region, including the border
     */
    boolean contains(Position position);

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return if the position is in the region, including the border
     */
    boolean contains(double x, double  y, double  z);

    boolean isOnBorder(Position position);
    boolean isOnBorder(double  x, double  y, double  z);

    Collection<ChunkPos> getChunks();

    boolean intersects(Region region);

}
