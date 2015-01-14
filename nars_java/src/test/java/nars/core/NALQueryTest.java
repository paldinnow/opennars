/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.core.build.Default;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.io.Answered;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static nars.logic.nal7.Tense.Eternal;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class NALQueryTest {
        
    
    @Ignore
    @Test
    public void testQuery1() throws Narsese.InvalidInputException {
        testQueryAnswered(0, 16);
    }
    @Test
    public void testQuery2() throws Narsese.InvalidInputException {
        testQueryAnswered(16, 0);
    }

    public void testQueryAnswered(int cyclesBeforeQuestion, int cyclesAfterQuestion) throws Narsese.InvalidInputException {
        
        final AtomicBoolean b = new AtomicBoolean(false);
        
        String question = cyclesBeforeQuestion == 0 ?
                "<a --> b>" /* unknown solution to be derived */ : 
                "<b --> a>" /* existing solution, to test finding existing solutions */;
        
        NAR n = new NAR(new Default().setInternalExperience(null));
        new TextOutput(n, System.out);
                
                n.believe("<a <-> b>", Eternal, 1.0f, 0.5f).
                
                believe("<b --> a>", Eternal, 1.0f, 0.5f).      
                
                run(cyclesBeforeQuestion).
                
                ask(question, new Answered() {

                    @Override
                    public void onSolution(Sentence belief) {
                        //System.out.println("solution: " + belief);
                        b.set(true);
                        off();
                    }

                    @Override
                    public void onChildSolution(Task child, Sentence belief) {
                        //System.out.println("  child: " + 
                        //child + " solution: " + belief);
                    }            
                }).
                
                run(cyclesAfterQuestion);
                
        assertTrue(b.get());
        
    }
}
