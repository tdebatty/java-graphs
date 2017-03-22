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

    /**
     *
     * @param capacity
     */
    public SynchronizedBoundedPriorityQueue(final int capacity) {
        queue = new BoundedPriorityQueue<E>(capacity);
    }

    @Override
    public final Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public final int size() {
        synchronized (this) {
            return queue.size();
        }
    }

    /**
     * {@inheritDoc}
     * @param e
     * @return
     */
    public final boolean offer(final E e) {
        synchronized (this) {
            return queue.offer(e);
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public final E poll() {
        synchronized (this) {
            return queue.poll();
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public final E peek() {
        synchronized (this) {
            return queue.peek();
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public final boolean isEmpty() {
        synchronized (this) {
            return queue.isEmpty();
        }
    }

    /**
     * {@inheritDoc}
     * @param o
     * @return
     */
    public final boolean contains(final Object o) {
        synchronized (this) {
            return queue.contains(o);
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public final Object[] toArray() {
        synchronized (this) {
            return queue.toArray();
        }
    }

    /**
     * {@inheritDoc}
     * @param <T>
     * @param a
     * @return
     */
    public final <T> T[] toArray(final T[] a) {
        synchronized (this) {
            return queue.toArray(a);
        }
    }

    /**
     * {@inheritDoc}
     * @param e
     * @return
     */
    public final boolean add(final E e) {
        synchronized (this) {
            return queue.add(e);
        }
    }

    /**
     * {@inheritDoc}
     * @param o
     * @return
     */
    public final boolean remove(final Object o) {
        synchronized (this) {
            return queue.remove(o);
        }
    }

    /**
     * {@inheritDoc}
     * @param c
     * @return
     */
    public final boolean containsAll(final Collection<?> c) {
        synchronized (this) {
            return queue.containsAll(c);
        }
    }

    /**
     * {@inheritDoc}
     * @param c
     * @return
     */
    public final boolean addAll(final Collection<? extends E> c) {
        synchronized (this) {
            return queue.addAll(c);
        }
    }

    /**
     * {@inheritDoc}
     * @param c
     * @return
     */
    public final boolean removeAll(final Collection<?> c) {
        synchronized (this) {
            return queue.removeAll(c);
        }
    }

    /**
     * {@inheritDoc}
     * @param c
     * @return
     */
    public final boolean retainAll(final Collection<?> c) {
        synchronized (this) {
            return queue.retainAll(c);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void clear() {
        synchronized (this) {
            queue.clear();
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public final E remove() {
        synchronized (this) {
            return queue.remove();
        }
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public final E element() {
        synchronized (this) {
            return queue.element();
        }
    }

    /**
     * {@inheritDoc}
     * @param other
     * @return
     */
    @Override
    public final boolean equals(final Object other) {
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

    @Override
    public final int hashCode() {
        synchronized (this) {
            return this.queue.hashCode();
        }
    }

    /**
     *
     * @return
     */
    @Override
    public final String toString() {
        synchronized (this) {
            return queue.toString();
        }
    }
}
