package cloudsimplus.part5;

import cloudsimplus.part5.datacenter1.Datacenter1;
import cloudsimplus.part5.datacenter2.Datacenter2;
import cloudsimplus.part5.datacenter3.Datacenter3;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Broker {
    private final Datacenter1 datacenter1;
    private final Datacenter2 datacenter2;
    private final Datacenter3 datacenter3;
    private final DatacenterBroker dc1Broker;
    private final DatacenterBroker dc2Broker;
    private final DatacenterBroker dc3Broker;
    private final CloudSim dc1Simulation;
    private final CloudSim dc2Simulation;
    private final CloudSim dc3Simulation;
    private final Map<CloudSim, Boolean> cloudSimStatus;

    public Broker() {

        dc1Simulation = new CloudSim();
        dc2Simulation = new CloudSim();
        dc3Simulation = new CloudSim();

        cloudSimStatus = new HashMap<>();
        cloudSimStatus.put(dc1Simulation, false);
        cloudSimStatus.put(dc2Simulation, false);
        cloudSimStatus.put(dc3Simulation, false);

        /*dc1Simulation.addOnClockTickListener(this::processSimulationInstance);
        dc2Simulation.addOnClockTickListener(this::processSimulationInstance);
        dc3Simulation.addOnClockTickListener(this::processSimulationInstance);*/

        dc1Broker = new DatacenterBrokerSimple(dc1Simulation);
        dc2Broker =  new DatacenterBrokerSimple(dc2Simulation);
        dc3Broker = new DatacenterBrokerSimple(dc3Simulation);

        dc1Broker.setName("broker1");
        dc2Broker.setName("broker2");
        dc3Broker.setName("broker3");

        datacenter1 = new Datacenter1(dc1Simulation, dc1Broker);
        datacenter2 = new Datacenter2(dc2Simulation, dc2Broker);
        datacenter3 = new Datacenter3(dc3Simulation, dc3Broker);
    }

    /**
     * This function will select the datacenter yielding the cheapest cost for utilizing all
     * resources for the consumer.
     * This function will take in the cloudlet specifications and operation name from the broker
     * and send the prices of the required VM specs for this cloudlet to execute.
     * The broker will also collect the cloudlet execution results after it finishes execution from the datacenter
     * and compare estimated and actual execution results.
     * @param cloudletOperation Can be one of :
     *                          1. process a file
     *                          2. open a file
     * @param fileName Name of file to be opened/processed
     * @param cloudletLength Length of cloudlet in Million Instructions
     * @param PES number of PEs required by the cloudlet to execute
     * @param FILE_SIZE input file size required by the cloudlet in bytes
     * @param OUTPUT_SIZE output file size required by the cloudlet in bytes
     * @return The datacenter selected to service the cloudlet
     */
    public MyDatacenterAbstract selectSaaSDatacenter(final String cloudletOperation, final String fileName, final int cloudletLength, final int PES,
                                                     final int FILE_SIZE, final int OUTPUT_SIZE) {

        final boolean datacenter1Service = this.datacenter1.isServiceAvailable(fileName, cloudletOperation);
        final boolean datacenter2Service = this.datacenter2.isServiceAvailable(fileName, cloudletOperation);
        final boolean datacenter3Service = this.datacenter3.isServiceAvailable(fileName, cloudletOperation);

        if (!datacenter1Service && !datacenter2Service && !datacenter3Service) {
            System.out.printf("None of the datacenters can provide requested services%n%n");
            return null;
        }

        MyDatacenterAbstract datacenter;

        if (datacenter1Service && datacenter2Service && datacenter3Service)
            // All 3 datacenters provide requested service, so select cheapest among all 3
            datacenter = getCheapestDatacenter(cloudletOperation, fileName, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);

        else if (datacenter1Service && datacenter2Service)
            // Only datacenters 1 and 2 provide the service
            datacenter = getCheaperDatacenter(cloudletOperation, fileName, this.datacenter1, this.datacenter2, PES,
                    cloudletLength, FILE_SIZE, OUTPUT_SIZE);

        else if (datacenter1Service && datacenter3Service)
            // Only datacenters 1 and 3 provide the service
            datacenter = getCheaperDatacenter(cloudletOperation, fileName, this.datacenter1, this.datacenter3, PES,
                    cloudletLength, FILE_SIZE, OUTPUT_SIZE);

        else if (datacenter2Service && datacenter3Service)
            // Only datacenters 2 and 3 provide the service
            datacenter = getCheaperDatacenter(cloudletOperation, fileName, this.datacenter2, this.datacenter3, PES,
                    cloudletLength, FILE_SIZE, OUTPUT_SIZE);

        else {
            // Just one of the datacenters provides the service
            if (datacenter1Service)
                datacenter = this.datacenter1;
            else if (datacenter2Service)
                datacenter = this.datacenter2;
            else datacenter = this.datacenter3;
            estimateCheapestDatacenterCosts(cloudletOperation, fileName, datacenter, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        }

        if (!datacenter.getCloudSim().isRunning() && !this.getCloudSimStatus().get(datacenter.getCloudSim())) {
            datacenter.start();
            this.getCloudSimStatus().put(datacenter.getCloudSim(), true);
        }

        return datacenter;
    }

    private Cloudlet submitSaaSCloudlets(final MyDatacenterAbstract datacenter, final String cloudletOperation,
                                     final String fileName, final int cloudletLength,
                                     final int PES, final int FILE_SIZE, final int OUTPUT_SIZE) {
        final Cloudlet saasCloudlet;

        if (datacenter instanceof Datacenter1) {
            saasCloudlet = ((Datacenter1) datacenter).submitCloudletsForSaaS(cloudletOperation, fileName, cloudletLength,
                    PES, FILE_SIZE, OUTPUT_SIZE);
        }
        else if (datacenter instanceof Datacenter2) {
            saasCloudlet = ((Datacenter2) datacenter).submitCloudletsForSaaS(cloudletOperation, fileName, cloudletLength,
                    PES, FILE_SIZE, OUTPUT_SIZE);
        }
        else {
            saasCloudlet = ((Datacenter3) datacenter).submitCloudletsForSaaS(cloudletOperation, fileName, cloudletLength,
                    PES, FILE_SIZE, OUTPUT_SIZE);
        }
        saasCloudlet.addOnFinishListener(evtInfo -> datacenter.printSimulationResults(saasCloudlet));
        return saasCloudlet;
    }

    /**
     * This function estimates the total execution time of the cloudlet, total RAM, BW and Storage usages.
     * Then it pessimistically (considering the worst case executions) computes the total costs for each resource for
     * each of the datacenter (it uses the same formula used in cloudsimplus but only with the cloudlet specifications given to it).
     * Total cost = (Total estimated cpu execution time * Datacenter cost per CPU second) + (Total estimated BW consumed * Datacenter cost per Mb of BW)
     *
     * Out of all datacenters it selects the one giving the smallest total cost and returns that datacenter
     * @param cloudletLength Length of cloudlet in Million Instructions
     * @param PES number of PEs required by the cloudlet to execute
     * @param FILE_SIZE input file size required by the cloudlet in bytes
     * @param OUTPUT_SIZE output file size required by the cloudlet in bytes
     * @return A datacenter instance
     * This function is overloaded to account for service unavailability in other datacenters
     */
    private MyDatacenterAbstract getCheapestDatacenter(final String operation, final String fileName,
                                                       final int cloudletLength, final int PES,
                                                       final int FILE_SIZE, final int OUTPUT_SIZE) {

        final double[] datacenter1Prices = this.datacenter1.getSaaSPricing();
        final double[] datacenter2Prices = this.datacenter2.getSaaSPricing();
        final double[] datacenter3Prices = this.datacenter3.getSaaSPricing();

        final Map<MyDatacenterAbstract, ArrayList<Double>> totalUsageCosts = initializeUsageCostsMap();

        /*
        * Maximum execution time is computed using the smallest MIPS rated host of the 3 datacenters
        */
        final double estimatedExecutionTime = ((double)cloudletLength / Double.min(datacenter1Prices[4],
                Double.min(datacenter2Prices[4], datacenter3Prices[4])))*PES;

        /*
        * Compute and store total estimated CPU costs of executing in all datacenters
        */
        computeAndStoreResourceCosts(estimatedExecutionTime, totalUsageCosts, datacenter1Prices[0], datacenter2Prices[0], datacenter3Prices[0]);

        /*
        * Maximum BW usage is assumed to occur while obtaining the input file
        * possibly from another VM or host
        * Compute and store total estimated bandwidth costs
        */
        final long maxBwUsage = FILE_SIZE+OUTPUT_SIZE;
        computeAndStoreResourceCosts(maxBwUsage, totalUsageCosts, datacenter1Prices[2], datacenter2Prices[2], datacenter3Prices[2]);

        final double cheapestCost =
                Double.min(totalUsageCosts.get(this.datacenter1).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                    Double.min(totalUsageCosts.get(this.datacenter2).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                        totalUsageCosts.get(this.datacenter3).stream().reduce(Double::sum).orElse(Double.MAX_VALUE)));

        final MyDatacenterAbstract cheapestDatacenter = totalUsageCosts
                .keySet()
                .stream()
                .filter(dc -> totalUsageCosts.get(dc).stream().reduce(Double::sum).orElse(0.0) == cheapestCost)
                .findFirst().orElse(null);

        final Cloudlet cloudlet = submitSaaSCloudlets(cheapestDatacenter, operation, fileName, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        printEstimatedCostsAtCheapestDatacenter(cloudlet, cheapestDatacenter, estimatedExecutionTime, OUTPUT_SIZE, maxBwUsage, OUTPUT_SIZE);
        return cheapestDatacenter;
    }

    private MyDatacenterAbstract getCheaperDatacenter(final String operation, final String fileName,
                                                      final MyDatacenterAbstract dc1,
                                                      final MyDatacenterAbstract dc2,
                                                      final int PES,
                                                      final int cloudletLength,
                                                      final int FILE_SIZE, final int OUTPUT_SIZE) {
        final double[] dc1Prices = dc1.getSaaSPricing();
        final double[] dc2Prices = dc2.getSaaSPricing();
        final Map<MyDatacenterAbstract, ArrayList<Double>> totalUsageCosts = initializeUsageCostsMap();

        /*
         * Maximum execution time is computed using the smallest MIPS rated host of the 2 datacenters
         */
        final double estimatedExecutionTime = ((double)cloudletLength / Double.min(dc1Prices[4], dc2Prices[4]))*PES;

        /*
         * Compute and store total estimated CPU costs of executing in all datacenters
         */
        computeAndStoreResourceCosts(estimatedExecutionTime, totalUsageCosts, dc1, dc1Prices[0]);
        computeAndStoreResourceCosts(estimatedExecutionTime, totalUsageCosts, dc2, dc2Prices[0]);

        final long maxBwUsage = FILE_SIZE+OUTPUT_SIZE;
        computeAndStoreResourceCosts(maxBwUsage, totalUsageCosts, dc1, dc1Prices[2]);
        computeAndStoreResourceCosts(maxBwUsage, totalUsageCosts, dc2, dc2Prices[2]);

        final double cheapestCost =
                Double.min(totalUsageCosts.get(this.datacenter1).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                        Double.min(totalUsageCosts.get(this.datacenter2).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                                totalUsageCosts.get(this.datacenter3).stream().reduce(Double::sum).orElse(Double.MAX_VALUE)));

        MyDatacenterAbstract cheapestDatacenter = totalUsageCosts
                .keySet()
                .stream()
                .filter(dc -> totalUsageCosts.get(dc).stream().reduce(Double::sum).orElse(0.0) == cheapestCost)
                .findFirst().orElse(null);

        final Cloudlet cloudlet = submitSaaSCloudlets(cheapestDatacenter, operation, fileName, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        printEstimatedCostsAtCheapestDatacenter(cloudlet, cheapestDatacenter, estimatedExecutionTime, OUTPUT_SIZE, maxBwUsage, OUTPUT_SIZE);
        return cheapestDatacenter;
    }

    private void estimateCheapestDatacenterCosts(final String operation, final String fileName, MyDatacenterAbstract datacenter,
                                                 final int cloudletLength, final int PES,
                                                 final int FILE_SIZE, final int OUTPUT_SIZE) {
        final double[] dcPrices = datacenter.getSaaSPricing();
        final Cloudlet cloudlet = submitSaaSCloudlets(datacenter, operation, fileName, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        printEstimatedCostsAtCheapestDatacenter(cloudlet, datacenter, (double)cloudletLength/dcPrices[4],
                OUTPUT_SIZE*10, FILE_SIZE*10, OUTPUT_SIZE);
    }

    /**
     * This function will compute estimated resource usages/costs and store in the usage map
     * @param estimatedResourceUsage Estimated resource usage (RAM/BW) or execution time (CPU)
     * @param usageMap Main map in which resource usage and costs are stored
     * @param datacenter1ResourceCost cost per resource use per unit in datacenter1
     * @param datacenter2ResourceCost cost per resource use per unit in datacenter2
     * @param datacenter3ResourceCost cost per resource use per unit in datacenter3
     * This function is overloaded to compute and store resource costs and usages for just 2
     * datacenters.
     */
    private void computeAndStoreResourceCosts(final double estimatedResourceUsage, final Map<MyDatacenterAbstract, ArrayList<Double>> usageMap,
                                         final double datacenter1ResourceCost, final double datacenter2ResourceCost, final double datacenter3ResourceCost) {
        usageMap.forEach((dc, list) -> {
            if (dc instanceof Datacenter1)
                usageMap.get(this.datacenter1).add(estimatedResourceUsage * datacenter1ResourceCost);
            else if (dc instanceof Datacenter2)
                usageMap.get(this.datacenter2).add(estimatedResourceUsage * datacenter2ResourceCost);
            else
                usageMap.get(this.datacenter3).add(estimatedResourceUsage * datacenter3ResourceCost);
        });
    }

    private void computeAndStoreResourceCosts(final double estimatedResourceUsage, final Map<MyDatacenterAbstract, ArrayList<Double>> usageMap,
            final MyDatacenterAbstract dc, final double dcResourceCost) {
        usageMap.get(
                (dc instanceof Datacenter1 ? this.datacenter1 : (dc instanceof Datacenter2 ? this.datacenter2 : this.datacenter3))
        ).add(estimatedResourceUsage * dcResourceCost);
    }

    /**
     * This function creates an empty Map from Datacenter to ArrayList and returns it
     */
    private Map<MyDatacenterAbstract, ArrayList<Double>> initializeUsageCostsMap() {
        Map<MyDatacenterAbstract, ArrayList<Double>> totalUsageCosts = new HashMap<>();

        totalUsageCosts.put(this.datacenter1, new ArrayList<>());
        totalUsageCosts.put(this.datacenter2, new ArrayList<>());
        totalUsageCosts.put(this.datacenter3, new ArrayList<>());
        return totalUsageCosts;
    }

    private void printSaaSPricing(final double[] datacenterPrices) {
        System.out.printf(
                "Your prices are:%n COST PER CPU PER SECOND - %.2f%n COST PER RAM PER SECOND - %.2f%n COST PER BW PER SECOND - %.2f%n COST PER STORAGE PER SECOND - %.2f%n%n",
                datacenterPrices[0], datacenterPrices[1], datacenterPrices[2], datacenterPrices[3]
        );
    }

    /**
     * This function will print the total estimated costs/usage of executing the cloudlet
     * in the cheapest datacenter.
     * @param datacenter The cheapest datacenter selected by the broker
     * @param estimatedExecutionTime The estimated total CPU execution time of cloudlet
     * @param estimatedRamUsage The estimated total RAM which will be used by cloudlet
     * @param estimatedBwUsage The estimated total BW which will be used by cloudlet
     * @param estimatedStorageUsage The estimated total Storage used by cloudlet
     */
    private void printEstimatedCostsAtCheapestDatacenter(Cloudlet cloudlet,
                                                         MyDatacenterAbstract datacenter,
                                                         double estimatedExecutionTime,
                                                         double estimatedRamUsage,
                                                         double estimatedBwUsage,
                                                         double estimatedStorageUsage) {
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("Cloudlet %d will be sent to %s%n", cloudlet.getId(), datacenter.getDatacenter().getName());
        System.out.printf("Pricing criteria for %s: CPU - $%.2f/second, RAM - $%.2f/MB, BW - $%.2f/Mb%n",
                datacenter.getDatacenter().getName(), datacenter.getDatacenter().getCharacteristics().getCostPerSecond(),
                datacenter.getDatacenter().getCharacteristics().getCostPerMem(), datacenter.getDatacenter().getCharacteristics().getCostPerBw());

        System.out.printf("Estimated Costs for executing cloudlet %d in %s%n",cloudlet.getId(), datacenter.getDatacenter().getName());
        System.out.printf("CPU Execution Time:\t%.4f seconds%n",
                estimatedExecutionTime);
        System.out.printf("RAM usage:\t%.4f MB%n",
                estimatedRamUsage);
        System.out.printf("BW usage:\t%.4f Mb%n",
                estimatedBwUsage);
        System.out.printf("Total estimated execution cost (BW + CPU) = $%.4f%n", (
                    estimatedExecutionTime*datacenter.getDatacenter().getCharacteristics().getCostPerSecond()
                +
                    estimatedBwUsage*datacenter.getDatacenter().getCharacteristics().getCostPerBw()
                ));
    }

    public Datacenter1 getDatacenter1() {
        return datacenter1;
    }

    public Datacenter2 getDatacenter2() {
        return datacenter2;
    }

    public Datacenter3 getDatacenter3() {
        return datacenter3;
    }

    public DatacenterBroker getDc1Broker() {
        return dc1Broker;
    }

    public DatacenterBroker getDc2Broker() {
        return dc2Broker;
    }

    public DatacenterBroker getDc3Broker() {
        return dc3Broker;
    }

    public CloudSim getDc1Simulation() {
        return dc1Simulation;
    }

    public CloudSim getDc2Simulation() {
        return dc2Simulation;
    }

    public CloudSim getDc3Simulation() {
        return dc3Simulation;
    }

    public Map<CloudSim, Boolean> getCloudSimStatus() {
        return cloudSimStatus;
    }
}
