package cloudsimplus.part5;

import cloudsimplus.part5.datacenter1.Datacenter1;
import cloudsimplus.part5.datacenter2.Datacenter2;
import cloudsimplus.part5.datacenter3.Datacenter3;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerBestFit;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Broker {
    private final Datacenter1 datacenter1;
    private final Datacenter2 datacenter2;
    private final Datacenter3 datacenter3;
    private final CloudSim cloudSim;

    public Broker() {
        cloudSim = new CloudSim();
        datacenter1 = new Datacenter1(cloudSim, new DatacenterBrokerBestFit(cloudSim));
        datacenter2 = new Datacenter2(cloudSim, new DatacenterBrokerBestFit(cloudSim));
        datacenter3 = new Datacenter3(cloudSim, new DatacenterBrokerBestFit(cloudSim));
    }

    /**
     * This function will select the datacenter yielding the cheapest cost for utilizing all
     * resources for the consumer.
     * This function will take in the cloudlet specifications and operation name from the broker
     * and send the prices of the required VM specs for this cloudlet to execute.
     * @param cloudletOperation Can be one of :
     *                          1. process a file
     *                          2. open a file
     * @param fileName Name of file to be opened/processed
     * @param cloudletLength Length of cloudlet in Million Instructions
     * @param PES number of PEs required by the cloudlet to execute
     * @param FILE_SIZE input file size required by the cloudlet in bytes
     * @param OUTPUT_SIZE output file size required by the cloudlet in bytes
     */
    public MyDatacenterAbstract selectSaaSDatacenter(final String cloudletOperation, final String fileName, final int cloudletLength, final int PES,
                                                     final int FILE_SIZE, final int OUTPUT_SIZE) {

        final boolean datacenter1Service = this.datacenter1.isServiceAvailable(fileName, cloudletOperation);
        final boolean datacenter2Service = this.datacenter2.isServiceAvailable(fileName, cloudletOperation);
        final boolean datacenter3Service = this.datacenter3.isServiceAvailable(fileName, cloudletOperation);

        if (!datacenter1Service && !datacenter2Service && !datacenter3Service) {
            System.out.print("None of the datacenters can provide requested services");
            return null;
        }

        MyDatacenterAbstract datacenter;

        if (datacenter1Service && datacenter2Service && datacenter3Service)
            datacenter = getCheapestDatacenter(cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        else if (datacenter1Service && datacenter2Service)
            datacenter = getCheaperDatacenter(this.datacenter1, this.datacenter2, cloudletLength, FILE_SIZE, OUTPUT_SIZE);
        else if (datacenter1Service && datacenter3Service)
            datacenter = getCheaperDatacenter(this.datacenter1, this.datacenter3, cloudletLength, FILE_SIZE, OUTPUT_SIZE);
        else if (datacenter2Service && datacenter3Service)
            datacenter = getCheaperDatacenter(this.datacenter2, this.datacenter3, cloudletLength, FILE_SIZE, OUTPUT_SIZE);
        else {
            if (datacenter1Service)
                datacenter = this.datacenter1;
            else if (datacenter2Service)
                datacenter = this.datacenter2;
            else datacenter = this.datacenter3;
            estimateDatacenterCosts(datacenter, cloudletLength, FILE_SIZE, OUTPUT_SIZE);
        }

        submitSaaSCloudlets(datacenter, cloudletOperation, fileName, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        datacenter.start();
        datacenter.printSimulationResults();
        return datacenter;
    }

    private void submitSaaSCloudlets(final MyDatacenterAbstract datacenter, final String cloudletOperation,
                                     final String fileName, final int cloudletLength,
                                     final int PES, final int FILE_SIZE, final int OUTPUT_SIZE) {
        if (datacenter instanceof Datacenter1) {
            ((Datacenter1) datacenter).submitCloudletsForSaaS(cloudletOperation, fileName, cloudletLength,
                    PES, FILE_SIZE, OUTPUT_SIZE);
        }
        else if (datacenter instanceof Datacenter2) {
            ((Datacenter2) datacenter).submitCloudletsForSaaS(cloudletOperation, fileName, cloudletLength,
                    PES, FILE_SIZE, OUTPUT_SIZE);
        }
        else {
            ((Datacenter3) datacenter).submitCloudletsForSaaS(cloudletOperation, fileName, cloudletLength,
                    PES, FILE_SIZE, OUTPUT_SIZE);
        }
    }

    /**
     * This function estimates the total execution time of the cloudlet, total RAM, BW and Storage usages.
     * Then it pessimistically (considering the worst case executions) computes the total costs for each resource for
     * each of the datacenter.
     * Out of all those it selects the one giving the smallest total cost and returns that datacenter
     * @param cloudletLength Length of cloudlet in Million Instructions
     * @param PES number of PEs required by the cloudlet to execute
     * @param FILE_SIZE input file size required by the cloudlet in bytes
     * @param OUTPUT_SIZE output file size required by the cloudlet in bytes
     * @return A datacenter instance
     * This function is overloaded to account for service unavailability in other datacenters
     */
    private MyDatacenterAbstract getCheapestDatacenter(final int cloudletLength, final int PES,
                                                       final int FILE_SIZE, final int OUTPUT_SIZE) {

        final double[] datacenter1Prices = this.datacenter1.getSaaSPricing();
        final double[] datacenter2Prices = this.datacenter2.getSaaSPricing();
        final double[] datacenter3Prices = this.datacenter3.getSaaSPricing();

        final Map<MyDatacenterAbstract, ArrayList<Double>> totalUsageCosts = initializeUsageCostsMap();

        /*
        * Maximum execution time is computed using the smallest MIPS rated host of the 3 datacenters
        */
        final double estimatedExecutionTime = (double)cloudletLength / Double.min(datacenter1Prices[4],
                Double.min(datacenter2Prices[4], datacenter3Prices[4]));

        /*
        * Compute and store total estimated CPU costs of executing in all datacenters
        */
        computeAndStoreResourceCosts(estimatedExecutionTime, totalUsageCosts, datacenter1Prices[0], datacenter2Prices[0], datacenter3Prices[0]);

        /*
        * Maximum RAM usage is assumed to be the output data size
        * Compute and store total estimated RAM costs of executing in all datacenters
        */
        final long maxRamUsage = OUTPUT_SIZE;
        computeAndStoreResourceCosts(maxRamUsage, totalUsageCosts, datacenter1Prices[1], datacenter2Prices[1], datacenter3Prices[1]);

        /*
        * Maximum BW usage is assumed to occur while obtaining the input file
        * possibly from another VM or host
        * Compute and store total estimated bandwidth costs
        */
        final long maxBwUsage = FILE_SIZE;
        computeAndStoreResourceCosts(maxBwUsage, totalUsageCosts, datacenter1Prices[2], datacenter2Prices[2], datacenter3Prices[2]);

        /*
        * Maximum storage usage is assumed to be for storing the final output data
        * Compute and store total estimated storage costs
        */
        computeAndStoreResourceCosts(OUTPUT_SIZE, totalUsageCosts, datacenter1Prices[3], datacenter2Prices[3], datacenter3Prices[3]);

        final double cheapestCost =
                Double.min(totalUsageCosts.get(this.datacenter1).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                    Double.min(totalUsageCosts.get(this.datacenter2).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                        totalUsageCosts.get(this.datacenter3).stream().reduce(Double::sum).orElse(Double.MAX_VALUE)));

        MyDatacenterAbstract cheapestDatacenter = totalUsageCosts
                .keySet()
                .stream()
                .filter(dc -> totalUsageCosts.get(dc).stream().reduce(Double::sum).orElse(0.0) == cheapestCost)
                .findFirst().orElse(null);
        printEstimatedCostsAtCheapestDatacenter(cheapestDatacenter, estimatedExecutionTime, maxRamUsage, maxBwUsage, OUTPUT_SIZE);
        return cheapestDatacenter;
    }

    private MyDatacenterAbstract getCheaperDatacenter(final MyDatacenterAbstract dc1,
                                                      final MyDatacenterAbstract dc2,
                                                      final int cloudletLength,
                                                      final int FILE_SIZE, final int OUTPUT_SIZE) {
        final double[] dc1Prices = dc1.getSaaSPricing();
        final double[] dc2Prices = dc2.getSaaSPricing();
        final Map<MyDatacenterAbstract, ArrayList<Double>> totalUsageCosts = initializeUsageCostsMap();

        /*
         * Maximum execution time is computed using the smallest MIPS rated host of the 2 datacenters
         */
        final double estimatedExecutionTime = (double)cloudletLength / Double.min(dc1Prices[4], dc2Prices[4]);

        /*
         * Compute and store total estimated CPU costs of executing in all datacenters
         */
        computeAndStoreResourceCosts(estimatedExecutionTime, totalUsageCosts, dc1, dc1Prices[0]);
        computeAndStoreResourceCosts(estimatedExecutionTime, totalUsageCosts, dc2, dc2Prices[0]);

        /*
         * Maximum RAM usage is assumed to be the output data size
         * Compute and store total estimated RAM costs of executing in all datacenters
         */
        final long maxRamUsage = OUTPUT_SIZE;
        computeAndStoreResourceCosts(maxRamUsage, totalUsageCosts, dc1, dc1Prices[1]);
        computeAndStoreResourceCosts(maxRamUsage, totalUsageCosts, dc2, dc2Prices[1]);

        /*
         * Maximum BW usage is assumed to occur while obtaining the input file
         * possibly from another VM or host
         * Compute and store total estimated bandwidth costs
         */
        final long maxBwUsage = FILE_SIZE;
        computeAndStoreResourceCosts(maxBwUsage, totalUsageCosts, dc1, dc1Prices[2]);
        computeAndStoreResourceCosts(maxBwUsage, totalUsageCosts, dc2, dc2Prices[2]);

        /*
         * Maximum storage usage is assumed to be for storing the final output data
         * Compute and store total estimated storage costs
         */
        computeAndStoreResourceCosts(OUTPUT_SIZE, totalUsageCosts, dc1, dc1Prices[3]);
        computeAndStoreResourceCosts(OUTPUT_SIZE, totalUsageCosts, dc2, dc2Prices[3]);

        final double cheapestCost =
                Double.min(totalUsageCosts.get(this.datacenter1).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                        Double.min(totalUsageCosts.get(this.datacenter2).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                                totalUsageCosts.get(this.datacenter3).stream().reduce(Double::sum).orElse(Double.MAX_VALUE)));

        MyDatacenterAbstract cheapestDatacenter = totalUsageCosts
                .keySet()
                .stream()
                .filter(dc -> totalUsageCosts.get(dc).stream().reduce(Double::sum).orElse(0.0) == cheapestCost)
                .findFirst().orElse(null);

        printEstimatedCostsAtCheapestDatacenter(cheapestDatacenter, estimatedExecutionTime, maxRamUsage, maxBwUsage, OUTPUT_SIZE);
        return cheapestDatacenter;
    }

    private void estimateDatacenterCosts(MyDatacenterAbstract datacenter, final int cloudletLength,
                                         final int FILE_SIZE, final int OUTPUT_SIZE) {
        final double[] dcPrices = datacenter.getSaaSPricing();

        printEstimatedCostsAtCheapestDatacenter(datacenter, (double)cloudletLength/dcPrices[4],
                OUTPUT_SIZE, FILE_SIZE, OUTPUT_SIZE);
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
            if (datacenter1ResourceCost > -1 && dc instanceof Datacenter1)
                usageMap.get(this.datacenter1).add(estimatedResourceUsage * datacenter1ResourceCost);
            else if (datacenter2ResourceCost > -1 && dc instanceof Datacenter2)
                usageMap.get(this.datacenter2).add(estimatedResourceUsage * datacenter2ResourceCost);
            else if (datacenter3ResourceCost > -1)
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
                "Your prices are:%n COST PER PE PER SECOND - %.2f%n COST PER RAM PER SECOND - %.2f%n COST PER BW PER SECOND - %.2f%n COST PER STORAGE PER SECOND - %.2f%n%n",
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
    private void printEstimatedCostsAtCheapestDatacenter(MyDatacenterAbstract datacenter,
                                                         double estimatedExecutionTime,
                                                         double estimatedRamUsage,
                                                         double estimatedBwUsage,
                                                         double estimatedStorageUsage) {
        System.out.printf("Estimated Costs for selected datacenter: %s%n",datacenter.getDatacenter().getName());
        System.out.printf("Total CPU Execution Time:%10.2f seconds%n",
                estimatedExecutionTime);
        System.out.printf("Total RAM usage:%10.2f MB%n",
                estimatedRamUsage);
        System.out.printf("Total BW usage:%10.2f Mb%n",
                estimatedBwUsage);
        System.out.printf("Total storage usage:%10.2f MB%n",
                estimatedStorageUsage);
    }
}
