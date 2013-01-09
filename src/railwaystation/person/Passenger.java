/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import java.util.LinkedList;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class Passenger extends TrainOrientedPerson {
    protected LinkedList<Companion> companions;
    protected boolean hasTicket;
    protected boolean leadCompanions;
    
    protected static final int MAX_ACTIVITIES = 5;

    public Passenger(RailwayStation station, String name, Train train) {
        super(station, name, train);
        companions = new LinkedList();
        hasTicket = false;
        leadCompanions = false;
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

    @Override
    public void reachDestination() {
        LinkedList<Companion> followers;
        while (path.hasNextRegion()) {
            currentRegion = path.getCurrentRegion();

            if (shouldGoToPlatform()) {
                cancelCurrentActivityAndWaitOnPlatform();
            }

            if (! path.isCancelled()) {
                followers = getFollowers();

                if (path.getNextRegion().canPeopleEnter(followers.size() + 1)) {
                    currentRegion.personLeaves(this);
                    currentRegion.companionsLeave(followers);
                    path.goToNextRegion();
                    currentRegion = path.getCurrentRegion();
                    currentRegion.personEnters(this);
                    currentRegion.companionsEnter(followers);
                    hold(path.getCurrentRegionWalkingTime());
                } else {
                    // jesli nie moze czekac w poczekalni, to niech czeka w hallu
                    futureActivities.addFirst(Activity.Type.WAIT_IN_HALL);
                    currentActivity.cancel();
                }
            }
        }
    }

    public LinkedList<Companion> getFollowers() {
        LinkedList<Companion> followers = new LinkedList();
        if (leadCompanions) {
            for (Companion companion : companions) {
                if (companion.isFollowing()) {
                    followers.add(companion);
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
                if (station.dist.complains()) {
                    futureActivities.add(Activity.Type.COMPLAIN);
                }
                futureActivities.add(Activity.Type.LEAVE_STATION);
                futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
                break;
                
            case DEPARTURING_PASSENGER:
                futureActivities.add(Activity.Type.ENTER_STATION);
                futureActivities.add(Activity.Type.BIND_COMPANIONS);
                if (! hasTicket) {
                    futureActivities.add(Activity.Type.BUY_TICKET);
                }
                if (station.dist.needsInformation()) {
                    futureActivities.add(Activity.Type.GET_INFO);
                }
                futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
                futureActivities.add(Activity.Type.ENTER_TRAIN);
                futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
                break;
        }
    }
}