package cloudsimplus.part5;

import ch.qos.logback.classic.Level;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.ResourceManageable;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.network.NetworkVm;
import org.cloudsimplus.listeners.*;
import org.cloudsimplus.util.Log;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;

public abstract class MyDatacenterAbstract {

    /**
     * This will open the config file and define specifications of initial cloudlets to
     * deploy in the datacenter to keep some of its VMs busy
     */
    private static final Config cloudletSpecsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/part5/initialCloudletSpecs.conf"));
    private static int CLOUDLETS = cloudletSpecsConfig.getInt("conf.CLOUDLETS.COUNT");
    private static int CLOUDLET_PES = cloudletSpecsConfig.getInt("conf.CLOUDLETS.PES");
    private static int CLOUDLET_LENGTH = cloudletSpecsConfig.getInt("conf.CLOUDLETS.LENGTH");
    private static int FILE_SIZE = cloudletSpecsConfig.getInt("conf.CLOUDLETS.FILE_SIZE");
    private static int OUTPUT_SIZE = cloudletSpecsConfig.getInt("conf.CLOUDLETS.OUTPUT_SIZE");

    /**
     * This function will take in the cloudlet specifications and operation name from the broker
     * and send the prices of the required VM specs for this cloudlet to execute.
     * @return An array of prices where:
     *                     - prices[0] - cost of using PE / second
     *                     - prices[1] - cost of using RAM / second
     *                     - prices[2] - cost of using BW / second
     *                     - prices[3] - cost of using STORAGE / second
     *                     - prices[4] - MIPS of each host in the datacenter
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
     * Getter for all entities, loggers and services defined here
     */

    public abstract Map<String, ArrayList<String>> getServices();

    public abstract CloudSim getCloudSim();

    public abstract Datacenter getDatacenter();

    public abstract DatacenterBroker getDatacenterBroker();

    public abstract Map<Host, Map<Double, Double>> getHostRamUtilizationMap();

    public abstract Map<Host, Map<Double, Double>> getHostBwUtilizationMap();

    public abstract Map<Host, Map<Double, Double>> getHostCpuUtilizationMap();

    public abstract Map<Vm, Map<Double, Double>> getVmRamUtilizationMap();

    public abstract Map<Vm, Map<Double, Double>> getVmBwUtilizationMap();

    public abstract Map<Vm, Map<Double, Double>> getVmCpuUtilizationMap();

    public abstract Map<Long, Map<Double, Double>> getCloudletRamMap();

    public abstract Map<Long, Map<Double, Double>> getCloudletBwMap();

    public abstract void setCloudletRamMap(Map<Long, Map<Double, Double>> newMap);

    public abstract void setCloudletBwMap(Map<Long, Map<Double, Double>> newMap);

    public abstract Logger getMyLogger();

    /**
     * This function will initialize the services map from filename to its list of operations allowed
     */
    public void initializeServicesMap(List<String> FILES) {
        FILES.forEach(file -> this.getServices().put(file, this.getOperations(file)));
    }

    /**
     * This will simply return the list of operations serviced for a specific file
     */
    public abstract ArrayList<String> getOperations(String FILES);

    /**
     * This function will reveal whether a particular service is offered for a given file or not
     */
    public boolean isServiceAvailable(String fileName, String operation) {
        return (this.getServices().containsKey(fileName) &&
                this.getServices().get(fileName).contains(operation));
    }

    /**
     * This will start the simulation for a datacenter
     */
    public void start() {
        this.getCloudSim().start();
    }

