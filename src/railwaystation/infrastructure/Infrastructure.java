/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import java.util.ArrayList;
import railwaystation.RailwayStation;

/**
 *
 * @author artur
 */
public class Infrastructure {
    public static final Integer MAX_CAPACITY = Integer.MAX_VALUE;

    protected Region entryRegion, waitingRoom, firstSubway;
    protected RailwayStation station;
    protected CashDeskRegion cashDeskRegion;
    protected ServingRegion informationDeskRegion;
    protected ArrayList<Platform> platforms;

    public Infrastructure(RailwayStation station) {
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
            subway = new Region(station, "tunnel-" + i.toString(), MAX_CAPACITY);
            if (i == 1) {
                firstSubway = subway;
            }
            if (platform != null) {
                previousSubway = platform.getAdjacentRegions().getFirst();
                bindRegions(subway, previousSubway);
                bindRegions(subway, platform);
            }
            platform = new Platform(station, i);
            platform.buildTracks();
            platforms.add(platform);
            bindRegions(platform, subway);
        }
    }

    // 1..platforms.count
    public Platform getPlatform(int i) {
        if (i == 0) { i = 1; }
        return platforms.get(i - 1); // indices starts from 0
    }

    public void bindWithPlatforms(Region region) {
        bindRegions(firstSubway, region);
    }

    public void bindRegions(Region a, Region b) {
        a.bind(b);
        b.bind(a);
    }
}
