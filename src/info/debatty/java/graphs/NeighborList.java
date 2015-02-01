package info.debatty.java.graphs;

import info.debatty.java.util.BoundedPriorityQueue;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author tibo
 */
public class NeighborList extends BoundedPriorityQueue<Neighbor> implements Serializable {

    public NeighborList(int size) {
        super(size);
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
}
