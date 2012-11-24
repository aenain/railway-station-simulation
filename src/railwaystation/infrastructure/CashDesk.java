/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class CashDesk extends Desk {
    public CashDesk(Model owner, String name, Region enclosingRegion) {
        super(owner, name, enclosingRegion);
    }

    @Override
    public void addPerson(Person person) {
        // do kasy w kolejce mogą się ustawić jedynie pasażerowie, więc nic nie rób
    }
}
