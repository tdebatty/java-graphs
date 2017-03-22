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

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Neighbor of an edge (stores the other node, and the similarity).
 *
 * @author Thibault Debatty
 * @param <T> Type of nodes
 */
public class Neighbor<T>
        implements Comparable, Serializable {

    private final T node;
    private final double similarity;

    /**
     *
     * @param node
     * @param similarity
     */
    public Neighbor(final T node, final double similarity) {
        this.node = node;
        this.similarity = similarity;
    }

    /**
     *
     * @return
     */
    public final T getNode() {
        return node;
    }

    /**
     *
     * @return
     */
    public final double getSimilarity() {
        return similarity;
    }


    /**
     *
     * @return (node.toString(),similarity)
     */
    @Override
    public final String toString() {
        return "(" + node.toString() + "," + similarity + ")";
    }

    /**
     * A neighbor has no reference to the origin node, hence only neighbors
     * from the same origin can be compared.
     * @param other
     * @return
     */
    @Override
    public final boolean equals(final Object other) {
        if (!other.getClass().getName().equals(this.getClass().getName())) {
            return false;
        }

        Neighbor other_neighbor = (Neighbor) other;
        return this.node.equals(other_neighbor.node);
    }

    @Override
    public final int hashCode() {
        return this.node.hashCode();
    }

    /**
     * This > other if this.similarity > other.similarity.
     * @param other
     * @return
     */
    @Override
    public final int compareTo(final Object other) {
        if (other == null) {
            return 1;
        }

        if (!other.getClass().isInstance(this)) {
            throw new InvalidParameterException();
        }

        if (((Neighbor) other).node.equals(this.node)) {
            return 0;
        }

        if (this.similarity == ((Neighbor) other).similarity) {
            return 0;
        }

        if (this.similarity > ((Neighbor) other).similarity) {
            return 1;
        }

        return -1;
    }
}
