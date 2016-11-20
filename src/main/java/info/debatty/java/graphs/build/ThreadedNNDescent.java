package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class ThreadedNNDescent<T> extends NNDescent<T> {

    // Internal state, used by worker objects
    private volatile int thread_count;
    private volatile List<T> nodes;
    private volatile Graph<T> graph;
    private volatile HashMap<T, ArrayList<T>>  old_lists_2, new_lists_2;

    // Multiple threads will write these at the same time
    private volatile ConcurrentHashMap<T, ArrayList<T>> old_lists, new_lists;

    @Override
    protected final Graph<T> _computeGraph(final List<T> nodes) {
        // Create worker threads
        thread_count = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService executor = Executors.newFixedThreadPool(thread_count);

        iterations = 0;

        if (nodes.size() <= (k + 1)) {
            return MakeFullyLinked(nodes);
        }

        // Initialize state...
        this.nodes = nodes;
        this.graph = new Graph<T>();
        this.graph.setK(getK());
        this.old_lists = new ConcurrentHashMap<T, ArrayList<T>>(nodes.size());
        this.new_lists = new ConcurrentHashMap<T, ArrayList<T>>(nodes.size());

        HashMap<String, Object> data = new HashMap<String, Object>();

        // B[v]←− Sample(V,K)×{?∞, true?} ∀v ∈ V
        // For each node, create a random neighborlist
        for (T v : nodes) {
            graph.put(v, RandomNeighborList(nodes, v));
        }

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
                old_lists.put(v, PickFalses(graph.getNeighbors(v)));
                new_lists.put(v, PickTruesAndMark(graph.getNeighbors(v)));
            }

            // old′ ←Reverse(old)
            // new′ ←Reverse(new)
            old_lists_2 = Reverse(nodes, old_lists);
            new_lists_2 = Reverse(nodes, new_lists);

            ArrayList<Future<Integer>> list = new ArrayList<Future<Integer>>();
            // Start threads...
            for (int t = 0; t < thread_count; t++) {
                list.add(executor.submit(new ThreadedNNDescent.NNThread(t)));
            }

            for (Future<Integer> future : list) {
                try {
                    c += future.get();
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            }

            //System.out.println("C : " + c);
            if (callback != null) {
                data.put("c", c);
                data.put("computed_similarities", computed_similarities);
                data.put("iterations", iterations);
                data.put("computed_similarities_ratio",
                        (double) computed_similarities
                                / (nodes.size() * (nodes.size() - 1) / 2));
                callback.call(data);
            }

            if (c <= (delta * nodes.size() * k)) {
                break;
            }

            if (iterations >= max_iterations) {
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
                        Union(
                                old_lists.get(v),
                                Sample(old_lists_2.get(v), (int) (rho * k))));
                new_lists.put(v,
                        Union(
                                new_lists.get(v),
                                Sample(new_lists_2.get(v), (int) (rho * k))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    T u1 = new_lists.get(v).get(j);

                    for (int k = j + 1; k < new_lists.get(u1).size(); k++) {
                        T u2 = new_lists.get(u1).get(k);
                        //int u2_i = Find(u2);

                        // l←− σ(u1,u2)
                        // c←− c+UpdateNN(B[u1], u2, l, true)
                        // c←− c+UpdateNN(B[u2], u1, l, true)
                        double s = Similarity(u1, u2);
                        c += UpdateNL(graph.getNeighbors(u1), u2, s);
                        c += UpdateNL(graph.getNeighbors(u2), u1, s);
                    }

                    // or u1 ∈ new[v], u2 ∈ old[v] do
                    for (int k = 0; k < old_lists.get(v).size(); k++) {
                        T u2 = old_lists.get(v).get(k);

                        if (u1.equals(u2)) {
                            continue;
                        }

                        //int u2_i = Find(u2);
                        double s = Similarity(u1, u2);
                        c += UpdateNL(graph.getNeighbors(u1), u2, s);
                        c += UpdateNL(graph.getNeighbors(u2), u1, s);
                    }
                }
            }
            return c;
        }
    }
}
