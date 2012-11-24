/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import java.util.LinkedList;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class ServingRegion extends Region {
    protected Model owner;
    protected LinkedList<Desk> desks;

    public ServingRegion(Model owner, String name, Integer capacity) {
        super(owner, name, capacity);
        this.owner = owner;
    }

    protected void initDesks(Integer count) {
        desks = new LinkedList();

        for (Integer i = 1; i <= count; i++) {
            desks.add(new Desk(owner, name + "-desk-" + i.toString(), this));
        }
    }

    @Override
    public boolean personEnters(Person person) {
        boolean canEnter = super.personEnters(person);

        // do kolejki
        if (canEnter) {
            getDeskWithShortestQueue().addPerson(person);
        }

        return canEnter;
    }

    protected Desk getDeskWithShortestQueue() {
        if (desks.isEmpty()) {
            return null;
        }
        Desk withShortestQueue = desks.getFirst();

        for (Desk desk : desks) {
            if (withShortestQueue.getWaitingCount() < desk.getWaitingCount()) {
                withShortestQueue = desk;
            }
        }

        return withShortestQueue;
    }
}
