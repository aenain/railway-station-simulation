/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import railwaystation.RailwayStation;

/**
 *
 * @author artur
 */
public class Companion extends Person {
    protected Passenger passenger;

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

    @Override
    public void createScenario() {
        switch(type) {
            case DEPARTURING_COMPANION:
                futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
                futureActivities.add(Activity.Type.LEAVE_STATION);
                break;
            case ARRIVING_COMPANION:
                futureActivities.add(Activity.Type.ENTER_STATION);
                futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
                futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
                futureActivities.add(Activity.Type.LEAVE_STATION);
                break;
        }
    }
}