package info.debatty.graphs;

/**
 *
 * @author tibo
 */
public class Node implements Comparable<Node>, Cloneable {
    public String id = "";
    public Object value;
    
    public static final String DELIMITER = "___";
    
    public Node() {
        
    }
    
    public Node(String id, Object value) {
        this.id = id;
        this.value = value;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public int compareTo(Node other) {
        return id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object other) {
        if (!other.getClass().getName().equals(this.getClass().getName())) {
            return false;
        }
        
        Node other_node = (Node) other;
        return other_node.id.equals(this.id);
    }    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
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
