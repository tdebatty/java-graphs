package info.debatty.util;

import java.util.PriorityQueue;

/**
 * This class implements a bounded priority queue
 * A structure that always keeps the n 'largest' elements
 * 
 * @author tibo
 * @param <E>
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> {
    protected int CAPACITY = Integer.MAX_VALUE;
    
    public static void main(String [] args) {
        BoundedPriorityQueue<Integer> q = new BoundedPriorityQueue(4);
        q.add(1);
        q.add(4);
        q.add(5);
        q.add(6);
        q.add(2);
        
        for (Integer i : q) {
            System.out.println(i);
        }
    }
    
    /**
     * Create a bounded priority queue with given maximum capacity
     * @param capacity 
     */
    public BoundedPriorityQueue(int capacity) {
        super();
        this.CAPACITY = capacity;
    }

    /**
     * Creates a priority queue with maximum capacity Integer.MAX_VALUE
     */
    public BoundedPriorityQueue() {
        super();
    }
    
    /**
     * When the queue is full, adds the element if it is larger than the smallest
     * element already in the queue.
     * 
     * It the element is not comparable, throws a ClassCastException
     * Returns true if the element was added
     * @param element
     * @return 
     */
    @Override
    public boolean add(E element) {
        if (! (element instanceof java.lang.Comparable) ) {
            throw new ClassCastException();
        }
        
        if (this.contains(element)) {
            return false;
        }
        
        if (this.size() < CAPACITY) {
            return super.add(element);
        }
                
        if (((Comparable) element).compareTo(this.peek()) > 0) {
            this.poll();
            return super.add(element);
        }
        
        return false;
    }
}