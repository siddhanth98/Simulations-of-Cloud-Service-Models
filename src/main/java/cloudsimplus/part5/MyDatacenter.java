package cloudsimplus.part5;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.listeners.VmEventInfo;
import org.cloudsimplus.util.Log;
import org.slf4j.Logger;

import java.util.*;

public abstract class MyDatacenter {

    /**
     * This function will take in the cloudlet specifications and operation name from the broker
     * and send the prices of the required VM specs for this cloudlet to execute.
     * @return An array of prices where:
     *                     - prices[0] - cost of using PE / second
     *                     - prices[1] - cost of using RAM / second
     *                     - prices[2] - cost of using BW / second
     *                     - prices[3] - cost of using STORAGE / second
     */

    public double[] getSaaSPricing() {
        return new double[] {
                (this.getDatacenter()).getCharacteristics().getCostPerSecond(),
                this.getDatacenter().getCharacteristics().getCostPerMem(),
                this.getDatacenter().getCharacteristics().getCostPerBw(),
                this.getDatacenter().getCharacteristics().getCostPerStorage(),
                this.getDatacenter().getHost(0).getMips()
        };
    }

    /**
     * Getter for all entities defined here
     */
    public abstract CloudSim getCloudSim();

    public abstract Datacenter getDatacenter();

    public abstract DatacenterBroker getDatacenterBroker();

    public abstract Map<Host, Map<Double, Double>> getHostRamUtilizationMap();

    public abstract Map<Host, Map<Double, Double>> getHostBwUtilizationMap();

    public abstract Map<Host, Map<Double, Double>> getHostCpuUtilizationMap();

    public abstract Map<Vm, Map<Double, Double>> getVmRamUtilizationMap();

    public abstract Map<Vm, Map<Double, Double>> getVmBwUtilizationMap();

    public abstract Map<Vm, Map<Double, Double>> getVmCpuUtilizationMap();

    public abstract Logger getMyLogger();

    public void start() {
        this.getCloudSim().start();
    }
    /**
     * Configure logging levels for specific loggers
     * For VM creation success/failure and cloudlet execution start/failure
     */
    public void configureLogs() {
        Log.setLevel(Level.OFF);
        Log.setLevel(CloudletSchedulerAbstract.LOGGER, Level.WARN);

        this.getDatacenterBroker().getVmWaitingList().forEach(vm -> {
            vm.addOnHostAllocationListener(this::logVmAllocationSuccess);
            vm.addOnCreationFailureListener(this::logVmAllocationFailure);
        });

        this.getDatacenterBroker().getCloudletSubmittedList().forEach(cloudlet -> {
            cloudlet.addOnStartListener(this::logCloudletExecutionStart);
            cloudlet.addOnFinishListener(this::logCloudletExecutionFinish);
            cloudlet.addOnUpdateProcessingListener(this::logCloudletExecutionUpdate);
        });

        ((ch.qos.logback.classic.Logger)this.getMyLogger()).setLevel(Level.INFO);
    }

    /**
     * This function logs the allocation of hosts to VMs
     */
    public void logVmAllocationSuccess(VmEventInfo evtInfo) {
        this.getMyLogger().info(String.format("%s:\t\tVM %d allocated to host %d",
                evtInfo.getVm().getSimulation().clockStr(), evtInfo.getVm().getId(), evtInfo.getVm().getHost().getId()));
    }

    /**
     * This function logs the failure of VM allocation
     */
    public void logVmAllocationFailure(VmEventInfo evtInfo) {
        this.getMyLogger().warn(String.format("%s:\t\tVM %d could not be allocated to any host", evtInfo.getVm().getSimulation().clockStr(), evtInfo.getVm().getId()));
    }

    /**
     * This function logs the allocation of VMs to cloudlets
     */
    public void logCloudletExecutionStart(CloudletEventInfo evtInfo) {
        this.getMyLogger().info(String.format("%s:\t\tCloudlet %d started executing on VM %d on host %d",
                evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId(), evtInfo.getCloudlet().getVm().getId(),
                evtInfo.getCloudlet().getVm().getHost().getId()));
    }

