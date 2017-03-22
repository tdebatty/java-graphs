/*
 * The MIT License
 *
 * Copyright 2015 tibo.
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
package info.debatty.java.graphs.examples;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.graphs.build.NNCTPH;
import info.debatty.java.stringsimilarity.Cosine;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tibo
 */
public class NNCTPHExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Read the file
        List<String> nodes = GraphBuilder.readFile(
                NNCTPHExample.class.getClassLoader()
                        .getResource("726-unique-spams").getFile());

        NNCTPH builder = new NNCTPH();
        builder.setNPartitions(20);
        builder.setOversampling(2);
        builder.setSimilarity(new SimilarityInterface<String>() {

            public double similarity(String value1, String value2) {
                Cosine cosine = new Cosine(3);
                return cosine.similarity(value1, value2);
            }
        });

        Graph<String> graph = builder.computeGraph(nodes);

        for (Map.Entry<String, NeighborList> entry : graph.entrySet()) {
            System.out.println(entry);
        }

        builder.test(nodes);
    }

}
