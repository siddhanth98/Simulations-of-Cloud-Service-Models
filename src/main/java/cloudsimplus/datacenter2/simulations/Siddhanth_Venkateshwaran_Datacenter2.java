package cloudsimplus.datacenter2.simulations;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.util.ContextInitializer;
import cloudsimplus.datacenter1.simulations.MyCloudletsTableBuilder;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.cloudlets.network.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.network.NetworkHost;
import org.cloudbus.cloudsim.network.switches.AggregateSwitch;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerAbstract;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.network.NetworkVm;
import org.cloudsimplus.autoscaling.HorizontalVmScaling;
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.listeners.VmEventInfo;
import org.cloudsimplus.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static cloudsimplus.datacenter2.simulations.Constants.*;

/**
 * This is the main class used for constructing datacenter 2.
 * It is a network datacenter having 100 network hosts.
 * The datacenter also has 10 edge switches, each connecting 10 hosts.
 */
public class Siddhanth_Venkateshwaran_Datacenter2 {

    /**
     * Declare simulation entities and other instance variables used here
     */
    private final CloudSim cloudSim;
    private final Datacenter datacenter;
    private final DatacenterBroker datacenterBroker;
    private final Map<Host, Map<Double, Double>> hostRamUtilizationMap;
    private final Map<Host, Map<Double, Double>> hostBwUtilizationMap;
    private final Map<Host, Map<Double, Double>> hostCpuUtilizationMap;
    private final Map<Vm, Map<Double, Double>> vmRamUtilizationMap;
    private final Map<Vm, Map<Double, Double>> vmBwUtilizationMap;
    private final Map<Vm, Map<Double, Double>> vmCpuUtilizationMap;

    private final Logger myLogger;
    private final Logger brokerLogger;

    /**
     * This constructor will be called when no horizontal scaling is to be used for the current simulation
     */
    public Siddhanth_Venkateshwaran_Datacenter2() {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "src/main/resources/configuration/logback-test.xml");
        cloudSim = new CloudSim();
        datacenter = createDatacenter();
        datacenterBroker = new DatacenterBrokerSimple(cloudSim);
        createAndSubmitVms(datacenterBroker);

        hostCpuUtilizationMap = initializeHostUtilizationMaps(datacenter);
        hostRamUtilizationMap = initializeHostUtilizationMaps(datacenter);
        hostBwUtilizationMap = initializeHostUtilizationMaps(datacenter);

        vmCpuUtilizationMap = initializeVmUtilizationMaps(datacenter);
        vmRamUtilizationMap = initializeVmUtilizationMaps(datacenter);
        vmBwUtilizationMap = initializeVmUtilizationMaps(datacenter);

         myLogger = LoggerFactory.getLogger(Siddhanth_Venkateshwaran_Datacenter2.class);

        cloudSim.addOnClockTickListener(this::createAndSubmitNewCloudlets);
        createAndSubmitCloudlets(datacenterBroker);

