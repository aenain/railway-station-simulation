/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Platform;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 * reprezentacja czlowieka zainteresowanego konkretnym pociagiem (agregacja klas Passenger i Companion)
 */
public class TrainOrientedPerson extends Person {
    public static TimeInstant END_OF_THE_DAY = new TimeInstant(24, TimeUnit.HOURS);
    protected Train train;
    protected TimeSpan trainDelay;
    protected Platform trainRealPlatform;

    public TrainOrientedPerson(RailwayStation station, String name, Train train) {
        super(station, name);
        this.train = train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public Train getTrain() {
        return train;
    }

    public TimeSpan getTrainDelay() {
        return trainDelay;
    }

    public void setTrainDelay(TimeSpan trainDelay) {
        this.trainDelay = trainDelay;
        if (currentActivity != null && currentActivity.type == Activity.Type.WAIT_ON_PLATFORM) {
            // tu coś może zrobić!
        }
    }

    public Platform getTrainRealPlatform() {
        return trainRealPlatform;
    }

    // jestesmy pewni, ze zmiana nastapila
    public void setTrainRealPlatform(Platform trainRealPlatform) {
        this.trainRealPlatform = trainRealPlatform;
        if (currentActivity != null && currentActivity.type == Activity.Type.WAIT_ON_PLATFORM) {
            switch(currentActivity.state) {
                case WALKING:
                    path.changeDestination(trainRealPlatform);
                    break;
                case DOING:
                    futureActivities.addFirst(Activity.Type.WAIT_ON_PLATFORM);
                    activate();
                    break;
            }
        }
    }

    public TimeInstant shouldGoToPlatformAt() {
        if (train == null) {
            return END_OF_THE_DAY;
        }
        TimeSpan delay = (trainDelay == null ? new TimeSpan(0) : trainDelay);
        TimeInstant referenceTime;

        switch (type) {
            case ARRIVING_COMPANION:
                referenceTime = train.getArrivalAt();
                break;
            case DEPARTURING_COMPANION:
            case DEPARTURING_PASSENGER:
            default:
                referenceTime = train.getDepartureAt();
                break;
        }

        return TimeOperations.subtract(TimeOperations.add(referenceTime, delay), station.config.getMaxTimeToGoToPlatform());
    }
    
    boolean shouldGoToPlatform() {
        if (train == null) { return false; }
        return presentTime().compareTo(shouldGoToPlatformAt()) >= 0;
    }

    // gdy juz nie jest zwiazany z pociagiem bedzie czekal az zostanie obsluzony
    @Override
    public void waitInQueue() {
        while (waiting && !currentActivity.isCancelled() && presentTime().compareTo(shouldGoToPlatformAt()) < 0) {
            hold(shouldGoToPlatformAt());
            // tu moze sprawdzac komunikaty czy cos
        }
        // nie zostal jeszcze obsluzony
        if (waiting) {
            currentDesk.removePerson(this);
        }
    }

    // gdy juz nie jest zwiazany z pociagiem bedzie czekal w nieskonczonosc
    public void waitInWaitingRoom() {
        while (!shouldGoToPlatform()) {
            hold(shouldGoToPlatformAt());
            // tu moze sprawdzac komunikaty czy cos
        }
    }

    // gdy juz nie jest zwiazany z pociagiem bedzie czekal w nieskonczonosc
    public void waitInHall() {
        while (!shouldGoToPlatform()) {
            hold(shouldGoToPlatformAt());
            // tu moze sprawdzac komunikaty czy cos
        }
    }

    public void missTrain() {
        if (train != null) {
            train = null;
        }

        futureActivities.clear();
        if (station.dist.complains()) {
            futureActivities.add(Activity.Type.COMPLAIN);
        }

        futureActivities.add(Activity.Type.LEAVE_STATION);
        if (this instanceof Passenger) {
            futureActivities.add(Activity.Type.UNBIND_COMPANIONS);
        }

        if (currentActivity != null) {
            currentActivity.cancel();
        }

        cancel();
        activate();
    }

    @Override
    public void lifeCycle() {
        // checks on the magic board all informations about the train
        trainDelay = train.getDelay();
        trainRealPlatform = train.getRealPlatform();

        super.lifeCycle();
    }
}
