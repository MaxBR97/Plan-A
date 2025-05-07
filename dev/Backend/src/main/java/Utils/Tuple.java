package Utils;

/**
 * A simple replacement for SnakeYAML's Tuple class
 * This provides API compatibility with the original Tuple class
 */
public class Tuple<A, B> {
    private final A first;
    private final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first element of this tuple.
     * Standard getter consistent with Java conventions.
     */
    public A getFirst() {
        return first;
    }

    /**
     * Returns the second element of this tuple.
     * Standard getter consistent with Java conventions.
     */
    public B getSecond() {
        return second;
    }
    
    /**
     * Returns the first element of this tuple.
     * For compatibility with SnakeYAML's original Tuple API.
     */
    public A _1() {
        return first;
    }
    
    /**
     * Returns the second element of this tuple.
     * For compatibility with SnakeYAML's original Tuple API.
     */
    public B _2() {
        return second;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Tuple<?, ?> tuple = (Tuple<?, ?>) obj;
        
        if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
        return second != null ? second.equals(tuple.second) : tuple.second == null;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}