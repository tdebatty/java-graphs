package info.debatty.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author tibo
 */
public class Brute {
    
    public static void main(String[] args) {
        Random r = new Random();
        int count = 10000;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        
        Brute brute = new Brute();
        brute.K = 10;
        brute.nodes = nodes;
        
        brute.callback = new BruteCallbackInterface() {

            @Override
            public void call(int node_id, int computed_similarities) {
                System.out.println("Node: " + node_id + " => " + computed_similarities); 
            }
        };
        
        brute.similarity = new SimilarityInterface() {

            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        };
                
        brute.Run();
        
        for (Node n : nodes) {
            NeighborList nl = brute.neighborlists.get(n);
            System.out.println(n);
            System.out.println(nl);
        }
        
        brute.Print();
    }
    
    public ArrayList<Node> nodes;
    
    public int K = 10;
    public SimilarityInterface similarity;
    public BruteCallbackInterface callback = null;
    
    public long running_time;
    protected int n;
    
    public HashMap<Node, NeighborList> neighborlists;
    
    public void Run() {
        
        n = nodes.size();
        long start_time = System.currentTimeMillis();
        int pos = 0;
        double[] similarities = new double[n * (n-1) / 2];
        for (int j = 0; j < n; j++) {
            
            Node node = nodes.get(j);
            for (int i = 0; i < j; i++) {
                double sim = similarity.similarity(node, nodes.get(i));
                similarities[pos] = sim;
                pos++;
            }
            
            if (callback != null) {
                callback.call(j, pos);
            }
        }
        
        /**
         *   
         * i\j|| 0 | 1 | 2 | 3 | 4 
         * =========================
         * 0  || x |   |   |   |
         * 1  || 0 | x |   |   |
         * 2  || 1 | 2 | x |   |
         * 3  || 3 | 4 | 5 | x |
         * 4  || 6 | 7 | 8 | 9 | x
         * 
         * for node 2 (i = 2),
         * similarities are located at 1, 2, 5, 8
         * corresponding to nodes   j= 0, 1, 2, 3
         */
        
        double s;
        neighborlists = new HashMap<Node, NeighborList>(n);
        // For each node, find the k nearest neighbors
        for (int i = 0; i < n; i++) {
            
            Node node = nodes.get(i);
            NeighborList nl = new NeighborList(K);
            
            for (int j = 0; j < n; j++) {
                
                
                if (i == j) {
                    continue;
                    
                }
                
                if (j < i) {
                    s = similarities[i * (i-1)/2 + j];
                    
                } else {
                    s = similarities[j * (j-1) / 2 + i];
                    
                }
                
                nl.add(new Neighbor(nodes.get(j), s));
            }
            neighborlists.put(node, nl);
        }
        
        running_time = (System.currentTimeMillis() - start_time);
        
    }
    
    public void Print() {
        System.out.println("n = " + n);
        System.out.println("Running_time = " + running_time + "ms");
        System.out.println("Similarities = " + (n * (n-1) /2) );
    }
}
