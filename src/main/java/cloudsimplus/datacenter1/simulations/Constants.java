package cloudsimplus.datacenter1.simulations;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * This class will define the specification and requirements constants required for simulation1
 */
public class Constants {
    /**
     * <p>The configuration files initializing specifications and requirements values are opened here</p>
     */
    static final Config specsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/cloudspecs.conf"));
    static final Config requirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/cloudlet_and_vm_requirements.conf")).resolve();

    /**
     * <p>Here the specifications for datacenter hosts are defined</p>
     */
    public static final int HOSTS = specsConfig.getInt("conf.HOSTS.COUNT");
    public static final int HOST_PES = specsConfig.getInt("conf.HOSTS.PE_COUNT");
    public static final int HOST_BW = specsConfig.getInt("conf.HOSTS.BW");
    public static final int HOST_RAM = specsConfig.getInt("conf.HOSTS.RAM");
    public static final int HOST_STORAGE = specsConfig.getInt("conf.HOSTS.STORAGE");
    public static final int HOST_MIPS = specsConfig.getInt("conf.HOSTS.MIPS");

    /**
     * <p>Here the requirements values for VMs and Cloudlets are defined</p>
     */
    public static final int VMS = requirementsConfig.getInt("conf.VMS.COUNT");
    public static final int VMS_BW = requirementsConfig.getInt("conf.VMS.BW");
    public static final int VMS_RAM = requirementsConfig.getInt("conf.VMS.RAM");
    public static final int VMS_STORAGE = requirementsConfig.getInt("conf.VMS.STORAGE");
    public static final int VMS_MIPS = requirementsConfig.getInt("conf.VMS.MIPS");

    public static final int CLOUDLETS = requirementsConfig.getInt("conf.CLOUDLETS.COUNT");
    public static final int CLOUDLET_LENGTH = requirementsConfig.getInt("conf.CLOUDLETS.LENGTH");
    public static final int CLOUDLET_PES = requirementsConfig.getInt("conf.CLOUDLETS.PE_COUNT");

    /**
     * <p>Define datacenter specific constants here.</p>
     */
    public static final int SCHEDULING_INTERVAL = specsConfig.getInt("conf.DATACENTER.SCHEDULING_INTERVAL");
    public static final int EDGE_SWITCHES = specsConfig.getInt("conf.DATACENTER.EDGE_SWITCHES");
}
