#java-graphs

Java implementation of various algorithms that build k-nearest neighbors graph (k-nn graph).

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
package info.debatty.java.graphs.examples;

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
        
        // Optionnallly, define a callback to get some feedback...
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
        
        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);
    }   
}
```

This will produce something like:

```
...
{computed_similarities=55217, c=4898, iterations=5, computed_similarities_ratio=0.11054454454454454}
{computed_similarities=65261, c=4372, iterations=6, computed_similarities_ratio=0.13065265265265266}
{computed_similarities=76109, c=3774, iterations=7, computed_similarities_ratio=0.15237037037037038}
{computed_similarities=86955, c=3024, iterations=8, computed_similarities_ratio=0.17408408408408407}
{computed_similarities=98366, c=2277, iterations=9, computed_similarities_ratio=0.19692892892892894}
{computed_similarities=109581, c=1491, iterations=10, computed_similarities_ratio=0.21938138138138139}
Theoretial speedup: 1.0
Computed similarities: 109581
Speedup ratio: 4.5582719632053
Correct edges: 8379(83.78999999999999%)
Quality-equivalent speedup: 3.8193760779697206

```
##Multi-threaded NN-Descent
The library also implements a multi-threaded version of NN-Descent:

```java
import info.debatty.java.graphs.*;
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
        builder.setMaxIterations(20);
        builder.setDelta(0.1);
        builder.setRho(0.5);
        
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
        
        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);
    }
}
```

##MinHash
Builds an approximate knn graph from strings using LSH MinHash algorithm.

The graph builder first creates a dictionary of all k-shingles (sequences of k characters) in the dataset. Then each string is converted into a set of k-shingles. The MinHash algorithm is used to bin the input strings into buckets, where similar strings (with a high Jaccard index) have a high probability to fall in the same bucket.

Inside each bucket, a classical graph building algorithm (Brute force or NN-Descent) can be used.

This algorithm is thus best used when the strings are considered as sets of n-grams (sequences of n characters), and the similarity between strings is computed using the Jaccard index (|A ∩ B| / |A ∪ B|)

```java
import info.debatty.java.graphs.CallbackInterface;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.graphs.build.StringMinHash;
import info.debatty.java.stringsimilarity.Jaccard;
import java.util.HashMap;
import java.util.List;


public class StringMinHashExample {

    public static void main(String[] args) {
        
        // Read the nodes from file...
        List<Node<String>> nodes = GraphBuilder.readFile("/home/tibo/Desktop/726-unique-spams");
        
        // Number of edges per node (the k in k-nn graph)
        int k = 10;
        
        // MinHash is the recommended LSH binning algorithm for Jaccard index
        // similarity
        SimilarityInterface similarity = new SimilarityInterface<String>() {
            Jaccard jaccard = new Jaccard(4);

            public double similarity(String v1, String v2) {
                return jaccard.similarity(v1, v2);
            }
        };
        
        // Create and configure graph builder
        // By default, all partitioning graph builders use 
        // Brute force inside the partitions
        StringMinHash builder = new StringMinHash();
        builder.setNStages(3);
        builder.setNPartitions(8);
        builder.setShingleSize(4);
        
        // Or we can use any graph builder...
        //NNDescent internal_nndescent = new NNDescent();
        //internal_nndescent.setDelta(0.1);
        //internal_nndescent.setRho(1.0);
        //builder.setInternalBuilder(internal_nndescent);
        
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
        
        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);
    }
}
```

## Super-Bit
Super-Bit is an improvement of Random Projection, an algorithm from the LSH family that will bin items together with a high probability if they have a high cosine similarity.

This graph builder works by converting the strings into a vector indicating the number of occurences of each n-gram (sequence of n characters). This is sometimes called the q-gram profile of the string. Then SuperBit LSH is used to bin similar strings (with a high cosine similarity) in the same bucket. Finally, another graph building algorithm (Brute-force or NN-Descent) is used to build the subgraphs inside the buckets.

This builder is thus best used when the strings are considered as sets of n-grams, and the cosine similarity is used.

```java
import info.debatty.java.graphs.*;
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.graphs.build.StringSuperBit;
import info.debatty.java.stringsimilarity.Cosine;
import java.util.HashMap;
import java.util.List;


public class StringSuperBitExample {

    public static void main(String[] args) {
        // Read the nodes from file...
        List<Node<String>> nodes = GraphBuilder.readFile("/home/tibo/Desktop/726-unique-spams");
        
        // Number of edges per node (the k in k-nn graph)
        int k = 10;
        
        // SuperBit is the recommended LSH algorihm when the similarity metric
        // used is cosine similarity
        SimilarityInterface similarity = new SimilarityInterface<String>() {
            Cosine cosine = new Cosine(4);

            public double similarity(String v1, String v2) {
                return cosine.similarity(v1, v2);
            }
        };
        
        // Create and configure graph builder
        // By default, all partitioning graph builders use 
        // Brute force inside the partitions
        StringSuperBit builder = new StringSuperBit();
        builder.setNStages(2);
        builder.setNPartitions(4);
        builder.setShingleSize(4);
        
        // Or we can use any graph builder...
        //NNDescent internal_nndescent = new NNDescent();
        //internal_nndescent.setDelta(0.1);
        //internal_nndescent.setRho(1.0);
        //builder.setInternalBuilder(internal_nndescent);
        
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
        
        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);
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