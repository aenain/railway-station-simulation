/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import railwaystation.person.Passenger;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class Desk extends SimProcess {
    protected Region enclosingRegion;
    protected ProcessQueue<Person> peopleToServe;

    public Desk(Model owner, String name, Region enclosingRegion) {
        super(owner, name, true);
        this.enclosingRegion = enclosingRegion;
        peopleToServe = new ProcessQueue(owner, name, true, true);
    }

    public void addPerson(Person person) {
        peopleToServe.insert(person);
    }

    public void addPassenger(Passenger passenger) {
        peopleToServe.insert(passenger);
    }

    public int getWaitingCount() {
        return peopleToServe.size();
    }

    @Override
    public void lifeCycle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
