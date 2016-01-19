# java-graphs

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.debatty/java-graphs/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.debatty/java-graphs) [![Build Status](https://travis-ci.org/tdebatty/java-graphs.svg?branch=master)](https://travis-ci.org/tdebatty/java-graphs) [![Coverage Status](https://coveralls.io/repos/tdebatty/java-graphs/badge.svg?branch=master&service=github)](https://coveralls.io/github/tdebatty/java-graphs?branch=master) [![API](http://api123.web-d.be/api123-head.svg)](http://api123.web-d.be/api/java-graphs/head/index.html)

Java implementation of various algorithms that build and proces k-nearest neighbors graph (k-nn graph).

Some of these algorithms build a k-nn graph independantly of the data type and similarity metric:
* Brute-force
* (Multi-threaded) NN-Descent

Implemented processing algorithms:
* Dijkstra algorithm to compute the shortest path between two nodes.
* Graph Nearest Neighbor Search (GNNS) algorithm from paper "Fast Approximate Nearest-Neighbor Search with k-Nearest Neighbor Graph" by Hajebi et al. This algorithm uses a k-nn graph to efficiently search the most similar node of a query point.
* Pruning (remove all edges for which the similarity is less than a threshold).
* Tarjan's algorithm to compute strongly connected subgraphs (where every node is reachable from every other node).
* Weakly connected components.

For the complete list, check the [documentation](http://api123.io/api/java-graphs/head/index.html) or the [examples](https://github.com/tdebatty/java-graphs/tree/master/src/main/java/info/debatty/java/graphs/examples).


## Installation

Using maven:
```
<dependency>
    <groupId>info.debatty</groupId>
    <artifactId>java-graphs</artifactId>
    <version>RELEASE</version>
</dependency>
```

Or from the [releases page](https://github.com/tdebatty/java-graphs/releases).

## Quick start

Most of the time, all you have to do is:

1. Create the nodes
2. Choose and configure the graph builder (mainly the similarity to use)
3. Compute the graph
4. Process the graph...

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
        
        // 1. Create the nodes
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), r.nextInt(10 * count)));
        }
        
        // 2. Instantiate and configure the build algorithm
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
        
        // 3. Run the algorithm and get computed graph
        Graph<Integer> graph = builder.computeGraph(nodes);
        
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
        
        // 4. Analyze the graph:
        // Count number of connected components
        System.out.println(graph.connectedComponents().size());
        
        // Search a query (fast approximative algorithm)
        System.out.println(graph.search(r.nextInt(10 * count), 1));
        
        // Count number of strongly connected components
        System.out.println(graph.stronglyConnectedComponents().size());
        
        // Convert the graph to an online graph (to which we can add new nodes)
        OnlineGraph<Integer> online_graph = new OnlineGraph<Integer>(graph);
        
        // Now we can add a node to the graph (using a fast approximate algorithm)
        online_graph.addNode(
                new Node<Integer>("my new node 1", r.nextInt(10 * count)));
    }
}
```

This will produce something like:

```
{computed_similarities=64361, c=4542, iterations=6, computed_similarities_ratio=0.12885085085085085}
{computed_similarities=75008, c=4031, iterations=7, computed_similarities_ratio=0.15016616616616615}
{computed_similarities=86254, c=3201, iterations=8, computed_similarities_ratio=0.17268068068068068}
{computed_similarities=97291, c=2302, iterations=9, computed_similarities_ratio=0.19477677677677677}
{computed_similarities=108458, c=1634, iterations=10, computed_similarities_ratio=0.21713313313313312}
Theoretical speedup: 1.0
Computed similarities: 108458
Speedup ratio: 4.605469398292427
Correct edges: 8180 (81.8%)
Quality-equivalent speedup: 3.767273967803205
6
[(523,9520,0.08333333333333333)]
12

```

Check the [documentation](http://api123.io/api/java-graphs/head/index.html) or the [examples](https://github.com/tdebatty/java-graphs/tree/master/src/main/java/info/debatty/java/graphs/examples) for other building and processing possibilities...