    /**
     * This function logs the cloudlet's execution end
     */
    public void logCloudletExecutionFinish(CloudletEventInfo evtInfo) {
        this.getMyLogger().info(String.format("%s:\t\tCloudlet %d finished executing on VM %d on host %d",
                evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId(), evtInfo.getCloudlet().getVm().getId(),
                evtInfo.getCloudlet().getVm().getHost().getId()));
    }

    /**
     * This function logs the cloudlet's execution update
     */
    public void logCloudletExecutionUpdate(CloudletEventInfo evtInfo) {
        switch(evtInfo.getCloudlet().getStatus()) {
            case FAILED_RESOURCE_UNAVAILABLE:
                this.getMyLogger().warn(String.format("%s:\t\tCloudlet %d could not be mapped to any VM",
                        evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId()));
                break;
            case FAILED:
                this.getMyLogger().warn(String.format("%s:\t\tCloudlet %d failed to execute",
                        evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId()));
                break;
        }
    }

    /**
     * Initialize data structure for storing Host BW/RAM utilization over the simulation lifetime
     * For each resource, it will be a multi-map with host as the key and another map as its value. This inner map
     * has the simulation time tick as its key and utilization % value at that time tick as the value
     * @param datacenter The current datacenter being used
     */
    public Map<Host, Map<Double, Double>> initializeHostUtilizationMaps(final Datacenter datacenter) {
        Map<Host, Map<Double, Double>> utilizationMap = new HashMap<>();
        datacenter.getHostList().forEach(host -> utilizationMap.put(host, new TreeMap<>()));
        return utilizationMap;
    }

    /**
     * Initialize data structure for storing VM BW/RAM utilization over the simulation lifetime
     * For each resource, it will be a multi-map with vm as the key and another map as its value. This inner map
     * has the simulation time tick as its key and utilization % value at that time tick as the value
     * @param datacenter The current datacenter being used
     */

    public Map<Vm, Map<Double, Double>> initializeVmUtilizationMaps(final Datacenter datacenter) {
        Map<Vm, Map<Double, Double>> utilizationMap = new HashMap<>();
        this.getDatacenterBroker().getVmWaitingList().forEach(vm -> utilizationMap.put(vm, new TreeMap<>()));
        return utilizationMap;
    }

    /**
     * This function will insert the amount of ram/bw currently being utilized in % for each host in the datacenter
     * "currently" means the current clock tick of the simulation
     * @param hostUtilizationMap The utilization map
     * @param resource Resource whose utilization is required (RAM / Bandwidth)
     * */
    public void collectHostUtilizationMetrics(final Map<Host, Map<Double, Double>> hostUtilizationMap,
                                               final Class<? extends ResourceManageable> resource) {
        this.getDatacenter().getHostList()
                .forEach(host ->
                        hostUtilizationMap.get(host).put(this.getCloudSim().clock(),
                                host.getProvisioner(resource).getResource().getPercentUtilization()));
    }

    /**
     * This function collects the percent of PE utilized at the current time clock tick
     * A separate function is defined for this because getResource().getPercentUtilization() used
     * in above function only gets RAM and BW utilization metrics and host.getUtilizationHistorySum() gives a PE utilization map
     * whose keys are clock ticks but those ticks do not correspond with the clock ticks that we have for RAM and BW maps.
     * */
    public void collectHostPeUtilizationMetrics(final Map<Host, Map<Double, Double>> hostCPUUtilizationMap) {
        this.getDatacenter().getHostList()
                .forEach(host ->
                        hostCPUUtilizationMap.get(host).put(this.getCloudSim().clock(),
                                host.getCpuPercentUtilization()));
    }

