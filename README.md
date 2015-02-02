#java-graphs
Implementation of some k nearest neighbors graph (k-nn graph) building algorithms.

##Download
https://github.com/tdebatty/java-graphs/releases

##NN-Descent
Implementation of NN-Descent, as proposed by Dong, Moses and Li; [Efficient k-nearest neighbor graph construction for generic similarity measures](http://portal.acm.org/citation.cfm?doid=1963405.1963487); Proceedings of the 20th international conference on World wide web.

The algorithm iteratively builds an approximate k-nn graph. At each iteration, for each node, the algorithm searches the edges (called neighbors) of current edges (neighbors), to improve the graph.

It takes two additional parameters to speed-up processing:
- sampling coefficient rho : indicates the ratio of neighbors of current neigbors that have to be analyzed at each iteration (default is 0.5);
- early termination coefficiant delta: the algorithm stops when less than this proportion of edges are modified (default 0.001).

```java
import info.debatty.java.graphs.*;

public class MyApp {
    
    public static void main(String[] args) {
        // Build some nodes
        Random r = new Random();
        int count = 1000;
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // Instantiate and configure the algorithm
        // k (number of edges per node)
        // and the similarity metric are mandatory
        NNDescent nnd = new NNDescent();
        nnd.setK(10);
        nnd.setSimilarity(new SimilarityInterface() {
            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
            }
        });

        // Optionnally, define a callback, to get some feedback
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
}
```

```
{computed_similarities=36624, c=3358, iterations=1}
{computed_similarities=89622, c=19049, iterations=2}
{computed_similarities=116663, c=7233, iterations=3}
...
(0 => 9136)[(91,9099,0.02631578947368421), (667,9165,0.03333333333333333), (404,9127,0.1), ...
(1 => 3750)[(976,3684,0.014925373134328358), (383,3815,0.015151515151515152), (488,3785,0.0...
(2 => 5667)[(790,5585,0.012048192771084338), (931,5589,0.012658227848101266), (672,5735,0.0...
(3 => 642)[(881,597,0.021739130434782608), (360,598,0.022222222222222223), (948,683,0.02380...
(4 => 3377)[(751,3413,0.02702702702702703), (380,3412,0.027777777777777776), (531,3408,0.03...

```

The library also implements a multi-threaded version of NN-Descent:

```java
import info.debatty.java.graphs.*;

public class MyApp {

    public static void main(String[] args) {
        // Create some random nodes
        Random r = new Random();
        int count = 1000;
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // Instantiate and configure the algorithm
        ThreadedNNDescent tnnd = new ThreadedNNDescent();
        // Define the number of threads (default is 4)
        tnnd.setThreadCount(3);
        tnnd.setK(10);
        tnnd.setSimilarity(new SimilarityInterface() {
            @Override
            public double similarity(Node n1, Node n2) {
                return 1.0 / (1.0 + Math.abs((Integer) n1.value - (Integer) n2.value));
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
```

##Brute-force

The brute-force algorithm builds the k-nn graph by computing all pairwize similarities between nodes. This can be extremely expensive as it requires the computation of n . (n-1) / 2 similarities, where n is the number of nodes.

```java
import info.debatty.java.graphs.*;

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
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute brute = new Brute();
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
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
          
        });
        
        // Run the algorithm, and get the resulting neighbor lists
        HashMap<Node, NeighborList> neighbor_lists = brute.computeGraph(nodes);
        
        // Display the computed neighbor lists
        for (Node n : nodes) {
            NeighborList nl = neighbor_lists.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
    }
}
```

```
{computed_similarities=0, node_id=0}
{computed_similarities=1, node_id=1}
{computed_similarities=3, node_id=2}
{computed_similarities=6, node_id=3}
{computed_similarities=10, node_id=4}
{computed_similarities=15, node_id=5}
{computed_similarities=21, node_id=6}
{computed_similarities=28, node_id=7}
...
(0 => 2174)[(815,2126,0.02040816326530612), (892,2143,0.03125), (347,2207,0.029411764705882353), (803,2170,0.2), (657,2146,0.034482758620689655), (534,2160,0.06666666666666667), (472,2187,0.07142857142857142), (504,2170,0.2), (570,2172,0.3333333333333333), (900,2200,0.037037037037037035)]

(1 => 7871)[(574,7912,0.023809523809523808), (100,7908,0.02631578947368421), (178,7878,0.125), (78,7850,0.045454545454545456), (212,7843,0.034482758620689655), (415,7874,0.25), (378,7875,0.2), (256,7889,0.05263157894736842), (265,7857,0.06666666666666667), (747,7843,0.034482758620689655)]

(2 => 7744)[(627,7715,0.03333333333333333), (524,7716,0.034482758620689655), (149,7761,0.05555555555555555), (110,7717,0.03571428571428571), (417,7740,0.2), (558,7743,0.5), (60,7732,0.07692307692307693), (85,7724,0.047619047619047616), (130,7719,0.038461538461538464), (672,7747,0.25)]

(3 => 1951)[(650,2020,0.014285714285714285), (377,1889,0.015873015873015872), (11,1896,0.017857142857142856), (118,1900,0.019230769230769232), (768,1933,0.05263157894736842), (109,1939,0.07692307692307693), (153,1997,0.02127659574468085), (227,1971,0.047619047619047616), (393,1954,0.25), (497,1955,0.2)]

(4 => 2444)[(630,2492,0.02040816326530612), (71,2486,0.023255813953488372), (720,2486,0.023255813953488372), (940,2478,0.02857142857142857), (970,2402,0.023255813953488372), (187,2411,0.029411764705882353), (5,2458,0.06666666666666667), (468,2447,0.25), (949,2461,0.05555555555555555), (829,2405,0.025)]
...
```

##Bounded priority queue
This library also implements a bounded priority queue , a data structure that always keeps the n 'largest' elements.

```java
import info.debatty.java.util.*;

public class MyApp {
    
    public static void main(String [] args) {
        BoundedPriorityQueue<Integer> q = new BoundedPriorityQueue(4);
        q.add(1);
        q.add(4);
        q.add(5);
        q.add(6);
        q.add(2);
        
        System.out.println(q);
    }
}
```

```
[2, 4, 5, 6]
```