        brokerLogger = LoggerFactory.getLogger(datacenterBroker.getClass().getSimpleName());
//        configureLogs();
    }

    /**
     * This constructor will be called when the simulation should use a horizontal scaling load balancer
     * @param destructionDelayFunction The lambda function which defines the number of seconds the broker should wait
     *                                 before destroying an idle VM.
     * @param overloadCondition Defines the function which defines the condition which should be satisfied to
     *                          horizontally scale up a VM.
     */
    public Siddhanth_Venkateshwaran_Datacenter2(Function<Vm, Double> destructionDelayFunction, Predicate<Vm> overloadCondition) {
        this();
        this.getDatacenterBroker().setVmDestructionDelayFunction(destructionDelayFunction);
        this.getDatacenterBroker()
                .getVmWaitingList()
                .forEach(vm -> attachHorizontalScalingInstance(vm, overloadCondition));
    }

    /**
     * Expose a function to start simulation instead of starting it in the constructor itself
     */
    public void start() {
        this.getCloudSim().start();
    }

    /**
     * Getter for each of the instance variables
     */
    public CloudSim getCloudSim() {
        return cloudSim;
    }

    public Datacenter getDatacenter() {
        return datacenter;
    }

    public DatacenterBroker getDatacenterBroker() {
        return datacenterBroker;
    }

    public Map<Host, Map<Double, Double>> getHostRamUtilizationMap() {
        return hostRamUtilizationMap;
    }

    public Map<Host, Map<Double, Double>> getHostBwUtilizationMap() {
        return hostBwUtilizationMap;
    }

    public Map<Host, Map<Double, Double>> getHostCpuUtilizationMap() {
        return hostCpuUtilizationMap;
    }

    public Map<Vm, Map<Double, Double>> getVmRamUtilizationMap() {
        return vmRamUtilizationMap;
    }

    public Map<Vm, Map<Double, Double>> getVmBwUtilizationMap() {
        return vmBwUtilizationMap;
    }

    public Map<Vm, Map<Double, Double>> getVmCpuUtilizationMap() {
        return vmCpuUtilizationMap;
    }

    /**
     * Configure logging levels for specific loggers
     * For VM creation success/failure and cloudlet execution start/failure
     */
    private void configureLogs() {
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
                this.getBrokerLogger().warn(String.format("%s:\t\tCloudlet %d could not be mapped to any VM",
                        evtInfo.getCloudlet().getSimulation().clockStr(), evtInfo.getCloudlet().getId()));
                break;
            case FAILED:
                this.getBrokerLogger().warn(String.format("%s:\t\tCloudlet %d failed to execute",
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

    /**
     * Similar to the host function, this will separately collect the Cpu utilization of the VM for the current clock tick
     */
    private void collectVmPeUtilizationMetrics(final Map<Vm, Map<Double, Double>> vmUtilizationMap) {
        this.getDatacenterBroker().getVmCreatedList().forEach(
                vm -> vmUtilizationMap.get(vm).put(this.getCloudSim().clock(),
                        vm.getCpuPercentUtilization())
        );
    }

    /**
     * Create the datacenter here
     */
    private Datacenter createDatacenter() {
        List<Host> hostList = createHosts();
        NetworkDatacenter datacenter = new NetworkDatacenter(this.getCloudSim(), hostList, new VmAllocationPolicyBestFit());
        datacenter.setSchedulingInterval(SCHEDULING_INTERVAL);
        createNetwork(this.getCloudSim(), datacenter);
        return datacenter;
    }

    /**
     * A functional interface which connects all hosts within a range to an edge switch
     */
    @FunctionalInterface
    private interface HostConnector {
        void connect(final EdgeSwitch edgeSwitch, final List<NetworkHost> hostList, final long lowerIdBound, final long upperIdBound);
    }

    /**
     * This function will set up all edge switches and connect hosts to proper edge switches
     * It uses the above defined functional interface to connect hosts to edge switches
     * @param datacenter The current datacenter being used
     */
    private void createNetwork(final CloudSim cloudSim, final NetworkDatacenter datacenter) {
        /*
        * Implement the host connector here
        */
        HostConnector connector = (edgeSwitch, hostList, lowerId, upperId) ->
                hostList
                .stream()
                .filter(host -> host.getId() >= lowerId && host.getId() < upperId)
                .forEach(edgeSwitch::connectHost);

        /*
        * Create edge switches here, connect each one to a group of hosts and add to the datacenter
        */
        EdgeSwitch[] edgeSwitches = new EdgeSwitch[EDGE_SWITCHES];
        for (int i = 0; i < EDGE_SWITCHES; i++) {
            edgeSwitches[i] = new EdgeSwitch(cloudSim, datacenter);
            edgeSwitches[i].setPorts(HOSTS / EDGE_SWITCHES);
            connector.connect(edgeSwitches[i], datacenter.getHostList(), EDGE_SWITCHES*i, EDGE_SWITCHES*i+EDGE_SWITCHES);
            datacenter.addSwitch(edgeSwitches[i]);
        }

        AggregateSwitch aggregateSwitch = new AggregateSwitch(cloudSim, datacenter);
    }

    /**
     * This will create the network hosts
     * Each host will have 20 PEs with 1000 MIPS per PE, 64 GB RAM and 10000 Mbps BW.
     * Each host uses simple provisioning policies for provisioning each resource to each VM
     * Each host also uses the time shared policy for scheduling its VMs
     */
    private List<Host> createHosts() {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < HOSTS; i++) {
            NetworkHost networkHost = new NetworkHost(HOST_RAM, HOST_BW, HOST_STORAGE, createPes());
            networkHost.enableStateHistory();
            networkHost.setRamProvisioner(new ResourceProvisionerSimple());
            networkHost.setBwProvisioner(new ResourceProvisionerSimple());
            networkHost.setVmScheduler(new VmSchedulerTimeShared());
            hostList.add(networkHost);
        }
        return hostList;
    }

    /**
     * This function will create the physical PEs for a host
     */
    private List<Pe> createPes() {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < HOST_PES; i++) {
            PeSimple pe = new PeSimple(HOST_MIPS, new PeProvisionerSimple());
            peList.add(pe);
        }
        return peList;
    }

    /**
     * Create network VMs here and submit the list to the broker
     * Each VM requires 20 PEs, 16 GB RAM, 1000 Mbps BW and 10 GB storage.
     * Each VM uses a space shared scheduling policy for executing its cloudlets.
     */
    private void createAndSubmitVms(final DatacenterBroker datacenterBroker) {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VMS; i++) {
            NetworkVm networkVm = new NetworkVm(i, VMS_MIPS, VMS_PES);
            networkVm.setRam(VMS_RAM).setBw(VMS_BW).setSize(VMS_STORAGE);
            networkVm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
            networkVm.getUtilizationHistory().enable();
            vmList.add(networkVm);
        }
        datacenterBroker.submitVmList(vmList);
    }

    /**
     * This will attach a horizontal scaling instance with each VM submitted to the broker.
     * It uses an overload predicate and a reference function which creates additional VMs for it.
     */
    private void attachHorizontalScalingInstance(final Vm vm, final Predicate<Vm> overloadPredicate) {
        HorizontalVmScaling horizontalVmScaling = new HorizontalVmScalingSimple();
        horizontalVmScaling
                .setVmSupplier(this::createVm)
                .setOverloadPredicate(overloadPredicate);
        vm.setHorizontalScaling(horizontalVmScaling);
    }

    /**
     * This will create an additional VM as directed by the horizontal scaling load balancer
     */
    private Vm createVm() {
        NetworkVm networkVm = new NetworkVm(VMS_MIPS, VMS_PES);
        networkVm.setRam(VMS_RAM).setBw(VMS_BW).setSize(VMS_STORAGE);
        networkVm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
        networkVm.getUtilizationHistory().enable();

        /*
        * Log when this new VM gets allocated and insert this VM in the utilization maps to collect its utilization metrics
        */
        networkVm.addOnHostAllocationListener(evtInfo -> {
            myLogger.info(String.format("%s:\t\tNew VM %d allocated to host %d",
                    evtInfo.getVm().getSimulation().clockStr(), networkVm.getId(), networkVm.getHost().getId()));
            this.getVmCpuUtilizationMap().put(networkVm, new TreeMap<>());
            this.getVmRamUtilizationMap().put(networkVm, new TreeMap<>());
            this.getVmBwUtilizationMap().put(networkVm, new TreeMap<>());
        });

        /*
        * Log the failure of allocation of this new VM
        */
        networkVm.addOnCreationFailureListener(this::logVmAllocationFailure);

        return networkVm;
    }

    /**
     * This will create a list of cloudlets and submit it to the broker.
     */
    private void createAndSubmitCloudlets(DatacenterBroker datacenterBroker) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        for (int i = 0; i < (CLOUDLETS); i++) {
            NetworkCloudlet networkCloudlet = new NetworkCloudlet(i, CLOUDLET_LENGTH, CLOUDLET_PES);
            networkCloudlet.setUtilizationModel(new UtilizationModelStochastic());
            cloudletList.add(networkCloudlet);
        }

        addExecutionTasks(cloudletList);
        datacenterBroker.submitCloudletList(cloudletList);
    }

    /**
     * Each network cloudlet in this simulation will either send or receive packets of specified size
     * This simulation has been configured as so (total of 30 cloudlets):
     *  - Cloudlet 0 will send specified number of packets to cloudlet 15
     *  - Cloudlet 1 will send specified number of packets to cloudlet 16
     *      ......
     *  - Cloudlet 14 will send specified number of packets to cloudlet 29
     *  This function will attach onStart listeners to each cloudlet to add execution send/receive tasks
     *  Listener is required as the addReceiveTask() method requires the source cloudlet to have assigned a VM
     *  This function uses an iterative approach as it is required to access cloudlets ahead in the list
     * @param cloudletList List of cloudlets to which tasks are to be added
     */
    private void addExecutionTasks(List<Cloudlet> cloudletList) {

        /*
        * Add listener to 1st half of the cloudlets to send packets
        */
        cloudletList
                .stream()
                .filter(cloudlet -> cloudlet.getId() >= 0 && cloudlet.getId() < Math.ceil((double)CLOUDLETS/(double)2.0))
                .forEach(cloudlet -> cloudlet.addOnStartListener(evtInfo -> {
                    addExecutionTask((NetworkCloudlet)evtInfo.getCloudlet());
                    addSendTask(
                            (NetworkCloudlet)evtInfo.getCloudlet(),
                            (NetworkCloudlet)(cloudletList.get((int)evtInfo.getCloudlet().getId()+CLOUDLETS/2))
                    );
                }));

        /*
        * Add listener to 2nd half of the cloudlets to receive packets
        */
        cloudletList
                .stream()
                .filter(cloudlet -> cloudlet.getId() >= CLOUDLETS/2)
                .forEach(cloudlet -> cloudlet.addOnStartListener(evtInfo -> {
                    addReceiveTask(
                            (NetworkCloudlet)cloudletList.get((int)evtInfo.getCloudlet().getId()-CLOUDLETS/2),
                            (NetworkCloudlet)evtInfo.getCloudlet()
                    );
                    addExecutionTask((NetworkCloudlet)evtInfo.getCloudlet());
                }));
    }

    /**
     * This function creates an execution task of specified size and adds it to the cloudlet
     * @param networkCloudlet Network cloudlet to which the task is to be added
     */
    private void addExecutionTask(NetworkCloudlet networkCloudlet) {
        CloudletTask executionTask = new CloudletExecutionTask(networkCloudlet.getTasks().size(), CLOUDLET_LENGTH);
        executionTask.setMemory(CLOUDLET_RAM);
        networkCloudlet.addTask(executionTask);
    }

    /**
     * This function adds a task to send packets of specified sizes to a cloudlet
     * @param  sourceNetworkCloudlet Source of the send task
     * @param destinationCloudlet Destination of the receive task
     */
    private void addSendTask(NetworkCloudlet sourceNetworkCloudlet, NetworkCloudlet destinationCloudlet) {
        CloudletSendTask sendTask = new CloudletSendTask(sourceNetworkCloudlet.getTasks().size());
        sendTask.setMemory(CLOUDLET_RAM);
        sourceNetworkCloudlet.addTask(sendTask);

        for (int i = 0; i < CLOUDLET_PACKET_COUNT; i++)
            sendTask.addPacket(destinationCloudlet, CLOUDLET_PACKET_SIZE);
    }

    /**
     * This function will add a task to receive packets of specified size to a cloudlet
     * @param sourceCloudlet Cloudlet which sent the packet
     * @param destinationCloudlet Cloudlet which receives the packet
     */
    private void addReceiveTask(NetworkCloudlet sourceCloudlet, NetworkCloudlet destinationCloudlet) {
        CloudletReceiveTask receiveTask = new CloudletReceiveTask(destinationCloudlet.getTasks().size(), sourceCloudlet.getVm());
        receiveTask.setMemory(CLOUDLET_RAM);
        receiveTask.setExpectedPacketsToReceive(CLOUDLET_PACKET_COUNT);
        destinationCloudlet.addTask(receiveTask);
    }

    /**
     * This function is called at every clock tick of the simulation and will create additional cloudlets
     * to feed into the VMs, upto the scaling termination time, after which it stops creating additional ones.
     * Every new cloudlet creation will be logged separately
     * @param evtInfo The event info object which can be used to access event entities and their data
     */
    private void createAndSubmitNewCloudlets(EventInfo evtInfo) {

        /*
         * Metrics are not collected at every time interval to reduce log size
         */
        if ((long)evtInfo.getTime() % (SCHEDULING_INTERVAL*3) == 0) {
            collectVmUtilizationMetrics(this.getVmRamUtilizationMap(), Ram.class);
            collectVmUtilizationMetrics(this.getVmBwUtilizationMap(), Bandwidth.class);
            collectVmPeUtilizationMetrics(this.getVmCpuUtilizationMap());

            collectHostUtilizationMetrics(this.getHostRamUtilizationMap(), Ram.class);
            collectHostUtilizationMetrics(this.getHostBwUtilizationMap(), Bandwidth.class);
            collectHostPeUtilizationMetrics(this.getHostCpuUtilizationMap());
        }

        /*
        * Cloudlets are created at alternate time intervals instead of at every time interval
        */
        if ((long)evtInfo.getTime() % (SCHEDULING_INTERVAL*2) == 0 && evtInfo.getTime() <= STREAM_TERMINATION_TIME) {
            List<Cloudlet> newCloudletList = new ArrayList<>();
            for (int i = 0; i < ADDITIONAL_CLOUDLETS; i++) {
                Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES);
                cloudlet.setUtilizationModel(new UtilizationModelStochastic());
                cloudlet.addOnStartListener(evt -> myLogger.info(String.format("New CLOUDLET %d started execution", evt.getCloudlet().getId())));
                newCloudletList.add(cloudlet);
            }
            this.getDatacenterBroker().submitCloudletList(newCloudletList);
        }
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
