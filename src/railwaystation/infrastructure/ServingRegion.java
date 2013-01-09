/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import java.util.LinkedList;
import railwaystation.RailwayStation;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class ServingRegion extends Region {
    protected Model owner;
    protected LinkedList<Desk> desks;

    public ServingRegion(RailwayStation owner, String name, Integer capacity) {
        super(owner, name, capacity);
        this.owner = owner;
        walkingTime = TimeSpan.ZERO;
    }

    protected void initDesks(Integer count) {
        desks = new LinkedList();

        for (Integer i = 1; i <= count; i++) {
            desks.add(new Desk(owner, name + "-desk-" + i.toString(), this));
        }
    }

    public void addPersonToShortestQueue(Person person) {
        getDeskWithShortestQueue().addPerson(person);
    }

    protected Desk getDeskWithShortestQueue() {
        if (desks.isEmpty()) {
            return null;
        }
        Desk withShortestQueue = desks.getFirst();

        for (Desk desk : desks) {
            if (withShortestQueue.count() > desk.count()) {
                withShortestQueue = desk;
            }
        }

        return withShortestQueue;
    }

    public LinkedList<Desk> getDesks() {
        return desks;
    }

    @Override
    public void stackPeopleChange() {
        for (Desk desk : desks) {
            desk.stackPeopleChange();
        }
        // super.stackPeopleChange(); nie rejestrujemy dla regionow z okienkami
    }
}
