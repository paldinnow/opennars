package nars.util.rope.impl;

import java.util.Iterator;
import nars.util.rope.Rope;

/**
 * Variation on ConcatenationRope that provides a cheap non-iterating hashCode
 * and equality comparison. It can only safely be compared (equals() and
 * hashCode()) with other FastConcatenationRopes sharing the same structure of
 * subcomponents.
 */
public class FastConcatenationRope extends ConcatenationRope {

    private int hash = 0;

    public FastConcatenationRope(Rope left, Rope right) {
        super(left, right);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            if (length() > 0)
                hash = (31 + left.hashCode()) * 31 + right.hashCode();
            else
                hash = 1;                    
        }
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FastConcatenationRope)) {
            return false;
        }

        FastConcatenationRope o = (FastConcatenationRope) other;

        if (length() != o.length()) {
            return false;
        }
        
        if (hashCode() != o.hashCode()) {
            return false;
        }        

        if (!left.equals(o.left)) {
            return false;
        }

        return right.equals(o.right);
    }

    @Override
    public Iterator<Character> iterator() {
        return new CompoundIterator(left.iterator(), right.iterator());
    }

    @Override
    public Rope rebalance() {
        //Disable autobalancing so hash value remains immutable
        return this;
    }
}
