package cloudsimplus.part5.datacenter2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.List;

/**
 * Open the specs configuration file here and get the configuration values
 */
public class Constants {
    private static final Config specsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/part5/Datacenter2/cloudspecs.conf"));
    private static final Config servicesConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/part5/Datacenter2/services.conf"));

    static final int EDGE_SWITCHES = specsConfig.getInt("conf.DATACENTER.EDGE_SWITCHES");
    static final double COST_PER_PE = specsConfig.getDouble("conf.DATACENTER.COST_PER_PE");
    static final double COST_PER_RAM = specsConfig.getDouble("conf.DATACENTER.COST_PER_RAM");
    static final double COST_PER_BW = specsConfig.getDouble("conf.DATACENTER.COST_PER_BW");
    static final double COST_PER_STORAGE = specsConfig.getDouble("conf.DATACENTER.COST_PER_STORAGE");
    static final int SCHEDULING_INTERVAL = specsConfig.getInt("conf.DATACENTER.SCHEDULING_INTERVAL");

    static final int HOSTS = specsConfig.getInt("conf.DATACENTER.HOSTS");
    static final long HOST_PES = specsConfig.getLong("conf.HOSTS.PE_COUNT");
    static final long HOST_BW = specsConfig.getLong("conf.HOSTS.BW");
    static final long HOST_RAM = specsConfig.getLong("conf.HOSTS.RAM");
    static final long HOST_STORAGE = specsConfig.getLong("conf.HOSTS.STORAGE");
    static final double HOST_MIPS = specsConfig.getDouble("conf.HOSTS.MIPS");

    static final int INITIAL_VM_COUNT = specsConfig.getInt("conf.VMS.INITIAL_COUNT");
    static final int INITIAL_VM_PES = specsConfig.getInt("conf.VMS.INITIAL_PE_COUNT");
    static final long INITIAL_VM_MIPS = specsConfig.getLong("conf.VMS.INITIAL_MIPS");
    static final int VMS = specsConfig.getInt("conf.VMS.COUNT");
    static final long VMS_RAM = specsConfig.getLong("conf.VMS.RAM");
    static final long VMS_BW = specsConfig.getLong("conf.VMS.BW");
    static final long VMS_STORAGE = specsConfig.getLong("conf.VMS.STORAGE");
    static final long VMS_PROCESSING_MIPS = specsConfig.getLong("conf.VMS.PROCESSING_MIPS");
    static final long VMS_READ_MIPS = specsConfig.getLong("conf.VMS.READ_MIPS");

    static final List<String> FILES = servicesConfig.getStringList("conf.files");
    static final List<String> FILE1_OPERATIONS = servicesConfig.getStringList("conf.services.file1.operations");
    static final List<String> FILE2_OPERATIONS = servicesConfig.getStringList("conf.services.file2.operations");
}
