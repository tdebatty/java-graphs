package info.debatty.graphs;

import info.debatty.util.BoundedPriorityQueue;
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

    public boolean add(Neighbor neighbor) {
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
    
    public int CountCommons(NeighborList other) throws CloneNotSupportedException {
        NeighborList copy = (NeighborList) other.clone();
        int count = 0;
        for (Object n : this.neighbors) {
            Object this_value = ((Neighbor) n).node.value;
            
            for (Object other_n : copy.neighbors) {
                if ( ((Neighbor) other_n).node.value.equals(this_value)) {
                    count++;
                    copy.neighbors.remove(other_n);
                    break;
                }
            }
        }
        
        return count;
    }

    public String NodeIds() {
        StringBuilder sb = new StringBuilder();
        for (Object n : neighbors) {
            sb.append( ((Neighbor) n).node.id);
            sb.append(";");
        }
        
        return sb.toString();
    }    
}
