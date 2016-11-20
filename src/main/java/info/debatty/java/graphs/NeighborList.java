package info.debatty.java.graphs;

import info.debatty.java.util.BoundedPriorityQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Bounded list of neighbors.
 *
 * @author Thibault Debatty
 */
public class NeighborList extends BoundedPriorityQueue<Neighbor>
        implements Serializable {

    public static <T> ArrayList<Edge>
        Convert2Edges(HashMap<T, NeighborList> graph) {
        ArrayList<Edge> edges = new ArrayList<Edge>();

        for (Map.Entry<T, NeighborList> pair : graph.entrySet()) {
            for (Neighbor<T> neighbor : pair.getValue()) {
                edges.add(
                        new Edge(
                                pair.getKey(),
                                neighbor.node,
                                neighbor.similarity));

            }
        }

        return edges;
    }

    /**
     * Create a new neighborlist of given size.
     * @param size size of the neighborlist
     */
    public NeighborList(final int size) {
        super(size);
    }

    /**
     * Count the nodes (based on node.id) that are present in both
     * neighborlists.
     *
     * @param other_nl
     * @return
     */
    public final int countCommonNodes(final NeighborList other_nl) {
        int count = 0;
        for (Neighbor n : this) {
            if (other_nl.contains(n)) {
                count++;
            }
        }
        return count;
    }

    // double has 15 significant digits
    private static final double EPSILON = 1E-12;

    /**
     * Count the number of equivalent neighbors (using similarities).
     * @param other
     * @return
     */
    public final int countCommons(final NeighborList other) {
        // Make a copy of both neighborlists
        PriorityQueue<Neighbor> copy_this = new PriorityQueue<Neighbor>(this);
        PriorityQueue<Neighbor> copy_other = new PriorityQueue<Neighbor>(other);

        int count = 0;
        Neighbor this_neighbor = copy_this.poll();
        Neighbor other_neighbor = copy_other.poll();

        while (true) {
            if (this_neighbor == null || other_neighbor == null) {
                // We reached the end of at least one neighborlist
                break;
            }

            double delta = this_neighbor.similarity - other_neighbor.similarity;
            if (delta < EPSILON && delta > -EPSILON) {
                count++;
                this_neighbor = copy_this.poll();
                other_neighbor = copy_other.poll();

            } else if (this_neighbor.similarity > other_neighbor.similarity) {
                other_neighbor = copy_other.poll();

            } else {
                this_neighbor = copy_this.poll();
            }
        }

        return count;
    }

    /**
     * Returns true if this neighborlist contains a neighbor corresponding to
     * this node.
     * @param node
     * @return
     */
    public final <T> boolean containsNode(final T node) {
        for (Neighbor n : this) {
            if (n.node.equals(node)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove from the neighborlist the neighbor corresponding to this node.
     * @param node
     * @return true if a neighbor was effectively removed from the list.
     */
    public final <T> boolean removeNode(final T node) {
        for (Neighbor n : this) {
            if (n.node.equals(node)) {
                this.remove(n);
                return true;
            }
        }
        return false;
    }
}
