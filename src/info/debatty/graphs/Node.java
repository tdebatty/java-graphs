package info.debatty.graphs;

/**
 *
 * @author tibo
 * @param <T> Type of value field
 */
public class Node<T>  {
    public String id = "";
    public T value;
    
    public static final String DELIMITER = "___";
    
    public Node() {
        
    }
    
    public Node(String id) {
        this.id = id;
    }
    
    public Node(String id, T value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return id.toString() + DELIMITER + value.toString();
    }
    
    public static Node parseString(String string) {
        String[] values = string.split(DELIMITER, 2);
        Node n = new Node();
        n.id = values[0];
        n.value = (Object) values[1];
        return n;
    }
}
