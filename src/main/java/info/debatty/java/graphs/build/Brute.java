package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.Neighbor;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Brute<T> extends GraphBuilder<T> {

    @Override
    protected Graph<T> _computeGraph(List<Node<T>> nodes) {

        int n = nodes.size();

        // Initialize all NeighborLists
        Graph<T> graph = new Graph<T>();
        for (Node node : nodes) {
            graph.put(node, new NeighborList(k));
        }

        computed_similarities = 0;
        double sim;
        Node n1;
        Node n2;
        HashMap<String, Object> data = new HashMap<String, Object>();

        for (int i = 0; i < n; i++) {

            n1 = nodes.get(i);
            for (int j = 0; j < i; j++) {
                n2 = nodes.get(j);
                sim = similarity.similarity((T) n1.value, (T) n2.value);
                computed_similarities++;

                graph.get(n1).add(new Neighbor(n2, sim));
                graph.get(n2).add(new Neighbor(n1, sim));
            }

            if (callback != null) {
                data.put("node_id", n1.id);
                data.put("computed_similarities", computed_similarities);
                callback.call(data);

            }
        }

        return graph;
    }
}
