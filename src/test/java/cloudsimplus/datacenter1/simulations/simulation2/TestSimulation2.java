package cloudsimplus.datacenter1.simulations.simulation2;

import static org.junit.Assert.*;

import cloudsimplus.datacenter1.simulations.Siddhanth_Venkateshwaran_Datacenter1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines tests for simulation 2 of datacenter 1
 * */
public class TestSimulation2 {
    private Siddhanth_Venkateshwaran_Datacenter1 simulation;
    private static final Config defaultRequirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/cloudlet_and_vm_requirements.conf"));

    private static final Config overridesConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/Simulation2/cloudlet_vm_requirements_overrides.conf"))
            .withFallback(defaultRequirementsConfig)
            .resolve();

    private static final int VMS_PES = overridesConfig.getInt("conf.VMS.PE_COUNT");
    private static final int SUBMISSION_DELAY = defaultRequirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.DELAY");
    private static final int ADDITIONAL_CLOUDLETS = defaultRequirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");

    /**
     * This method tests if all undelayed cloudlets submitted to the broker
     * start executing immediately or not i.e. whether waiting time of each was equal to 0 or not.
     * Because for simulation 2, each VM used all of the 10 PEs of its host
     * and so each VM must be able to execute 2 cloudlets at the same time,
     * given that each cloudlet requires 5 PEs.
     */
    @Test
    public void testCloudletStartTimes() {
        List<Cloudlet> delayedCloudlets = new ArrayList<>();
        simulation = new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit(), VMS_PES,
                new CloudletSchedulerSpaceShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);
        simulation
                .getDatacenterBroker()
                .getCloudletSubmittedList()
                .stream()
                .filter(cloudlet -> cloudlet.getSubmissionDelay() == 0)
                .forEach(cloudlet -> cloudlet.addOnStartListener(evtInfo -> {
                    if (evtInfo.getCloudlet().getWaitingTime() > 0)
                        delayedCloudlets.add(cloudlet);
                }));

        simulation.start();
        simulation.printSimulationResults();
        assertEquals(String.format("Following cloudlets got delayed - %s", Arrays.toString(delayedCloudlets.toArray())),
                0, delayedCloudlets.size());
    }
}
