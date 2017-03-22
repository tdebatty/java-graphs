package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.Neighbor;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.SimilarityInterface;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class Brute<T> extends GraphBuilder<T> {

    @Override
    protected final Graph<T> computeGraph(
            final List<T> nodes,
            final int k,
            final SimilarityInterface<T> similarity) {

        int n = nodes.size();

        // Initialize all NeighborLists
        Graph<T> graph = new Graph<T>();
        for (T node : nodes) {
            graph.put(node, new NeighborList(k));
        }

        for (int i = 0; i < n; i++) {

            T n1 = nodes.get(i);
            for (int j = 0; j < i; j++) {
                T n2 = nodes.get(j);
                double sim = similarity.similarity(n1, n2);

                graph.getNeighbors(n1).add(new Neighbor(n2, sim));
                graph.getNeighbors(n2).add(new Neighbor(n1, sim));
            }
        }

        return graph;
    }
}
