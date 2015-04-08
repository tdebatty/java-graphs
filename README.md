#java-graphs
Implementation of various algorithms that build k-nearest neighbors graph (k-nn graph).

Some of these algorithms are independant of the data type and similarity metric:
* Brute-force
* (Multi-threaded) NN-Descent

Some algorithms are dedicated to String datasets:
* MinHash
* SuperBit
<!-- * K-Medoids  * CTPH * BOW -->

##Installation

Using maven:
```
<dependency>
    <groupId>info.debatty</groupId>
    <artifactId>java-graphs</artifactId>
    <version>RELEASE</version>
</dependency>
```

Or from the [releases page](https://github.com/tdebatty/java-graphs/releases).

##Brute-force

The brute-force algorithm builds the k-nn graph by computing all pairwize similarities between nodes. This can be extremely expensive as it requires the computation of n . (n-1) / 2 similarities, where n is the number of nodes.

```java
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class BruteExample {

    public static void main(String[] args) {
        
        // Generate some random nodes
        Random r = new Random();
        int count = 1000;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute builder = new Brute<Integer>();
        builder.setK(10);
        builder.setSimilarity(new SimilarityInterface<Integer>() {

            public double similarity(Integer value1, Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        });
        
        
        // Optionaly, we can define a callback, to get some feedback...
        builder.setCallback(new CallbackInterface() {

            @Override
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
          
        });
        
        // Run the algorithm, and get the resulting neighbor lists
        HashMap<Node, NeighborList> neighbor_lists = builder.computeGraph(nodes);
        
        // Display the computed neighbor lists
        for (Node n : nodes) {
            NeighborList nl = neighbor_lists.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
    }   
}
```

This will produce something like:

```
...
{computed_similarities=490545, node_id=990}
{computed_similarities=491536, node_id=991}
{computed_similarities=492528, node_id=992}
{computed_similarities=493521, node_id=993}
{computed_similarities=494515, node_id=994}
{computed_similarities=495510, node_id=995}
{computed_similarities=496506, node_id=996}
{computed_similarities=497503, node_id=997}
{computed_similarities=498501, node_id=998}
{computed_similarities=499500, node_id=999}

(0 => 5800)[(105,5760,0.024390243902439025), (801,5763,0.02631578947368421), ...
(1 => 783)[(223,830,0.020833333333333332), (670,744,0.025), (749,813,0.032258...
(2 => 7152)[(828,7187,0.027777777777777776), (367,7122,0.03225806451612903), ...
(3 => 8584)[(543,8560,0.04), (639,8606,0.043478260869565216), (305,8607,0.041...
...
```


##NN-Descent
Implementation of NN-Descent, as proposed by Dong, Moses and Li; [Efficient k-nearest neighbor graph construction for generic similarity measures](http://portal.acm.org/citation.cfm?doid=1963405.1963487); Proceedings of the 20th international conference on World wide web.

The algorithm iteratively builds an approximate k-nn graph. At each iteration, for each node, the algorithm searches the edges (called neighbors) of current edges (neighbors), to improve the graph.

It takes two additional parameters to speed-up processing:
- sampling coefficient rho : indicates the ratio of neighbors of current neigbors that have to be analyzed at each iteration (default is 0.5);
- early termination coefficiant delta: the algorithm stops when less than this proportion of edges are modified (default 0.001).

```java
import info.debatty.java.graphs.*;
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.NNDescent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NNDescentExample {

    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;
        int k = 10;
        
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // Instantiate and configure algorithm
        NNDescent builder = new NNDescent();
        builder.setK(k);
        
        // early termination coefficient
        builder.setDelta(0.1);
        
        // sampling coefficient
        builder.setRho(0.2);
        
        builder.setMaxIterations(10);
        
        builder.setSimilarity(new SimilarityInterface<Integer>() {

            @Override
            public double similarity(Integer v1, Integer v2) {
                return 1.0 / (1.0 + Math.abs(v1 - v2));
            }
        });

		// Optioannlly, set a callback to get some feedback...
        builder.setCallback(new CallbackInterface() {

            @Override
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        // Run the algorithm and get computed neighborlists
        HashMap<Node, NeighborList> graph = builder.computeGraph(nodes);
        
        // Display neighborlists
        for (Node n : nodes) {
            NeighborList nl = graph.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
        
        // Compare with brute-force algorithm
        Brute brute = new Brute<Integer>();
        brute.setK(k);
        brute.setSimilarity(new SimilarityInterface<Integer>() {

            @Override
            public double similarity(Integer v1, Integer v2) {
                return 1.0 / (1.0 + Math.abs(v1 - v2));
            }
        });
        
        HashMap<Node, NeighborList> ground_truth_graph = brute.computeGraph(nodes);
        
        int correct = 0;
        for (Node node : nodes) {            
            correct += graph.get(node).CountCommonValues(ground_truth_graph.get(node));
        }
        
        System.out.println(
                "Computed similarities: " + builder.getComputedSimilarities());
        double speedup_ratio =
                (double) (nodes.size() * (nodes.size() - 1) / 2) / 
                builder.getComputedSimilarities();
        System.out.println("Speedup ratio: " + speedup_ratio);
        
        double correct_ratio = (double) correct / (nodes.size() * k);
        System.out.println("Correct edges: " + correct + 
                "(" + correct_ratio * 100 + "%)");
        
        System.out.println("Quality-equivalent speedup: " + 
                speedup_ratio * correct_ratio);
    }
    
}
```

```
...
(994 => 2319)[(225,2253,0.014925373134328358), (977,2382,0.015625), (294,2302,0.05555555555555555),... 
(995 => 8744)[(296,8836,0.010752688172043012), (15,8835,0.010869565217391304), (238,8688,0.01754385...
(996 => 3675)[(43,3555,0.008264462809917356), (126,3768,0.010638297872340425), (563,3757,0.01204819...
(997 => 862)[(410,838,0.04), (775,841,0.045454545454545456), (212,841,0.045454545454545456), (590...
(998 => 2120)[(585,2157,0.02631578947368421), (66,2138,0.05263157894736842), (61,2140,0.04761904761...
(999 => 1496)[(5,1579,0.011904761904761904), (9,1433,0.015625), (275,1578,0.012048192771084338), ...
Computed similarities: 108451
Speedup ratio: 4.605766659597422
Correct edges: 8255(82.55%)
Quality-equivalent speedup: 3.802060377497672

```
##Multi-threaded NN-Descent
The library also implements a multi-threaded version of NN-Descent:

```java
import info.debatty.java.graphs.*;
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.ThreadedNNDescent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ThreadedNNDescentExample {

    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;
        int k = 10;
        
        ArrayList<Node<Double>> nodes = new ArrayList<Node<Double>>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Double>(String.valueOf(i), r.nextDouble()));
        }
        
        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };
        
        // Instantiate and configure the algorithm
        ThreadedNNDescent builder = new ThreadedNNDescent<Double>();
        builder.setThreadCount(3);
        builder.setK(k);
        builder.setSimilarity(similarity);
        
        // Optionnally, define callback
        builder.setCallback(new CallbackInterface() {
            @Override
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        // Run the algorithm and get computed neighbor lists
        HashMap<Node, NeighborList> graph = builder.computeGraph(nodes);
        
        // Display neighbor lists
        for (Node n : nodes) {
            NeighborList nl = graph.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
        
        // Compare with brute-force algorithm
        Brute brute = new Brute<Double>();
        brute.setK(k);
        brute.setSimilarity(similarity);
        HashMap<Node, NeighborList> ground_truth_graph = brute.computeGraph(nodes);
        
        int correct = 0;
        for (Node node : nodes) {            
            correct += graph.get(node).CountCommonValues(ground_truth_graph.get(node));
        }
        
        System.out.println("Computed similarities: " 
                + builder.getComputedSimilarities());
        double speedup_ratio = 
                (double) (nodes.size() * (nodes.size() - 1) / 2) / 
                builder.getComputedSimilarities();
        System.out.println("Speedup ratio: " + speedup_ratio);
        
        double correct_ratio = (double) correct / (nodes.size() * k);
        System.out.println("Correct edges: " + correct + 
                "(" + correct_ratio * 100 + "%)");
        
        System.out.println("Quality-equivalent speedup: " + 
                speedup_ratio * correct_ratio);
    }
}
```

##MinHash
Builds an approximate knn graph from strings using LSH MinHash algorithm. MinHash is used to bin the input strings into buckets, where similar strings (with a high Jaccard index) have a high probability to fall in the same bucket.

This algorithm is best used when the strings are represented as sets of n-grams (sequences of n characters), and the similarity between strings is computed using the Jaccard index (|A ∩ B| / |A ∪ B|)

```java
import info.debatty.java.graphs.*;
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.graphs.build.NNDescent;
import info.debatty.java.graphs.build.StringMinHash;
import info.debatty.java.stringsimilarity.QGram;
import java.util.HashMap;
import java.util.List;

public class StringMinHashExample {

    public static void main(String[] args) {
        
        // Read the nodes from file...
        List<Node<String>> nodes = GraphBuilder.readFile(args[0]);
        
        // Parameters
        int k = 10;
        
        SimilarityInterface similarity = new SimilarityInterface<String>() {
            QGram qg = new QGram(4);

            public double similarity(String v1, String v2) {
                return qg.similarity(v1, v2);
            }
        };
        
        // Create and configure graph builder
        // By default, all partitioning graph builders use
        // Brute force inside the partitions
        StringMinHash builder = new StringMinHash();
        builder.setNStages(3);
        builder.setNPartitions(30);
        builder.setShingleSize(4);
        
        // Or we can use any graph builder...
        NNDescent internal_nndescent = new NNDescent();
        internal_nndescent.setDelta(0.1);
        internal_nndescent.setRho(1.0);
        builder.setInternalBuilder(internal_nndescent);
        
        // Optionnally, get some feedback
        builder.setCallback(new CallbackInterface() {

            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        builder.setK(k);
        builder.setSimilarity(similarity);
        
        // Compute graph
        HashMap<Node<String>, NeighborList> graph = builder.computeGraph(nodes);
        System.out.println("Done!");
        
        // Use Brute force to compare results
        Brute brute = new Brute();
        brute.setK(k);
        brute.setSimilarity(similarity);
        HashMap<Node, NeighborList> ground_truth = brute.computeGraph(nodes);
        
        int correct = 0;
        for (Node node : nodes) {
            correct += graph.get(node).CountCommonValues(ground_truth.get(node));
        }
        
        System.out.println("Theoretial speedup: " + 
                builder.estimatedSpeedup());
        System.out.println("Computed similarities: " + 
                builder.getComputedSimilarities());
        double speedup_ratio = 
                (double) (nodes.size() * (nodes.size() - 1) / 2) / 
                builder.getComputedSimilarities();
        System.out.println("Speedup ratio: " + speedup_ratio);
        
        double correct_ratio = (double) correct / (nodes.size() * k);
        System.out.println("Correct edges: " + correct + 
                "(" + correct_ratio * 100 + "%)");
        
        System.out.println("Quality-equivalent speedup: " 
                + speedup_ratio * correct_ratio);
    }
}
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