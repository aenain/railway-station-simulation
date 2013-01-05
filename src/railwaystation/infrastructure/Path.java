/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.TimeSpan;
import java.util.*;

/**
 *
 * @author artur
 * UWAGA! 
 * klasa tak naprawdę nie wyszukuje sciezki!
 * topologia polaczen zostala wprowadzona na sztywno!!
 */
public class Path {
    protected Region currentRegion;
    protected LinkedList<Region> regionsToVisit;
    protected boolean cancelled;

    public Path(Region currentRegion) {
        this.currentRegion = currentRegion;
        cancelled = false;
        regionsToVisit = new LinkedList();
    }

    public void appendRegion(Region nextRegion) {
        regionsToVisit.add(nextRegion);
    }

    public boolean hasNextRegion() {
        return !(cancelled || regionsToVisit.isEmpty());
    }

    public void goToNextRegion() {
        currentRegion = regionsToVisit.removeFirst();
    }

    public void changeDestination(Region destination) {
        regionsToVisit = regionsBetween(currentRegion, destination);
        regionsToVisit.add(destination);
    }

    public void dump() {
        if (! regionsToVisit.isEmpty()) { 
            System.out.println(currentRegion + " to " + regionsToVisit.getLast());
            System.out.print(currentRegion);
            ListIterator<Region> it = regionsToVisit.listIterator();
            while (it.hasNext()) {
                System.out.print("," + it.next());
            }
            System.out.println();
        } else {
            System.out.println("path in " + currentRegion);
        }
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Region getCurrentRegion() {
        return currentRegion;
    }

    public Region getNextRegion() {
        return regionsToVisit.getFirst();
    }

    public TimeSpan getCurrentRegionWalkingTime() {
        return currentRegion.getWalkingTime();
    }

    public static Path findBetween(Region start, Region destination) {
        Infrastructure structure = start.station.structure;
        Path path = new Path(start);
        if (!start.equals(destination)) {
            path.changeDestination(destination);
        }
        return path;
    }

    protected static LinkedList<Region> regionsBetween(Region start, Region destination) {
        Infrastructure structure = start.station.structure;
        LinkedList<Region> regions = new LinkedList();
        
        if(!structure.arePathsComputed()){
            structure.computeAllPaths();
        }
        
        Region step = destination;
        HashMap<Region, Region> p = structure.getPathsFrom(start);
        while(true) {
            step = p.get(step);
            if(step == start) {
                break;
            }
            regions.addFirst(step);
        }

        return regions;
    }

    // tylko dla tuneli i peronów zwróci niepustą tablicę, pozostałe regiony są połączone z hallem
    protected static LinkedList<Region> regionsBetweenHallAndRegion(Region b) {
        Infrastructure structure = b.station.structure;
        Region hall = structure.entryRegion;
        LinkedList<Region> regions = new LinkedList();

        if (!hall.equals(b) && !hall.adjacentRegions.contains(b)) {
            if (b instanceof Platform) {
                regions = regionsBetweenSubwayAndPlatform((Subway)structure.getSubway(1), (Platform)b);
            } else {
                regions = regionsBetweenSubways((Subway)structure.getSubway(1), (Subway)b);
            }
            regions.addFirst(structure.getSubway(1));
        }
        return regions;
    }

    // tylko dla tuneli i peronów zwróci niepustą tablicę, pozostałe regiony są połączone z hallem
    protected static LinkedList<Region> regionsBetweenRegionAndHall(Region a) {
        Infrastructure structure = a.station.structure;
        Region hall = structure.entryRegion;
        LinkedList<Region> regions = new LinkedList();

        if (!hall.equals(a) && !hall.adjacentRegions.contains(a)) {
            if (a instanceof Platform) {
                regions = regionsBetweenPlatformAndSubway((Platform)a, (Subway)structure.getSubway(1));
            } else {
                regions = regionsBetweenSubways((Subway)a, (Subway)structure.getSubway(1));
            }
            regions.add(structure.getSubway(1));
        }
        return regions;
    }

    protected static LinkedList<Region> regionsBetweenSubways(Subway a, Subway b) {
        Infrastructure structure = a.station.structure;
        LinkedList<Region> regions = new LinkedList();
        if (a.number < b.number) {
            for (int i = a.number + 1; i < b.number; i++) {
                regions.add(structure.getSubway(i));
            }
        } else {
            for (int i = a.number - 1; i > b.number; i--) {
                regions.add(structure.getSubway(i));
            }
        }
        return regions;
    }

    protected static LinkedList<Region> regionsBetweenPlatforms(Platform a, Platform b) {
        Infrastructure structure = a.station.structure;
        LinkedList<Region> regions = new LinkedList();
        if (a.number < b.number) {
            for (int i = a.number + 1; i <= b.number; i++) {
                regions.add(structure.getSubway(i));
            }
        } else {
            for (int i = a.number; i > b.number; i--) {
                regions.add(structure.getSubway(i));
            }
        }
        return regions;
    }

    protected static LinkedList<Region> regionsBetweenSubwayAndPlatform(Subway a, Platform b) {
        Infrastructure structure = a.station.structure;
        LinkedList<Region> regions = new LinkedList();
        if (a.number < b.number) {
            for (int i = a.number + 1; i <= b.number; i++) {
                regions.add(structure.getSubway(i));
            }
        } else {
            for (int i = a.number - 1; i > b.number; i--) {
                regions.add(structure.getSubway(i));
            }
        }
        return regions;
    }

    protected static LinkedList<Region> regionsBetweenPlatformAndSubway(Platform a, Subway b) {
        Infrastructure structure = a.station.structure;
        LinkedList<Region> regions = new LinkedList();
        if (a.number < b.number) {
            for (int i = a.number + 1; i < b.number; i++) {
                regions.add(structure.getSubway(i));
            }
        } else {
            for (int i = a.number; i > b.number; i--) {
                regions.add(structure.getSubway(i));
            }
        }
        return regions;
    }
}