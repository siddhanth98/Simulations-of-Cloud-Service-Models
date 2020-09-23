package cloudsimplus.datacenter1.simulations.simulation1;

import cloudsimplus.datacenter1.simulations.Siddhanth_Venkateshwaran_Datacenter1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;

import static org.junit.Assert.*;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.vms.Vm;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines the tests to be run for simulation 1
 * To prevent running the same simulation for all tests, the simulation object is
 * reinstantiated in every test
 * */
public class TestSimulation1 {
    static Siddhanth_Venkateshwaran_Datacenter1 simulation1;
    static final Config requirementsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/cloudlet_and_vm_requirements.conf"));
    static final int VMS_PES = requirementsConfig.getInt("conf.VMS.PE_COUNT");
    static final int SUBMISSION_DELAY = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.DELAY");
    static final int ADDITIONAL_CLOUDLETS = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");

    /**
     * Tests if all VMs have been allocated to some host(s)
     * Attaches a creation failure listener to all created VMs and adds those VMs to the failed VMs list
     */
    @Test
    public void testVmAllocation() {
        simulation1 = new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit(), VMS_PES,
                new CloudletSchedulerSpaceShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);
        final List<Vm> vmFailedList = new ArrayList<>();
        simulation1.getDatacenterBroker()
                .getVmWaitingList()
                .forEach(vm -> vm.addOnCreationFailureListener(evtInfo -> vmFailedList.add(evtInfo.getVm())));
        simulation1.start();

        assertNull(String.format("At least 1 vm could not be allocated - %s", Arrays.toString(vmFailedList.toArray())),
                vmFailedList.stream().findAny().orElse(null));
        simulation1.getCloudSim().abort();
    }

    /**
     * Tests whether all cloudlets executed successfully
     */
    @Test
    public void testCloudletSubmission() {
        simulation1 = new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit(), VMS_PES,
                new CloudletSchedulerSpaceShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);

        final List<Cloudlet> cloudlets = new ArrayList<>();
        simulation1.getDatacenterBroker().getCloudletSubmittedList()
                .forEach(cloudlet -> cloudlet.addOnUpdateProcessingListener(evtInfo -> {
                    if (evtInfo.getCloudlet().getStatus() == Cloudlet.Status.FAILED ||
                            evtInfo.getCloudlet().getStatus() == Cloudlet.Status.CANCELED)
                        cloudlets.add(evtInfo.getCloudlet());
                }));

        simulation1.start();
        assertNull(String.format("Following cloudlets failed to execute %s", Arrays.toString(cloudlets.toArray())),
                cloudlets.stream().findAny().orElse(null));
        simulation1.getCloudSim().abort();
    }
}
