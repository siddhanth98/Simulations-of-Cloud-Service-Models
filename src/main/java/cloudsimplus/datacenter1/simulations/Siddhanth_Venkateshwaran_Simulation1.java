package cloudsimplus.datacenter1.simulations;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter;
import org.cloudbus.cloudsim.hosts.network.NetworkHost;
import org.cloudbus.cloudsim.network.switches.EdgeSwitch;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.network.NetworkVm;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.ArrayList;
import java.util.List;

import static cloudsimplus.datacenter1.simulations.Constants.*;

public class Siddhanth_Venkateshwaran_Simulation1 {
    /**
     * <p>
     *      This simulation will use the network datacenter1 having 20 hosts
     *      Hosts are connected using an edge switch
     *      It will use the best-fit policy for allocating VMs to hosts.
     *      The best-fit policy will always choose the host having the most number of PEs in use.
     * </p>
     */
    public static void main(String[] args) {
        new Siddhanth_Venkateshwaran_Simulation1();
    }

    /**
     * <p>
     *     The constructor will initialize the simulation object.
     *     Create the datacenter.
     *     Create the datacenter broker.
     *     Submit the VMs and Cloudlets to the broker.
     *     Start the simulation and print the simulation results when it is over.
     * </p>
     */
    private Siddhanth_Venkateshwaran_Simulation1() {
        CloudSim cloudSim = new CloudSim();
        Datacenter datacenter = createDatacenter(cloudSim);
        DatacenterBroker datacenterBroker = new DatacenterBrokerSimple(cloudSim);
        createAndSubmitVms(datacenterBroker);
        createAndSubmitCloudlets(datacenterBroker);
        cloudSim.start();
        printSimulationResults(datacenterBroker);
    }

    /**
     * <p>
     *     This will create the network datacenter for the specified simulation.
     *     @param cloudSim The current simulation object
     * </p>
     */
    private NetworkDatacenter createDatacenter(final CloudSim cloudSim) {
        List<NetworkHost> networkHosts = createHosts();
        NetworkDatacenter networkDatacenter = new NetworkDatacenter(cloudSim, networkHosts, new VmAllocationPolicyBestFit());
        networkDatacenter.setSchedulingInterval(SCHEDULING_INTERVAL);
        createNetwork(cloudSim, networkDatacenter);
        return networkDatacenter;
    }

    /**
     * This will set up 2 edge switches and connect 10 hosts to each one.
     * First 10 hosts will be connected to the 1st edge switch
     * and the next 10 hosts to the 2nd edge switch
     */
    private void createNetwork(final CloudSim cloudSim, final NetworkDatacenter datacenter) {
        EdgeSwitch[] edgeSwitches = new EdgeSwitch[]
                {new EdgeSwitch(cloudSim, datacenter), new EdgeSwitch(cloudSim, datacenter)};
        datacenter.addSwitch(edgeSwitches[0]);
        datacenter.addSwitch(edgeSwitches[1]);

        edgeSwitches[0].setPorts(10);
        edgeSwitches[1].setPorts(10);

        datacenter
                .<NetworkHost>getHostList()
                .stream()
                .filter(host -> host.getId() <= 9)
                .forEach(host -> edgeSwitches[0].connectHost(host));
        datacenter
                .<NetworkHost>getHostList()
                .stream()
                .filter(host -> host.getId() > 9)
                .forEach(host -> edgeSwitches[1].connectHost(host));
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
        List<Pe> peList = createPes();
        for (int i = 0; i < HOSTS; i++) {
            NetworkHost host = new NetworkHost(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
            host.setId(i);
            host.setRamProvisioner(new ResourceProvisionerSimple());
            host.setBwProvisioner(new ResourceProvisionerSimple());
            host.setVmScheduler(new VmSchedulerTimeShared());
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
     */
    private void createAndSubmitVms(final DatacenterBroker datacenterBroker) {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < VMS; i++) {
            NetworkVm networkVm = new NetworkVm(i, VMS_MIPS, VMS_PES);
            networkVm.setRam(VMS_RAM).setBw(VMS_BW).setSize(VMS_STORAGE);
            networkVm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(networkVm);
        }
        datacenterBroker.submitVmList(vmList);
    }

    private void createAndSubmitCloudlets(final DatacenterBroker datacenterBroker) {
        List<NetworkCloudlet> networkCloudlets = new ArrayList<>();
        for (int i = 0; i < CLOUDLETS; i++) {
            NetworkCloudlet networkCloudlet = new NetworkCloudlet(CLOUDLET_LENGTH, CLOUDLET_PES);
            networkCloudlet.setBroker(datacenterBroker);
            networkCloudlets.add(networkCloudlet);
        }
        datacenterBroker.submitCloudletList(networkCloudlets);
    }

    private void printSimulationResults(final DatacenterBroker datacenterBroker) {
        new CloudletsTableBuilder(datacenterBroker.getCloudletFinishedList()).build();
    }
}
