package info.debatty.java.graphs;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Thibault Debatty
 */

public class NNDescent extends GraphBuilder {
    
    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // Instantiate and configure algorithm
        NNDescent nnd = new NNDescent();
        nnd.setK(10);
        nnd.setSimilarity(new SimilarityInterface() {

            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        });
        
        nnd.setCallback(new CallbackInterface() {

            @Override
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        
        
        // Run the algorithm and get computed neighborlists
        HashMap<Node, NeighborList> neighborlists = nnd.computeGraph(nodes);
        
        // Display neighborlists
        for (Node n : nodes) {
            NeighborList nl = neighborlists.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
        
    }

    protected double rho = 0.5; // Standard : 1, Fast: 0.5
    protected double delta = 0.001;
    protected int max_iterations = Integer.MAX_VALUE;
    
    protected int iterations = 0;
    protected int c;
    
    
    /**
     * Get the number of edges modified at the last iteration
     * @return 
     */
    public int getC() {
        return c;
    }
    
    
    /**
     * Get the number of executed iterations
     * @return 
     */
    public int getIterations() {
        return iterations;
    }

    public double getRho() {
        return rho;
    }

    /**
     * Sampling coefficient.
     * In interval ]0, 1.0]
     * Typical value for fast computation is 0.5
     * Use 1.0 for precise computation
     * Default is 0.5
     * @param rho 
     */
    public void setRho(double rho) {
        if (rho > 1.0 || rho <= 0.0) {
            throw new InvalidParameterException("0 < rho <= 1.0");
        }
        this.rho = rho;
    }

    public double getDelta() {
        return delta;
    }

    /**
     * Early termination coefficient.
     * The algorithm stops when less then this proportion of edges are modified
     * Should be in ]0, 1.0[
     * Default is 0.001
     * @param delta 
     */
    public void setDelta(double delta) {
        if (rho >= 1.0 || rho <= 0.0) {
            throw new InvalidParameterException("0 < delta < 1.0");
        }
        this.delta = delta;
    }

    public int getMaxIterations() {
        return max_iterations;
    }

    /**
     * Set the maximum number of iterations
     * Default is no max (Integer.MAX_VALUE)
     * @param max_iterations 
     */
    public void setMaxIterations(int max_iterations) {
        if (max_iterations < 0) {
            throw new InvalidParameterException("max_iterations should be positive!");
        }
        this.max_iterations = max_iterations;
    }

    
    @Override
    public HashMap<Node, NeighborList> computeGraph(List<Node> nodes) {
        iterations = 0;
        
        if (nodes.size() <= (k+1)) {
            return MakeFullyLinked(nodes);
        }
        
        HashMap<Node, NeighborList> neighborlists = new HashMap<Node, NeighborList>(nodes.size());
        HashMap<Node, ArrayList> old_lists, new_lists, old_lists_2, new_lists_2;
        
        old_lists = new HashMap<Node, ArrayList>(nodes.size());
        new_lists = new HashMap<Node, ArrayList>(nodes.size());
        
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

            // for v ∈ V do
            for (int i = 0; i < nodes.size(); i++) {
                Node v = nodes.get(i);
                // old[v]←− old[v] ∪ Sample(old′[v], ρK)
                // new[v]←− new[v] ∪ Sample(new′[v], ρK)
                old_lists.put(v, Union(old_lists.get(v), Sample(old_lists_2.get(v), (int) (rho * k))));
                new_lists.put(v, Union(new_lists.get(v), Sample(new_lists_2.get(v), (int) (rho * k))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    Node u1 = (Node) new_lists.get(v).get(j);

                        //int u1_i = Find(u1); // position of u1 in nodes
                    for (int l = j + 1; l < new_lists.get(u1).size(); l++) {
                        Node u2 = (Node) new_lists.get(u1).get(l);
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
        
        return neighborlists;
    }

    protected ArrayList<Node> Union(ArrayList<Node> l1, ArrayList<Node> l2) {
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

    protected NeighborList RandomNeighborList(List<Node> nodes, Node for_node) {
        //System.out.println("Random NL for node " + for_node);
        NeighborList nl = new NeighborList(k);
        Random r = new Random();

        while (nl.size() < k) {
            Node node = nodes.get(r.nextInt(nodes.size()));
            if (! node.equals(for_node)) {
                double s = Similarity(node, for_node);
                nl.add(new Neighbor(node, s));
            }
        }
        
        return nl;
    }

    protected ArrayList<Node> PickFalses(NeighborList neighborList) {
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
    protected ArrayList<Node> PickTruesAndMark(NeighborList neighborList) {
        ArrayList<Node> r = new ArrayList<Node>();
        for (Neighbor n : neighborList) {
            if (n.is_new && Math.random() < rho) {
                n.is_new = false;
                r.add(n.node);
            }
        }

        return r;
    }


    protected HashMap<Node, ArrayList> Reverse(List<Node> nodes, HashMap<Node, ArrayList> lists) {

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
     * @param nodes
     * @param count
     * @return 
     */

    protected ArrayList<Node> Sample(ArrayList<Node> nodes, int count) {
        Random r = new Random();
        while (nodes.size() > count) {
            nodes.remove(r.nextInt(nodes.size()));
        }

        return nodes;

    }

    protected int UpdateNL(NeighborList nl, Node n, double similarity) {
        Neighbor neighbor = new Neighbor(n, similarity);
        return nl.add(neighbor) ? 1 : 0;
    }


    protected double Similarity(Node n1, Node n2) {
        computed_similarities++;
        return similarity.similarity(n1, n2);
        
    }

    protected HashMap<Node, NeighborList> MakeFullyLinked(List<Node> nodes) {
        HashMap<Node, NeighborList> neighborlists = new HashMap<Node, NeighborList>(nodes.size());
        for (Node node : nodes) {
            NeighborList neighborlist = new NeighborList(k);
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
        
        return neighborlists;
    }
}
