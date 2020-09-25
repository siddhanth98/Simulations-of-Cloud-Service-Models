package cloudsimplus.part5;

import cloudsimplus.part5.datacenter1.Datacenter1;
import cloudsimplus.part5.datacenter2.Datacenter2;
import cloudsimplus.part5.datacenter3.Datacenter3;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerBestFit;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;

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
     *                          1. processDocument (more compute and RAM intensive)
     *                          2. saveDocument (more storage intensive)
     * @param cloudletLength Length of cloudlet in Million Instructions
     * @param PES number of PEs required by the cloudlet to execute
     * @param FILE_SIZE input file size required by the cloudlet in bytes
     * @param OUTPUT_SIZE output file size required by the cloudlet in bytes
     */
    public MyDatacenter selectSaaSDatacenter(final String cloudletOperation, final String fileName, final int cloudletLength, final int PES,
                                           final int FILE_SIZE, final int OUTPUT_SIZE) {
        final double[] datacenter1Prices = this.datacenter1.getSaaSPricing();
        final double[] datacenter2Prices = this.datacenter2.getSaaSPricing();
        final double[] datacenter3Prices = this.datacenter3.getSaaSPricing();

        MyDatacenter datacenter = getCheapestDatacenter(datacenter1Prices, datacenter2Prices, datacenter3Prices,
                cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE);
        submitSaaSCloudlets(datacenter, cloudletOperation, fileName, cloudletLength,
                PES, FILE_SIZE, OUTPUT_SIZE);
        return datacenter;
    }

    private void submitSaaSCloudlets(final Object datacenter, final String cloudletOperation,
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
     *
     * @param datacenter1Prices Array of resource costs and host MIPS for datacenter1
     * @param datacenter2Prices Array of resource costs and host MIPS for datacenter2
     * @param datacenter3Prices Array of resource costs and host MIPS for datacenter3
     * @param cloudletLength Length of cloudlet in Million Instructions
     * @param PES number of PEs required by the cloudlet to execute
     * @param FILE_SIZE input file size required by the cloudlet in bytes
     * @param OUTPUT_SIZE output file size required by the cloudlet in bytes
     * @return A datacenter instance
     */
    private MyDatacenter getCheapestDatacenter(final double[] datacenter1Prices,
                                             final double[] datacenter2Prices,
                                             final double[] datacenter3Prices,
                                             final int cloudletLength, final int PES,
                                             final int FILE_SIZE, final int OUTPUT_SIZE) {

        final Map<MyDatacenter, ArrayList<Double>> totalUsageCosts = initializeUsageCostsMap();
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
        computeAndStoreResourceCosts(OUTPUT_SIZE, totalUsageCosts, datacenter1Prices[1], datacenter2Prices[1], datacenter3Prices[1]);

        /*
        * Maximum BW usage is assumed to occur while obtaining the input file
        * possibly from another VM or host
        * Compute and store total estimated bandwidth costs
        */
        computeAndStoreResourceCosts(FILE_SIZE, totalUsageCosts, datacenter1Prices[2], datacenter2Prices[2], datacenter3Prices[2]);

        /*
        * Maximum storage usage is assumed to be for storing the final output data
        * Compute and store total estimated storage costs
        */
        computeAndStoreResourceCosts(OUTPUT_SIZE, totalUsageCosts, datacenter1Prices[3], datacenter2Prices[3], datacenter3Prices[3]);
        final double cheapestCost =
                Double.min(totalUsageCosts.get(this.datacenter1).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                    Double.min(totalUsageCosts.get(this.datacenter2).stream().reduce(Double::sum).orElse(Double.MAX_VALUE),
                        totalUsageCosts.get(this.datacenter3).stream().reduce(Double::sum).orElse(Double.MAX_VALUE)));

        return totalUsageCosts
                .keySet()
                .stream()
                .filter(dc -> totalUsageCosts.get(dc).stream().reduce(Double::sum).orElse(0.0) == cheapestCost)
                .findFirst().orElse(null);

    }

    private void computeAndStoreResourceCosts(final double estimatedResourceUsage, final Map<MyDatacenter, ArrayList<Double>> usageMap,
                                         final double datacenter1ResourceCost, final double datacenter2ResourceCost, final double datcenter3ResourceCost) {
        usageMap.forEach((dc, list) -> {
            if (dc instanceof Datacenter1)
                usageMap.get(this.datacenter1).add(estimatedResourceUsage * datacenter1ResourceCost);
            else if (dc instanceof Datacenter2)
                usageMap.get(this.datacenter2).add(estimatedResourceUsage * datacenter2ResourceCost);
            else usageMap.get(this.datacenter3).add(estimatedResourceUsage * datcenter3ResourceCost);
        });
    }

    /**
     * This function creates an empty Map from Datacenter to ArrayList and returns it
     */
    private Map<MyDatacenter, ArrayList<Double>> initializeUsageCostsMap() {
        Map<MyDatacenter, ArrayList<Double>> totalUsageCosts = new HashMap<>();

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

}
