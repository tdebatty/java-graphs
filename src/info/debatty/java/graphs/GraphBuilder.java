/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package info.debatty.java.graphs;

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
    protected int computed_similaritites = 0;
    
    public int getK() {
        return k;
    }

    public void setK(int k) {
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

    public int getComputedSimilaritites() {
        return computed_similaritites;
    }

    public abstract HashMap<Node, NeighborList> computeGraph(List<Node> nodes);
}
