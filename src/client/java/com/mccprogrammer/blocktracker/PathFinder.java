package com.mccprogrammer.blocktracker;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class PathFinder {

    private static final int MAX_DEPTH = 1000; // Limit search
    private static final int MAX_DISTANCE = 100; // Limit distance

    /**
     * Finds a path from start to end using A*.
     * Returns a list of BlockPos representing the path, or empty list if no path
     * found.
     * This should proceed in a separate thread if possible, but for simplicity we
     * run it synchronously with limits.
     */
    public static List<BlockPos> findPath(BlockPos start, BlockPos end) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null)
            return Collections.emptyList();

        if (start.equals(end))
            return Collections.singletonList(start);
        if (start.getSquaredDistance(end) > MAX_DISTANCE * MAX_DISTANCE)
            return Collections.emptyList();

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<BlockPos, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, null, 0, start.getSquaredDistance(end));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;

        while (!openSet.isEmpty()) {
            if (iterations++ > MAX_DEPTH)
                break;

            Node current = openSet.poll();
            if (current.pos.equals(end) || current.pos.getSquaredDistance(end) < 2) {
                return reconstructPath(current);
            }

            for (BlockPos neighborPos : getNeighbors(current.pos)) {
                if (!isWalkable(world, neighborPos))
                    continue;

                double gCost = current.gCost + 1;
                Node neighbor = allNodes.get(neighborPos);

                if (neighbor == null) {
                    neighbor = new Node(neighborPos, current, gCost, neighborPos.getSquaredDistance(end));
                    allNodes.put(neighborPos, neighbor);
                    openSet.add(neighbor);
                } else if (gCost < neighbor.gCost) {
                    neighbor.gCost = gCost;
                    neighbor.parent = current;
                    // re-sort (simple way is remove and add, though inefficient)
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        }

        return Collections.emptyList();
    }

    private static List<BlockPos> reconstructPath(Node node) {
        List<BlockPos> path = new ArrayList<>();
        while (node != null) {
            path.add(node.pos);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        neighbors.add(pos.north());
        neighbors.add(pos.south());
        neighbors.add(pos.east());
        neighbors.add(pos.west());
        neighbors.add(pos.up());
        neighbors.add(pos.down());
        return neighbors;
    }

    private static boolean isWalkable(ClientWorld world, BlockPos pos) {
        // Simple check: Block must be air or passable, and block below must be solid.
        // This is a very basic verification.
        BlockState state = world.getBlockState(pos);
        BlockState below = world.getBlockState(pos.down());

        return !state.isSolidBlock(world, pos) && below.isSolidBlock(world, pos.down());
    }

    private static class Node {
        BlockPos pos;
        Node parent;
        double gCost;
        double hCost;
        double fCost;

        Node(BlockPos pos, Node parent, double gCost, double hCost) {
            this.pos = pos;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
}
