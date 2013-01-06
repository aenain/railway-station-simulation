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
public class Companion extends TrainOrientedPerson {
    protected Passenger passenger;

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

    @Override
    public void createScenario() {
        switch (type) {
            case DEPARTURING_COMPANION:
                futureActivities.add(Activity.Type.ENTER_STATION);
                futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
                /* pasazer moze mu wygenerowac aktywnosci w aktywnosci ENTER_TRAIN
                 * tu lepiej nie generowac, bo sie moze okazac, ze pasazerowi pociag uciekl
                 * i wtedy razem opuszcza dworzec
                 */
                futureActivities.add(Activity.Type.LEAVE_STATION);
                break;
            case ARRIVING_COMPANION:
                futureActivities.add(Activity.Type.ENTER_STATION);
                if (station.dist.needsInformation()) {
                    futureActivities.add(Activity.Type.GET_INFO);
                }
                futureActivities.add(Activity.Type.WAIT_ON_PLATFORM);
                futureActivities.add(Activity.Type.FOLLOW_PASSENGER);
                futureActivities.add(Activity.Type.LEAVE_STATION);
                break;
        }
    }
}