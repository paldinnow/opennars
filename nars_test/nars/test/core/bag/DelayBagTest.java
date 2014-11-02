/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core.bag;

import nars.core.NAR;
import nars.core.build.NeuromorphicNARBuilder;
import nars.storage.DelayBag;
import static nars.test.core.bag.BagOperationsTest.testBagSequence;
import org.junit.Test;

/**
 *
 * @author me
 */
public class DelayBagTest {
    
    @Test 
    public void testIO() {
        NAR n = new NeuromorphicNARBuilder().build();
        DelayBag b = new DelayBag(1000);
        b.setMemory(n.memory);
        
        testBagSequence(b);
        
    }
}