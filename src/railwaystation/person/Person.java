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
import railwaystation.infrastructure.*;

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
    protected RailwayStation station;
    protected Activity currentActivity;
    protected LinkedList<Activity.Type> futureActivities;
    protected Region currentRegion;
    protected Desk currentDesk;
    protected boolean waiting = false;

    public Person(RailwayStation station, String name) {
        super(station, name, true);
        this.station = station;
        futureActivities = new LinkedList();
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setCurrentRegion(Region region) {
        currentRegion = region;
    }

    public Region getCurrentRegion(Region region) {
        return currentRegion;
    }

    public void setCurrentDesk(Desk desk) {
        currentDesk = desk;
    }

    public Type getType() {
        return type;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public Activity.Type getCurrentActivityType() {
        if (currentActivity == null) {
            return null;
        } else {
            return currentActivity.type;
        }
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void reachDestination() {
        while (path.hasNextRegion()) {
            if (path.getNextRegion().canPersonEnter()) {
                path.getCurrentRegion().personLeaves(this);
                path.goToNextRegion();
                path.getCurrentRegion().personEnters(this);
                currentRegion = path.getCurrentRegion();
                hold(path.getCurrentRegionWalkingTime());
            } else {
                hold(new TimeSpan(15, TimeUnit.SECONDS));
            }
        }
    }

    public void waitInQueue() {
        if (waiting && !currentActivity.isCancelled()) {
            // czeka az zostanie obsluzony
            passivate();
        }
    }

    @Override
    public void lifeCycle() {
        createScenario();
        startActivities();
        station.logger.changeThreads(type, -1);
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