package cloudsimplus.part5.datacenter1;

import ch.qos.logback.classic.util.ContextInitializer;
import cloudsimplus.part5.MyDatacenterAbstract;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.network.NetworkHost;
import org.cloudbus.cloudsim.network.switches.AggregateSwitch;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static cloudsimplus.part5.datacenter1.Constants.*;

/**
 * This class will construct datacenter 1 and initialize simulation entities
 */
public class Datacenter1 extends MyDatacenterAbstract {
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

    private final Map<String, ArrayList<String>> services;

    /*
    * These maps are not final as they will only be initialized when cloudlets are
    * submitted to the broker.
    */
    private Map<Long, Map<Double, Double>> cloudletCpuMap;
    private Map<Long, Map<Double, Double>> cloudletRamMap;
    private Map<Long, Map<Double, Double>> cloudletBwMap;
    private Map<Long, Map<Double, Double>> cloudletStorageMap;

    private final Logger myLogger;

    public Datacenter1(final CloudSim cloudSim, final DatacenterBroker datacenterBroker) {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "src/main/resources/configuration/logback-test.xml");
        this.services = new HashMap<>();
        this.cloudSim = cloudSim;
        datacenter = createDatacenter();
        this.datacenterBroker = datacenterBroker;

        hostCpuUtilizationMap = initializeHostUtilizationMaps(datacenter);
        hostRamUtilizationMap = initializeHostUtilizationMaps(datacenter);
        hostBwUtilizationMap = initializeHostUtilizationMaps(datacenter);

        vmCpuUtilizationMap = initializeVmUtilizationMaps(datacenter);
        vmRamUtilizationMap = initializeVmUtilizationMaps(datacenter);
        vmBwUtilizationMap = initializeVmUtilizationMaps(datacenter);

        /*createAndSubmitInitialVms(INITIAL_VM_COUNT, INITIAL_VM_PES, INITIAL_VM_MIPS,
                VMS_RAM, VMS_BW, VMS_STORAGE);
        createAndSubmitInitialCloudlets();*/

        myLogger = LoggerFactory.getLogger(Datacenter1.class.getSimpleName());
        this.cloudSim.addOnClockTickListener(super::processOnClockTickListener);
        initializeServicesMap(FILES);
//        configureLogs();
    }

    /**
     * Getter for all entities defined here
     */

    @Override
    public Map<String, ArrayList<String>> getServices() {
        return services;
    }

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

    @Override
    public Map<Long, Map<Double, Double>> getCloudletRamMap() {
        return cloudletRamMap;
    }

    @Override
    public Map<Long, Map<Double, Double>> getCloudletBwMap() {
        return cloudletBwMap;
    }

    public Logger getMyLogger() {
        return myLogger;
    }

    public void setCloudletRamMap(Map<Long, Map<Double, Double>> cloudletRamMap) {
        this.cloudletRamMap = cloudletRamMap;
    }

    @Override
    public void setCloudletBwMap(Map<Long, Map<Double, Double>> cloudletBwMap) {
        this.cloudletBwMap = cloudletBwMap;
    }

    public ArrayList<String> getOperations(String file) {
        return (ArrayList<String>)(file.equals("file1") ? FILE1_OPERATIONS : (file.equals("file2") ? FILE2_OPERATIONS : FILE3_OPERATIONS));
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
        networkDatacenter.setName("Datacenter1");
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

    public void createAndSubmitVms(final int VMS, final int PES, final long MIPS, final long RAM, final long BW,
                                   final long STORAGE, final CloudletScheduler cloudletScheduler,
                                   final VmScheduler vmScheduler) {
        super.createAndSubmitVms(VMS, PES, MIPS, RAM, BW, STORAGE, cloudletScheduler, vmScheduler);
    }

    public void createAndSubmitCloudlets(final int CLOUDLETS, final long cloudletLength, final int PES,
                                         final int FILE_SIZE, final int OUTPUT_SIZE,
                                         final UtilizationModel peUtilizationModel, final UtilizationModel ramUtilizationModel,
                                         final UtilizationModel bwUtilizationModel) {
        super.createAndSubmitCloudlets(CLOUDLETS, cloudletLength, PES, FILE_SIZE, OUTPUT_SIZE,
                peUtilizationModel, ramUtilizationModel, bwUtilizationModel);
    }

    /**
     * This function creates VMs for providing SaaS service to a cloudlet.
     * It will be invoked by the main broker.
     * If the operation is a write operation then it will be a straightforward cloudlet execution.
     * If the operation is a read operation then a file to the requiredFilesList of the executing cloudlet
     * @param operationName The type of operation - read/write
     * @param fileName The file to be read/written to
     * @param cloudletLength Length of cloudlet in MI (million instructions)
     * @param PES Number of PEs required by the cloudlet
     * @param FILE_SIZE Input file size of the cloudlet
     * @param OUTPUT_SIZE Output file size of the cloudlet
     */
    public Cloudlet submitCloudletsForSaaS (final String operationName, String fileName, final int cloudletLength, final int PES, final int FILE_SIZE,
                                 final int OUTPUT_SIZE) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        Cloudlet cloudlet = new CloudletSimple(cloudletLength, PES);
        UtilizationModel ramUtilModel = new UtilizationModelStochastic(UtilizationModel.Unit.ABSOLUTE, new UniformDistr(1, OUTPUT_SIZE*10));
        UtilizationModel bwUtilModel = new UtilizationModelStochastic(UtilizationModel.Unit.ABSOLUTE, new UniformDistr(1, FILE_SIZE*10));

        cloudlet.setUtilizationModelRam(ramUtilModel)
                .setUtilizationModelCpu(bwUtilModel)
                .setFileSize(FILE_SIZE)
                .setOutputSize(OUTPUT_SIZE);

        /*
        * If there are no waiting VMs currently then allocate a new VM to the new cloudlet
        */
        if (this.getDatacenterBroker().getVmWaitingList().isEmpty() && this.getDatacenterBroker().getVmCreatedList().isEmpty()) {
            if (operationName.equals("process"))
                createAndSubmitVms(VMS, PES, VMS_PROCESSING_MIPS, VMS_RAM, VMS_BW, VMS_STORAGE);
            else createAndSubmitVms(VMS, PES, VMS_READ_MIPS, VMS_RAM, VMS_BW, VMS_STORAGE);
        }
        cloudletList.add(cloudlet);
        this.getDatacenterBroker().submitCloudletList(cloudletList);
        this.fillCloudletMaps(cloudlet);
        return cloudlet;
    }
}
