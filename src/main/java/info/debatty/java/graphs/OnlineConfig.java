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
public class OnlineConfig extends FastSearchConfig {

    /**
     * Depth for updating edges when adding or removing nodes.
     */
    public static final int DEFAULT_UPDATE_DEPTH = 3;

    private int update_depth = DEFAULT_UPDATE_DEPTH;

    /**
     *
     * @return
     */
    public final int getUpdateDepth() {
        return update_depth;
    }

    /**
     *
     * @param update_depth
     */
    public final void setUpdateDepth(final int update_depth) {
        this.update_depth = update_depth;
    }

    /**
     *
     * @return
     */
    public static OnlineConfig getDefault() {
        OnlineConfig conf = new OnlineConfig();
        conf.setExpansion(DEFAULT_SEARCH_EXPANSION);
        conf.setLongJumps(DEFAULT_SEARCH_RANDOM_JUMPS);
        conf.setSpeedup(DEFAULT_SEARCH_SPEEDUP);
        return conf;
    }
}
