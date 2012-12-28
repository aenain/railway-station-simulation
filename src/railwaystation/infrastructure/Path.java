/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.TimeSpan;
import java.util.LinkedList;

/**
 *
 * @author artur
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

    // TODO! computation of the path between two points
    public static Path findBetween(Region start, Region destination) {
        Infrastructure structure = start.station.structure;
        Path path = new Path(start);
        Region newStart, newDestination;

        if ((start instanceof ServingRegion) || (start instanceof CashDeskRegion)) {
            newStart = structure.getEntryRegion();
            path.appendRegion(newStart);
        } else {
            newStart = start;
        }

        if ((destination instanceof ServingRegion) || (destination instanceof CashDeskRegion)) {
            newDestination = structure.getEntryRegion();
        } else {
            newDestination = destination;
        }

        // idzie z peronu do pomieszczenia w głównym budynku
        if ((newStart instanceof Platform) && !(newDestination instanceof Platform)) {
            Platform platform = (Platform)newStart;
            for (int i = platform.number; i > 0; i--) {
                path.appendRegion(structure.getSubway(i));
            }
        }

        // idzie z głównego budynku na peron
        if (!(newStart instanceof Platform) && (newDestination instanceof Platform)) {
            Platform platform = (Platform)newDestination;
            for (int i = 1; i <= platform.number; i++) {
                path.appendRegion(structure.getSubway(i));
            }
        }

        if (! newStart.equals(newDestination)) {
            path.appendRegion(newDestination);
        }

        if (! newDestination.equals(destination)) {
            path.appendRegion(destination);
        }

        return path;
    }
}
