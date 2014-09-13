package nars.core.sense;

import java.io.Serializable;
import nars.core.Memory;
import nars.util.meter.data.DataSet;
import nars.util.meter.sensor.EventValueSensor;

/**
 *
 * @author me
 */


public class LogicSense extends AbstractSense implements Serializable {
    
    //public final Sensor CONCEPT_FIRE;
    
    public final EventValueSensor TASK_IMMEDIATE_PROCESS;
    
    public final EventValueSensor TASKLINK_FIRE;
    
    /** triggered at beginning of StructuralRules.reason(), entry point of inference.
        counts invocation and records priority of termlink parameter.
     */
    public final EventValueSensor REASON; 
    
    /**
       triggered for each StructuralRules.contraposition().
       counts invocation and records complexity of statement parameter      
     */
    public final EventValueSensor CONTRAPOSITION; 
    
    
    public final EventValueSensor TASK_ADD_NEW;
    public final EventValueSensor TASK_DERIVED;
    public final EventValueSensor TASK_EXECUTED;
    
    public final EventValueSensor CONCEPT_NEW;
    
    public final EventValueSensor JUDGMENT_PROCESS;
    public final EventValueSensor GOAL_PROCESS;
    public final EventValueSensor QUESTION_PROCESS;
    public final EventValueSensor LINK_TO_TASK;
    
    //public final ThreadBlockTimeTracker CYCLE_BLOCK_TIME;
    private long conceptNum;
    private double conceptPriorityMean;
    //private Object conceptPrioritySum;
    private long conceptBeliefsSum;
    private long conceptQuestionsSum;               
    
    public final EventValueSensor BELIEF_REVISION;
    public final EventValueSensor DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS;
    public final EventValueSensor DED_SECOND_LAYER_VARIABLE_UNIFICATION;
    public final EventValueSensor DED_CONJUNCTION_BY_QUESTION;
    public final EventValueSensor ANALOGY;
    public final EventValueSensor IO_INPUTS_BUFFERED;
    public final EventValueSensor TASK_ADD_NOVEL;
    public final EventValueSensor SHORT_TERM_MEMORY_UPDATE;
    public final EventValueSensor DERIVATION_LATENCY;
    

    public LogicSense() {
        super();
        
        add(TASK_IMMEDIATE_PROCESS = new EventValueSensor("task.immediate_process"));
        TASK_IMMEDIATE_PROCESS.setSampleWindow(32);
        
        add(TASKLINK_FIRE = new EventValueSensor("tasklink.fire"));
        TASKLINK_FIRE.setSampleWindow(32);
        
        add(CONCEPT_NEW = new EventValueSensor("concept.new"));
        CONCEPT_NEW.setSampleWindow(32);
        
        add(REASON = new EventValueSensor("reason.tasktermlinks"));
        REASON.setSampleWindow(32);
        
        add(CONTRAPOSITION = new EventValueSensor("reason.contraposition"));
        CONTRAPOSITION.setSampleWindow(32);

        add(TASK_ADD_NEW = new EventValueSensor("task.new.add"));
        TASK_ADD_NEW.setSampleWindow(32);
        add(TASK_DERIVED = new EventValueSensor("task.derived"));
        TASK_DERIVED.setSampleWindow(32);
        add(TASK_EXECUTED = new EventValueSensor("task.executed"));
        TASK_EXECUTED.setSampleWindow(32);
        
        add(TASK_ADD_NOVEL = new EventValueSensor("task.novel.add"));

        add(JUDGMENT_PROCESS = new EventValueSensor("judgment.process"));
        add(GOAL_PROCESS = new EventValueSensor("goal.process"));
        add(QUESTION_PROCESS = new EventValueSensor("question.process"));
        add(LINK_TO_TASK = new EventValueSensor("task.link_to"));
        
        add(BELIEF_REVISION = new EventValueSensor("reason.belief.revised"));
        add(DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS = new EventValueSensor("reason.ded2ndunifterms"));
        add(DED_SECOND_LAYER_VARIABLE_UNIFICATION = new EventValueSensor("reason.ded2ndunif"));
        add(DED_CONJUNCTION_BY_QUESTION = new EventValueSensor("reason.dedconjbyquestion"));
        add(ANALOGY = new EventValueSensor("reason.analogy"));
        
        add(IO_INPUTS_BUFFERED = new EventValueSensor("io.inputs.buffered"));
        
        add(SHORT_TERM_MEMORY_UPDATE = new EventValueSensor("exec.short_term_memory.update"));
        
        add(DERIVATION_LATENCY = new EventValueSensor("reason.derivation.latency"));
        DERIVATION_LATENCY.setSampleWindow(64);
    }
    
