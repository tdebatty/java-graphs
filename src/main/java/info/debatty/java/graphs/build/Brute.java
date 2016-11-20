package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.Neighbor;
import info.debatty.java.graphs.NeighborList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Brute<T> extends GraphBuilder<T> {

    @Override
    protected final Graph<T> _computeGraph(final List<T> nodes) {

        int n = nodes.size();

        // Initialize all NeighborLists
        Graph<T> graph = new Graph<T>();
        for (T node : nodes) {
            graph.put(node, new NeighborList(k));
        }

        computed_similarities = 0;
        double sim;
        T n1;
        T n2;
        HashMap<String, Object> callback_data = new HashMap<String, Object>();

        for (int i = 0; i < n; i++) {

            n1 = nodes.get(i);
            for (int j = 0; j < i; j++) {
                n2 = nodes.get(j);
                sim = similarity.similarity(n1, n2);
                computed_similarities++;

                graph.getNeighbors(n1).add(new Neighbor(n2, sim));
                graph.getNeighbors(n2).add(new Neighbor(n1, sim));
            }

            if (callback != null) {
                callback_data.put("node_id", n1);
                callback_data.put(
                        "computed_similarities",
                        computed_similarities);
                callback.call(callback_data);

            }
        }

        return graph;
    }
}
