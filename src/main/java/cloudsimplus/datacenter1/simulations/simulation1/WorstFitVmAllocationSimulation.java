package cloudsimplus.datacenter1.simulations.simulation1;

import cloudsimplus.datacenter1.simulations.Siddhanth_Venkateshwaran_Datacenter1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyWorstFit;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;

import java.io.File;

public class WorstFitVmAllocationSimulation {
    /**
     * <p>The configuration file initializing requirement value is opened here</p>
     */
    static final Config requirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/cloudlet_and_vm_requirements.conf")).resolve();

    public static final int VMS_PES = requirementsConfig.getInt("conf.VMS.PE_COUNT");
    public static final int SUBMISSION_DELAY = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.DELAY");
    public static final int ADDITIONAL_CLOUDLETS = requirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");

    public static void main(String[] args) {
        Siddhanth_Venkateshwaran_Datacenter1 bestFitVmAllocationSimulation1 =
                new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyWorstFit(), VMS_PES,
                        new CloudletSchedulerSpaceShared(), SUBMISSION_DELAY, ADDITIONAL_CLOUDLETS);
        bestFitVmAllocationSimulation1.start();
        bestFitVmAllocationSimulation1.printSimulationResults();
    }
}
