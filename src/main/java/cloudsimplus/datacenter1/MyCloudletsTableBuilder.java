package cloudsimplus.datacenter1;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.Identifiable;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.builders.tables.TableColumn;

import java.util.List;

public class MyCloudletsTableBuilder extends CloudletsTableBuilder {
    private static final String TIME_FORMAT = "%.0f";
    private static final String SECONDS = "Seconds";
    private static final String CPU_CORES = "CPU cores";

    public MyCloudletsTableBuilder(List<? extends Cloudlet> list) {
        super(list);
    }

    @Override
    protected void createTableColumns() {
        final String ID = "ID";
        addColumnDataFunction(getTable().addColumn("Cloudlet", ID), Identifiable::getId);
        addColumnDataFunction(getTable().addColumn("Host", ID), cloudlet -> cloudlet.getVm().getHost().getId());
        addColumnDataFunction(getTable().addColumn("VM", ID), cloudlet -> cloudlet.getVm().getId());
        addColumnDataFunction(getTable().addColumn("CloudletLen", "MI"), Cloudlet::getLength);
        addColumnDataFunction(getTable().addColumn("CloudletPEs", CPU_CORES), Cloudlet::getNumberOfPes);

        TableColumn col = getTable().addColumn("StartTime", SECONDS).setFormat(TIME_FORMAT);
        addColumnDataFunction(col, Cloudlet::getExecStartTime);

        col = getTable().addColumn("FinishTime", SECONDS).setFormat(TIME_FORMAT);
        addColumnDataFunction(col, cl -> roundTime(cl, cl.getFinishTime()));

        col = getTable().addColumn("ExecTime", SECONDS).setFormat(TIME_FORMAT);
        addColumnDataFunction(col, cl -> roundTime(cl, cl.getActualCpuTime()));
    }

    private double roundTime(final Cloudlet cloudlet, final double time) {

        /*If the given time minus the start time is less than 1,
         * it means the execution time was less than 1 second.
         * This way, it can't be round.*/
        if(time - cloudlet.getExecStartTime() < 1){
            return time;
        }

        final double startFraction = cloudlet.getExecStartTime() - (int) cloudlet.getExecStartTime();
        return Math.round(time - startFraction);
    }
}