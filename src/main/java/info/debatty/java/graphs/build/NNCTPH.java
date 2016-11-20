/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
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
package info.debatty.java.graphs.build;

import info.debatty.java.spamsum.ESSum;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the k-nn graph by partitioning the graph using Context Triggered
 * Piecewize Hashing. This graph builder is meant for string node values.
 *
 * @author Thibault Debatty
 */
public class NNCTPH<T> extends PartitioningGraphBuilder<T> {

    @Override
    protected final List<T>[] _partition(
            final List<T> nodes) {
        ESSum ess = new ESSum(oversampling, n_partitions, 1);

        ArrayList<T>[] buckets = new ArrayList[n_partitions];

        for (T node : nodes) {
            int[] hash = ess.HashString(node.toString());

            for (int stage = 0; stage < oversampling; stage++) {
                int partition = hash[stage];

                if (buckets[partition] == null) {
                    buckets[partition] = new ArrayList<T>();
                }

                // !! this is not efficient !!!!
                if (!buckets[partition].contains(node)) {
                    buckets[partition].add(node);
                }
            }
        }

        return buckets;
    }
}
