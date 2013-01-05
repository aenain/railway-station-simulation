/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import railwaystation.RailwayStation;

/**
 *
 * @author artur
 */
public class Subway extends Region {
    protected int number;

    public Subway(RailwayStation station, int number) {
        super(station, "tunnel-" + Integer.toString(number), Infrastructure.MAX_CAPACITY);
        this.number = number;
    }
}
