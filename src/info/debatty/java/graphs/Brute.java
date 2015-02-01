package info.debatty.java.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author tibo
 */
public class Brute extends GraphBuilder {
    
    public static void main(String[] args) {
        
        // Generate some random nodes
        Random r = new Random();
        int count = 1000;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        
        // Instantiate and configure the brute-force graph building algorithm
        Brute brute = new Brute();
        
        // The minimum is to define k (number of edges per node)
        // and a similarity between nodes
        brute.setK(10);
        brute.setSimilarity(new SimilarityInterface() {
            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        });
        
        // Optionaly, we can define a callback, to get some feedback...
        brute.setCallback(new CallbackInterface() {
            @Override
            public void call(String node_id, int computed_similarities) {
                System.out.println("Node: " + node_id + " => " + computed_similarities); 
            }
        });
        
        // Run the algorithm, and get the resulting neighbor lists
        HashMap<Node, NeighborList> neighbor_lists = brute.computeGraph(nodes);
        
        for (Node n : nodes) {
            NeighborList nl = neighbor_lists.get(n);
            System.out.println(n);
            System.out.println(nl);
        }
    }
    
    @Override
    public HashMap<Node, NeighborList> computeGraph(List<Node> nodes) {
        
        int n = nodes.size();
        HashMap<Node, NeighborList> neighborlists = new HashMap<Node, NeighborList>(n);
        
        // Initialize all NeighborLists
        for (Node node : nodes) {
            neighborlists.put(node, new NeighborList(k));
        }
        
        computed_similaritites = 0;
        double sim;
        Node n1;
        Node n2;
        
        for (int i = 0; i < n; i++) {
            
            n1 = nodes.get(i);
            for (int j = 0; j < i; j++) {
                n2 = nodes.get(j);
                sim = similarity.similarity(n1, n2);
                computed_similaritites++;
                
                neighborlists.get(n1).add(new Neighbor(n2, sim));
                neighborlists.get(n2).add(new Neighbor(n1, sim));
            }
            
            if (callback != null) {
                callback.call(n1.id, computed_similaritites);
            }
        }
        
        return neighborlists;
    }
}
