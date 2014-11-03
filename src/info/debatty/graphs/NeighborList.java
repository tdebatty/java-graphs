package info.debatty.graphs;

import info.debatty.util.BoundedPriorityQueue;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author tibo
 */
public class NeighborList implements Iterable<Neighbor> {
    public static final String DELIMITER = ";;;";

    protected BoundedPriorityQueue neighbors;

    public NeighborList() {
        neighbors = new BoundedPriorityQueue<Neighbor>();
    }

    public NeighborList(int size) {
        neighbors = new BoundedPriorityQueue<Neighbor>(size);
    }

    public synchronized boolean add(Neighbor neighbor) {
        return neighbors.add(neighbor);
    }

    @Override
    public String toString() {
        if (neighbors.isEmpty()) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (Object n : neighbors) {
            builder.append( ((Neighbor)n).toString() ).append(DELIMITER);
        }
        builder.delete(builder.length()-3, Integer.MAX_VALUE);
        
        return builder.toString();
    }
    
    public static NeighborList parseString(String string) {
        String[] values = string.split(DELIMITER);
        NeighborList nl = new NeighborList();
        for (String s : values) {
            try {
                nl.add(Neighbor.parseString(s));
            } catch (Exception ex) {
                System.out.println("Failed to parse " + string);
            }
        }
        return nl;
    }

    public boolean contains(Neighbor n) {
        return neighbors.contains(n);
    }

    @Override
    public Iterator<Neighbor> iterator() {
        return neighbors.iterator();
    }

    public int size() {
        return neighbors.size();
    }
    
    
    /**
     * Count common values between this NeighborList and the other.
     * Both neighborlists are not modified.
     * 
     * @param other_nl
     * @return 
     */
    public int CountCommonValues(NeighborList other_nl) {
        //NeighborList copy = (NeighborList) other.clone();
        ArrayList other_values = new ArrayList();
        for (Neighbor n : other_nl) {
            other_values.add(n.node.value);
        }
        
        int count = 0;
        for (Object n : this.neighbors) {
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
}
