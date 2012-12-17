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
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.infrastructure.Platform;
import railwaystation.infrastructure.Track;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 * klasa reprezentujaca rozklad jazdy, generuje pociagi i je schedule'uje odpowiednio
 */
public class TimeTable {
    protected LinkedList<Train> trains;
    protected JSONArray rawTrains;

    public TimeTable() {
        trains = new LinkedList();
    }

    public void readSchedule(File schedule) {
        JSONObject data = railwaystation.io.JSONReader.read(schedule);
        try {
            this.rawTrains = data.getJSONArray("trains");
        } catch (JSONException ex) {
            System.err.println("error getting trains out of the schedule.");
        }
    }

    public void generateTrains(RailwayStation station) {
        JSONObject raw;
        Train train;
        TimeInstant arrivalAt = null, departureAt = null;
        String type;

        if (rawTrains != null) {
            try {
                for (int i = 0; i < rawTrains.length(); i++) {
                    raw = rawTrains.getJSONObject(i);
                    train = new Train(station, raw.optString("symbol", "train-" + Integer.toString(i)));
                    // ustaw atrybuty i passivate go na czas do arrival_at - external_delay_info_time_span
                    type = raw.optString("type", "");
                    if (type.equals("transit")) {
                        arrivalAt = parseTime(raw.getString("arrival_at"));
                        departureAt = parseTime(raw.getString("departure_at"));
                    }
                    else if (type.equals("arrival")) {
                        arrivalAt = parseTime(raw.getString("arrival_at"));
                        departureAt = TimeOperations.add(arrivalAt, Train.DEFAULT_PLATFORM_WAITING_TIME);
                    }
                    else if (type.equals("departure")) {
                        departureAt = parseTime(raw.getString("departure_at"));
                        arrivalAt = TimeOperations.subtract(departureAt, Train.DEFAULT_PLATFORM_WAITING_TIME);
                    }
                    train.setArrivalAt(arrivalAt);
                    train.setDepartureAt(departureAt);
                    train.setInternalArrivalDuration(station.internalArrivalDuration);

                    train.setSource(raw.optString("from", null));
                    train.setDestination(raw.optString("to", null));

                    Platform platform = station.getInfrastructure().getPlatform(raw.optInt("platform", 1));
                    Track track = platform.getTrack(raw.optInt("rail", 1));

                    train.setTrack(track);
                    // TODO! wygenerowanie tablicy z liczba towarzyszy
                    train.activate(TimeOperations.subtract(arrivalAt, station.externalDelayInfoSpan));
                }
            } catch (JSONException ex) {
                System.err.println("error generating trains.");
            }
        }
    }

    public static TimeInstant parseTime(String time) {
        if (Pattern.matches("^[0-9]{2}:[0-9]$", time)) {
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
            return String.format("%2d:%2d", hours, minutes);
        } else {
            minutes = time.getTimeTruncated(TimeUnit.MINUTES) - hours * 60;
            seconds = time.getTimeRounded(TimeUnit.SECONDS) - (hours * 60 + minutes) * 60;
            return String.format("%2d:%2d:%2d", hours, minutes, seconds);
        }
    }
}
