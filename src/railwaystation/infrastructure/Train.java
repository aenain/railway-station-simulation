/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.RailwayStation;
import railwaystation.TimeTable;
import railwaystation.person.Passenger;

/**
 *
 * @author artur
 */
public class Train extends Region {
    public static enum Type { arrival, departure, transit };
    protected ProcessQueue<Passenger> gettingOutPassengers, gettingInPassengers;
    protected Integer passengerCount;
    protected Platform platform, realPlatform;
    protected Track track, realTrack;
    protected TimeInstant arrivalAt, departureAt; // scheduled
    protected TimeInstant realSemaphoreArrivalAt;
    protected TimeSpan externalDelay = TimeSpan.ZERO, semaphoreDelay = TimeSpan.ZERO, totalDelay = TimeSpan.ZERO, internalArrival;
    protected String source, destination;
    protected Type type;

    public Train(RailwayStation owner, String name) {
        super(owner, name, Infrastructure.MAX_CAPACITY);
        passengerCount = 100;
        gettingOutPassengers = new ProcessQueue(owner, name + "-getting-out-passengers", true, true);
        gettingInPassengers = new ProcessQueue(owner, name + "-getting-in-passengers", true, true);
    }

    public void setType(Type type) {
        this.type = type;
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
            station.sendDelayNotification(this);
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
            station.sendPlatformChangeNotification(this);
        }
        hold(internalArrival);
    }

    // transfer
    public void transferPassengers() {
        // TODO! wysiadka przyjezdnych (utworzenie pasazerow + polaczenie ich z osobami towarzyszacymi)
        // ile hold?
        // registerPeopleChange()
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

    public void generatePassengers() {
        // TODO!
    }

    @Override
    public int count() {
        return passengerCount;
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
            data.put("count", passengerCount);
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

            if (type == Type.arrival) {
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
