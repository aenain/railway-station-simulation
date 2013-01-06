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

    @Override
    public String toString() {
        return "Path: " + currentRegion + " " + regionsToVisit;
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
            if(step.equals(start)) {
                break;
            }
            regions.addFirst(step);
        }

        return regions;
    }
}
