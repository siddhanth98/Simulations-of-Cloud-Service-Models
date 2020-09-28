package cloudsimplus.part5.simulations;

import cloudsimplus.part5.Broker;
import cloudsimplus.part5.MyDatacenterAbstract;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.math3.random.JDKRandomGenerator;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * This class will randomly select VM specs and cloudlet specs from the configuration file and send it to the broker.
 */
public class IaasSimulation {
    private final static Config userRequirementsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/part5/iaas_user_requirements.conf"));

    /*
    * Get all possible vm specs here
    */
    private final static List<Integer> vmCountList = userRequirementsConfig.getIntList("conf.vms.count");
    private final static List<Integer> vmPeCountList = userRequirementsConfig.getIntList("conf.vms.pe_count");
    private final static List<Long> vmMipsList = userRequirementsConfig.getLongList("conf.vms.mips");
    private final static List<Long> vmRamList = userRequirementsConfig.getLongList("conf.vms.ram");
    private final static List<Long> vmBwList = userRequirementsConfig.getLongList("conf.vms.bw");
    private final static List<Long> vmSizeList = userRequirementsConfig.getLongList("conf.vms.size");
    private final static List<String> cloudletSchedulingPolicyList = userRequirementsConfig.getStringList("conf.vms.cloudlet_scheduling_policy");
    private final static List<String> vmSchedulingPolicyList = userRequirementsConfig.getStringList("conf.vms.vm_scheduling_policy");

    /*
    * Get list of cloudlet specs here
    */
    private final static List<Integer> cloudletCountList = userRequirementsConfig.getIntList("conf.cloudlets.count");
    private final static List<Integer> cloudletPeCountList = userRequirementsConfig.getIntList("conf.cloudlets.pe_count");
    private final static List<Integer> cloudletLengthList = userRequirementsConfig.getIntList("conf.cloudlets.length");
    private final static List<String> cloudletUtilizationModelList = userRequirementsConfig.getStringList("conf.cloudlets.utilization_model");
    private final static List<Integer> fileSizeList = userRequirementsConfig.getIntList("conf.cloudlets.file_size");
    private final static List<Integer> outputSizeList = userRequirementsConfig.getIntList("conf.cloudlets.output_size");

    public static void main(String[] args) {
        final Random randomSelector = new JDKRandomGenerator();
        final Broker broker = new Broker();
        int vmCount, vmPeCount, cloudletCount, cloudletPeCount, fileSize, outputSize;
        long vmMips, ram, bw, size, cloudletLength;
        String cloudletSchedulingPolicy, vmSchedulingPolicy, cloudletPeUtilizationModel, cloudletRamUtilizationModel,
                cloudletBwUtilizationModel;

        MyDatacenterAbstract dc;

        for (int i = 0; i < 100; i++) {
            vmCount = vmCountList.get(randomSelector.nextInt(vmCountList.size()));
            vmPeCount = vmPeCountList.get(randomSelector.nextInt(vmPeCountList.size()));
            vmMips = vmMipsList.get(randomSelector.nextInt(vmMipsList.size()));
            ram = vmRamList.get(randomSelector.nextInt(vmRamList.size()));
            bw = vmBwList.get(randomSelector.nextInt(vmBwList.size()));
            size = vmSizeList.get(randomSelector.nextInt(vmSizeList.size()));
            vmSchedulingPolicy = vmSchedulingPolicyList.get(randomSelector.nextInt(vmSchedulingPolicyList.size()));

            cloudletCount = cloudletCountList.get(randomSelector.nextInt(cloudletCountList.size()));
            cloudletPeCount = cloudletPeCountList.get(randomSelector.nextInt(cloudletPeCountList.size()));
            cloudletLength  = cloudletLengthList.get(randomSelector.nextInt(cloudletLengthList.size()));
            fileSize = fileSizeList.get(randomSelector.nextInt(fileSizeList.size()));
            outputSize = outputSizeList.get(randomSelector.nextInt(outputSizeList.size()));
            cloudletSchedulingPolicy = cloudletSchedulingPolicyList.get(randomSelector.nextInt(cloudletSchedulingPolicyList.size()));
            cloudletPeUtilizationModel = cloudletUtilizationModelList.get(randomSelector.nextInt(cloudletUtilizationModelList.size()));
            cloudletRamUtilizationModel = cloudletUtilizationModelList.get(randomSelector.nextInt(cloudletUtilizationModelList.size()));
            cloudletBwUtilizationModel = cloudletUtilizationModelList.get(randomSelector.nextInt(cloudletUtilizationModelList.size()));

            vmCount = cloudletCount;

            dc = broker.selectIaaSDatacenter(vmMips, vmPeCount, cloudletLength, cloudletPeCount, fileSize, outputSize);

            broker.submitIaaSVmSpecs(dc, vmCount, vmPeCount, vmMips, ram, bw, size, cloudletSchedulingPolicy, vmSchedulingPolicy);
            broker.submitIaaSCloudletSpecs(dc, cloudletCount, cloudletPeCount, cloudletLength, fileSize, outputSize,
                    cloudletPeUtilizationModel, cloudletRamUtilizationModel, cloudletBwUtilizationModel);
        }
        broker.finishIaaSCloudletSubmissions();
    }
}
