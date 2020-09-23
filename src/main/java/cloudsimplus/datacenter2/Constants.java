package cloudsimplus.datacenter2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * This class defines the constants to be used for datacenter 2 simulations
 */
public class Constants {
    /**
     * Open configuration files here
     */
    private static final Config specsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter2/cloudspecs.conf"));
    private static final Config requirementsConfig = ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter2/Simulations/cloudlet_vm_requirements.conf"));

    /**
     * Define datacenter constants here
     */

    static final int EDGE_SWITCHES = specsConfig.getInt("conf.DATACENTER.EDGE_SWITCHES");
    static final int SCHEDULING_INTERVAL = specsConfig.getInt("conf.DATACENTER.SCHEDULING_INTERVAL");
    static final int SCALING_TERMINATION_TIME = specsConfig.getInt("conf.DATACENTER.SCALING_TERMINATION_TIME");

    static final int HOSTS = specsConfig.getInt("conf.DATACENTER.HOSTS");
    static final int HOST_PES = specsConfig.getInt("conf.HOSTS.PE_COUNT");
    static final int HOST_RAM = specsConfig.getInt("conf.HOSTS.RAM");
    static final int HOST_BW = specsConfig.getInt("conf.HOSTS.BW");
    static final int HOST_STORAGE = specsConfig.getInt("conf.HOSTS.STORAGE");
    static final int HOST_MIPS = specsConfig.getInt("conf.HOSTS.MIPS");

    static final int VMS = requirementsConfig.getInt("conf.VMS.COUNT");
    static final int VMS_PES = requirementsConfig.getInt("conf.VMS.PE_COUNT");
    static final int VMS_RAM = requirementsConfig.getInt("conf.VMS.RAM");
    static final int VMS_BW = requirementsConfig.getInt("conf.VMS.BW");
    static final int VMS_STORAGE = requirementsConfig.getInt("conf.VMS.STORAGE");
    static final int VMS_MIPS = requirementsConfig.getInt("conf.VMS.MIPS");
    static final int SCALING_VM_COUNT = requirementsConfig.getInt("conf.VMS.SCALING_COUNT");

    static final int CLOUDLETS = requirementsConfig.getInt("conf.CLOUDLETS.COUNT");
    static final int CLOUDLET_LENGTH = requirementsConfig.getInt("conf.CLOUDLETS.LENGTH");
    static final int CLOUDLET_PES = requirementsConfig.getInt("conf.CLOUDLETS.PE_COUNT");
    static final int ADDITIONAL_CLOUDLETS = requirementsConfig.getInt("conf.CLOUDLETS.ADDITIONAL_CLOUDLETS_COUNT");
    static final int SUBMISSION_DELAY = requirementsConfig.getInt("conf.CLOUDLETS.SUBMISSION_DELAY");
    static final int CLOUDLET_RAM = requirementsConfig.getInt("conf.CLOUDLETS.RAM");
    static final int CLOUDLET_PACKET_SIZE = requirementsConfig.getInt("conf.CLOUDLETS.PACKET_SIZE");
    static final int CLOUDLET_PACKET_COUNT = requirementsConfig.getInt("conf.CLOUDLETS.PACKET_COUNT");
}
