package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Edge;
import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class ThreadedNNDescent<T> extends AbstractNNDescent<T> {

    // Internal state, used by worker objects
    private SimilarityInterface<T> similarity;
    private volatile int thread_count;
    private volatile List<T> nodes;
    private volatile Graph<T> graph;
    private volatile HashMap<T, ArrayList<T>>  old_lists_2, new_lists_2;

    // Multiple threads will write these at the same time
    private volatile ConcurrentHashMap<T, ArrayList<T>> old_lists, new_lists;

    @Override
    protected final Graph<T> nndescent(
            final List<T> nodes,
            final SimilarityInterface<T> similarity) {

        // Create worker threads
        thread_count = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService executor = Executors.newFixedThreadPool(thread_count);

        // Initialize state...
        this.similarity = similarity;
        this.nodes = nodes;
        this.graph = new Graph<T>();
        this.graph.setK(getK());
        this.old_lists = new ConcurrentHashMap<T, ArrayList<T>>(nodes.size());
        this.new_lists = new ConcurrentHashMap<T, ArrayList<T>>(nodes.size());


        // B[v]←− Sample(V,K)×{?∞, true?} ∀v ∈ V
        // For each node, create a random neighborlist
        for (T v : nodes) {
            graph.put(v, randomNeighborList(nodes, v));
        }

        int iterations = 0;

        // loop
        while (true) {
            iterations++;

            // for v ∈ V do
            // old[v]←− all items in B[v] with a false flag
            // new[v]←− ρK items in B[v] with a true flag
            // Mark sampled items in B[v] as false;
            for (int i = 0; i < nodes.size(); i++) {
                T v = nodes.get(i);
                old_lists.put(v, pickFalses(v, graph.getNeighbors(v)));
                new_lists.put(v, pickTruesAndMark(v, graph.getNeighbors(v)));
            }

            // old′ ←Reverse(old)
            // new′ ←Reverse(new)
            old_lists_2 = reverse(nodes, old_lists);
            new_lists_2 = reverse(nodes, new_lists);

            ArrayList<Future<Integer>> list = new ArrayList<Future<Integer>>();
            // Start threads...
            for (int t = 0; t < thread_count; t++) {
                list.add(executor.submit(new ThreadedNNDescent.NNThread(t)));
            }


            int c = 0;
            for (Future<Integer> future : list) {
                try {
                    c += future.get();
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            }

            if (c <= (getDelta() * nodes.size() * getK())) {
                break;
            }

            if (iterations >= getMaxIterations()) {
                break;
            }
        }

        executor.shutdown();

        // Clear local state
        this.new_lists = null;
        this.new_lists_2 = null;
        this.nodes = null;
        this.old_lists = null;
        this.old_lists_2 = null;

        return graph;
    }

    @Override
    protected final Set<Edge> getSetInstance(final int size) {
        return Collections.synchronizedSet(new HashSet<Edge>(size));
    }

    /**
     *
     */
    class NNThread implements Callable<Integer> {

        private final int slice;

        NNThread(final int slice) {
            this.slice = slice;
        }

        @Override
        public Integer call() {
            int c = 0;
            // for v ∈ V do
            int start = slice * nodes.size() / thread_count;
            int end = (slice + 1) * nodes.size() / thread_count;

            // Last slice should go to the end...
            if (slice == (thread_count - 1)) {
                end = nodes.size();
            }

            for (int i = start; i < end; i++) {
                T v = nodes.get(i);
                // old[v]←− old[v] ∪ Sample(old′[v], ρK)
                // new[v]←− new[v] ∪ Sample(new′[v], ρK)
                old_lists.put(v,
                        union(
                                old_lists.get(v),
                                sample(
                                        old_lists_2.get(v),
                                        (int) (getRho() * getK()))));
                new_lists.put(v,
                        union(
                                new_lists.get(v),
                                sample(
                                        new_lists_2.get(v),
                                        (int) (getRho() * getK()))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    T u1 = new_lists.get(v).get(j);

                    for (int k = j + 1; k < new_lists.get(u1).size(); k++) {
                        T u2 = new_lists.get(u1).get(k);
                        //int u2_i = Find(u2);

                        // l←− σ(u1,u2)
                        // c←− c+UpdateNN(B[u1], u2, l, true)
                        // c←− c+UpdateNN(B[u2], u1, l, true)
                        double s = similarity.similarity(u1, u2);
                        c += updateNL(graph.getNeighbors(u1), u2, s);
                        c += updateNL(graph.getNeighbors(u2), u1, s);
                    }

                    // or u1 ∈ new[v], u2 ∈ old[v] do
                    for (int k = 0; k < old_lists.get(v).size(); k++) {
                        T u2 = old_lists.get(v).get(k);

                        if (u1.equals(u2)) {
                            continue;
                        }

                        //int u2_i = Find(u2);
                        double s = similarity.similarity(u1, u2);
                        c += updateNL(graph.getNeighbors(u1), u2, s);
                        c += updateNL(graph.getNeighbors(u2), u1, s);
                    }
                }
            }
            return c;
        }
    }
}
