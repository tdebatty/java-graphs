/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class BoundedPriorityQueueTest extends TestCase {

    /**
     * Test of add method, of class BoundedPriorityQueue.
     */
    public final void testAdd() {
        System.out.println("add");
        BoundedPriorityQueue<Integer> instance = new BoundedPriorityQueue(4);
        instance.add(1);
        instance.add(4);
        instance.add(5);
        instance.add(6);
        instance.add(2);
        instance.add(0);
        assertEquals(instance.size(), 4);
        assertEquals((int) instance.peek(), 2);
    }

    /**
     * Test of getCapacity method, of class BoundedPriorityQueue.
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void testSerializeQueue()
            throws IOException, ClassNotFoundException {

        System.out.println("Serialize queue");
        System.out.println("===============");

        BoundedPriorityQueue<Double> queue =
                new BoundedPriorityQueue<Double>(10);
        queue.add(10.2);
        queue.add(1.55);
        queue.add(15.5);

        File temp_file = File.createTempFile("tempfile", ".tmp");

        ObjectOutputStream output = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(temp_file)));
        output.writeObject(queue);
        output.close();

        ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(temp_file));
        BoundedPriorityQueue<Double> deserialized_queue =
                (BoundedPriorityQueue<Double>) ois.readObject();

        assertEquals(queue.getCapacity(), deserialized_queue.getCapacity());
        assertEquals(queue, deserialized_queue);
    }
}
