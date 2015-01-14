package nars.core.build;

import nars.core.Core;
import nars.core.control.experimental.AntCore;
import nars.logic.entity.Concept;
import nars.logic.entity.Term;
import nars.util.bag.Bag;

/**
 *
 * https://en.wikipedia.org/wiki/Neuromorphic_engineering
 */
public class Neuromorphic extends Curve {
    private int numAnts;

    public Neuromorphic(int numAnts) {
        super();        
        this.type = "neuromorphic";
        this.numAnts = numAnts;
    }

    @Override
    public Core newAttention() {
        if (numAnts == -1)
            numAnts = param.conceptsFiredPerCycle.get();
        return new AntCore(numAnts, 2.0f, getConceptBagSize(), getConceptBuilder());
    }

    
    @Override
    public Bag<Concept, Term> newConceptBag() {
        /** created by AntAttention */
        return null;
    }

    /*
    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {
        
        DelayBag<TaskLink,Task> taskLinks = new FairDelayBag(
                param.taskLinkForgetDurations, getConceptTaskLinks()) {

            
        };
        taskLinks.setMemory(m);
        
        DelayBag<TermLink,TermLink> termLinks = new FairDelayBag(
                param.termLinkForgetDurations, getConceptTermLinks());
        
        termLinks.setMemory(m);
        
        return new Concept(b, t, taskLinks, termLinks, m);
    }*/

    /*
    @Override
    public Bag<Task<Term>, Sentence<Term>> newNovelTaskBag() {
        return new FairDelayBag(param.novelTaskForgetDurations, taskBufferSize);
    }*/

    
    
    
}