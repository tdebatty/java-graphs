/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
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

import info.debatty.java.graphs.SimilarityInterface;

/**
 * Wraps a similarity interface with a counter, so we can check how many
 * similarities were computed.
 * @author Thibault Debatty
 * @param <T> Type of nodes in the graph
 */
public class SimilarityCounter<T> implements SimilarityInterface<T> {
    private final SimilarityInterface<T> similarity;
    private int count = 0;

    /**
     *
     * @param similarity
     */
    public SimilarityCounter(final SimilarityInterface<T> similarity) {
        this.similarity = similarity;
    }

    /**
     * {@inheritDoc}
     *
     * @param node1
     * @param node2
     * @return
     */
    public final double similarity(final T node1, final T node2) {
        count++;
        return similarity.similarity(node1, node2);
    }

    /**
     * Get the number of computed similarities.
     * @return
     */
    public final int getCount() {
        return count;
    }

}
