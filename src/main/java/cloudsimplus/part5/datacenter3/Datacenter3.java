package cloudsimplus.part5.datacenter3;

import cloudsimplus.part5.MyDatacenter;
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
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.network.NetworkVm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static cloudsimplus.part5.datacenter3.Constants.*;

public class Datacenter3 extends MyDatacenter {
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

    public Datacenter3(final CloudSim cloudSim, final DatacenterBroker datacenterBroker) {
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

        /*
         * If there are no waiting VMs currently then allocate a new VM to the new cloudlet
         */
        if (this.getDatacenterBroker().getVmWaitingList().isEmpty()) {
            createAndSubmitVmsForSaas(1, PES*2, cloudletLength/2, VMS_RAM,
                    VMS_BW, OUTPUT_SIZE);
        }
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
    private void createAndSubmitVmsForSaas(final int VMS, final int VMS_PES, final int VMS_MIPS, final int RAM,
                                           final int BW, final int STORAGE) {
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
}
