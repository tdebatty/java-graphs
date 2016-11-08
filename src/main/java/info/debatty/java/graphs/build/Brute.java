package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.Neighbor;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.NodeInterface;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Brute<T extends NodeInterface<U>, U> extends GraphBuilder<T, U> {

    @Override
    protected final Graph<T, U> _computeGraph(final List<T> nodes) {

        int n = nodes.size();

        // Initialize all NeighborLists
        Graph<T, U> graph = new Graph<T, U>();
        for (NodeInterface node : nodes) {
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
                sim = similarity.similarity(n1.getValue(), n2.getValue());
                computed_similarities++;

                graph.get(n1).add(new Neighbor(n2, sim));
                graph.get(n2).add(new Neighbor(n1, sim));
            }

            if (callback != null) {
                callback_data.put("node_id", n1.getId());
                callback_data.put(
                        "computed_similarities",
                        computed_similarities);
                callback.call(callback_data);

            }
        }

        return graph;
    }
}
