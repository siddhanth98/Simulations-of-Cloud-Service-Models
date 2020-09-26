package cloudsimplus.part5.datacenter2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * Open the specs configuration file here and get the configuration values
 */
public class Constants {
    private static final Config specsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/part5/Datacenter2/cloudspecs.conf"));

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

    static final long VMS_RAM = specsConfig.getLong("conf.VMS.RAM");
    static final long VMS_BW = specsConfig.getLong("conf.VMS.BW");
    static final long VMS_MIPS = specsConfig.getLong("conf.VMS.MIPS");
}
