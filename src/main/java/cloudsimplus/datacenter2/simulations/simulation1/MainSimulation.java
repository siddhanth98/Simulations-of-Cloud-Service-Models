package cloudsimplus.datacenter2.simulations.simulation1;

import cloudsimplus.datacenter2.simulations.Siddhanth_Venkateshwaran_Datacenter2;

import java.util.Arrays;

public class MainSimulation {
    public static void main(String[] args) {
        Siddhanth_Venkateshwaran_Datacenter2 simulation =
                new Siddhanth_Venkateshwaran_Datacenter2();
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
