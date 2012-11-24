/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;

/**
 *
 * @author artur
 */
public class Platform extends Region {
    public Platform(Model owner, String name) {
        super(owner, name, Infrastructure.PLATFORM_CAPACITY);
    }
}
