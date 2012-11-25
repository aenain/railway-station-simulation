/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import java.util.LinkedList;
import railwaystation.RailwayStation;
import railwaystation.person.Passenger;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class CashDeskRegion extends ServingRegion {
    public CashDeskRegion(RailwayStation owner, String name, Integer capacity) {
        super(owner, name, capacity);
    }

    @Override
    protected void initDesks(Integer count) {
        desks = new LinkedList();

        for (Integer i = 1; i <= count; i++) {
            desks.add(new CashDesk(owner, name + "-desk-" + i.toString(), this));
        }
    }

    @Override
    public boolean personEnters(Person person) {
        boolean canEnter = super.personEnters(person);

        // do kolejki ustaw tylko pasażerów
        if (canEnter && person instanceof Passenger) {
            getDeskWithShortestQueue().addPassenger((Passenger)person);
        }

        return canEnter;
    }
}
