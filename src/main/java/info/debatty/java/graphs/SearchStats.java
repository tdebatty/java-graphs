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
 * This object will contain additional stats produced by fastSearch algorithm
 * (real number of computed similarities, restarts, restarts because of
 * cross-partition edges).
 *
 * @author Thibault Debatty
 */
public class SearchStats {
    private int computed_similarities;
    private int restarts;
    private int cross_partition_restarts;

    /**
     *
     * @return
     */
    public final int getComputedSimilarities() {
        return computed_similarities;
    }

    /**
     *
     * @return
     */
    public final int getRestarts() {
        return restarts;
    }

    /**
     *
     * @return
     */
    public final int getCrossPartitionRestarts() {
        return cross_partition_restarts;
    }

    final void incComputedSimilarities() {
        computed_similarities++;
    }

    final void incRestarts() {
        restarts++;
    }

    final void incCrossPartitionRestarts() {
        cross_partition_restarts++;
    }

}
