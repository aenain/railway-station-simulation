/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.infrastructure.Platform;
import railwaystation.infrastructure.Track;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur klasa reprezentujaca rozklad jazdy, generuje pociagi i je
 * schedule'uje odpowiednio
 */
public class TimeTable {

    protected LinkedList<Train> trains;
    protected JSONArray rawTrains;
    protected String stationName;
    protected RailwayStation station;

    public TimeTable() {
        trains = new LinkedList();
    }

    public void readSchedule(File schedule) {
        JSONObject data = railwaystation.io.JSONReader.read(schedule);
        try {
            this.stationName = data.getString("station");
            this.rawTrains = data.getJSONArray("trains");
        } catch (JSONException ex) {
            System.err.println("error getting trains out of the schedule.");
        }
    }

    public void generateTrains(RailwayStation station) {
        JSONObject raw;
        Train train;
        TimeInstant arrivalAt = null, departureAt = null;
        String type, source, destination;
        int platformNumber;

        this.station = station;

        if (rawTrains != null) {
            try {
                for (int i = 0; i < rawTrains.length(); i++) {
                    raw = rawTrains.getJSONObject(i);
                    train = new Train(station, raw.optString("symbol", "train-" + Integer.toString(i)));
                    trains.add(train);

                    type = raw.optString("type", "");
                    if (type.equals("transit")) {
                        arrivalAt = parseTime(raw.getString("arrival_at"));
                        departureAt = parseTime(raw.getString("departure_at"));
                        train.setType(Train.Type.TRANSIT);
                    } else if (type.equals("arrival")) {
                        arrivalAt = parseTime(raw.getString("arrival_at"));
                        departureAt = TimeOperations.add(arrivalAt, station.config.defaultPlatformWaitingTime);
                        train.setType(Train.Type.ARRIVAL);
                    } else if (type.equals("departure")) {
                        departureAt = parseTime(raw.getString("departure_at"));
                        arrivalAt = TimeOperations.subtract(departureAt, station.config.defaultPlatformWaitingTime);
                        train.setType(Train.Type.DEPARTURE);
                    }
                    train.setArrivalAt(arrivalAt);
                    train.setDepartureAt(departureAt);
                    train.setInternalArrivalDuration(station.config.internalArrivalDuration);

                    source = raw.optString("from", null);
                    if (source == null) {
                        source = stationName;
                    }
                    destination = raw.optString("to", null);
                    if (destination == null) {
                        destination = stationName;
                    }

                    train.setSource(source);
                    train.setDestination(destination);

                    platformNumber = raw.optInt("platform", 1);
                    // TODO! cos wymyslic, zeby to przestawiac na inne perony lepiej
                    if (station.config.platformCount < platformNumber) {
                        platformNumber = 1;
                    }
                    Platform platform = station.getInfrastructure().getPlatform(platformNumber);
                    Track track = platform.getTrack(raw.optInt("rail", 1));
                    train.setTrack(track);

                    train.activate(TimeOperations.subtract(arrivalAt, station.config.externalDelayInfoSpan));
                }
            } catch (JSONException ex) {
                System.err.println("error generating trains.");
            }
        }
    }

    public void flushTrainEvents() {
        for (Train train : trains) {
            if (train.isOnPlatform()) {
                train.stackPeopleChange();
            }
        }
    }

    public static TimeInstant parseTime(String time) {
        if (time.matches("^[0-9]{2}:[0-9]{2}$")) {
            String[] parts = time.split(":");
            long minutes = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            return new TimeInstant(minutes, TimeUnit.MINUTES);
        } else {
            return null;
        }
    }

    /*
     * @param precision String (minutes | seconds)
     */
    public static String timeToString(TimeInstant time, String precision) {
        long seconds, minutes, hours = time.getTimeTruncated(TimeUnit.HOURS);
        if (precision.equals("minutes")) {
            minutes = time.getTimeRounded(TimeUnit.MINUTES) - hours * 60;
            return String.format("%02d:%02d", hours, minutes);
        } else {
            minutes = time.getTimeTruncated(TimeUnit.MINUTES) - hours * 60;
            seconds = time.getTimeRounded(TimeUnit.SECONDS) - (hours * 60 + minutes) * 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
