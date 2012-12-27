/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Path;
import railwaystation.infrastructure.Region;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class Person extends SimProcess {
    public static enum Type {
        ARRIVING_PASSENGER, DEPARTURING_PASSENGER, TRANSIT_PASSENGER, VISITOR, ARRIVING_COMPANION, DEPARTURING_COMPANION
    };
    protected Type type;
    protected Path path;
    protected Train train;
    protected RailwayStation station;
    protected Activity currentActivity;
    protected LinkedList<Activity.Type> futureActivities;
    protected Region currentRegion;

    public Person(RailwayStation station, String name) {
        super(station, name, true);
        this.station = station;
        futureActivities = new LinkedList();
    }

    public Person(RailwayStation station, String name, Train train) {
        super(station, name, true);
        this.station = station;
        this.train = train;
        futureActivities = new LinkedList();
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setCurrentRegion(Region region) {
        currentRegion = region;
    }

    public Type getType() {
        return type;
    }

    public void reachDestination() {
        while (path.hasNextRegion()) {
            if (path.getNextRegion().canPersonEnter()) {
                path.getCurrentRegion().personLeaves(this);
                path.goToNextRegion();
                path.getCurrentRegion().personEnters(this);
                hold(path.getCurrentRegionWalkingTime());
            } else {
                hold(new TimeSpan(1, TimeUnit.MINUTES));
            }
        }
    }

    @Override
    public void lifeCycle() {
        createScenario();
        startActivities();
    }

    public void createScenario() {
        // do nothing at all.
    }

    public void startActivities() {
        while (!futureActivities.isEmpty()) {
            currentActivity = new Activity(this, futureActivities.removeFirst());
            currentActivity.goToDestination();
            currentActivity.start();
        }
    }
}