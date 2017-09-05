# java-graphs

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.debatty/java-graphs/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.debatty/java-graphs) [![Build Status](https://travis-ci.org/tdebatty/java-graphs.svg?branch=master)](https://travis-ci.org/tdebatty/java-graphs) [![Coverage Status](https://coveralls.io/repos/tdebatty/java-graphs/badge.svg?branch=master&service=github)](https://coveralls.io/github/tdebatty/java-graphs?branch=master) [![Javadocs](http://www.javadoc.io/badge/info.debatty/java-graphs.svg)](http://www.javadoc.io/doc/info.debatty/java-graphs)

Java implementation of various algorithms that build and process k-nearest neighbors graph (k-nn graph).

Graph building algorithms:
* (Multi-threaded) Brute-force: works with any similarity measure;
* (Multi-threaded) NN-Descent: works with any similarity measure;
* Online graph building, as published in ["Fast Online k-nn Graph Building"](http://arxiv.org/abs/1602.06819);
* NNCTPH, as published in ["Building k-nn graphs from large text data"](http://dx.doi.org/10.1109/BigData.2014.7004276), for text datasets;



Implemented processing algorithms:
* Dijkstra algorithm to compute the shortest path between two nodes;
* Improved Graph based Nearest Neighbor Search (iGNNS) algorithm, as published in ["Fast Online k-nn Graph Building"](http://arxiv.org/abs/1602.06819);
* Pruning (remove all edges for which the similarity is less than a threshold);
* Tarjan's algorithm to compute strongly connected subgraphs (where every node is reachable from every other node);
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

        // Create the nodes
        ArrayList<Integer> nodes = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(r.nextInt(10 * count));
        }

        // Instantiate and configure the build algorithm
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

        // Run the algorithm and get computed graph
        Graph<Integer> graph = builder.computeGraph(nodes);

        // Display neighborlists
        for (Integer n : nodes) {
            NeighborList nl = graph.getNeighbors(n);
            System.out.print(n);
            System.out.println(nl);
        }

        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);

        // Analyze the graph:
        // Count number of connected components
        System.out.println(graph.connectedComponents().size());

        // Search a query (fast approximative algorithm)
        System.out.println(graph.fastSearch(r.nextInt(10 * count), 1));

        // Count number of strongly connected components
        System.out.println(graph.stronglyConnectedComponents().size());

        // Now we can add a node to the graph (using a fast approximate algorithm)
        graph.fastAdd(r.nextInt(10 * count));
    }
}
```

This will produce something like:

```
...
{computed_similarities=58141, computed_similarities_ratio=0.1163983983983984, c=4426, iterations=5}
{computed_similarities=69126, computed_similarities_ratio=0.1383903903903904, c=3962, iterations=6}
{computed_similarities=80369, computed_similarities_ratio=0.1608988988988989, c=3575, iterations=7}
{computed_similarities=91560, computed_similarities_ratio=0.1833033033033033, c=2777, iterations=8}
{computed_similarities=102698, computed_similarities_ratio=0.2056016016016016, c=2074, iterations=9}
{computed_similarities=114014, computed_similarities_ratio=0.22825625625625626, c=1317, iterations=10}
Theoretical speedup: 1.0
Computed similarities: 114014
Speedup ratio: 4.381040924798709
Correct edges: 8220 (82.19999999999999%)
Quality-equivalent speedup: 3.6012156401845385
14
[(6181,0.06666666666666667)]
26

```

Check the [documentation](http://api123.io/api/java-graphs/head/index.html) or the [examples](https://github.com/tdebatty/java-graphs/tree/master/src/main/java/info/debatty/java/graphs/examples) for other building and processing possibilities...
