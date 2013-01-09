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
    protected boolean hasPurposeToGoToPlatform = true;

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
            switch (currentActivity.state) {
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

    @Override
    public void reachDestination() {
        while (path.hasNextRegion()) {
            currentRegion = path.getCurrentRegion();

            if (shouldGoToPlatform()) {
                cancelCurrentActivityAndWaitOnPlatform();
            }

            if (! path.isCancelled()) {
                if (path.getNextRegion().canPersonEnter()) {
                    currentRegion.personLeaves(this);
                    path.goToNextRegion();
                    currentRegion = path.getCurrentRegion();
                    currentRegion.personEnters(this);
                    hold(path.getCurrentRegionWalkingTime());
                } else {
                    // jesli nie moze czekac w poczekalni, to niech czeka w hallu
                    futureActivities.addFirst(Activity.Type.WAIT_IN_HALL);
                    currentActivity.cancel();
                }
            }
        }
    }

    protected void cancelCurrentActivityAndWaitOnPlatform() {
        if (getCurrentActivityType() != Activity.Type.WAIT_ON_PLATFORM) {
            if (futureActivities.contains(Activity.Type.WAIT_ON_PLATFORM)) {
                while (!futureActivities.isEmpty() && futureActivities.getFirst() != Activity.Type.WAIT_ON_PLATFORM) {
                    futureActivities.removeFirst();
                }
            } else {
                futureActivities.addFirst(Activity.Type.WAIT_ON_PLATFORM);
            }
            currentActivity.cancel();
        }
    }

    protected boolean canGoToWaitingRoom() {
        boolean nextActivityIsWaitingOnPlatform = !futureActivities.isEmpty() && futureActivities.getFirst() == Activity.Type.WAIT_ON_PLATFORM;
        return nextActivityIsWaitingOnPlatform && hasTimeToGoToWaitingRoom();
    }

    protected boolean hasTimeToGoToWaitingRoom() {
        TimeInstant canGoUntil = canGoToWaitingRoomUntil();

        if (canGoUntil != null) {
            return presentTime().compareTo(canGoUntil) < 1;
        } else {
            return false;
        }
    }

    public TimeInstant canGoToWaitingRoomUntil() {
        TimeInstant eventAt = getTrainEventAt();

        if (eventAt != null) {
            return TimeOperations.subtract(eventAt, station.config.getMinTimeToGoToWaitingRoom());
        } else {
            return null;
        }
    }

    protected boolean shouldGoToPlatform() {
        TimeInstant shouldGoAt = shouldGoToPlatformAt();

        if (shouldGoAt != null) {
            return hasPurposeToGoToPlatform && presentTime().compareTo(shouldGoAt) >= 0;
        } else {
            return false;
        }
    }

    public TimeInstant shouldGoToPlatformAt() {
        TimeInstant eventAt = getTrainEventAt();

        if (eventAt != null) {
            return TimeOperations.subtract(eventAt, station.config.getMaxTimeToGoToPlatform());
        } else {
            return null;
        }
    }

    // zwraca godzine interesujacego nas eventu: przyjazdu lub odjazdu pociagu
    public TimeInstant getTrainEventAt() {
        if (train == null) {
            return null;
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

        return TimeOperations.add(referenceTime, delay);
    }

    // gdy juz nie jest zwiazany z pociagiem bedzie czekal az zostanie obsluzony
    @Override
    public void waitInQueue() {
        TimeInstant until;

        while (waiting && !currentActivity.isCancelled() && !shouldGoToPlatform()) {
            until = shouldGoToPlatformAt();
            if (until != null) {
                hold(until);
            } else {
                passivate();
            }
        }
        // nie zostal jeszcze obsluzony
        if (waiting) {
            currentDesk.removePerson(this);
        }
    }

    // gdy juz nie jest zwiazany z pociagiem bedzie czekal w nieskonczonosc
    public void waitInMainBuilding() {
        TimeInstant until;

        while (!shouldGoToPlatform()) {
            until = shouldGoToPlatformAt();
            if (until != null) {
                hold(until);
            } else {
                passivate();
            }
        }
    }

    public void missTrain() {
        if (train != null) {
            train = null;
        }

        hasPurposeToGoToPlatform = false;

        futureActivities.clear();
        if (station.dist.complains()) {
            futureActivities.add(Activity.Type.COMPLAIN);
        }

        futureActivities.add(Activity.Type.LEAVE_STATION);
        if (this instanceof Passenger) {
            station.getSummary().addPassengersMissedTrain(1);
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

    @Override
    public void startActivities() {
        while (!futureActivities.isEmpty()) {
            if (canGoToWaitingRoom()) {
                futureActivities.addFirst(Activity.Type.WAIT_IN_WAITING_ROOM);
            }
            currentActivity = new Activity(this, futureActivities.removeFirst());
            currentActivity.goToDestination();
            currentActivity.start();
        }
    }
}
