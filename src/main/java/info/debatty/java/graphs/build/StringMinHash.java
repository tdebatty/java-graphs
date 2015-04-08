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

package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Node;
import info.debatty.java.lsh.LSH;
import info.debatty.java.lsh.MinHash;
import info.debatty.java.stringsimilarity.KShingling;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thibault Debatty
 */
public class StringMinHash extends PartitioningGraphBuilder<String> {
    
    private int shingle_size = 4;
    
    public int getShingleSize() {
        return shingle_size;
    }
    
    /**
     * Default = 4
     * @param shingle_size 
     */
    public void setShingleSize(int shingle_size) {
        this.shingle_size = shingle_size;
    }

    @Override
    protected List<Node<String>>[][] _partition(List<Node<String>> nodes) {
        HashMap<String, Object> feedback_data = new HashMap<String, Object>();
        // Compute the dictionary of all shingles (4-grams)
        KShingling ks = new KShingling(shingle_size);
        for (Node node : nodes) {
            ks.parse((String) node.value);
        }
        
        if (callback != null) {
            feedback_data.put("step", "Dictionary computation");
            feedback_data.put("dictionary-size", ks.size());
            callback.call(feedback_data);
        }
        
        ArrayList<Node<String>>[][] partitions = new ArrayList[n_stages][n_partitions];
        
        double threshold = 0.6;
        int R = (int) log(threshold, 1.0/(double) n_stages);
        int signature_size = R * n_stages;
        
        MinHash mh = new MinHash(signature_size, ks.size());
        LSH lsh = new LSH();
        lsh.setS(n_stages);
        lsh.setB(n_partitions);
        
        int computed_hashes = 0;
        for (Node node : nodes) {
            Set<Integer> set = ks.integerSetOf((String) node.value);
            int[] signature = mh.hash(set);
            int[] lsh_hash = lsh.hash(signature);
            computed_hashes++;
            
            for (int i = 0; i < n_stages; i++) {
                int bucket = lsh_hash[i];
                if (partitions[i][bucket] == null) {
                    partitions[i][bucket] = new ArrayList<Node<String>>();
                }
                
                partitions[i][bucket].add(node);
            }
            
            if (callback != null) {
                feedback_data.put("step", "Hashes computation");
                feedback_data.put("computed-hashes", computed_hashes);
                callback.call(feedback_data);
            }
        }
        
        return partitions;
    }

    private double log(double x, double base) {
        return Math.log(base) / Math.log(x);
    }
}
