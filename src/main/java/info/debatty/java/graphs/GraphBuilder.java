package info.debatty.java.graphs;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author tibo
 */
public abstract class GraphBuilder {
    protected int k = 10;
    protected SimilarityInterface similarity;
    protected CallbackInterface callback = null;
    protected int computed_similarities = 0;
    
    public int getK() {
        return k;
    }

    /**
     * Define k the number of edges per node.
     * Default value is 10
     * @param k 
     */
    public void setK(int k) {
        if (k <=0) {
            throw new InvalidParameterException("k must be > 0");
        }
        this.k = k;
    }

    public SimilarityInterface getSimilarity() {
        return similarity;
    }

    public void setSimilarity(SimilarityInterface similarity) {
        this.similarity = similarity;
    }

    public CallbackInterface getCallback() {
        return callback;
    }

    public void setCallback(CallbackInterface callback) {
        this.callback = callback;
    }

    public int getComputedSimilarities() {
        return computed_similarities;
    }
    
    public HashMap<Node, NeighborList> computeGraph(List<Node> nodes) {
        if (nodes.isEmpty()) {
            throw new InvalidParameterException("Nodes list is empty");
        }
        
        if (similarity == null) {
            throw new InvalidParameterException("Similarity is not defined");
        }
        
        return _computeGraph(nodes);
        
    }

    protected abstract HashMap<Node, NeighborList> _computeGraph(List<Node> nodes);
}
