package nars.analyze.meter;

import nars.core.AbstractPlugin;
import nars.core.Events;
import nars.core.NAR;
import nars.io.condition.OutputCondition;
import nars.io.meter.Metrics;
import nars.io.meter.event.HitMeter;
import nars.logic.entity.Task;
import nars.util.data.CuckooMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* Created by me on 2/10/15.
*/
public class CountDerivationCondition extends AbstractPlugin {

    //SM = success method
    final static String methodInvolvedInSuccessfulDerivation_Prefix = "D";
    final static String methodInvolvedInDerivation_Prefix = "d";

    boolean includeNonSuccessDerivations = true;

    private final Metrics metrics;
    final Map<Task, StackTraceElement[]> derived = new CuckooMap();
    final List<OutputCondition> successesThisCycle = new ArrayList();

    public CountDerivationCondition(Metrics m) {
        super();
        this.metrics = m;
    }

    @Override
    public Class[] getEvents() {
        return new Class[] { Events.TaskDerive.class, OutputCondition.class, Events.CycleEnd.class };
    }

    @Override public void onEnabled(NAR n) {       }

    @Override public void onDisabled(NAR n) {        }

    @Override
    public void event(Class event, Object[] args) {

        if (event == OutputCondition.class) {

            OutputCondition o = (OutputCondition) args[0];

            if (!o.succeeded) {
                throw new RuntimeException(o + " signaled when it has not succeeded");
            }

            //buffer to calculate at end of cycle when everything is collected
            successesThisCycle.add(o);

        }
        else if (event == Events.TaskDerive.class) {
            Task t = (Task)args[0];
            derived.put(t, Thread.currentThread().getStackTrace());
        }
        else if (event == Events.CycleEnd.class) {

            /** usually true reason tasks should only be one, because
             * this event will be triggered only the first time it has
             * become successful. */
            for (OutputCondition o : successesThisCycle) {
                for (Task tt : o.getTrueReasons()) {
                    traceStack(tt, true);
                }
            }

            /** any successful derivations will be counted again in the
             * general meters */
            if (includeNonSuccessDerivations) {
                for (Task x : derived.keySet()) {
                    traceStack(x, false);
                }
            }

            //reset everything for next cycle
            derived.clear();
            successesThisCycle.clear();
        }
    }

    public void traceStack(Task t, boolean success) {
        StackTraceElement[] s = derived.get(t);
        if (s == null) {
            //probably a non-derivation condition, ex: immediate reaction to an input event, etc.. or execution
            //throw new RuntimeException("A stackTrace for successful output condition " + t + " was not recorded");
            return;
        }

        String prefix;
        if (success)
            prefix = methodInvolvedInSuccessfulDerivation_Prefix;
        else
            prefix = methodInvolvedInDerivation_Prefix;

        boolean tracing = false;
        String prevMethodID = null;
        for (int i = 0; i < s.length; i++) {
            StackTraceElement e = s[i];

            String className = e.getClassName();
            String methodName = e.getMethodName();


            if (tracing && className.contains(".ConceptFireTask") && methodName.equals("accept")) {
                tracing = false;
            }

            if (tracing) {
                int cli = className.lastIndexOf(".") + 1;
                if (cli!=-1)
                    className = className.substring(cli, className.length()); //class's simpleName

                String methodID = className + '_' + methodName;
                String sm = prefix + '_' + methodID;

                HitMeter m = (HitMeter) metrics.getMeter(sm);
                if (m == null) {
                    metrics.addMeter(m = new HitMeter(sm));
                }
                m.hit();

                if (prevMethodID!=null)
                    traceMethodCall(prevMethodID, methodID, success);

                prevMethodID = methodID;
            }
            else if (className.endsWith(".NAL") && methodName.equals("deriveTask")) {
                tracing = true; //begins with next stack element
            }
        }
    }

    protected void traceMethodCall(String prevMethodID, String methodID, boolean success) {


    }
}
