/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import java.util.ArrayList;

/**
 *
 * @author artur
 */
public class Infrastructure {

    public static final Integer PLATFORM_COUNT = 4,
            WAITING_ROOM_CAPACITY = 10000,
            HALL_CAPACITY = 1000,
            CASH_DESKS_CAPACITY = 500,
            CASH_DESK_COUNT = 10,
            INFO_DESKS_CAPACITY = 100,
            INFO_DESK_COUNT = 3,
            SUBWAY_CAPACITY = 5000,
            PLATFORM_CAPACITY = Integer.MAX_VALUE, // close enough to the inf.
            TRAIN_CAPACITY = Integer.MAX_VALUE;
    protected Region entryRegion, waitingRoom, firstSubway;
    protected Model station;
    protected CashDeskRegion cashDeskRegion;
    protected ServingRegion informationDeskRegion;
    protected ArrayList<Platform> platforms;

    public Infrastructure(Model station) {
        entryRegion = null;
        firstSubway = null;
        this.station = station;
        platforms = new ArrayList();
    }

    public Region createRegion(String name, int capacity) {
        return new Region(station, name, capacity);
    }

    public ServingRegion createServingRegion(String name, int capacity, int deskCount) {
        ServingRegion region = new ServingRegion(station, name, capacity);
        region.initDesks(deskCount);
        return region;
    }

    public CashDeskRegion createCashDeskRegion(String name, int capacity, int deskCount) {
        CashDeskRegion region = new CashDeskRegion(station, name, capacity);
        region.initDesks(deskCount);
        return region;
    }

    public void setEntryRegion(Region entryRegion) {
        this.entryRegion = entryRegion;
    }

    public Region getEntryRegion() {
        return entryRegion;
    }

    public void setWaitingRoom(Region waitingRoom) {
        this.waitingRoom = waitingRoom;
    }

    public Region getWaitingRoom() {
        return waitingRoom;
    }

    public void setCashDeskRegion(CashDeskRegion region) {
        this.cashDeskRegion = region;
    }

    public CashDeskRegion getCashDeskRegion() {
        return cashDeskRegion;
    }

    public void setInformationDeskRegion(ServingRegion region) {
        this.informationDeskRegion = region;
    }

    public ServingRegion getInformationDeskRegion() {
        return informationDeskRegion;
    }

    public void createPlatforms(int platformCount) {
        Region subway, previousSubway;
        Platform platform = null;

        for (Integer i = 1; i <= platformCount; i++) {
            subway = new Region(station, "subway-" + i.toString(), SUBWAY_CAPACITY);
            if (i == 1) {
                firstSubway = subway;
            }
            if (platform != null) {
                previousSubway = platform.getAdjacentRegions().getFirst();
                bindRegions(subway, previousSubway);
                bindRegions(subway, platform);
            }
            platform = new Platform(station, "platform-" + i.toString());
            platforms.add(platform);
            bindRegions(platform, subway);
        }
    }

    public void bindWithPlatforms(Region region) {
        bindRegions(firstSubway, region);
    }

    public void bindRegions(Region a, Region b) {
        a.bind(b);
        b.bind(a);
    }
}
