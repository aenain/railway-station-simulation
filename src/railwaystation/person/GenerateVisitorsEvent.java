/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;
import railwaystation.Distribution;
import railwaystation.RailwayStation;

/**
 *
 * @author KaMyLuS
 */
public class GenerateVisitorsEvent extends ExternalEvent {
    
    public static final TimeSpan INTERVAL = new TimeSpan(1, TimeUnit.HOURS);
    private RailwayStation station;
    private Distribution distribution;
    private static int hour = 0;

    public GenerateVisitorsEvent(RailwayStation station, Distribution dist) {
        super(station, "generate-visitors-event", false);
        this.station = station;
        this.distribution = dist;
    }
    
    @Override
    public void eventRoutine() {
        station.getPeopleGenerator().generateVisitors(distribution.hourlyVisitorCount(hour));
        hour++;
        
        GenerateVisitorsEvent event = new GenerateVisitorsEvent(station, distribution);
        event.setSchedulingPriority(getSchedulingPriority());
        event.schedule(INTERVAL);
    }
}
