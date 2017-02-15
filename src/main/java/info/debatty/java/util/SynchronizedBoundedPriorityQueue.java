/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package info.debatty.java.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 *
 * @author Thibault Debatty
 * @param <E>
 */
public class SynchronizedBoundedPriorityQueue<E>
        implements Serializable, Iterable<E>, Collection<E>, Queue<E> {

    private final BoundedPriorityQueue<E> queue;

    public SynchronizedBoundedPriorityQueue(final int capacity) {
        queue = new BoundedPriorityQueue<E>(capacity);
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public int size() {
        synchronized (this) {
            return queue.size();
        }
    }

    public boolean offer(E e) {
        synchronized (this) {
            return queue.offer(e);
        }
    }

    public E poll() {
        synchronized (this) {
            return queue.poll();
        }
    }

    public E peek() {
        synchronized (this) {
            return queue.peek();
        }
    }

    public boolean isEmpty() {
        synchronized (this) {
            return queue.isEmpty();
        }
    }

    public boolean contains(Object o) {
        synchronized (this) {
            return queue.contains(o);
        }
    }

    public Object[] toArray() {
        synchronized (this) {
            return queue.toArray();
        }
    }

    public <T> T[] toArray(T[] a) {
        synchronized (this) {
            return queue.toArray(a);
        }
    }

    public boolean add(E e) {
        synchronized (this) {
            return queue.add(e);
        }
    }

    public boolean remove(Object o) {
        synchronized (this) {
            return queue.remove(o);
        }
    }

    public boolean containsAll(Collection<?> c) {
        synchronized (this) {
            return queue.containsAll(c);
        }
    }

    public boolean addAll(Collection<? extends E> c) {
        synchronized (this) {
            return queue.addAll(c);
        }
    }

    public boolean removeAll(Collection<?> c) {
        synchronized (this) {
            return queue.removeAll(c);
        }
    }

    public boolean retainAll(Collection<?> c) {
        synchronized (this) {
            return queue.retainAll(c);
        }
    }

    public void clear() {
        synchronized (this) {
            queue.clear();
        }
    }

    public E remove() {
        synchronized (this) {
            return queue.remove();
        }
    }

    public E element() {
        synchronized (this) {
            return queue.element();
        }
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!other.getClass().isInstance(this)) {
            return false;
        }

        SynchronizedBoundedPriorityQueue<E> other_synced =
                (SynchronizedBoundedPriorityQueue<E>) other;

        synchronized (this) {
            return queue.equals(other_synced.queue);
        }
    }

    public String toString() {
        synchronized (this) {
            return queue.toString();
        }
    }
}
