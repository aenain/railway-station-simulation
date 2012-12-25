/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.SimProcess;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Path;
import railwaystation.infrastructure.Region;

/**
 *
 * @author artur
 */
public class Person extends SimProcess {
    public static enum Type { ARRIVING_PASSENGER, DEPARTURING_PASSENGER, TRANSIT_PASSENGER, VISITOR, ARRIVING_COMPANION, DEPARTURING_COMPANION };
    protected Type type;
    protected Path path;
    protected RailwayStation station;

    public Person(RailwayStation station, String name, Path path) {
        super(station, name, true);
        this.station = station;
        this.path = path;
    }

    public Person(RailwayStation station, String name) {
        super(station, name, true);
        this.station = station;
        path = null;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    // przesuwa do następnego regionu i go zwraca
    public Region goToNextRegion() {
        Region region;

        region = path.getCurrentRegion();
        region.personLeaves(this);
        path.goToNextRegion();

        region = path.getCurrentRegion();
        region.personEnters(this); // jeżeli nie może tam wejść, to idź gdzieś indziej...
        
        return region;
    }

    // zmienia cel wędrówki po dworcu
    public void headToRegion(Region destination) {
        path.changeDestination(destination);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void lifeCycle() {
        // wywołania goToNextRegion() i headToRegion()
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
