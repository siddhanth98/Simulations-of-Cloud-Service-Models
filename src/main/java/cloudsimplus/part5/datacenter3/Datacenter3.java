package cloudsimplus.part5.datacenter3;

import cloudsimplus.part5.MyDatacenterAbstract;
import cloudsimplus.part5.datacenter2.Datacenter2;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.network.NetworkHost;
import org.cloudbus.cloudsim.network.switches.AggregateSwitch;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static cloudsimplus.part5.datacenter3.Constants.*;

public class Datacenter3 extends MyDatacenterAbstract {
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
    private final Map<String, ArrayList<String>> services;

    /*
     * These maps are not final as they will only be initialized when cloudlets are
     * submitted to the broker.
     */
    private Map<Long, Map<Double, Double>> cloudletCpuMap;
    private Map<Long, Map<Double, Double>> cloudletRamMap;
    private Map<Long, Map<Double, Double>> cloudletBwMap;
    private Map<Long, Map<Double, Double>> cloudletStorageMap;

    public Datacenter3(final CloudSim cloudSim, final DatacenterBroker datacenterBroker) {
        this.services = new HashMap<>();
        this.cloudSim = cloudSim;
        this.datacenter = createDatacenter();
        this.datacenterBroker = datacenterBroker;

        this.hostRamUtilizationMap = initializeHostUtilizationMaps(this.datacenter);
        this.hostBwUtilizationMap = initializeHostUtilizationMaps(this.datacenter);
        this.hostCpuUtilizationMap = initializeHostUtilizationMaps(this.datacenter);

        this.vmRamUtilizationMap = initializeVmUtilizationMaps(this.datacenter);
        this.vmBwUtilizationMap = initializeVmUtilizationMaps(this.datacenter);
        this.vmCpuUtilizationMap = initializeVmUtilizationMaps(this.datacenter);

        myLogger = LoggerFactory.getLogger(Datacenter2.class.getSimpleName());
        this.cloudSim.addOnClockTickListener(super::processOnClockTickListener);

        initializeServicesMap(FILES);
//        configureLogs();
    }

    /**
     * Getter for all entities defined here
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

    public Logger getMyLogger() {
        return myLogger;
    }

    @Override
    public Map<Long, Map<Double, Double>> getCloudletRamMap() {
        return cloudletRamMap;
    }

    @Override
    public Map<Long, Map<Double, Double>> getCloudletBwMap() {
        return cloudletBwMap;
    }

    @Override
    public Map<String, ArrayList<String>> getServices() {
        return services;
    }

    @Override
    public void setCloudletRamMap(Map<Long, Map<Double, Double>> cloudletRamMap) {
        this.cloudletRamMap = cloudletRamMap;
    }

    @Override
    public void setCloudletBwMap(Map<Long, Map<Double, Double>> cloudletBwMap) {
        this.cloudletBwMap = cloudletBwMap;
    }

    public ArrayList<String> getOperations(String file) {
            return (ArrayList<String>) (file.equals("file2") ? FILE2_OPERATIONS : FILE3_OPERATIONS);
    }

    /**
     * Create a network datacenter here
     * The datacenter costs are set as per the configuration file values
     */
    private Datacenter createDatacenter() {
        List<NetworkHost> hostList = createHosts();
        NetworkDatacenter networkDatacenter = new NetworkDatacenter(this.getCloudSim(), hostList, new VmAllocationPolicySimple());
        networkDatacenter.setSchedulingInterval(SCHEDULING_INTERVAL);
        networkDatacenter.getCharacteristics()
                .setCostPerSecond(COST_PER_PE)
                .setCostPerMem(COST_PER_RAM)
                .setCostPerBw(COST_PER_BW)
                .setCostPerStorage(COST_PER_STORAGE);
        networkDatacenter.setName("Datacenter3");
        createNetwork(this.getCloudSim(), networkDatacenter);
        return networkDatacenter;
    }

    private List<NetworkHost> createHosts() {
        List<NetworkHost> hostList = new ArrayList<>();

        for (int i = 0; i < HOSTS; i++) {
            NetworkHost networkHost = new NetworkHost(HOST_RAM, HOST_BW, HOST_STORAGE, createPes());
            networkHost.setRamProvisioner(new ResourceProvisionerSimple());
            networkHost.setBwProvisioner(new ResourceProvisionerSimple());
            networkHost.setVmScheduler(new VmSchedulerTimeShared());
            networkHost.enableStateHistory();
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
        final EdgeSwitch[] edgeSwitches = new EdgeSwitch[EDGE_SWITCHES];
        for (int i = 0; i < EDGE_SWITCHES; i++) {
            edgeSwitches[i] = new EdgeSwitch(cloudSim, datacenter);
            edgeSwitches[i].setPorts(HOSTS / EDGE_SWITCHES);
            connector.connect(edgeSwitches[i], datacenter.getHostList(), EDGE_SWITCHES*i, EDGE_SWITCHES*i+EDGE_SWITCHES);
            datacenter.addSwitch(edgeSwitches[i]);
        }

        AggregateSwitch aggregateSwitch = new AggregateSwitch(cloudSim, datacenter);
    }

    /**
     * This function creates VMs for providing SaaS service to a cloudlet.
     * It will be invoked by the main broker.
     * If the operation is a write operation then it will be a straightforward cloudlet execution.
     * If the operation is a read operation then a file to the requiredFilesList of the executing cloudlet
     */
    public void submitCloudletsForSaaS (final String operationName, String fileName, final int cloudletLength, final int PES, final int FILE_SIZE,
                                        final int OUTPUT_SIZE) {
        Cloudlet cloudlet = new CloudletSimple(cloudletLength, PES);
        cloudlet.setUtilizationModel(new UtilizationModelStochastic())
                .setFileSize(FILE_SIZE)
                .setOutputSize(OUTPUT_SIZE);
        if (operationName.equals("read"))
            cloudlet.addRequiredFile(fileName);

        this.getDatacenterBroker().submitCloudlet(cloudlet);
        this.fillCloudletMaps(cloudlet);
        /*
         * If there are no waiting VMs currently then allocate a new VM to the new cloudlet
         */
        if (this.getDatacenterBroker().getVmWaitingList().isEmpty()) {
            createAndSubmitVmsForSaas(1, PES*2, VMS_MIPS, VMS_RAM,
                    VMS_BW, OUTPUT_SIZE);
        }
    }
}
