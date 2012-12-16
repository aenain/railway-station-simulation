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
    public static final TimeSpan DEFAULT_PLATFORM_WAITING_TIME = new TimeSpan(10, TimeUnit.MINUTES);
    protected ProcessQueue<Passenger> gettingOutPassengers, gettingInPassengers;
    protected Integer passengerCount;
    protected Platform platform, realPlatform;
    protected Track track, realTrack;
    protected TimeInstant arrivalAt, departureAt, realSemaphoreArrivalAt; // scheduled
    protected TimeSpan externalDelay = TimeSpan.ZERO, semaphoreDelay = TimeSpan.ZERO, totalDelay = TimeSpan.ZERO, internalArrival;
    protected String source, destination;

    public Train(RailwayStation owner, String name) {
        super(owner, name, Infrastructure.TRAIN_CAPACITY);
        passengerCount = 100;
        gettingOutPassengers = new ProcessQueue(owner, name + "-getting-out-passengers", true, true);
        gettingInPassengers = new ProcessQueue(owner, name + "-getting-in-passengers", true, true);
    }

    public void setArrivalAt(TimeInstant arrivalAt) {
        this.arrivalAt = arrivalAt;
    }

    public void setDepartureAt(TimeInstant departureAt) {
        this.departureAt = departureAt;
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
        externalDelay = station.generateExternalDelay();
        station.sendDelayNotification(this);
        registerTrainChange();
        hold(TimeOperations.add(TimeOperations.subtract(arrivalAt, internalArrival), externalDelay));
    }

    protected void arriveToSemaphore() {
        realSemaphoreArrivalAt = presentTime();
        realTrack = station.getBestTrack(this);
        realPlatform = realTrack.getPlatform();
        realTrack.addTrain(this);

        if (! realTrack.isEmpty()) {
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
            data.put("duration", internalArrival.getTimeRounded(TimeUnit.SECONDS));
            data.put("count", passengerCount);
            station.registerVisualizationEvent("train-semaphore-departure", data);
        } catch (JSONException ex) {
            System.err.println("error building event: train-semaphore-departure");
        }
    }

    protected void registerTrainChange() {
        JSONObject data = new JSONObject();
        JSONObject platforms = new JSONObject();

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

            // TODO! rethink this.
            data.put("scheduledAt", TimeTable.timeToString(departureAt, "minutes"));

            long delayInSeconds = TimeOperations.add(externalDelay, semaphoreDelay).getTimeRounded(TimeUnit.SECONDS);
            data.put("delay", delayInSeconds);
        } catch (JSONException ex) {
            System.err.println("error building event: train-change");
        }
    }
}