    /**
     * Configure logging levels for specific loggers
     * For VM creation success/failure and cloudlet execution start/failure
     */
    public void configureLogs() {
        Log.setLevel(Level.OFF);
        Log.setLevel(DatacenterBroker.LOGGER, Level.WARN);
        Log.setLevel(CloudletSchedulerAbstract.LOGGER, Level.OFF);
        Log.setLevel(DatacenterBroker.LOGGER, Level.INFO);

        /*this.getDatacenterBroker().getVmWaitingList().forEach(vm -> {
            vm.addOnHostAllocationListener(this::logVmAllocationSuccess);
            vm.addOnCreationFailureListener(this::logVmAllocationFailure);
        });

        this.getDatacenterBroker().getCloudletSubmittedList().forEach(cloudlet -> {
            cloudlet.addOnStartListener(this::logCloudletExecutionStart);
            cloudlet.addOnFinishListener(this::logCloudletExecutionFinish);
            cloudlet.addOnUpdateProcessingListener(this::logCloudletExecutionUpdate);
        });

        ((ch.qos.logback.classic.Logger)this.getMyLogger()).setLevel(Level.INFO);*/
    }

    /**
     * This function logs the allocation of hosts to VMs
     */
    public void logVmAllocationSuccess(VmEventInfo evtInfo) {
        this.getMyLogger().info(String.format("%s:\t\t%s\t\tVM %d (PES - %d, RAM - %d, BW - %d), allocated to host %d",
                evtInfo.getVm().getSimulation().clockStr(), this.getMyLogger().getName(), evtInfo.getVm().getId(),
                evtInfo.getVm().getNumberOfPes(), evtInfo.getVm().getRam().getCapacity(), evtInfo.getVm().getBw().getCapacity(), evtInfo.getVm().getHost().getId()));
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
     * This is the simulation event listener which is responsible to collect usage metrics of
     * the cloudlet
     */
    public void processOnClockTickListener(EventInfo evtInfo) {
        this.getDatacenterBroker().getCloudletCreatedList().forEach(this::collectCloudletRamUtilization);
        this.getDatacenterBroker().getCloudletCreatedList().forEach(this::collectCloudletBwUtilization);
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

    public void fillCloudletMaps(Cloudlet cloudlet) {
        if (getCloudletRamMap() == null)
            this.setCloudletRamMap(new HashMap<>());
        if (getCloudletBwMap() == null)
            this.setCloudletBwMap(new HashMap<>());

        this.getCloudletRamMap().put(cloudlet.getId(), new TreeMap<>());
        this.getCloudletBwMap().put(cloudlet.getId(), new TreeMap<>());
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
                vm -> {
                    System.out.printf("%s%n", vmUtilizationMap.get(vm));
                    vmUtilizationMap.get(vm).put(this.getCloudSim().clock(),
                            vm.getResource(resource).getPercentUtilization());
                }
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
     * This function will collect the Cloudlet's Ram utilization at the time interval.
     * It will also log the cloudlet's current relative and absolute VM ram utilization,
     * with the VM's total allocated RAM in MB.
     */
    public void collectCloudletRamUtilization(Cloudlet cloudlet) {
        this.getCloudletRamMap()
                .get(cloudlet.getId())
                .put(this.getCloudSim().clock(), cloudlet.getUtilizationOfRam());
        /*getMyLogger().info(String.format("%7.4f:\tRAM Relative Usage-%10.4f%%,\tAbsolute usage-%10.4f MB,\tVM Total RAM-%5d MB",
                this.getCloudSim().clock(), cloudlet.getUtilizationOfRam()*100,
                cloudlet.getUtilizationOfRam()*cloudlet.getVm().getRam().getCapacity(),
                cloudlet.getVm().getRam().getCapacity()));*/
    }

    /**
     * This function will collect the cloudlet's Bw utilization at the time interval.
     * It will also log the cloudlet's current relative and absolute BW ram utilization,
     * with the VM's total allocated BW in Mb.
     */
    public void collectCloudletBwUtilization(Cloudlet cloudlet) {
        this.getCloudletBwMap()
                .get(cloudlet.getId())
                .put(this.getCloudSim().clock(), cloudlet.getUtilizationOfBw());
        /*getMyLogger().info(String.format("%7.4f:\tBW Relative Usage-%10.4f%%,\tAbsolute Usage-%10.4f Mb,\tVM Total BW-%5d Mbps",
                this.getCloudSim().clock(), cloudlet.getUtilizationOfBw()*100,
                cloudlet.getUtilizationOfBw()*cloudlet.getVm().getBw().getCapacity(),
                cloudlet.getVm().getBw().getCapacity()));*/
    }

    /**
     * This will create and submit initial VMs to the broker
     */
    public void createAndSubmitInitialVms(final int VMS, final int PES, final long MIPS,
                                                   final long RAM, final long BW, final long STORAGE) {
        final List<Vm> vmList = new ArrayList<>(VMS);

        for (int i = 0; i < VMS; i++) {
            NetworkVm networkVm = new NetworkVm(i, MIPS, PES);
            networkVm.setRam(RAM).setBw(BW).setSize(STORAGE);
            networkVm.setCloudletScheduler(new CloudletSchedulerSpaceShared());

            vmList.add(networkVm);
        }
        this.getDatacenterBroker().submitVmList(vmList);
    }

    /**
     * This will create and submit a set of cloudlets initially before the simulation starts
     */
    public void createAndSubmitInitialCloudlets() {
        List<Cloudlet> cloudletList = new ArrayList<>(CLOUDLETS);

        for (int i = 0; i < CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(i, CLOUDLET_LENGTH, CLOUDLET_PES);
            cloudlet.setFileSize(FILE_SIZE);
            cloudlet.setOutputSize(OUTPUT_SIZE);
            cloudlet.setUtilizationModel(new UtilizationModelStochastic());
            cloudletList.add(cloudlet);
            this.fillCloudletMaps(cloudlet);
        }
        this.getDatacenterBroker().submitCloudletList(cloudletList);
    }

    /**
     * This function will create VMs depending on the type of SaaS service.
     * The broker is responsible for getting the type of required service and passing in
     * relevant arguments to this function - number of VMs, number of PEs/VM, MIPS, RAM, and so on.
     * @param VMS - number of VMs to create
     * @param VMS_PES - number of PEs per VM
     * @param VMS_MIPS - MIPS rating of each PE in VM
     * @param RAM - RAM of each VM
     * @param BW - bandwidth of each VM
     * @param STORAGE - storage size of each VM
     */
    public void createAndSubmitVms(final int VMS, final int VMS_PES, final long VMS_MIPS, final long RAM,
                                   final long BW, final long STORAGE) {
        List<NetworkVm> networkVms = new ArrayList<>();
        for (int i = 0; i < VMS; i++) {
            NetworkVm networkVm = new NetworkVm(i, VMS_MIPS, VMS_PES);
            networkVm.setRam(RAM).setBw(BW).setSize(STORAGE);
            networkVm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
            networkVm.getUtilizationHistory().enable();
            networkVms.add(networkVm);
        }
        this.getDatacenterBroker().submitVmList(networkVms);
    }

    /**
     * This function will cloudlet execution results recorded at each clock interval along with average RAM and BW usages
     */
    public void printSimulationResults() {

        List<Cloudlet> finishedCloudletList = this.getDatacenterBroker().getCloudletFinishedList();
        finishedCloudletList.forEach(this::printSimulationResults);
    }

    /**
     * Helper Function for printing actual execution results for a cloudlet
     */
    public void printSimulationResults(Cloudlet cloudlet) {
        System.out.printf("%nActual costs for executing cloudlet %d in %s%n", cloudlet.getId(), this.getDatacenter().getName());
        System.out.printf("Total Execution time:%10.2f seconds%n", cloudlet.getUtilizationModelCpu().getUtilization(this.getCloudSim().clock()));

        System.out.printf("Average RAM usage:\t%10.2f MB%n",
                (this.getCloudletRamMap().get(cloudlet.getId()).values().stream().reduce(0.0, Double::sum))/(this.getCloudletRamMap().get(cloudlet.getId()).size()));

        System.out.printf("Average BW usage:\t%10.2f Mb%n",
                (this.getCloudletBwMap().get(cloudlet.getId()).values().stream().reduce(0.0, Double::sum))/(this.getCloudletBwMap().get(cloudlet.getId()).size()));
        System.out.printf("Cloudlet total execution cost = $%.4f%n", cloudlet.getTotalCost());
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
