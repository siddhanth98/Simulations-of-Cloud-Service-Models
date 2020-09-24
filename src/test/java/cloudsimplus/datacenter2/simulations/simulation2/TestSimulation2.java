package cloudsimplus.datacenter2.simulations.simulation2;

import static org.junit.Assert.*;

import cloudsimplus.datacenter2.simulations.Siddhanth_Venkateshwaran_Datacenter2;
import org.junit.Test;

public class TestSimulation2 {
    private Siddhanth_Venkateshwaran_Datacenter2 simulation;

    /**
     * This function will test whether new VM requests are made to the broker when CPU usage of any VM
     * goes above 70%.
     */
    @Test
    public void testVmCpuUsage() {
        simulation = new Siddhanth_Venkateshwaran_Datacenter2();
        simulation.getDatacenterBroker()
                .getVmExecList()
                .forEach(vm -> vm.addOnUpdateProcessingListener(
                        evtInfo -> {
                            if (evtInfo.getVm().getCpuPercentUtilization() > 0.7) {
                                System.out.printf("VM %d", evtInfo.getVm().getId());
                                assertNotNull(String.format("VM %d overloaded. Additional requests for VMs not made%n", evtInfo.getVm().getId()),
                                        evtInfo.getVm().getBroker().getVmWaitingList().stream().findAny());
                            }
                        }
                ));
        simulation.start();
        simulation.printVmUtilizationMetrics();
    }
}
