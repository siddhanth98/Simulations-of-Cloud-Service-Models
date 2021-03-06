package cloudsimplus.datacenter1.simulations;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.util.ContextInitializer;
import org.cloudbus.cloudsim.allocationpolicies.*;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.Identifiable;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.network.NetworkHost;
import org.cloudbus.cloudsim.network.switches.AggregateSwitch;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerCompletelyFair;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.network.NetworkVm;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.VmEventInfo;
import org.cloudsimplus.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static cloudsimplus.datacenter1.simulations.Constants.*;

public class Siddhanth_Venkateshwaran_Datacenter1 {
    /**
     * <p>
     *      This simulation will use the network datacenter1 having 10 hosts
     *      Hosts are connected using an edge switch
     *      This datacenter will use the best-fit policy for allocating VMs to hosts.
     *      The best-fit policy will always choose the host having the most number of PEs in use to run
     *      a given VM. Thus running hosts will tend to be completely utilized and there will be a
     *      large number of hosts which will remain mostly idle.
     * </p>
     */

    private final CloudSim cloudSim;
    private final Datacenter datacenter;
    private final DatacenterBroker datacenterBroker;
    private final Map<Host, Map<Double, Double>> hostRamUtilizationMap;
    private final Map<Host, Map<Double, Double>> hostBwUtilizationMap;
    private final Map<Host, Map<Double, Double>> hostCpuUtilizationMap;

    private final Map<Vm, Map<Double, Double>> vmRamUtilizationMap;
    private final Map<Vm, Map<Double, Double>> vmCpuUtilizationMap;
    private final Map<Vm, Map<Double, Double>> vmBwUtilizationMap;

    private final Logger brokerLogger;

    /**
     * <p>
     *     The constructor will initialize the simulation object.
     *     Create the datacenter.
     *     Create the datacenter broker.
     *     Submit the VMs and Cloudlets to the broker.
     *     Start the simulation and print the simulation results when it is over.
     * </p>
     */
    public Siddhanth_Venkateshwaran_Datacenter1(final VmAllocationPolicyAbstract vmAllocationPolicy,
                                                final int VMS_PES, final int ADDITIONAL_CLOUDLETS) {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "src/main/resources/configuration/logback-test.xml");
        cloudSim = new CloudSim();
        datacenter = createDatacenter(cloudSim, vmAllocationPolicy);
        datacenterBroker = new DatacenterBrokerSimple(cloudSim);

        hostRamUtilizationMap = initializeHostUtilizationMaps(datacenter);
        hostBwUtilizationMap = initializeHostUtilizationMaps(datacenter);
        hostCpuUtilizationMap = initializeHostUtilizationMaps(datacenter);

        createAndSubmitVms(datacenterBroker, VMS_PES);
        vmRamUtilizationMap = initializeVmUtilizationMaps(datacenter);
        vmBwUtilizationMap = initializeVmUtilizationMaps(datacenter);
        vmCpuUtilizationMap = initializeVmUtilizationMaps(datacenter);

        cloudSim.addOnClockTickListener(this::processOnClockTickListener);
        createAndSubmitCloudlets(datacenterBroker, ADDITIONAL_CLOUDLETS);

