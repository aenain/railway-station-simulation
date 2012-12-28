/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 *
 * @author artur
 */
public class CashDesk extends Desk {
    public CashDesk(Model owner, String name, Region enclosingRegion) {
        super(owner, name, enclosingRegion);
    }

    @Override
    protected TimeSpan servingTime() {
        return enclosingRegion.station.dist.sellingTicketTime();
    }
}
