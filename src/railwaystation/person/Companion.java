/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import railwaystation.RailwayStation;
import railwaystation.infrastructure.Path;

/**
 *
 * @author artur
 */
public class Companion extends Person {
    protected Passenger passenger;

    public Companion(RailwayStation station, String name, Path path) {
        super(station, name, path);
        passenger = null;
    }

    public Companion(RailwayStation station, String name) {
        super(station, name);
        passenger = null;
    }

    public Companion(RailwayStation station, String name, Passenger passenger) {
        super(station, name);
        this.passenger = passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        
    }
}
