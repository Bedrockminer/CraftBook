package com.sk89q.craftbook.sponge.util;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockUtil {

    /**
     * Gets the relative direction of 'other' from 'base'.
     *
     * @param base The location of base.
     * @param other The location of other.
     * @return The relative direction
     */
    public static Direction getFacing(Location base, Location other) {
        for (Direction dir : Direction.values()) {
            if (base.getRelative(dir).getPosition().equals(other.getPosition()))
                return dir;
        }

        return null;
    }

    private static Direction[] directFaces = null;

    /**
     * Get faces that are directly touching the block.
     *
     * @return Faces that are directly touching the block.
     */
    public static Direction[] getDirectFaces() {
        if(directFaces == null)
            directFaces = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return directFaces;
    }

    public static List<Location> getAdjacentExcept(Location location, Direction ... directions) {
        List<Location> locations = new ArrayList<>();

        for(Direction direction : getDirectFaces()) {
            boolean passes = true;
            for(Direction direction1 : directions) {
                if(direction1 == direction) {
                    passes = false;
                    break;
                }
            }
            if (passes) {
                locations.add(location.getRelative(direction));
            }
        }

        return locations;
    }

    public static boolean doesStatePassFilters(Collection<BlockFilter> filters, BlockState state) {
        for(BlockFilter filter : filters)
            for(BlockState blockState : filter.getApplicableBlockStates())
                if(blockState.equals(state))
                    return true;
        return false;
    }

    /**
     * Gets the length of a line of blocks, with a maximum length.
     *
     * @param startBlock The starting location
     * @param testState The block that the line is made of
     * @param direction The direction of the line from the starting block
     * @param maximum The maximum length
     * @return The found length
     */
    public static int getLength(Location startBlock, BlockState testState, Direction direction, int maximum) {
        int length = 0;

        while(length < maximum) {
            if(startBlock.getBlock().equals(testState)) {
                length ++;
                startBlock = startBlock.getRelative(direction);
            } else {
                break;
            }
        }

        return length;
    }

    public static int getMinimumLength(Location firstBlock, Location secondBlock, BlockState testState, Direction direction, int maximum) {
        return Math.min(getLength(firstBlock, testState, direction, maximum), getLength(secondBlock, testState, direction, maximum));
    }
}
