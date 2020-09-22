package cloudsimplus.datacenter1.simulations.simulation2;

import cloudsimplus.datacenter1.simulations.Siddhanth_Venkateshwaran_Datacenter1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;

import java.io.File;

/**
 * This is the main class of Simulation 2 using 10 PEs per VM
 */
public class BestFitVmAllocationSimulation {
    /**
     * <p>The configuration files initializing specifications and requirements values are opened here</p>
     */
    static final Config requirementsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/cloudlet_and_vm_requirements.conf"));
    static final Config requirementsOverridesConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/Simulation2/cloudlet_vm_requirements_overrides.conf"))
            .withFallback(requirementsConfig)
            .resolve();

    public static final int VMS_PES = requirementsOverridesConfig.getInt("conf.VMS.PE_COUNT");
    public static final int SUBMISSION_DELAY = requirementsOverridesConfig.getInt("conf.ADDITIONAL_CLOUDLETS.DELAY");
    public static final int ADDITIONAL_CLOUDLETS = requirementsOverridesConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");

    public static void main(String[] args) {
        Siddhanth_Venkateshwaran_Datacenter1 simulation =
                new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit(), VMS_PES,
                        new CloudletSchedulerSpaceShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);

        simulation.start();
        simulation.printSimulationResults();
        simulation.printHostUtilizationMetrics();
        System.out.printf("%n%n%n");
        simulation.printVmUtilizationMetrics();
    }
}
