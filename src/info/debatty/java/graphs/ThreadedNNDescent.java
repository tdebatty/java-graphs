package info.debatty.java.graphs;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author tibo
 */
public class ThreadedNNDescent extends NNDescent {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // Instantiate and configure the algorithm
        ThreadedNNDescent tnnd = new ThreadedNNDescent();
        tnnd.setThreadCount(3);
        tnnd.setK(4);
        tnnd.setSimilarity(new SimilarityInterface() {
            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        });
        
        // Optionnally, define callback
        tnnd.setCallback(new CallbackInterface() {
            @Override
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        // Run the algorithm and get computed neighbor lists
        HashMap<Node, NeighborList> neighborlists = tnnd.computeGraph(nodes);
        
        // Display neighbor lists
        for (Node n : nodes) {
            NeighborList nl = neighborlists.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
    }
    
    protected int thread_count = 4;
    
    /**
     * Set the number of threads
     * Default is 4
     * @param thread_count 
     */
    public void setThreadCount(int thread_count) {
        if (thread_count <= 0) {
            throw new InvalidParameterException("thread_count should be > 0");
        }
        
        this.thread_count = thread_count;
    }
    
    public int getThreadCount() {
        return thread_count;
    }
    
    private ExecutorService executor;
    
    // Internal state, used by worker objects
    List<Node> nodes;
    HashMap<Node, NeighborList> neighborlists;
    HashMap<Node, ArrayList> old_lists, new_lists, old_lists_2, new_lists_2;

    @Override
    public HashMap<Node, NeighborList> computeGraph(List<Node> nodes) {
        
        // Create worker threads
        executor = Executors.newFixedThreadPool(thread_count);
        
        iterations = 0;
        
        if (nodes.size() <= (k+1)) {
            return MakeFullyLinked(nodes);
        }
        
        // Initialize state...
        this.nodes = nodes;
        this.neighborlists = new HashMap<Node, NeighborList>(nodes.size());
        this.old_lists = new HashMap<Node, ArrayList>(nodes.size());
        this.new_lists = new HashMap<Node, ArrayList>(nodes.size());
        
        HashMap<String, Object> data = new HashMap<String, Object>();
    
        // B[v]←− Sample(V,K)×{?∞, true?} ∀v ∈ V
        // For each node, create a random neighborlist
        for (Node v : nodes) {
            neighborlists.put(v, RandomNeighborList(nodes, v));
        }

        // loop
        while (true) {
            iterations++;
            c = 0;
            
            // for v ∈ V do
            // old[v]←− all items in B[v] with a false flag
            // new[v]←− ρK items in B[v] with a true flag
            // Mark sampled items in B[v] as false;
            for (int i = 0; i < nodes.size(); i++) {
                Node v = nodes.get(i);
                old_lists.put(v, PickFalses(neighborlists.get(v)));
                new_lists.put(v, PickTruesAndMark(neighborlists.get(v)));

            }

            // old′ ←Reverse(old)
            // new′ ←Reverse(new)
            old_lists_2 = Reverse(nodes, old_lists);
            new_lists_2 = Reverse(nodes, new_lists);

            ArrayList<Future<Integer>> list = new ArrayList<Future<Integer>>();
            // Start threads...
            for (int t = 0; t < thread_count; t++) {
                list.add(executor.submit(new ThreadedNNDescent.NNThread(t)));

            }

            for (Future<Integer> future : list) {
                try {
                    c += future.get();
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            }
            
            //System.out.println("C : " + c);
            if (callback != null) {
                data.put("c", c);
                data.put("computed_similarities", computed_similarities);
                data.put("iterations", iterations);
                
                callback.call(data);
            }

            if (c <= (delta * nodes.size() * k)) {
                break;
            }
            
            if (iterations >= max_iterations) {
                break;
            }
        }
        
        executor.shutdown();
        
        // Clear local state
        HashMap<Node, NeighborList> n = this.neighborlists;
        this.neighborlists = null;
        this.new_lists = null;
        this.new_lists_2 = null;
        this.nodes = null;
        this.old_lists = null;
        this.old_lists_2 = null;
        
        return n;
    }
    
    
    class NNThread implements Callable<Integer> {
        int slice;
        
        public NNThread(int slice) {
            this.slice = slice;
        }

        @Override
        public Integer call() {
            int c = 0;
            // for v ∈ V do
            int start = slice * nodes.size()/thread_count;
            int end = (slice + 1) * nodes.size()/thread_count;
            
            // Last slice should go to the end...
            if (slice == (thread_count - 1)) {
                end = nodes.size();
            }
            
            for (int i = start; i < end; i++) {
                Node v = nodes.get(i);
                // old[v]←− old[v] ∪ Sample(old′[v], ρK)
                // new[v]←− new[v] ∪ Sample(new′[v], ρK)
                old_lists.put(v, Union(old_lists.get(v), Sample(old_lists_2.get(v), (int) (rho * k))));
                new_lists.put(v, Union(new_lists.get(v), Sample(new_lists_2.get(v), (int) (rho * k))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    Node u1 = (Node) new_lists.get(v).get(j);

                    for (int k = j + 1; k < new_lists.get(u1).size(); k++) {
                        Node u2 = (Node) new_lists.get(u1).get(k);
                        //int u2_i = Find(u2);

                        // l←− σ(u1,u2)
                        // c←− c+UpdateNN(B[u1], u2, l, true)
                        // c←− c+UpdateNN(B[u2], u1, l, true)
                        double s = Similarity(u1, u2);
                        c += UpdateNL(neighborlists.get(u1), u2, s);
                        c += UpdateNL(neighborlists.get(u2), u1, s);
                    }

                    // or u1 ∈ new[v], u2 ∈ old[v] do
                    for (int k = 0; k < old_lists.get(v).size(); k++) {
                        Node u2 = (Node) old_lists.get(v).get(k);
            
                        if (u1.equals(u2)) {
                            continue;
                        }
                        
                        //int u2_i = Find(u2);
                        double s = Similarity(u1, u2);
                        c += UpdateNL(neighborlists.get(u1), u2, s);
                        c += UpdateNL(neighborlists.get(u2), u1, s);
                    }
                }
            }
            return c;
        }
    }
}