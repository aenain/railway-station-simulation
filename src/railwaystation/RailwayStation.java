/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.Model;
import railwaystation.infrastructure.CashDeskRegion;
import railwaystation.infrastructure.Infrastructure;
import railwaystation.infrastructure.Region;
import railwaystation.infrastructure.ServingRegion;

/**
 *
 * @author artur
 */
public class RailwayStation extends Model {
    protected Infrastructure infrastructure;

    public RailwayStation() {
        super(null, "railway-station", true, true);
        infrastructure = new Infrastructure(this);
        infrastructure.createPlatforms(Infrastructure.PLATFORM_COUNT);

        Region hall = infrastructure.createRegion("hall", Infrastructure.HALL_CAPACITY);
        infrastructure.setEntryRegion(hall);

        ServingRegion informationDeskRegion = infrastructure.createServingRegion("information", Infrastructure.INFO_DESKS_CAPACITY, Infrastructure.INFO_DESK_COUNT);
        infrastructure.setInformationDeskRegion(informationDeskRegion);

        CashDeskRegion cashDeskRegion = infrastructure.createCashDeskRegion("cash-desks", Infrastructure.CASH_DESKS_CAPACITY, Infrastructure.CASH_DESK_COUNT);
        infrastructure.setCashDeskRegion(cashDeskRegion);

        Region waitingRoom = infrastructure.createRegion("waiting-room", Infrastructure.WAITING_ROOM_CAPACITY);
        infrastructure.setWaitingRoom(waitingRoom);

        // polaczenia miedzy elementami infrastruktury dworcowej
        infrastructure.bindRegions(hall, waitingRoom);
        infrastructure.bindWithPlatforms(hall);
        infrastructure.bindWithPlatforms(waitingRoom);
        infrastructure.bindRegions(hall, informationDeskRegion);
        infrastructure.bindRegions(hall, cashDeskRegion);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    }

    @Override
    public String description() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void doInitialSchedules() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}