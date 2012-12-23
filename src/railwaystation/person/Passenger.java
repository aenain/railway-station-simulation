/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.Model;
import java.util.LinkedList;
import railwaystation.infrastructure.Path;
import railwaystation.infrastructure.Region;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class Passenger extends Person {
    protected LinkedList<Companion> companions;
    protected Train train;

    public Passenger(Model owner, String name, Train train) {
        super(owner, name);
        companions = new LinkedList();
        this.train = train;
    }

    public void addCompanion(Companion companion) {
        companions.add(companion);
    }

    @Override
    public void setPath(Path path) {
        super.setPath(path);
        for (Companion companion : companions) {
            companion.setPath(path.clone());
        }
    }

    @Override
    public void headToRegion(Region destination) {
        super.headToRegion(destination);
        for (Companion companion : companions) {
            companion.headToRegion(destination);
        }
    }

    @Override
    public Region goToNextRegion() {
        Region nextRegion = super.goToNextRegion();
        for (Companion companion : companions) {
            companion.goToNextRegion();
        }
        return nextRegion;
    }
}
