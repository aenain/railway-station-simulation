/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import railwaystation.person.Passenger;

/**
 *
 * @author artur
 */
public class Train extends Region {
    protected ProcessQueue<Passenger> gettingOutPassengers, gettingInPassengers;
    protected Integer otherPassengerCount;

    public Train(Model owner, String name) {
        super(owner, name, Infrastructure.TRAIN_CAPACITY);
        otherPassengerCount = 100;
        gettingOutPassengers = new ProcessQueue(owner, name + "-getting-out-passengers", true, true);
        gettingInPassengers = new ProcessQueue(owner, name + "-getting-in-passengers", true, true);
    }
}
