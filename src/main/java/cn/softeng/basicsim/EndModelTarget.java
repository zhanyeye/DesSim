package cn.softeng.basicsim;

import cn.softeng.events.ProcessTarget;

/**
 * @date: 12/23/2020 8:56 PM
 */
public class EndModelTarget extends ProcessTarget {
    @Override
    public void process() {
        Simulation.endRun();
    }

    @Override
    public String getDescription() {
        return "SimulationEnd";
    }
}
