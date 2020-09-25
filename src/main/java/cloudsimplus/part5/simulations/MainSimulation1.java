package cloudsimplus.part5.simulations;

import cloudsimplus.part5.Broker;
import cloudsimplus.part5.MyDatacenter;
import cloudsimplus.part5.datacenter1.Datacenter1;
import cloudsimplus.part5.datacenter2.Datacenter2;
import cloudsimplus.part5.datacenter3.Datacenter3;
import org.cloudbus.cloudsim.datacenters.Datacenter;

public class MainSimulation1 {
    public static void main(String[] args) {
        Broker broker = new Broker();
        MyDatacenter dc1 = broker.selectSaaSDatacenter("write", "file1.txt", 10000, 10,
                2, 20);
        dc1.start();

    }
}
