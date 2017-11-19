/*
 * The MIT License
 *
 * Copyright 2017 tibo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.debatty.java.graphs;

/**
 *
 * @author tibo
 */
public class FastSearchConfig {

    /**
     * Fast search: speedup compared to exhaustive search.
     */
    public static final double DEFAULT_SEARCH_SPEEDUP = 4.0;

    /**
     * Fast search: expansion parameter.
     */
    public static final double DEFAULT_SEARCH_EXPANSION = 1.2;

    /**
     * Fast search: number of random jumps per node (to simulate small world
     * graph).
     */
    public static final int DEFAULT_SEARCH_RANDOM_JUMPS = 2;


    /**
     * Get an instance of default search parameters.
     * @return
     */
    public static FastSearchConfig getDefault() {
        FastSearchConfig conf = new FastSearchConfig();
        conf.expansion = DEFAULT_SEARCH_EXPANSION;
        conf.speedup = DEFAULT_SEARCH_SPEEDUP;
        conf.long_jumps = DEFAULT_SEARCH_RANDOM_JUMPS;
        conf.k = 1;

        return conf;
    }

    private int k;
    private double speedup;
    private int long_jumps;
    private double expansion;

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getSpeedup() {
        return speedup;
    }

    public void setSpeedup(double speedup) {
        if (speedup <= 1.0) {
            throw new IllegalArgumentException("Speedup should be > 1.0");
        }

        this.speedup = speedup;
    }

    public int getLongJumps() {
        return long_jumps;
    }

    public void setLongJumps(int long_jumps) {
        this.long_jumps = long_jumps;
    }

    public double getExpansion() {
        return expansion;
    }

    public void setExpansion(double expansion) {
        this.expansion = expansion;
    }
}