    @Override
    public void sense(Memory memory) {
        put("concept.count", conceptNum);
        put("concept.priority.mean", conceptPriorityMean);
        put("concept.beliefs.mean", conceptNum > 0 ? ((double)conceptBeliefsSum)/conceptNum : 0);
        put("concept.questions.mean", conceptNum > 0 ? ((double)conceptQuestionsSum)/conceptNum : 0);
        
        put("task.novel.total", memory.novelTasks.size());
        //put("memory.newtasks.total", memory.newTasks.size()); //redundant with output.tasks below

        //TODO move to EmotionState
        put("emotion.happy", memory.emotion.happy());
        put("emotion.busy", memory.emotion.busy());

        
        {
            DataSet fire = TASKLINK_FIRE.get();
            //DataSet reason = TASKLINK_REASON.get();
            put("reason.fire.tasklink.priority.mean", fire.mean());
            put("reason.fire.tasklinks", TASKLINK_FIRE.getHits());
            
            putHits(REASON);
            
            //only makes sense as a mean, since it occurs multiple times during a cycle
            put("reason.tasktermlink.priority.mean", REASON.get().mean());                        
        }
        {
            putValue(IO_INPUTS_BUFFERED);
        }
        {            
            putHits(CONTRAPOSITION);
            
            //put("reason.contrapositions.complexity.mean", CONTRAPOSITION.get().mean());
            
            putHits(BELIEF_REVISION);
            put("reason.ded_2nd_layer_variable_unification_terms", DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS.getHits());
            put("reason.ded_2nd_layer_variable_unification", DED_SECOND_LAYER_VARIABLE_UNIFICATION.getHits());
            put("reason.ded_conjunction_by_question", DED_CONJUNCTION_BY_QUESTION.getHits());
            
            putHits(ANALOGY);
        }
        {
            DataSet d = DERIVATION_LATENCY.get();
            double min = d.min();
            if (!Double.isFinite(min)) min = 0;
            double max = d.max();
            if (!Double.isFinite(max)) max = 0;
            
            put(DERIVATION_LATENCY.name() + ".min", min);
            put(DERIVATION_LATENCY.name() + ".max", max);
            put(DERIVATION_LATENCY.name() + ".mean", d.mean());
        }
        {
            putHits(TASK_ADD_NEW);
            putHits(TASK_ADD_NOVEL);
            put("task.new.priority.mean", TASK_ADD_NEW.get().mean());
            put("task.derived", TASK_DERIVED.getHits());
            put("task.derived.priority.mean", TASK_DERIVED.get().mean());
            put("task.executed", TASK_EXECUTED.getHits());
            put("task.executed.priority.mean", TASK_EXECUTED.get().mean());
            put("task.immediate_processed", TASK_IMMEDIATE_PROCESS.getHits());
            //put("task.immediate_processed.priority.mean", TASK_IMMEDIATE_PROCESS.get().mean());
        }
        {
            put("task.link_to", LINK_TO_TASK.getHits());
            put("task.goal.process", GOAL_PROCESS.getHits());
            put("task.judgment.process", JUDGMENT_PROCESS.getHits());
            put("task.question.process", QUESTION_PROCESS.getHits());            
        }
        {
            put("concept.new", CONCEPT_NEW.getHits());
            put("concept.new.complexity.mean", CONCEPT_NEW.get().mean());
        }
        
        putHits(SHORT_TERM_MEMORY_UPDATE);
    }
    
    public void putValue(final EventValueSensor s) {
        put(s.getName(), s.getValue());
    }
    public void putHits(final EventValueSensor s) {
        put(s.getName(), s.getHits());
    }
    public void putMean(final EventValueSensor s) {
        put(s.getName(), s.get().mean());
    }

    public void setConceptBeliefsSum(long conceptBeliefsSum) {
        this.conceptBeliefsSum = conceptBeliefsSum;
    }

    public void setConceptNum(long conceptNum) {
        this.conceptNum = conceptNum;
    }

    public void setConceptPriorityMean(double conceptPriorityMean) {
        this.conceptPriorityMean = conceptPriorityMean;
    }

//    public void setConceptPrioritySum(double conceptPrioritySum) {
//        this.conceptPrioritySum = conceptPrioritySum;
//    }

    public void setConceptQuestionsSum(long conceptQuestionsSum) {
        this.conceptQuestionsSum = conceptQuestionsSum;
    }
    
    
}