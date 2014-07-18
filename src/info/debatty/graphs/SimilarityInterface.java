package info.debatty.graphs;

import java.io.Serializable;

/**
 *
 * @author tibo
 */
public interface SimilarityInterface extends Serializable {
    public double similarity(Node n1, Node n2);
}
