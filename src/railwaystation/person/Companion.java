/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import railwaystation.RailwayStation;

/**
 *
 * @author artur
 */
public class Companion extends Person {
    protected Passenger passenger;
    protected static final int MAX_ACTIVITIES = 5;

    public Companion(RailwayStation station, String name) {
        super(station, name);
        passenger = null;
    }

    public Companion(RailwayStation station, String name, Passenger passenger) {
        super(station, name, passenger.train);
        this.passenger = passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public boolean isFollowing() {
        return (currentActivity != null && currentActivity.type == Activity.Type.FOLLOW_PASSENGER);
    }

    public void missTrain() {
        train.removeNotifyListener(this);

        if (type == Person.Type.ARRIVING_COMPANION) {
            futureActivities.clear();
            futureActivities.add(Activity.Type.LEAVE_STATION);

            if (currentActivity != null) {
                currentActivity.cancel();
            }

            cancel();
            activate();
        }
    }
    
    public TimeInstant shouldGoToPlatformAt() {
        // TODO! a delay'e?
        TimeSpan delay = (trainDelay == null ? new TimeSpan(0) : trainDelay);
        return TimeOperations.subtract(TimeOperations.add(train.getArrivalAt(), delay), station.config.getMaxTimeToGoToPlatform());
    }
    
    boolean shouldGoToPlatform() {
        return presentTime().compareTo(shouldGoToPlatformAt()) >= 0;
    }
    
     void checkGoToPlatform() {
        if(type == Person.Type.ARRIVING_COMPANION && shouldGoToPlatform() ) {
            currentActivity.cancel();
            futureActivities.clear();
            futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
            futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
            futureActivities.add(Activity.Type.LEAVE_STATION);
        }
    }
    
    void checkGoOutFromPlatform() {
        if(type == Person.Type.ARRIVING_COMPANION && !shouldGoToPlatform() ) {
            currentActivity.cancel();
            futureActivities.clear();
            generateActivities();
        }
    }
    
    public void generateActivities() {
        int activitiesCount = Generator.rand(0, MAX_ACTIVITIES);
        Activity lastActivity = new Activity(this, null);
        for(int i = 0; i < activitiesCount; i++) {
            int nextActivityNo = Generator.rand(1, 4);
            switch(nextActivityNo) {
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
            case DEPARTURING_COMPANION:
                futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
                futureActivities.add(Activity.Type.LEAVE_STATION);
                break;
            case ARRIVING_COMPANION:
                futureActivities.add(Activity.Type.ENTER_STATION);
                if(shouldGoToPlatform()) {
                    futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
                    futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
                    futureActivities.add(Activity.Type.LEAVE_STATION);
                }
                else {
                    generateActivities();
                }
                break;
        }
    }
}