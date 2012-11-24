/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.Model;
import railwaystation.infrastructure.Path;

/**
 *
 * @author artur
 */
public class Companion extends Person {

    protected Passenger passenger;

    public Companion(Model owner, String name, Path path) {
        super(owner, name, path);
        passenger = null;
    }

    public Companion(Model owner, String name) {
        super(owner, name);
        passenger = null;
    }

    public Companion(Model owner, String name, Passenger passenger) {
        super(owner, name);
        this.passenger = passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        
    }
}
