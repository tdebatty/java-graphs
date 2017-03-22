/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.debatty.java.graphs;

/**
 * Represent a weighted edge (a link from node n1 to node n2).
 * Internally, the edge is a source node plus a neighbor.
 *
 * @author Thibault Debatty
 * @param <T> Type of the nodes
 */
public class Edge<T> {

    private static final int HASH_BASE = 3;
    private static final int HASH_MULT = 83;

    private final T origin;
    private final Neighbor<T> neighbor;

    /**
     * Build an edge between n1 and neighbor.node.
     * @param origin
     * @param neighbor
     */
    public Edge(final T origin, final Neighbor<T> neighbor) {
        this.origin = origin;
        this.neighbor = neighbor;
    }

    /**
     * Get the origin of the edge (the source node).
     * @return
     */
    public final T getOrigin() {
        return origin;
    }

    /**
     * Get the neighbor.
     * @return
     */
    public final Neighbor<T> getNeighbor() {
        return neighbor;
    }



    @Override
    public final String toString() {
        return origin.toString() + " => " + neighbor.toString();
    }

    @Override
    public final int hashCode() {
        int hash = HASH_BASE;
        hash = HASH_MULT * hash + this.origin.hashCode();
        hash = HASH_MULT * hash + this.neighbor.hashCode();
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Edge<?> other = (Edge<?>) obj;

        if (!this.origin.equals(other.origin)) {
            return false;
        }

        if (!this.neighbor.equals(other.neighbor)) {
            return false;
        }

        return true;
    }
}
