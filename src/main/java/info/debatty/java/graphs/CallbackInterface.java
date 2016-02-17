package info.debatty.java.graphs;

import java.util.HashMap;

/**
 *
 * @author Thibault Debatty
 */
public interface CallbackInterface {

    /**
     *
     * @param data hashmap containing info about current state of algorithm
     */
    void call(HashMap<String, Object> data);
}
