package info.debatty.java.graphs;

import java.io.Serializable;

/**
 *
 * @author tibo
 * @param <T>
 */
public interface SimilarityInterface<T> extends Serializable {

    public double similarity(T node1, T node2);
}