    /** This function will insert the amount of ram/bw currently being utilized in % for each vm in the datacenter
     * "currently" means the current clock tick of the simulation
     * @param vmUtilizationMap The utilization map
     * @param resource Resource whose utilization is required (RAM / Bandwidth)
     */
    public void collectVmUtilizationMetrics(final Map<Vm, Map<Double, Double>> vmUtilizationMap,
                                             final Class<? extends ResourceManageable> resource) {

        this.getDatacenterBroker().getVmCreatedList().forEach(
                vm -> vmUtilizationMap.get(vm).put(this.getCloudSim().clock(),
                        vm.getResource(resource).getPercentUtilization())
        );
    }

    /**
     * Similar to the host function, this will separately collect the Cpu utilization of the VM for the current clock tick
     */
    public void collectVmPeUtilizationMetrics(final Map<Vm, Map<Double, Double>> vmUtilizationMap) {
        this.getDatacenterBroker().getVmCreatedList().forEach(
                vm -> vmUtilizationMap.get(vm).put(this.getCloudSim().clock(),
                        vm.getCpuPercentUtilization())
        );
    }

    /**
     * This function will print a table detailing cloudlet execution results like:
     *  - Cloudlet IDs
     *  - Assigned VM IDs and Host IDs
     *  - Length of cloudlet in MI (million instructions)
     *  - Number of PEs required by each cloudlet, etc.
     * */
    public void printSimulationResults() {
        System.out.printf("%nCLOUDLET EXECUTION RESULTS (SORTED BY ID)");
        List<Cloudlet> finishedCloudletsList = this.getDatacenterBroker().getCloudletFinishedList();
        finishedCloudletsList.sort(Comparator.comparingLong(Cloudlet::getId));
        new MyCloudletsTableBuilder(finishedCloudletsList).build();
        System.out.printf("%n%n%n");

        System.out.printf("%nCLOUDLET EXECUTION RESULTS (SORTED BY EXECUTION START TIME)");
        finishedCloudletsList = this.getDatacenterBroker().getCloudletFinishedList();
        finishedCloudletsList.sort(Comparator.comparingDouble(Cloudlet::getExecStartTime));
        new MyCloudletsTableBuilder((finishedCloudletsList)).build();
        System.out.println();
    }

    /**
     * This function will print utilization metrics of each VM at each clock tick
     *  - PE, RAM and BW in %
     */
    public void printVmUtilizationMetrics() {
        this.getDatacenterBroker().getVmCreatedList()
                .forEach(vm -> {
                    System.out.printf("---------------------------------VM %d---------------------------------%n", vm.getId());
                    this.getVmRamUtilizationMap().get(vm).forEach((clock, ram) ->
                            System.out.printf("TIME - %6.2f\tCPU - %5.2f%%\tRAM - %5.2f%%\tBandwidth - %5.2f%%%n",
                                    clock, 100 * this.getVmCpuUtilizationMap().get(vm).get(clock), 100 * ram, 100 * this.getVmBwUtilizationMap().get(vm).get(clock)));
                    System.out.println("-----------------------------------------------------------------------------");
                });
        System.out.printf("%n%n%n");
    }

    /**
     * This function will print utilization metrics of each host at each clock tick
     *  - PE, RAM and BW in %
     */
    public void printHostUtilizationMetrics() {
        this.getDatacenter().getHostList().forEach(host -> {
            System.out.printf("--------------------------------HOST %d------------------------------------%n", host.getId());
            this.getHostRamUtilizationMap().get(host).forEach((clock, ram) ->
                    System.out.printf("TIME - %6.2f\tCPU - %5.2f%%\tRAM - %5.2f%%\tBandwidth - %5.2f%%%n",
                            clock, 100*this.getHostCpuUtilizationMap().get(host).get(clock), 100*ram, 100*this.getHostBwUtilizationMap().get(host).get(clock)));
            System.out.println("------------------------------------------------------------------------------------");
        });
        System.out.printf("%n%n%n");
    }
}
