package cloudsimplus.datacenter1.simulations.simulation1;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;

import static org.junit.Assert.*;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.junit.Test;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSimulation1 {
    static Siddhanth_Venkateshwaran_Datacenter1 simulation1;

    /**
     * Initializes the simulation before all tests
     */
    @BeforeClass
    public static void startSimulation1() {
        simulation1 = new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit());
    }

    /**
     * Checks if all VMs have been allocated to some host(s)
     * Attaches a creation failure listener to all created VMs and adds those VMs to the failed VMs list
     */
    @Test
    public void testVmAllocation() {
        final List<Vm> vmFailedList = new ArrayList<>();
        simulation1.getDatacenterBroker()
                .getVmWaitingList()
                .forEach(vm -> vm.addOnCreationFailureListener(evtInfo -> vmFailedList.add(evtInfo.getVm())));
        simulation1.start();

        assertNull(String.format("At least 1 vm could not be allocated - %s", Arrays.toString(vmFailedList.toArray())),
                vmFailedList.stream().findAny().orElse(null));
    }

    /**
     * Checks to see if all cloudlets executed successfully
     * Fails otherwise
     */
    @Test
    public void testCloudletSubmission() {
        final List<Cloudlet> cloudlets = new ArrayList<>();
        simulation1.getDatacenterBroker().getCloudletSubmittedList()
                .forEach(cloudlet -> cloudlet.addOnUpdateProcessingListener(evtInfo -> {
                    if (evtInfo.getCloudlet().getStatus() == Cloudlet.Status.FAILED ||
                            evtInfo.getCloudlet().getStatus() == Cloudlet.Status.CANCELED)
                        cloudlets.add(evtInfo.getCloudlet());
                }));

        simulation1.start();
        simulation1.printSimulationResults();
        assertNull(String.format("Following cloudlets failed to execute %s", Arrays.toString(cloudlets.toArray())),
                cloudlets.stream().findAny().orElse(null));
    }
}
