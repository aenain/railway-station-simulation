/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import java.util.LinkedList;
import railwaystation.RailwayStation;
import railwaystation.person.Person;

/**
 *
 * @author artur
 * Klasa reprezentująca część infrastruktury dworca,
 * do której może wejść człowiek odwiedzający dworzec.
 */
public class Region extends SimProcess implements Visitable {
    protected ProcessQueue<Person> people;
    protected LinkedList<Region> adjacentRegions;
    protected String name;

    public Region(RailwayStation owner, String name, Integer capacity) {
        super(owner, name, true);
        this.name = name;
        people = new ProcessQueue(owner, name + "-people", true, true);
        people.setQueueCapacity(capacity);
        adjacentRegions = new LinkedList();
    }

    @Override
    public void lifeCycle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void personLeaves(Person person) {
        people.remove(person);
    }

    @Override
    public boolean personEnters(Person person) {
        if (people.size() < people.getQueueLimit()) {
            people.insert(person);
            return true;
        }

        return false;
    }

    public void bind(Region other) {
        adjacentRegions.add(other);
    }

    public LinkedList<Region> getAdjacentRegions() {
        return adjacentRegions;
    }
}
