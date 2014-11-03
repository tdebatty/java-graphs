package info.debatty.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author tibo
 */
public class ThreadedNNDescent {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Random r = new Random();
        int count = 10000;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        ThreadedNNDescent tnnd = new ThreadedNNDescent();
        tnnd.K = 10;
        tnnd.nodes = nodes;
        tnnd.rho = 0.5;
        tnnd.delta = 0.001;
        
        tnnd.callback= new NNDescentCallbackInterface() {

            @Override
            public void call(int iteration, int computed_similarities, int c) {
                System.out.println(
                        "Iteration " + iteration + " : " + 
                        computed_similarities + " similarities " +
                        "c = " + c);
            }
        };
        
        tnnd.similarity = new SimilarityInterface() {

            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        };
                
        tnnd.Run();
        
        /*
        for (Node n : nodes) {
            NeighborList nl = tnnd.neighborlists.get(n);
            System.out.println(n);
            System.out.println(nl);
        }
        */
        
        tnnd.Print();
    }
    
    class NNThread implements Callable<Integer> {
        int id;
        
        public NNThread(int id) {
            this.id = id;
        }
        

        @Override
        public Integer call() {
            int c = 0;
            // for v ∈ V do
            int start = id * nodes.size()/thread_count;
            int end = (id + 1) * nodes.size()/thread_count;
            for (int i = start; i < end; i++) {
                Node v = nodes.get(i);
                // old[v]←− old[v] ∪ Sample(old′[v], ρK)
                // new[v]←− new[v] ∪ Sample(new′[v], ρK)
                old_lists.put(v, Union(old_lists.get(v), Sample(old_lists_2.get(v), (int) (rho * K))));
                new_lists.put(v, Union(new_lists.get(v), Sample(new_lists_2.get(v), (int) (rho * K))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    Node u1 = (Node) new_lists.get(v).get(j);
                    
                    //int u1_i = Find(u1); // position of u1 in nodes

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

    public ArrayList<Node> nodes;
    public int K = 10;
    // Standard : 1, Fast: 0.5
    public double rho = 1.0;
    public double delta = 0.001;
    public SimilarityInterface similarity;
    
    public int max_iterations = Integer.MAX_VALUE;
    public int thread_count = 4;
    
    public NNDescentCallbackInterface callback = null;

    /**
     * Contains one NeighborList for each Node
     *
     */
    public ConcurrentHashMap<Node, NeighborList> neighborlists;

    public int iterations = 0;
    public int computed_similarities = 0;
    public long running_time = 0;
    
    HashMap<Node, ArrayList> old_lists, new_lists, old_lists_2, new_lists_2;
    
    public ThreadedNNDescent() {
        similarity = new SimilarityInterface() {

            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        };
    }

    public void Run() {
        long start_time = System.currentTimeMillis();
        
        neighborlists = new ConcurrentHashMap<Node, NeighborList>(nodes.size());
        
        if (nodes.size() <= (K+1)) {
            MakeFullyLinked();
            return;
        }
        
        old_lists = new HashMap<Node, ArrayList>(nodes.size());
        new_lists = new HashMap<Node, ArrayList>(nodes.size());
    
        // B[v]←− Sample(V,K)×{?∞, true?} ∀v ∈ V
        // For each node, create a random neighborlist
        for (Node v : nodes) {
            neighborlists.put(v, RandomNeighborList(v));
        }
        
        // Create worker threads
        ExecutorService executor = Executors.newFixedThreadPool(thread_count);

        // loop
        while (true) {
            this.iterations++;

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
            old_lists_2 = Reverse(old_lists);
            new_lists_2 = Reverse(new_lists);
            
            // c←− 0 update counter
            int c = 0;
            
            ArrayList<Future<Integer>> list = new ArrayList<Future<Integer>>();
            // Start threads...
            for (int t = 0; t < thread_count; t++) {
                list.add(executor.submit(new NNThread(t)));
                
            }
            
            for (Future<Integer> future : list) {
                try {
                    c += future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
            
            //System.out.println("C : " + c);
            if (callback != null) {
                callback.call(iterations, computed_similarities, c);
            }

            if (c <= (delta * nodes.size() * K)) {
                break;
            }
            
            if (iterations >= max_iterations) {
                break;
            }
        }
        executor.shutdown();
        running_time = (System.currentTimeMillis() - start_time);
    }

    private ArrayList<Node> Union(ArrayList<Node> l1, ArrayList<Node> l2) {
        ArrayList<Node> r = new ArrayList<Node>();
        for (Node n : l1) {
            if (!r.contains(n)) {
                r.add(n);
            }
        }
        
        for (Node n : l2) {
            if (!r.contains(n)) {
                r.add(n);
            }
        }

        return r;
    }

    private NeighborList RandomNeighborList(Node for_node) {
        //System.out.println("Random NL for node " + for_node);
        NeighborList nl = new NeighborList(K);
        Random r = new Random();

        while (nl.size() < K) {
            Node node = nodes.get(r.nextInt(nodes.size()));
            if (! node.equals(for_node)) {
                double s = Similarity(node, for_node);
                nl.add(new Neighbor(node, s));
            }
        }
        
        //System.out.println(nl);
        return nl;
    }

    private ArrayList<Node> PickFalses(NeighborList neighborList) {
        ArrayList<Node> falses = new ArrayList<Node>();
        for (Neighbor n : neighborList) {
            if (!n.is_new) {
                falses.add(n.node);
            }
        }

        return falses;
    }
    
    /**
     * pick new neighbors with a probability of rho, and mark them as false
     *
     * @param neighborList
     * @return
     */
    private ArrayList<Node> PickTruesAndMark(NeighborList neighborList) {
        ArrayList<Node> r = new ArrayList<Node>();
        for (Neighbor n : neighborList) {
            if (n.is_new && Math.random() < rho) {
                n.is_new = false;
                r.add(n.node);
            }
        }

        return r;
    }


    private HashMap<Node, ArrayList> Reverse(HashMap<Node, ArrayList> lists) {

        HashMap<Node, ArrayList> R = new HashMap<Node, ArrayList>(nodes.size());
        
        // Create all arraylists
        for (Node n : nodes) {
            R.put(n, new ArrayList<Node>());
        }

        // For each node and corresponding arraylist
        for (Node node : nodes) {
            ArrayList<Node> list = lists.get(node);
            for (Node other_node : list) {
                R.get(other_node).add(node);
            }
        }

        return R;
    }

    /**
     * Reverse NN array R[v] is the list of elements (u) for which v is a
     * neighbor (v is in B[u])
     *
     */

    private ArrayList<Node> Sample(ArrayList<Node> nodes, int count) {
        Random r = new Random();
        while (nodes.size() > count) {
            nodes.remove(r.nextInt(nodes.size()));
        }

        return nodes;

    }

    private int UpdateNL(NeighborList nl, Node n, double similarity) {
        Neighbor neighbor = new Neighbor(n, similarity);
        return nl.add(neighbor) ? 1 : 0;
    }


    private double Similarity(Node n1, Node n2) {
        computed_similarities++;
        return similarity.similarity(n1, n2);
        
    }

    /**
     * Return the position of Node u2 in the array list of nodes
     * @param u2
     * @return 
     */
    private int Find(Node u2) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).equals(u2)) {
                return i;
            }
        }
        
        return -1;
    }

    private void MakeFullyLinked() {
        for (Node node : nodes) {
            NeighborList neighborlist = new NeighborList();
            for (Node other_node : nodes) {
                if (node.equals(other_node)) {
                    continue;
                }
                
                neighborlist.add(new Neighbor(
                        other_node,
                        Similarity(node, other_node)
                ));
            }
            neighborlists.put(node, neighborlist);
        }
    }

    void Print() {
        System.out.println("K: " + this.K);
        System.out.println("Computed similarities: " + this.computed_similarities);
        System.out.println("Delta: " + this.delta);
        System.out.println("Iterations: " + this.iterations);
        System.out.println("Rho: " + this.rho);
        System.out.println("Running time: " + this.running_time + "ms");
        
    }
}