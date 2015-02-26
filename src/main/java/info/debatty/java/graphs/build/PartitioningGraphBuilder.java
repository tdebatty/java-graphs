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

import info.debatty.java.graphs.Neighbor;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The number of stages (n_stages) and the number of partitions (n_partitions)
 * allow you to control the speedup and precision.
 * 
 * If brute-force method is used inside the partitions:
 * number of similarities to compute ≃ n² / 2 x n_stages / n_partitions
 * where n is the size of the dataset
 * 
 * Thus speedup with respect to pure brute force ≃ n_partitions / n_stages
 * 
 * At the same time:
 * - increasing n_stages will increase the precision
 * - increasing n_partitions will decrease the precision
 * The exact relation between precision and these 2 parameters depends on 
 * the algorithm used...
 * 
 * @param <t>
 */
public abstract class PartitioningGraphBuilder<t> extends GraphBuilder<t> {

    protected int n_stages = 2;
    protected int n_partitions = 4;
    protected GraphBuilder internal_builder = new Brute();

    public int getNStages() {
        return n_stages;
    }

    /**
     * Default = 2
     * @param n_stages 
     */
    public void setNStages(int n_stages) {
        this.n_stages = n_stages;
    }

    /**
     * Default = 4
     * @return 
     */
    public int getNPartitions() {
        return n_partitions;
    }

    /**
     * Default = 4
     * @param n_partitions 
     */
    public void setNPartitions(int n_partitions) {
        this.n_partitions = n_partitions;
    }

    public GraphBuilder getInternalBuilder() {
        return internal_builder;
    }

    /**
     * Default = Brute force
     * @param internal_builder 
     */
    public void setInternalBuilder(GraphBuilder internal_builder) {
        this.internal_builder = internal_builder;
    }
    
    @Override
    protected HashMap<Node<t>, NeighborList> _computeGraph(List<Node<t>> nodes) {
        // Create $n_stages$ x $n_partitions$ partitions
        List<Node<t>>[][] partitioning = _partition(nodes);
        
        // Initialize all NeighborLists
        HashMap<Node<t>, NeighborList> neighborlists = new HashMap<Node<t>, NeighborList>(nodes.size());
        for (Node node : nodes) {
            neighborlists.put(node, new NeighborList(k));
        }
        
        // Loop over all stages and partitions
        for (int s = 0; s < n_stages; s++) {
            
            // Can be executed in parallel
            for (int p = 0; p < n_partitions; p++) {
                
                // Use NNDescent to compute edges
                /*NNDescent nnd = new NNDescent();
                nnd.setK(k);
                nnd.setSimilarity(similarity);
                HashMap<Node, NeighborList> subgraph = nnd.computeGraph(partitioning[s][p]);
                computed_similarities += nnd.getComputedSimilarities();
                        */
                
                GraphBuilder builder;
                try {
                    builder = (GraphBuilder) internal_builder.clone();
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(PartitioningGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
                builder.setK(k);
                builder.setSimilarity(similarity);
                HashMap<Node, NeighborList> subgraph = builder.computeGraph(partitioning[s][p]);
                computed_similarities += builder.getComputedSimilarities();
                
                // Add to current neighborlists
                //neighborlists.putAll(subgraph);
                for (Entry<Node, NeighborList> e : subgraph.entrySet()) {
                    //neighborlists.get(e.getKey()).addAll(e.getValue());
                    Node node = e.getKey();
                    for (Neighbor neighbor : e.getValue()) {
                        neighborlists.get(node).add(neighbor);
                    }
                }
                
                //return neighborlists;
            }
        }
        
        return neighborlists;
        
    }
    
    public double estimatedSpeedup() {
        return (double) n_partitions / n_stages;
    }
    
    abstract protected List<Node<t>>[][] _partition(List<Node<t>> nodes);
}
