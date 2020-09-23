package cloudsimplus.datacenter1.simulations.simulation4;

import static org.junit.Assert.*;

import cloudsimplus.datacenter1.simulations.Siddhanth_Venkateshwaran_Datacenter1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class defined the test for simulation 4
 */
public class TestSimulation4 {
    private Siddhanth_Venkateshwaran_Datacenter1 simulation;

    private static final Config requirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/cloudlet_and_vm_requirements.conf"));
    private static final int VMS_PES = requirementsConfig.getInt("conf.VMS.PE_COUNT");
    private static final int SUBMISSION_DELAY = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.DELAY");
    private static final int ADDITIONAL_CLOUDLETS = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");

    /**
     * This test checks whether all submitted cloudlets started immediately or not.
     * A time shared cloudet scheduling policy is used which indicates that all cloudlets should have waiting times
     * equal to 0
     */
    @Test
    public void testCloudletWaitingTimes() {
        List<Cloudlet> waitingCloudlets = new ArrayList<>();
        simulation = new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit(), VMS_PES,
                new CloudletSchedulerTimeShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);
        simulation.getDatacenterBroker()
                .getCloudletSubmittedList()
                .forEach(cloudlet -> cloudlet.addOnStartListener(evtInfo -> {
                    if (evtInfo.getCloudlet().getWaitingTime() > 0)
                        waitingCloudlets.add(evtInfo.getCloudlet());
                }));
        simulation.start();
        simulation.printSimulationResults();
        assertEquals(String.format("Following cloudlets got a delayed start of execution - %s", Arrays.toString(waitingCloudlets.toArray())),
                0, waitingCloudlets.size());
    }
}
