package cloudsimplus.datacenter1.simulations.simulation2;

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
    static final Config simulation1RequirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/Simulation1/cloudlet_and_vm_requirements.conf"));
    static final Config simulation2RequirementsConfig =
            ConfigFactory.parseFile(new File("src/main/resources/configuration/Datacenter1/Simulations/Simulation2/cloudlet_vm_requirements_overrides.conf"))
            .withFallback(simulation1RequirementsConfig)
            .resolve();

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
    public static final int VMS = simulation1RequirementsConfig.getInt("conf.VMS.COUNT");
    public static final int VMS_PES = simulation2RequirementsConfig.getInt("conf.VMS.PE_COUNT");
    public static final int VMS_BW = simulation1RequirementsConfig.getInt("conf.VMS.BW");
    public static final int VMS_RAM = simulation1RequirementsConfig.getInt("conf.VMS.RAM");
    public static final int VMS_STORAGE = simulation1RequirementsConfig.getInt("conf.VMS.STORAGE");
    public static final int VMS_MIPS = simulation1RequirementsConfig.getInt("conf.VMS.MIPS");

    public static final int CLOUDLETS = simulation1RequirementsConfig.getInt("conf.CLOUDLETS.COUNT");
    public static final int CLOUDLET_LENGTH = simulation1RequirementsConfig.getInt("conf.CLOUDLETS.LENGTH");
    public static final int CLOUDLET_PES = simulation1RequirementsConfig.getInt("conf.CLOUDLETS.PE_COUNT");

    public static final int ADDITIONAL_CLOUDLETS = simulation1RequirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.COUNT");
    public static final int ADDITIONAL_CLOUDLETS_LENGTH = simulation1RequirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.LENGTH");
    public static final int ADDITIONAL_CLOUDLETS_PES = simulation1RequirementsConfig.getInt("conf.ADDITIONAL_CLOUDLETS.PE_COUNT");

    /**
     * <p>Define datacenter specific constants here.</p>
     */
    public static final int SCHEDULING_INTERVAL = specsConfig.getInt("conf.DATACENTER.SCHEDULING_INTERVAL");
    public static final int EDGE_SWITCHES = specsConfig.getInt("conf.DATACENTER.EDGE_SWITCHES");
}
