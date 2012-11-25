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
public class Platform extends Region {
    public Platform(RailwayStation owner, String name) {
        super(owner, name, Infrastructure.PLATFORM_CAPACITY);
    }
}
