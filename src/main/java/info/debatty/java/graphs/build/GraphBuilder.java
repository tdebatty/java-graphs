package info.debatty.java.graphs.build;

import info.debatty.java.graphs.CallbackInterface;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.SimilarityInterface;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tibo
 * @param <t>
 */
public abstract class GraphBuilder<t> implements Cloneable {
    protected int k = 10;
    protected SimilarityInterface<t> similarity;
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

    public void setSimilarity(SimilarityInterface<t> similarity) {
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
    
    public HashMap<Node<t>, NeighborList> computeGraph(List<Node<t>> nodes) {
        if (nodes.isEmpty()) {
            throw new InvalidParameterException("Nodes list is empty");
        }
        
        if (similarity == null) {
            throw new InvalidParameterException("Similarity is not defined");
        }
        computed_similarities = 0;
        
        return _computeGraph(nodes);
        
    }
    
    public static List<Node<String>> readFile(String path) {
        try {
            FileReader fileReader;
            fileReader = new FileReader(path);
            
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<Node<String>> nodes = new ArrayList<Node<String>>();
            String line = null;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                nodes.add(new Node(String.valueOf(i), line));
                i++;
            }
            bufferedReader.close();
            return  nodes;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected abstract HashMap<Node<t>, NeighborList> _computeGraph(List<Node<t>> nodes);
}
