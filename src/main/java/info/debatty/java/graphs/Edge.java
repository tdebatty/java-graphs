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
 */
public class Edge<T> {

    public T n1;
    public Neighbor<T> neighbor;

    public Edge() {

    }

    public Edge(T n1, Neighbor<T> neighbor) {
        this.n1 = n1;
        this.neighbor = neighbor;
    }

    @Override
    public String toString() {
        return n1.toString() + " => " + neighbor.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.n1 != null ? this.n1.hashCode() : 0);
        hash = 83 * hash + (this.neighbor != null ? this.neighbor.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge<?> other = (Edge<?>) obj;
        if (this.n1 != other.n1 && (this.n1 == null || !this.n1.equals(other.n1))) {
            return false;
        }
        if (this.neighbor != other.neighbor && (this.neighbor == null || !this.neighbor.equals(other.neighbor))) {
            return false;
        }
        return true;
    }


}
