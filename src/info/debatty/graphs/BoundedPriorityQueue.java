package info.debatty.graphs;

import java.util.PriorityQueue;

/**
 *
 * @author tibo
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> {
    protected int CAPACITY = Integer.MAX_VALUE;
    
    public static void main(String [] args) {
        BoundedPriorityQueue<Integer> q = new BoundedPriorityQueue(4);
        q.add(1);
        q.add(2);
        q.add(3);
        q.add(4);
        q.add(5);
        q.add(6);
        
        for (Integer i : q) {
            System.out.println(i);
        }
    }
    
    public BoundedPriorityQueue(int capacity) {
        super();
        this.CAPACITY = capacity;
    }

    public BoundedPriorityQueue() {
        super();
    }
    
    /**
     * Return true if the element was added
     * @param element
     * @return 
     */
    @Override
    public boolean add(E element) {
        if (this.contains(element)) {
            return false;
        }
        
        if (this.size() < CAPACITY) {
            return super.add(element);
        }
        
        if (! (element instanceof java.lang.Comparable) ) {
            throw new ClassCastException();
        }
                
        if (((Comparable) element).compareTo(this.peek()) > 0) {
            this.poll();
            return super.add(element);
        }
        
        return false;
    }
}