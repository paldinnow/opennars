package nars.core.build;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import nars.core.Attention;
import nars.core.Build;
import nars.core.Memory;
import nars.core.Memory.Forgetting;
import nars.core.Memory.Timing;
import nars.core.NAR;
import nars.core.Param;
import nars.core.Parameters;
import static nars.core.build.Default.InternalExperienceMode.Full;
import static nars.core.build.Default.InternalExperienceMode.Minimal;
import nars.core.control.DefaultAttention;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.io.DefaultTextPerception;
import nars.io.TextInput;
import nars.language.Term;
import nars.operator.Operator;
import nars.operator.mental.Anticipate;
import nars.plugin.app.plan.TemporalParticlePlanner;
import nars.plugin.input.PerceptionAccel;
import nars.plugin.mental.Abbreviation;
import nars.plugin.mental.Counting;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.mental.RuntimeNARSettings;
import nars.storage.Bag;
import nars.storage.CacheBag;
import nars.storage.LevelBag;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Default extends Build implements ConceptBuilder {

    

    
    int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    int taskLinkBagSize;
    
    int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    int termLinkBagSize;
    
    /** determines maximum number of concepts */
    int conceptBagSize;    
    
    int conceptBagLevels;
    
    /** max # subconscious "subconcept" concepts */
    int subconceptBagSize;

    /** Size of TaskBuffer */
    int taskBufferSize;
    
    int taskBufferLevels;
    
    public static enum InternalExperienceMode {
        None, Minimal, Full
    }
    
    InternalExperienceMode internalExperience = InternalExperienceMode.Minimal;
        
    
    transient TemporalParticlePlanner pluginPlanner = null;

    
    public Default() {
        super();
    
        this.type = "level";
        
       // temporalPlanner(8, 64, 16);
        
        setConceptBagSize(1000);        
        setSubconceptBagSize(0);
        setConceptBagLevels(100);
        
        setTaskLinkBagSize(20);
        setTaskLinkBagLevels(100);

        setTermLinkBagSize(100);
        setTermLinkBagLevels(100);
        
        setNovelTaskBagSize(10);
        setNovelTaskBagLevels(100);

        
        param.duration.set(Parameters.DURATION);
        param.conceptForgetDurations.set(2.0);
        param.taskLinkForgetDurations.set(4.0);
        param.termLinkForgetDurations.set(10.0);
        param.novelTaskForgetDurations.set(2.0);
                
        param.conceptBeliefsMax.set(7);
        param.conceptGoalsMax.set(7);
        param.conceptQuestionsMax.set(5);
        
        param.conceptsFiredPerCycle.set(1);
        
        param.termLinkMaxReasoned.set(3);
        param.termLinkMaxMatched.set(10);
        param.termLinkRecordLength.set(10);
        
        param.setForgetting(Forgetting.Iterative);
        param.setTiming(Timing.Iterative);
        param.noiseLevel.set(100);

        param.reliance.set(0.9f);
        
        param.decisionThreshold.set(0.10);
    
        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());
    }

    

    public Default temporalPlanner(float searchDepth, int planParticles, int inlineParticles) {
        pluginPlanner = new TemporalParticlePlanner(searchDepth, planParticles, inlineParticles);
        return this;
    }

    @Override
    public NAR init(NAR n) {
        
        for (Operator o : DefaultOperators.get())
            n.memory.addOperator(o);
        for (Operator o : ExampleOperators.get())
            n.memory.addOperator(o);
        
                
        n.addPlugin(new DefaultTextPerception());
        
        n.addPlugin(new RuntimeNARSettings());
        
        n.addPlugin(new PerceptionAccel());
        
       // n.addPlugin(new TemporalParticlePlanner());
        
        if(pluginPlanner!=null) {
            n.addPlugin(pluginPlanner);
        }
        
        n.addPlugin(new Anticipate());      // expect an event
        
        if (internalExperience==Minimal) {            
            n.addPlugin(new InternalExperience());
        }
        else if (internalExperience==Full) {            
            n.addPlugin(new FullInternalExperience());
            n.addPlugin(new Abbreviation());
            n.addPlugin(new Counting());
        }
        
        return n;
    }


    ConceptBuilder getConceptBuilder() {
        return this;
    }

    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {        
        Bag<TaskLink,Task> taskLinks = new LevelBag<>(getTaskLinkBagLevels(), getConceptTaskLinks());
        Bag<TermLink,TermLink> termLinks = new LevelBag<>(getTermLinkBagLevels(), getConceptTermLinks());
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }

    
    public Bag<Concept,Term> newConceptBag() {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize());
    }
    
    CacheBag<Term,Concept> newSubconceptBag() {        
        if (getSubconceptBagSize() == 0) return null;
        return new CacheBag(getSubconceptBagSize());
    }

    @Override
    public Attention newAttention() {
        return new DefaultAttention(newConceptBag(), newSubconceptBag(), getConceptBuilder());
    }
    
    public Bag<Task<Term>,Sentence<Term>> newNovelTaskBag() {
        return new LevelBag<>(getNovelTaskBagLevels(), getNovelTaskBagSize());
    }

    public Default setSubconceptBagSize(int subconceptBagSize) {
        this.subconceptBagSize = subconceptBagSize;
        return this;
    }
    public int getSubconceptBagSize() {
        return subconceptBagSize;
    }
 
    
    
    public int getConceptBagSize() { return conceptBagSize; }    
    public Default setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    
    
    public int getConceptBagLevels() { return conceptBagLevels; }    
    public Default setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
        
    /**
     * @return the taskLinkBagLevels
     */
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }
       
    public Default setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }

    public Default setNovelTaskBagSize(int taskBufferSize) {
        this.taskBufferSize = taskBufferSize;
        return this;
    }

    public int getNovelTaskBagSize() {
        return taskBufferSize;
    }
    
    public Default setNovelTaskBagLevels(int l) {
        this.taskBufferLevels = l;
        return this;
    }

    public int getNovelTaskBagLevels() {
        return taskBufferLevels;
    }
    

    public int getConceptTaskLinks() {
        return taskLinkBagSize;
    }

    public Default setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public Default setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }

    public int getConceptTermLinks() {
        return termLinkBagSize;
    }

    public Default setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    
    public Default realTime() {
        param.setTiming(Timing.Real);
        param.setForgetting(Forgetting.Periodic);
        return this;
    }
    public Default simulationTime() {
        param.setTiming(Timing.Simulation);
        param.setForgetting(Forgetting.Periodic);
        return this;
    }

    
    
    public static class CommandLineNARBuilder extends Default {
        
        List<String> filesToLoad = new ArrayList();
        
        public CommandLineNARBuilder(String[] args) {
            super();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--silence".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.noiseLevel.set(100-sl);
                }
                else if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.noiseLevel.set(sl);
                }    
                else {
                    filesToLoad.add(arg);
                }
                
            }        
        }

        @Override
        public NAR init(NAR n) {
            n = super.init(n); 
            
            for (String x : filesToLoad) {
                try {
                    n.addInput( new TextInput(new File(x) ) );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                n.run(1);
            }
            
            return n;
        }

        
        
        
        /**
         * Decode the silence level
         *
         * @param param Given argument
         * @return Whether the argument is not the silence level
         */
        public static boolean isReallyFile(String param) {
            return !"--silence".equals(param);
        }
    }

    public InternalExperienceMode getInternalExperience() {
        return internalExperience;
    }

    public Default setInternalExperience(InternalExperienceMode internalExperience) {
        this.internalExperience = internalExperience;
        return this;
    }

    
    
    public static Default fromJSON(String filePath) {
        
        try {
            String c = readFile(filePath, Charset.defaultCharset());                        
            return Param.json.fromJson(c, Default.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static String readFile(String path, Charset encoding) 
        throws IOException  {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
      }
}