        brokerLogger = LoggerFactory.getLogger(datacenterBroker.getClass().getSimpleName());
        configureLogs();
    }

    /**
     * This constructor will do the work of setting different cloudlet scheduling policies
     * specifically for simulations 3 and 4
     */
    public Siddhanth_Venkateshwaran_Datacenter1(final VmAllocationPolicyAbstract vmAllocationPolicy,
                                                final int VMS_PES, final CloudletSchedulerAbstract cloudletScheduler,
                                                final int ADDITIONAL_CLOUDLETS) {
        this(vmAllocationPolicy, VMS_PES, ADDITIONAL_CLOUDLETS);
        this.getDatacenterBroker()
                .getVmWaitingList()
                .forEach(vm -> {
                    if (cloudletScheduler instanceof CloudletSchedulerSpaceShared)
                        vm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
                    else if (cloudletScheduler instanceof CloudletSchedulerCompletelyFair)
                        vm.setCloudletScheduler(new CloudletSchedulerCompletelyFair());
                    else if (cloudletScheduler instanceof CloudletSchedulerTimeShared)
                        vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
                });
    }

    /**
     * This constructor is used when no additional additional cloudlets
     * or submission delays is to be used
     */
    public Siddhanth_Venkateshwaran_Datacenter1(final VmAllocationPolicyAbstract vmAllocationPolicy,
                                                final int VMS_PES, final CloudletSchedulerAbstract cloudletScheduler) {
        this(vmAllocationPolicy, VMS_PES, cloudletScheduler, 0);
    }

    /**
     * This constructor is used when additional cloudlets with specific submission delay
     * are to be used
     */
    public Siddhanth_Venkateshwaran_Datacenter1(final VmAllocationPolicyAbstract vmAllocationPolicy,
                                                final int VMS_PES, final CloudletSchedulerAbstract cloudletScheduler,
                                                final int submissionDelay, final int ADDITIONAL_CLOUDLETS) {
        this(vmAllocationPolicy, VMS_PES, cloudletScheduler, ADDITIONAL_CLOUDLETS);
        this.getDatacenterBroker()
                .getCloudletSubmittedList()
                .stream()
                .filter(cloudlet -> cloudlet.getId() >= CLOUDLETS)
                .forEach(cloudlet -> cloudlet.setSubmissionDelay(submissionDelay));
    }

    /**
     * Expose a function to start the simulation to let junit access it and pause it if required
     * without starting it in the constructor itself
     */
    public void start() {
        this.getCloudSim().start();
    }

    /**
     * Getter for the current simulation
     */
    public CloudSim getCloudSim() {
        return cloudSim;
    }

    /**
     * Getter for the datacenter
     */
    public Datacenter getDatacenter() {
        return datacenter;
    }

    /**
     * Getter for the broker
     */
    public DatacenterBroker getDatacenterBroker() {
        return datacenterBroker;
    }

    /**
     * Getter for the cpu utilization map
     */
    public Map<Host, Map<Double, Double>> getHostCpuUtilizationMap() {
        return hostCpuUtilizationMap;
    }

    /**
     * Getter for the ram utilization map
     */
    public Map<Host, Map<Double, Double>> getHostRamUtilizationMap() {
        return hostRamUtilizationMap;
    }

    /**
     * Getter for the bandwidth utilization map
     */
    public Map<Host, Map<Double, Double>> getHostBwUtilizationMap() {
        return hostBwUtilizationMap;
    }

    /**
     * Getter for vm ram utilization map
     */
    public Map<Vm, Map<Double, Double>> getVmRamUtilizationMap() {
        return vmRamUtilizationMap;
    }

    /**
     * Getter for vm cpu utilization map
     */
    public Map<Vm, Map<Double, Double>> getVmCpuUtilizationMap() {
        return vmCpuUtilizationMap;
    }

    /**
     * Getter for vm bw utilization map
     */
    public Map<Vm, Map<Double, Double>> getVmBwUtilizationMap() {
        return vmBwUtilizationMap;
    }

    /**
     * Configure logging levels for specific loggers
     * For VM creation success/failure and cloudlet execution start/failure
     */
    private void configureLogs() {
        Log.setLevel(Level.OFF);
        Log.setLevel(CloudletSchedulerAbstract.LOGGER, Level.WARN);

        datacenterBroker.getVmWaitingList().forEach(vm -> {
            vm.addOnHostAllocationListener(this::logVmAllocationSuccess);
            vm.addOnCreationFailureListener(this::logVmAllocationFailure);
        });

        datacenterBroker.getCloudletSubmittedList().forEach(cloudlet -> {
            cloudlet.addOnStartListener(this::logCloudletExecutionStart);
            cloudlet.addOnFinishListener(this::logCloudletExecutionFinish);
            cloudlet.addOnUpdateProcessingListener(this::logCloudletExecutionUpdate);
        });

        ((ch.qos.logback.classic.Logger)brokerLogger).setLevel(Level.INFO);
    }

    public Logger getBrokerLogger() {
        return brokerLogger;
    }

    /**
     * This function logs the allocation of hosts to VMs
     */
    private void logVmAllocationSuccess(VmEventInfo evtInfo) {
        this.getBrokerLogger().info(String.format("%s:\t\tVM %d allocated to host %d",
                evtInfo.getVm().getSimulation().clockStr(), evtInfo.getVm().getId(), evtInfo.getVm().getHost().getId()));
    }

    /**
     * This function logs the failure of VM allocation
     */
    private void logVmAllocationFailure(VmEventInfo evtInfo) {
        this.getBrokerLogger().warn(String.format("%s:\t\tVM %d could not be allocated to any host", evtInfo.getVm().getSimulation().clockStr(), evtInfo.getVm().getId()));
    }

    /**
     * This function logs the allocation of VMs to cloudlets
     */
    private void logCloudletExecutionStart(CloudletEventInfo evtInfo) {
        this.getBrokerLogger().info(String.format("%s:\t\tCloudlet %d started executing on VM %d on host %d",
                evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId(), evtInfo.getCloudlet().getVm().getId(),
                evtInfo.getCloudlet().getVm().getHost().getId()));
    }

    /**
     * This function logs the cloudlet's execution end
     */
    private void logCloudletExecutionFinish(CloudletEventInfo evtInfo) {
        this.getBrokerLogger().info(String.format("%s:\t\tCloudlet %d finished executing on VM %d on host %d",
                evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId(), evtInfo.getCloudlet().getVm().getId(),
                evtInfo.getCloudlet().getVm().getHost().getId()));
    }

    /**
     * This function logs the cloudlet's execution update
     */
    private void logCloudletExecutionUpdate(CloudletEventInfo evtInfo) {
        switch(evtInfo.getCloudlet().getStatus()) {
            case FAILED_RESOURCE_UNAVAILABLE:
                brokerLogger.warn(String.format("%s:\t\tCloudlet %d could not be mapped to any VM",
                        evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId()));
                break;
            case FAILED:
                brokerLogger.warn(String.format("%s:\t\tCloudlet %d failed to execute",
                        evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId()));
                break;
        }
    }

    /**
     * This will call the function responsible to collect the RAM and BW metrics when the simulation clock ticks
     * @param evt This object has data passed with the event between different entities (VM, Host, Broker, Cloudlet)
     * Data give access to the entity objects (VM, Host, Broker, Datacenter, etc.)
     */
    private void processOnClockTickListener(final EventInfo evt) {
        collectHostUtilizationMetrics(this.getHostRamUtilizationMap(), Ram.class);
        collectHostUtilizationMetrics(this.getHostBwUtilizationMap(), Bandwidth.class);
        collectHostPeUtilizationMetrics(this.getHostCpuUtilizationMap());

        collectVmUtilizationMetrics(this.getVmRamUtilizationMap(), Ram.class);
        collectVmUtilizationMetrics(this.getVmBwUtilizationMap(), Bandwidth.class);
        collectVmPeUtilizationMetrics(this.getVmCpuUtilizationMap());
    }

    /**
     * Initialize data structure for storing Host BW/RAM utilization over the simulation lifetime
     * For each resource, it will be a multi-map with host as the key and another map as its value. This inner map
     * has the simulation time tick as its key and utilization % value at that time tick as the value
     * @param datacenter The current datacenter being used
     */
    private Map<Host, Map<Double, Double>> initializeHostUtilizationMaps(final Datacenter datacenter) {
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

    private Map<Vm, Map<Double, Double>> initializeVmUtilizationMaps(final Datacenter datacenter) {
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
    private void collectHostUtilizationMetrics(final Map<Host, Map<Double, Double>> hostUtilizationMap,
                                               final Class<? extends ResourceManageable> resource) {
        this.getDatacenter().getHostList()
                .forEach(host ->
                    hostUtilizationMap.get(host).put(this.cloudSim.clock(),
                            host.getProvisioner(resource).getResource().getPercentUtilization()));
    }

    /**
     * This function collects the percent of PE utilized at the current time clock tick
     * A separate function is defined for this because getResource().getPercentUtilization() used
     * in above function only gets RAM and BW utilization metrics and host.getUtilizationHistorySum() gives a PE utilization map
     * whose keys are clock ticks but those ticks do not correspond with the clock ticks that we have for RAM and BW maps.
     * */
    private void collectHostPeUtilizationMetrics(final Map<Host, Map<Double, Double>> hostCPUUtilizationMap) {
        this.getDatacenter().getHostList()
                .forEach(host ->
                        hostCPUUtilizationMap.get(host).put(this.cloudSim.clock(),
                                host.getCpuPercentUtilization()));
    }

    /** This function will insert the amount of ram/bw currently being utilized in % for each vm in the datacenter
     * "currently" means the current clock tick of the simulation
     * @param vmUtilizationMap The utilization map
     * @param resource Resource whose utilization is required (RAM / Bandwidth)
     */
    private void collectVmUtilizationMetrics(final Map<Vm, Map<Double, Double>> vmUtilizationMap,
                                             final Class<? extends ResourceManageable> resource) {

        this.getDatacenterBroker().getVmCreatedList().forEach(
                                vm -> vmUtilizationMap.get(vm).put(this.getCloudSim().clock(),
                                                    vm.getResource(resource).getPercentUtilization())
                        );
    }

    private void collectVmPeUtilizationMetrics(final Map<Vm, Map<Double, Double>> vmUtilizationMap) {
        this.getDatacenter()
            .getHostList()
            .forEach(host -> host.getVmList()
                                 .forEach(vm -> vmUtilizationMap.get(vm).put(this.getCloudSim().clock(),
                                                                            vm.getCpuPercentUtilization()
                                                                    )
                                 )
            );
    }

    /**
     * <p>
     *     This will create the network datacenter for the specified simulation.
     *     @param cloudSim The current simulation object
     * </p>
     */
    private NetworkDatacenter createDatacenter(final CloudSim cloudSim, final VmAllocationPolicyAbstract vmAllocationPolicy) {
        List<NetworkHost> networkHosts = createHosts();
        NetworkDatacenter networkDatacenter;

        if (vmAllocationPolicy instanceof VmAllocationPolicyBestFit)
            networkDatacenter = new NetworkDatacenter(cloudSim, networkHosts, new VmAllocationPolicyBestFit());
        else if (vmAllocationPolicy instanceof VmAllocationPolicyWorstFit)
            networkDatacenter = new NetworkDatacenter(cloudSim, networkHosts, new VmAllocationPolicyWorstFit());
        else if (vmAllocationPolicy instanceof VmAllocationPolicyRandom)
            networkDatacenter = new NetworkDatacenter(cloudSim, networkHosts, new VmAllocationPolicyRandom(new UniformDistr()));
        else networkDatacenter = new NetworkDatacenter(cloudSim, networkHosts, new VmAllocationPolicySimple());

        networkDatacenter.setSchedulingInterval(SCHEDULING_INTERVAL);
        createNetwork(cloudSim, networkDatacenter);
        return networkDatacenter;
    }

    /**
     * A functional interface which will take the following arguments:
     *      - Edge Switch
     *      - HostList
     *      - Lower id bound
     *      - Upper id bound
     * Filter out hosts whose IDs fall in the specified range
     * Connect those hosts to the edge switch
     * */
    @FunctionalInterface
    private interface HostConnector {
        void connect(final EdgeSwitch edgeSwitch, final List<NetworkHost> hostList, final int lowerIdBound, final int upperIdBound);
    }

    /**
     * This will set up edge switches and connect a set of hosts to each one.
     * First n hosts will be connected to the 1st edge switch,
     * next n hosts to the 2nd edge switch and so on.
     * Each edge switch has a default downlink bandwidth of 800 Mbps,
     * which is the same as the uplink bandwidth of hosts.
     * 1 aggregate switch is created for processing packets from edge switches for other edge switches
     */
    private void createNetwork(final CloudSim cloudSim, final NetworkDatacenter datacenter) {
        /*
         * Implement the host connector here
         * */
        HostConnector myConnector = (mySwitch, hostList, lower, upper) ->
                hostList
                        .stream()
                        .filter(host -> host.getId() >= lower && host.getId() < upper)
                        .forEach(mySwitch::connectHost);

        EdgeSwitch[] edgeSwitches = new EdgeSwitch[EDGE_SWITCHES];
        for (int i = 0; i < EDGE_SWITCHES; i++) {
            edgeSwitches[i] = new EdgeSwitch(cloudSim, datacenter);
            myConnector.connect(edgeSwitches[i], datacenter.getHostList(),
                    EDGE_SWITCHES*i, EDGE_SWITCHES*i+EDGE_SWITCHES);
            edgeSwitches[i].setPorts(HOSTS / EDGE_SWITCHES);
            datacenter.addSwitch(edgeSwitches[i]);
        }

        /*
        * Create an aggregate switch and add it to the datacenter
        * Default downlink bandwidth is 800 Mbps
        * Switching delay is 0.002 milliseconds
        * How will an edge switch actually forward packet to its aggregate switch ?
        *   - It internally accesses the abstract switch's uplink switches list but
        *     not sure when this aggregate switch is added to that uplink switches list
        */
        AggregateSwitch aggregateSwitch = new AggregateSwitch(cloudSim, datacenter);
    }

    /**
     * <p>
     *     This function will create the network hosts.
     *     Hosts will use the simple provisioning policy for provisioning RAM and BW.
     *     If resource is available then it will be provisioned otherwise not.
     *     Each host will use the time sharing scheduling policy for executing VMs.
     *     This means that VMs will be preempted when their time slice is
     *     over for executing another VM.
     * </p>
     */
    private List<NetworkHost> createHosts() {
        List<NetworkHost> networkHosts = new ArrayList<>();
        for (int i = 0; i < HOSTS; i++) {
            NetworkHost host = new NetworkHost(HOST_RAM, HOST_BW, HOST_STORAGE, createPes());
            host.setId(i);
            host.setRamProvisioner(new ResourceProvisionerSimple());
            host.setBwProvisioner(new ResourceProvisionerSimple());
            host.setVmScheduler(new VmSchedulerTimeShared());
            host.enableStateHistory();
            networkHosts.add(host);
        }
        return networkHosts;
    }

    /**
     * <p>
     *     Create required PEs (cores) for hosts.
     *     Each host will use the simple provisioning policy for provisioning PEs to VMs.
     *     If there is available physical PE then it will be allocated otherwise not.
     * </p>
     */
    private List<Pe> createPes() {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < HOST_PES; i++)
            peList.add(new PeSimple(HOST_MIPS, new PeProvisionerSimple()));
        return peList;
    }

    /**
     * This function will create a list of VMs and submit the list to the broker.
     * Each VM will use the simple scheduling policy for executing its cloudlets.
     * Each cloudlet will get a time slice to execute after which it will be preempted
     * by another cloudlet for use of the VM.
     * @param datacenterBroker The broker of the datacenter
     * @param VMS_PES the number of PEs required for each VM
     */
    private void createAndSubmitVms(final DatacenterBroker datacenterBroker, final int VMS_PES) {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VMS; i++) {
            NetworkVm networkVm = new NetworkVm(i, VMS_MIPS, VMS_PES);
            networkVm.setRam(VMS_RAM).setBw(VMS_BW).setSize(VMS_STORAGE);
            networkVm.getUtilizationHistory().enable();
            networkVm.setCloudletScheduler(new CloudletSchedulerSpaceShared());

            vmList.add(networkVm);
        }
        datacenterBroker.submitVmList(vmList);
    }

    /**
     * This will create simple cloudlets which are supposed to execute for the specified time duration.
     * Network cloudlets require inter-communication (send/receive tasks), but that is not required for this simulation.
     * Each cloudlet uses a stochastic/random utilization model for both PE and RAM.
     * For BW, the utilization model is set to increase by 10% on every clock tick.
     * Additionally 100 more cloudlets will be submitted to the broker with a specific delay to simulate
     * a stream of cloudlets.
     * @param datacenterBroker The broker of the datacenter
     * @param ADDITIONAL_CLOUDLETS The number of additional cloudlets
     */
    private void createAndSubmitCloudlets(final DatacenterBroker datacenterBroker, final int ADDITIONAL_CLOUDLETS) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < CLOUDLETS+ADDITIONAL_CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(i, CLOUDLET_LENGTH, CLOUDLET_PES);
            cloudlet.setUtilizationModel(new UtilizationModelStochastic());
            cloudletList.add(cloudlet);
        }

        datacenterBroker.submitCloudletList(cloudletList);
    }

    /**
     * This function will print a table detailing cloudlet execution results like:
     *  - Cloudlet IDs
     *  - Assigned VM IDs and Host IDs
     *  - Length of cloudlet in MI (million instructions)
     *  - Number of PEs required by each cloudlet, etc.
     * */
    public void printSimulationResults() {
        List<Cloudlet> finishedCloudletsList = this.getDatacenterBroker().getCloudletFinishedList();
        finishedCloudletsList.sort(Comparator.comparingLong(Identifiable::getId));
        new MyCloudletsTableBuilder(finishedCloudletsList).build();
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
