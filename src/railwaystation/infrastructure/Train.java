/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.RailwayStation;
import railwaystation.TimeTable;
import railwaystation.person.Passenger;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class Train extends Region {
    public static enum Type { ARRIVAL, DEPARTURE, TRANSIT };
    protected ProcessQueue<Passenger> passengers;
    protected Integer otherPassengerCount;
    protected Platform platform, realPlatform;
    protected Track track, realTrack;
    protected TimeInstant arrivalAt, departureAt; // scheduled
    protected TimeInstant realSemaphoreArrivalAt;
    protected TimeSpan externalDelay = TimeSpan.ZERO, semaphoreDelay = TimeSpan.ZERO, totalDelay = TimeSpan.ZERO, internalArrival;
    protected String source, destination;
    protected Type type;
    protected LinkedList<Person> listeners;

    public Train(RailwayStation owner, String name) {
        super(owner, name, Infrastructure.MAX_CAPACITY);
        otherPassengerCount = 100;
        passengers = new ProcessQueue(owner, name + "-passengers", true, true);
        listeners = new LinkedList();
    }

    public void addNotifyListener(Person person) {
        listeners.add(person);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setArrivalAt(TimeInstant arrivalAt) {
        this.arrivalAt = arrivalAt;
    }

    public TimeInstant getArrivalAt() {
        return arrivalAt;
    }

    public void setDepartureAt(TimeInstant departureAt) {
        this.departureAt = departureAt;
    }

    public TimeInstant getDepartureAt() {
        return departureAt;
    }

    public void setInternalArrivalDuration(TimeSpan internalArrival) {
        this.internalArrival = internalArrival;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setTrack(Track track) {
        this.track = track;
        this.platform = track.platform;
    }

    public Track getTrack() {
        return track;
    }

    public void addPassenger(Passenger passenger) {
        passengers.insert(passenger);
    }

    public int getMinDeparturingCount() {
        if (type == Train.Type.ARRIVAL) { return 0; }
        return station.config.getMinDeparturingPassengerCount();
    }

    public int getMaxDeparturingCount() {
        if (type == Train.Type.ARRIVAL) { return 0; }
        return station.config.getMaxDeparturingPassengerCount();
    }

    public int getMinArrivingCount() {
        if (type == Train.Type.DEPARTURE) { return 0; }
        return station.config.getMinArrivingPassengerCount();
    }

    public int getMaxArrivingCount() {
        if (type == Train.Type.DEPARTURE) { return 0; }
        return station.config.getMaxArrivingPassengerCount();
    }

    @Override
    public void lifeCycle() {
        informAboutExternalDelay();
        arriveToSemaphore();
        arriveToPlatform();
        transferPassengers();
        departure();
    }

    protected void informAboutExternalDelay() {
        externalDelay = station.dist.externalDelay();
        if (trainChanged()) {
            registerTrainChange();
            station.sendDelayNotification(this, listeners);
        }
        hold(TimeOperations.add(TimeOperations.subtract(arrivalAt, internalArrival), externalDelay));
    }

    protected void arriveToSemaphore() {
        realSemaphoreArrivalAt = presentTime();
        realTrack = station.getBestTrack(this);
        realPlatform = realTrack.getPlatform();

        boolean trackEmpty = realTrack.isEmpty();
        realTrack.addTrain(this);

        if (! trackEmpty) {
            passivate(); // czeka przy semaforze na mozliwosc wjazdu na peron
        }
    }

    // opuszczenie semafora i wjazd na peron
    public void arriveToPlatform() {
        semaphoreDelay = TimeOperations.diff(presentTime(), realSemaphoreArrivalAt);
        registerSemaphoreDeparture();
        if (! realPlatform.equals(platform)) {
            registerTrainChange();
            station.sendPlatformChangeNotification(this, listeners);
        }
        hold(internalArrival);
    }

    // transfer
    public void transferPassengers() {
        Passenger passenger;
        while (! passengers.isEmpty()) {
            passenger = passengers.removeFirst();
            hold(new TimeSpan(2, TimeUnit.SECONDS)); // czas wysiadania
            registerPeopleChange();
            // wstaw pasażera na peron, podepnij mu towarzyszy i ustal ścieżkę
        }

        // departuring passengers sie powinni pakowac

        if (presentTime().compareTo(departureAt) < 1) {
            hold(departureAt);
        }
    }

    public void departure() {
        totalDelay = TimeOperations.diff(presentTime(), departureAt);
        registerPlatformDeparture();
        hold(internalArrival);
        leaveTrack();
    }

    public void leaveTrack() {
        track.removeTrain(this);
        track = null;
    }

    @Override
    public int count() {
        return otherPassengerCount + passengers.size();
    }

    protected void registerPlatformDeparture() {
        JSONObject data = new JSONObject();
        JSONObject delay = new JSONObject();

        try {
            long externalDelayInSeconds = externalDelay.getTimeRounded(TimeUnit.SECONDS);
            long totalDelayInSeconds = totalDelay.getTimeRounded(TimeUnit.SECONDS);

            delay.put("external", externalDelayInSeconds);
            delay.put("internal", totalDelayInSeconds - externalDelayInSeconds);
            data.put("delay", delay);
            data.put("train", name);
            data.put("duration", internalArrival.getTimeRounded(TimeUnit.SECONDS));
            station.registerVisualizationEvent("train-platform-departure", data);
        } catch (JSONException ex) {
            System.err.println("error building event: train-platform-departure");
        }
    }

    protected void registerSemaphoreDeparture() {
        JSONObject data = new JSONObject();
        JSONObject delay = new JSONObject();

        try {
            delay.put("external", externalDelay.getTimeRounded(TimeUnit.SECONDS));
            delay.put("semaphore", semaphoreDelay.getTimeRounded(TimeUnit.SECONDS));
            data.put("delay", delay);
            data.put("train", name);
            data.put("platform", realPlatform.number);
            data.put("rail", realTrack.number);
            data.put("from", source);
            data.put("to", destination);
            data.put("duration", internalArrival.getTimeRounded(TimeUnit.SECONDS));
            data.put("count", count());
            station.registerVisualizationEvent("train-semaphore-departure", data);
        } catch (JSONException ex) {
            System.err.println("error building event: train-semaphore-departure");
        }
    }

    // if it's delayed or it's platform has changed
    protected boolean trainChanged() {
        return TimeSpan.ZERO.compareTo(externalDelay) < 0 ||
               TimeSpan.ZERO.compareTo(semaphoreDelay) < 0 ||
               !(realPlatform == null || platform.equals(realPlatform));
    }

    protected void registerTrainChange() {
        JSONObject data = new JSONObject();
        JSONObject platforms = new JSONObject();
        TimeInstant scheduledAt;

        try {
            platforms.put("old", platform.number);
            if (realPlatform != null) {
                platforms.put("new", realPlatform.number);
            } else {
                platforms.put("new", platform.number);
            }
            data.put("platform", platforms);
            data.put("from", source);
            data.put("to", destination);

            if (type == Type.ARRIVAL) {
                scheduledAt = arrivalAt;
            } else {
                scheduledAt = departureAt;
            }
            data.put("scheduledAt", TimeTable.timeToString(scheduledAt, "minutes"));

            long delayInSeconds = TimeOperations.add(externalDelay, semaphoreDelay).getTimeRounded(TimeUnit.SECONDS);
            data.put("delay", delayInSeconds);
        } catch (JSONException ex) {
            System.err.println("error building event: train-change");
        }
    }
}
