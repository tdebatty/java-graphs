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
package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Edge;
import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of NN-Descent k-nn graph building algorithm. Based on the
 * paper "Efficient K-Nearest Neighbor Graph Construction for Generic Similarity
 * Measures" by Dong et al. http://www.cs.princeton.edu/cass/papers/www11.pdf
 *
 * NN-Descent works by iteratively exploring the neighbors of neighbors... It is
 * not suitable for small datasets (less than 500 items)!
 *
 * @author Thibault Debatty
 * @param <T> The type of nodes
 */
public class NNDescent<T> extends AbstractNNDescent<T> {

    @Override
    protected final Graph<T> nndescent(
            final List<T> nodes,
            final SimilarityInterface<T> similarity) {

        Graph<T> neighborlists = new Graph<T>(nodes.size());

        HashMap<T, ArrayList<T>> old_lists =
                new HashMap<T, ArrayList<T>>(nodes.size());
        HashMap<T, ArrayList<T>> new_lists =
                new HashMap<T, ArrayList<T>>(nodes.size());


        // B[v]←− Sample(V,K)×{?∞, true?} ∀v ∈ V
        // For each node, create a random neighborlist
        for (T v : nodes) {
            neighborlists.put(v, randomNeighborList(nodes, v));
        }

        int c = 0;
        int iterations = 0;

        // loop
        while (true) {
            iterations++;
            c = 0;

            // for v ∈ V do
            // old[v]←− all items in B[v] with a false flag
            // new[v]←− ρK items in B[v] with a true flag
            // Mark sampled items in B[v] as false;
            for (int i = 0; i < nodes.size(); i++) {
                T v = nodes.get(i);
                old_lists.put(v, pickFalses(v, neighborlists.getNeighbors(v)));
                new_lists.put(v,
                        pickTruesAndMark(v, neighborlists.getNeighbors(v)));

            }

            // old′ ←Reverse(old)
            // new′ ←Reverse(new)
            HashMap<T, ArrayList<T>> old_lists_2 = reverse(nodes, old_lists);
            HashMap<T, ArrayList<T>> new_lists_2 = reverse(nodes, new_lists);

            // for v ∈ V do
            for (int i = 0; i < nodes.size(); i++) {
                T v = nodes.get(i);
                // old[v]←− old[v] ∪ Sample(old′[v], ρK)
                // new[v]←− new[v] ∪ Sample(new′[v], ρK)
                old_lists.put(v, union(
                        old_lists.get(v),
                        sample(old_lists_2.get(v), (int) (getRho() * getK()))));
                new_lists.put(v, union(
                        new_lists.get(v),
                        sample(new_lists_2.get(v), (int) (getRho() * getK()))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    T u1 = new_lists.get(v).get(j);

                    //int u1_i = Find(u1); // position of u1 in nodes
                    for (int l = j + 1; l < new_lists.get(u1).size(); l++) {
                        T u2 = new_lists.get(u1).get(l);
                            //int u2_i = Find(u2);

                        // l←− σ(u1,u2)
                        // c←− c+UpdateNN(B[u1], u2, l, true)
                        // c←− c+UpdateNN(B[u2], u1, l, true)
                        double s = similarity.similarity(u1, u2);
                        c += updateNL(neighborlists.getNeighbors(u1), u2, s);
                        c += updateNL(neighborlists.getNeighbors(u2), u1, s);
                    }

                    // or u1 ∈ new[v], u2 ∈ old[v] do
                    for (int l = 0; l < old_lists.get(v).size(); l++) {
                        T u2 = old_lists.get(v).get(l);

                        if (u1.equals(u2)) {
                            continue;
                        }

                        //int u2_i = Find(u2);
                        double s = similarity.similarity(u1, u2);
                        c += updateNL(neighborlists.getNeighbors(u1), u2, s);
                        c += updateNL(neighborlists.getNeighbors(u2), u1, s);
                    }
                }
            }

            if (c <= (getDelta() * nodes.size() * getK())) {
                break;
            }

            if (iterations >= getMaxIterations()) {
                break;
            }
        }

        return neighborlists;
    }

    @Override
    protected final Set<Edge> getSetInstance(final int size) {
        return new HashSet<Edge>(size);
    }
}
