/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import railwaystation.infrastructure.Path;
import railwaystation.infrastructure.Region;

/**
 *
 * @author artur
 */
public class Person extends SimProcess {
    protected Path path;

    public Person(Model owner, String name, Path path) {
        super(owner, name, true);
        this.path = path;
    }

    public Person(Model owner, String name) {
        super(owner, name, true);
        path = null;
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
