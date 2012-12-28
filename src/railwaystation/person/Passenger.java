/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import java.util.LinkedList;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class Passenger extends Person {
    protected LinkedList<Companion> companions;
    protected boolean hasTicket;
    protected boolean leadCompanions;

    public Passenger(RailwayStation station, String name, Train train) {
        super(station, name, train);
        companions = new LinkedList();
        hasTicket = false;
        leadCompanions = false;
        train.addNotifyListener(this);
    }

    public void setTicketPossession(boolean hasTicket) {
        this.hasTicket = hasTicket;
    }

    public boolean getTicketPossession() {
        return hasTicket;
    }

    public void addCompanion(Companion companion) {
        companions.add(companion);
    }

    public TimeInstant shouldGoToPlatformAt() {
        // TODO! a delay'e?
        return TimeOperations.subtract(train.getDepartureAt(), station.config.getMaxTimeToGoToPlatform());
    }

    @Override
    public void waitInQueue() {
        while (waiting && presentTime().compareTo(shouldGoToPlatformAt()) < 0) {
            hold(shouldGoToPlatformAt());
            // tu moze sprawdzac komunikaty czy cos
        }
        // nie zostal jeszcze obsluzony
        if (waiting) {
            currentDesk.removePerson(this);
        }
    }
//
//    @Override
//    public void waitInWaitingRoom() {
//        while (presentTime().compareTo(shouldGoToPlatformAt()) < 0) {
//            hold(shouldGoToPlatformAt());
//            // tu moze sprawdzac komunikaty czy cos
//        }
//    }

    @Override
    public void reachDestination() {
        LinkedList<Person> followers;
        while (path.hasNextRegion()) {
            followers = getFollowers();
            if (path.getNextRegion().canPeopleEnter(followers.size() + 1)) {
                currentRegion = path.getCurrentRegion();
                currentRegion.personLeaves(this);
                currentRegion.peopleLeave(followers);
                path.goToNextRegion();
                currentRegion = path.getCurrentRegion();
                currentRegion.personEnters(this);
                currentRegion.peopleEnter(followers);
                hold(path.getCurrentRegionWalkingTime());
            } else {
                // jesli nie moze czekac w poczekalni, to niech czeka w hallu
                futureActivities.addFirst(Activity.Type.WAIT_IN_HALL);
                currentActivity.cancel();
                path.cancel();
            }
        }
    }

    public LinkedList<Person> getFollowers() {
        LinkedList<Person> followers = new LinkedList();
        if (leadCompanions) {
            for (Companion companion : companions) {
                if (companion.isFollowing()) {
                    followers.add((Person)companion);
                }
            }
        }
        return followers;
    }

    @Override
    public void createScenario() {
        switch(type) {
            case ARRIVING_PASSENGER:
                futureActivities.add(Activity.Type.LEAVE_TRAIN);
                futureActivities.add(Activity.Type.BIND_COMPANIONS);
                futureActivities.add(Activity.Type.LEAVE_STATION);
                futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
                break;
            case DEPARTURING_PASSENGER:
                futureActivities.add(Activity.Type.ENTER_STATION);
                futureActivities.add(Activity.Type.BIND_COMPANIONS);
                futureActivities.add(Activity.Type.BUY_TICKET);
                futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
                futureActivities.add(Activity.Type.ENTER_TRAIN);
                futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
                break;
        }
    }
}