package nars.logic.reason.concept;

import nars.logic.CompositionalRules;
import nars.logic.reason.ConceptFire;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;

/**
* Created by me on 2/7/15.
*/
public class DeduceConjunctionByQuestion extends ConceptFireTaskTerm {

    @Override
    public boolean apply(ConceptFire f, TaskLink taskLink, TermLink termLink) {
        if (f.getCurrentBelief()!=null)
            CompositionalRules.dedConjunctionByQuestion(
                    taskLink.getSentence(), f.getCurrentBelief(), f);
        return true;
    }
}