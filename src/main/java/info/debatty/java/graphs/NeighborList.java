package info.debatty.java.graphs;

import info.debatty.java.util.BoundedPriorityQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Bounded list of neighbors
 * @author Thibault Debatty
 */
public class NeighborList extends BoundedPriorityQueue<Neighbor> implements Serializable {
    
    public static ArrayList<Edge> Convert2Edges(HashMap<Node, NeighborList> graph) {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        
        for (Map.Entry<Node, NeighborList> pair : graph.entrySet()) {
            for (Neighbor neighbor : pair.getValue()) {
                edges.add(new Edge(pair.getKey(), neighbor.node, neighbor.similarity));
                
            }
        }
        
        return edges;
    }

    public NeighborList(int size) {
        super(size);
    }
    
    /**
     * Count the values (using node.value) that are present in both neighborlists.
     * Both neighborlists are not modified.
     * 
     * @param other_nl
     * @return the numver of values that are present in both neighborlists
     */
    public int CountCommonValues(NeighborList other_nl) {
        //NeighborList copy = (NeighborList) other.clone();
        ArrayList other_values = new ArrayList();
        for (Neighbor n : other_nl) {
            other_values.add(n.node.value);
        }
        
        int count = 0;
        for (Neighbor n : this) {
            Object this_value = ((Neighbor) n).node.value;
            
            for (Object other_value : other_values) {
                if ( other_value.equals(this_value)) {
                    count++;
                    other_values.remove(other_value);
                    break;
                }
            }
        }
        
        return count;
    }
    
    /**
     * Count the nodes (based on node.id) that are present in both neighborlists.
     * @param other_nl
     * @return 
     */
    public int CountCommons(NeighborList other_nl) {
        int count = 0;
        for (Neighbor n : this) {
            if (other_nl.contains(n)) {
                count++;
            }
        }
        return count;
    }
}
