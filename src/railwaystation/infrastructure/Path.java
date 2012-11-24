/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import java.util.LinkedList;

/**
 *
 * @author artur
 */
public class Path {
    protected Region currentRegion;
    protected LinkedList<Region> regionsToVisit;

    public Path(Region currentRegion) {
        this.currentRegion = currentRegion;
        regionsToVisit = new LinkedList();
    }

    public void appendRegion(Region nextRegion) {
        regionsToVisit.add(nextRegion);
    }

    public boolean reachedDestination() {
        return regionsToVisit.isEmpty();
    }

    public void goToNextRegion() {
        if (! reachedDestination()) {
            currentRegion = regionsToVisit.removeFirst();
        }
    }

    public void changeDestination(Region destination) {
        // TODO! jakos sprytniej zarzadzac lista regionsToVisit
        regionsToVisit.clear();
        regionsToVisit.add(destination);
    }

    public Region getCurrentRegion() {
        return currentRegion;
    }

    @Override
    public Path clone() {
        Path path = new Path(currentRegion);
        for (Region toVisit : regionsToVisit) {
            path.appendRegion(toVisit);
        }
        return path;
    }

    public static Path findBetween(Region start, Region destination) {
        Path path = new Path(start);
        // TODO! computation of the path between two points
        path.appendRegion(destination);
        return path;
    }
}
