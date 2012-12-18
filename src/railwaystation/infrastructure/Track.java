/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.ProcessQueue;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author artur
 * klasa reprezentująca tor przy peronie
 */
public class Track {
    protected Platform platform;
    protected ProcessQueue<Train> trains;
    protected int number;

    public Track(Platform platform, int number) {
        String name = "rail-" + Integer.toString(platform.number)+ "-" + Integer.toString(number);
        this.trains = new ProcessQueue(platform.station, name + "-trains", true, true);
        this.platform = platform;
        this.number = number;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void addTrain(Train train) {
        trains.insert(train);
        if (trains.length() > 1) {
            registerWaitingTrainsChange();
        }
    }

    public void removeTrain(Train train) {
        trains.remove(train);
        registerWaitingTrainsChange();
        if (! trains.isEmpty()) {
            trains.get(0).activate();
        }
    }

    public boolean isEmpty() {
        return trains.isEmpty();
    }

    protected void registerWaitingTrainsChange() {
        JSONObject data = new JSONObject();

        try {
            data.put("platform", platform.number);
            data.put("rail", number);
            data.put("count", trains.length() - 1);
            platform.station.registerVisualizationEvent("waiting-trains-change", data);
        } catch (JSONException ex) {
            System.err.println("error building event: waiting-trains-change");
        }
    }
}
