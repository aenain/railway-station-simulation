/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.ExternalEvent;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class GenerationEvent extends ExternalEvent {
    private RailwayStation station;
    private Train train;

    public GenerationEvent(RailwayStation station, Train train) {
        super(station, "generation-train-people-event", false);
        this.station = station;
        this.train = train;
    }
    @Override
    public void eventRoutine() {
        station.getPeopleGenerator().generate(train);
    }
    
}
