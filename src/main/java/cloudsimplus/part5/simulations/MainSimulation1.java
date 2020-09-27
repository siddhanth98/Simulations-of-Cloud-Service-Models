package cloudsimplus.part5.simulations;

import cloudsimplus.part5.Broker;
import cloudsimplus.part5.MyDatacenterAbstract;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.math3.random.JDKRandomGenerator;

import java.io.File;
import java.util.List;
import java.util.Random;

public class MainSimulation1 {
    private static final Config userRequirementsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/part5/saas_user_requirements.conf"));
    private static final List<String> filesList = userRequirementsConfig.getStringList("conf.files");
    private static final List<String> operationsList = userRequirementsConfig.getStringList("conf.operations");
    private static final List<Integer> cloudletLengthsList = userRequirementsConfig.getIntList("conf.cloudletLengths");
    private static final List<Integer> fileSizeList = userRequirementsConfig.getIntList("conf.fileSizes");
    private static final List<Integer> outputSizeList = userRequirementsConfig.getIntList("conf.outputSizes");
    private static final List<Integer> pesList = userRequirementsConfig.getIntList("conf.PES");

    public static void main(String[] args) {
        Broker broker = new Broker();
        Random randomSelector = new JDKRandomGenerator();
        String file, operation;
        int cloudletLength, fileSize, outputSize, pes;
        MyDatacenterAbstract dc;

        for (int i = 0; i < 30; i++) {
            operation = operationsList.get(randomSelector.nextInt(operationsList.size()));
            file = filesList.get(randomSelector.nextInt(filesList.size()));
            cloudletLength = cloudletLengthsList.get(randomSelector.nextInt(cloudletLengthsList.size()));
            fileSize = fileSizeList.get(randomSelector.nextInt(fileSizeList.size()));
            outputSize = outputSizeList.get(randomSelector.nextInt(outputSizeList.size()));
            pes = pesList.get(randomSelector.nextInt(pesList.size()));

            try {
                dc = broker.selectSaaSDatacenter(operation, file, cloudletLength, pes, fileSize, outputSize);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
