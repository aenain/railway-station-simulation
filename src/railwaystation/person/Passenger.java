/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.Model;
import java.util.LinkedList;
import railwaystation.infrastructure.Path;
import railwaystation.infrastructure.Region;

/**
 *
 * @author artur
 */
public class Passenger extends Person {

    protected LinkedList<Companion> companions;

    public Passenger(Model owner, String name) {
        super(owner, name);
        companions = new LinkedList();
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
