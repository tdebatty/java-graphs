package info.debatty.java.graphs;

import java.io.Serializable;

/**
 *
 * @author tibo
 * @param <T>
 */
public interface SimilarityInterface<T> extends Serializable {

    /**
     * Compute the similarity between two nodes.
     * @param node1
     * @param node2
     * @return
     */
    double similarity(final T node1, final T node2);
}
