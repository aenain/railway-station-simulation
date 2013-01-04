/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
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

    public TimeInstant shouldGoToPlatformAt() {
        // TODO! a delay'e?
        TimeSpan delay = (trainDelay == null ? new TimeSpan(0) : trainDelay);
        return TimeOperations.subtract(TimeOperations.add(train.getDepartureAt(), delay), station.config.getMaxTimeToGoToPlatform());
    }
    
    boolean shouldGoToPlatform() {
        return presentTime().compareTo(shouldGoToPlatformAt()) >= 0;
    }

    @Override
    public void waitInQueue() {
        while (waiting && !currentActivity.isCancelled() && presentTime().compareTo(shouldGoToPlatformAt()) < 0) {
            hold(shouldGoToPlatformAt());
            
            // tu moze sprawdzac komunikaty czy cos
            checkGoToPlatform();
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
            }
        }
    }

    public void missTrain() {
        futureActivities.clear();
        futureActivities.add(Activity.Type.LEAVE_STATION);
        futureActivities.add(Activity.Type.UNBIND_COMPANIONS);

        if (currentActivity != null) {
            currentActivity.cancel();
        }

        cancel();
        activate();
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
    
    public void generateActivities() {
        int activitiesCount = Generator.rand(0, MAX_ACTIVITIES);
        Activity lastActivity = new Activity(this, null);
        for(int i = 0; i < activitiesCount; i++) {
            int nextActivityNo = Generator.rand(0, 4);
            switch(nextActivityNo) {
                case 0:
                    futureActivities.add(Activity.Type.BUY_TICKET);
                    lastActivity.setType(Activity.Type.BUY_TICKET);
                    break;
                case 1:
                    futureActivities.add(Activity.Type.COMPLAIN);
                    lastActivity.setType(Activity.Type.COMPLAIN);
                    break; 
                case 2:
                    futureActivities.add(Activity.Type.GET_INFO);
                    lastActivity.setType(Activity.Type.GET_INFO);
                    break; 
                case 3:
                    if(lastActivity.getType() != Activity.Type.WAIT_IN_HALL){
                        futureActivities.add(Activity.Type.WAIT_IN_HALL);
                        lastActivity.setType(Activity.Type.WAIT_IN_HALL);
                    }
                    break; 
                case 4:
                    if(lastActivity.getType() != Activity.Type.WAIT_IN_WAITING_ROOM){
                        futureActivities.add(Activity.Type.WAIT_IN_WAITING_ROOM);
                        lastActivity.setType(Activity.Type.WAIT_IN_HALL);
                    }
                    break; 
            }
        }
    }

    @Override
    public void createScenario() {
        switch(type) {
            case ARRIVING_PASSENGER:
                futureActivities.add(Activity.Type.LEAVE_TRAIN);
                futureActivities.add(Activity.Type.BIND_COMPANIONS);
                generateActivities();
                futureActivities.add(Activity.Type.LEAVE_STATION);
                futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
                break;
                
            case DEPARTURING_PASSENGER:
                futureActivities.add(Activity.Type.ENTER_STATION);
                futureActivities.add(Activity.Type.BIND_COMPANIONS);
                
                if(shouldGoToPlatform()) {
                    futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
                    futureActivities.add(Activity.Type.ENTER_TRAIN);
                    futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
                }
                else {
                    generateActivities();
                }
                break;
                
                
        }
    }
    
    void checkGoToPlatform() {
        if(type == Person.Type.DEPARTURING_PASSENGER && shouldGoToPlatform() ) {
            currentActivity.cancel();
            futureActivities.clear();
            futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
            futureActivities.add(Activity.Type.ENTER_TRAIN);
            futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
        }
    }
    
    void checkGoOutFromPlatform() {
        if(type == Person.Type.DEPARTURING_PASSENGER && !shouldGoToPlatform() ) {
            currentActivity.cancel();
            futureActivities.clear();
            generateActivities();
        }
    }
}