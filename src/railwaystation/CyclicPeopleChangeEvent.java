/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;
import railwaystation.infrastructure.Infrastructure;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class CyclicPeopleChangeEvent extends ExternalEvent {
    public static final TimeSpan INTERVAL = new TimeSpan(10, TimeUnit.SECONDS);
    private Infrastructure infrastructure;
    private RailwayStation station;
    private TimeTable timeTable;

    public CyclicPeopleChangeEvent(RailwayStation station) {
        super(station, "people-change-event", false);
        this.station = station;
        this.infrastructure = station.structure;
        this.timeTable = station.getTimeTable();
    }

    @Override
    public void eventRoutine() {
        infrastructure.flushRegionEvents();
        timeTable.flushTrainEvents();
        new CyclicPeopleChangeEvent(station).schedule(INTERVAL);
    }
    
}
