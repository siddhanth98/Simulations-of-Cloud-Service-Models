package cloudsimplus.datacenter1.simulations.simulation2;

import cloudsimplus.datacenter1.simulations.Siddhanth_Venkateshwaran_Datacenter1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyWorstFit;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;

import java.io.File;
import java.util.Arrays;

public class WorstFitVmAllocationSimulation {
    /**
     * <p>The configuration files initializing specifications and requirements values are opened here</p>
     */
    static final Config requirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/Simulation2/cloudlet_vm_requirements_overrides.conf")).resolve();

    public static final int VMS_PES = requirementsConfig.getInt("conf.VMS.PE_COUNT");
    public static final int SUBMISSION_DELAY = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.DELAY");
    public static final int ADDITIONAL_CLOUDLETS = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");

    public static void main(String[] args) {
        Siddhanth_Venkateshwaran_Datacenter1 simulation =
                new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyWorstFit(), VMS_PES,
                        new CloudletSchedulerSpaceShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);
        simulation.start();
        if (args.length > 0) {
            Arrays
                    .stream(args)
                    .distinct()
                    .forEach(arg -> {
                        switch (arg) {
                            case "cloudlets":
                                simulation.printSimulationResults();
                                break;
                            case "vms":
                                simulation.printVmUtilizationMetrics();
                                break;
                            case "hosts":
                                simulation.printHostUtilizationMetrics();
                                break;
                        }
                    });
        }
    }
}
