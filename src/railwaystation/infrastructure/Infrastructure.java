/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import java.util.*;
import java.util.Map.Entry;
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
    protected ArrayList<Subway> subways;
    
    protected static boolean computedPaths = false;
    protected static HashMap<Region, HashMap<Region, Region>> paths = new HashMap<Region, HashMap<Region, Region>>();

    public Infrastructure(RailwayStation station) {
        entryRegion = null;
        firstSubway = null;
        this.station = station;
        platforms = new ArrayList();
        subways = new ArrayList();
    }
    
    public void computePaths(Region start) {
        HashSet<Region> vis = new HashSet<Region>();
        Queue<Region> q = new LinkedList<Region>();
        HashMap<Region, Region> p = new HashMap<Region, Region>();
        
        q.add(start);
        vis.add(start);
        
        while(!q.isEmpty()) {
            Region v = q.remove();
            for(Region adj : v.adjacentRegions) {
                if(!vis.contains(adj)) {
                    vis.add(adj);
                    q.add(adj);
                    p.put(adj, v);
                }
            }
        }
        paths.put(start, p);        
    }
    
    public boolean arePathsComputed() {
        return computedPaths;
    }
    
    public HashMap<Region, Region> getPathsFrom(Region start) {
        return paths.get(start);
    }
    
    public void computeAllPaths() {
        ArrayList<Region> regions = new ArrayList<Region>();
        regions.add(entryRegion);
        regions.add(waitingRoom);
        regions.add(firstSubway);
        regions.add(cashDeskRegion);
        regions.add(informationDeskRegion);
        regions.addAll(platforms);
        regions.addAll(subways);
        
        for(Region region : regions) {
            computePaths(region);
        }
        computedPaths = true;
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

    public ArrayList<Platform> getPlatforms() {
        return platforms;
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
        Subway subway, previousSubway;
        Platform platform = null;

        for (Integer i = 1; i <= platformCount; i++) {
            subway = new Subway(station, i);
            subways.add(subway);

            if (i == 1) {
                firstSubway = subway;
            }
            if (platform != null) {
                previousSubway = (Subway)platform.getAdjacentRegions().getFirst();
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
        return platforms.get(i - 1); // indices start from 0
    }

    // 1..platforms.count
    public Subway getSubway(int i) {
        if (i == 0) { i = 1; }
        return subways.get(i - 1); // indices start from 0
    }

    public void bindWithPlatforms(Region region) {
        bindRegions(firstSubway, region);
    }

    public void bindRegions(Region a, Region b) {
        a.bind(b);
        b.bind(a);
    }

    public void activateDesks() {
        for (Desk desk : cashDeskRegion.desks) {
            desk.activate();
        }
        for (Desk desk : informationDeskRegion.desks) {
            desk.activate();
        }
    }

    public void flushRegionEvents() {
        for (Platform platform : platforms) {
            platform.stackPeopleChange();
        }
        for (Region subway : subways) {
            subway.stackPeopleChange();
        }
        if (entryRegion != null) { entryRegion.stackPeopleChange(); }
        if (cashDeskRegion != null) { cashDeskRegion.stackPeopleChange(); }
        if (informationDeskRegion != null) { informationDeskRegion.stackPeopleChange(); }
        if (waitingRoom != null) { waitingRoom.stackPeopleChange(); }
    }
}
