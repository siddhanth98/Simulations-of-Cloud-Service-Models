package cloudsimplus.part5.simulations;

import cloudsimplus.part5.Broker;
import cloudsimplus.part5.MyDatacenterAbstract;

public class MainSimulation1 {
    public static void main(String[] args) {
        Broker broker = new Broker();
        MyDatacenterAbstract dc1 = broker.selectSaaSDatacenter("write", "file1.txt", 1000000, 10,
                2, 20);
    }
}
