package info.debatty.java.util;

import java.util.PriorityQueue;

/**
 * This class implements a bounded priority queue
 * A structure that always keeps the n 'largest' elements
 * 
 * @author Thibault Debatty
 * @param <E>
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> {
    
    protected int CAPACITY = Integer.MAX_VALUE;
    
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
     * 
     * @param element
     * @return true if element was added
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