package cloudsimplus.datacenter2.simulations.simulation2;

import cloudsimplus.datacenter2.Siddhanth_Venkateshwaran_Datacenter2;

import java.util.Arrays;

public class MainSimulationClass {
    public static void main(String[] args) {
        Siddhanth_Venkateshwaran_Datacenter2 simulation =
                new Siddhanth_Venkateshwaran_Datacenter2(vm -> 10.0, vm -> vm.getCpuPercentUtilization() > 0.6);
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
