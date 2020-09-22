package cloudsimplus.datacenter1.simulations.simulation2;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;

public class BestFitVmAllocationSimulation {
    public static void main(String[] args) {
        Siddhanth_Venkateshwaran_Datacenter1 bestFitVmAllocationSimulation2 =
                new Siddhanth_Venkateshwaran_Datacenter1(new VmAllocationPolicyBestFit());
        bestFitVmAllocationSimulation2.start();
        bestFitVmAllocationSimulation2.printSimulationResults();
    }
}
