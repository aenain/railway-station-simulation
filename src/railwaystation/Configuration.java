/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.TimeSpan;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author artur
 */
public class Configuration {
    protected TimeSpan minGoToWaitingRoom, maxGoToPlatform, externalDelayInfoSpan;
    protected TimeSpan minCompanionComingTime, maxCompanionComingTime, minComingTimeWithTicket, maxComingTimeWithTicket, minComingTimeWithoutTicket, maxComingTimeWithoutTicket;
    protected double complainingProbability, buyingTicketProbability, gettingInformationProbability, externalDelayProbability;
    protected int platformCount, cashDeskCount, infoDeskCount, waitingRoomCapacity;
    protected int minArrivingPassengerCount, maxArrivingPassengerCount, minDeparturingPassengerCount, maxDeparturingPassengerCount; // per train
    protected TimeSpan internalArrivalDuration, defaultPlatformWaitingTime, minExternalDelay, maxExternalDelay; // ride from semaphore to the platform | how long should a train wait for passengers on a platform
    protected TimeSpan minSellingTicketTime, maxSellingTicketTime, minServingInformationTime, maxServingInformationTime;

    protected CrowdSpeed crowdSpeed;
    protected SchedulingAlgorithm schedulingAlgorithm;
    protected int[] visitorComingDist;
    protected double[] companionCountDist;

    private InputStream stream;
    private JSONObject config;

    public Configuration(InputStream stream) {
        this.stream = stream;
        this.config = null;
    }

    public int getMinArrivingPassengerCount() {
        return minArrivingPassengerCount;
    }

    public int getMaxArrivingPassengerCount() {
        return maxArrivingPassengerCount;
    }

    public int getMinDeparturingPassengerCount() {
        return minDeparturingPassengerCount;
    }

    public int getMaxDeparturingPassengerCount() {
        return maxDeparturingPassengerCount;
    }

    public TimeSpan getMaxTimeToGoToPlatform() {
        return maxGoToPlatform;
    }

    public TimeSpan getMinTimeToGoToWaitingRoom() {
        return minGoToWaitingRoom;
    }

    public TimeSpan getMaxCompanionComingTime() {
        return maxCompanionComingTime;
    }

    public TimeSpan getMaxComingTimeWithTicket() {
        return maxComingTimeWithTicket;
    }

    public TimeSpan getMaxComingTimeWithoutTicket() {
        return maxComingTimeWithoutTicket;
    }

    public void read() {
        try {
            this.config = railwaystation.io.JSONReader.read(stream).getJSONObject("simulation");
        } catch (JSONException ex) {
            System.err.println("error reading json configuration");
        }
    }

    public void setParameters() {
        if (config == null) { read(); }

        try {
            JSONArray distribution;
            minGoToWaitingRoom = new TimeSpan(config.getInt("go_to_waiting_room_min_time_span"), TimeUnit.MINUTES);
            maxGoToPlatform = new TimeSpan(config.getInt("go_to_platform_max_time_span"), TimeUnit.MINUTES);
            externalDelayInfoSpan = new TimeSpan(config.getInt("external_delay_info_time_span"), TimeUnit.MINUTES);

            minCompanionComingTime = new TimeSpan(config.getInt("min_companion_coming_time_span"), TimeUnit.MINUTES);
            maxCompanionComingTime = new TimeSpan(config.getInt("max_companion_coming_time_span"), TimeUnit.MINUTES);
            minComingTimeWithTicket = new TimeSpan(config.getInt("min_coming_time_span_with_ticket"), TimeUnit.MINUTES);
            maxComingTimeWithTicket = new TimeSpan(config.getInt("max_coming_time_span_with_ticket"), TimeUnit.MINUTES);
            minComingTimeWithoutTicket = new TimeSpan(config.getInt("min_coming_time_span_without_ticket"), TimeUnit.MINUTES);
            maxComingTimeWithoutTicket = new TimeSpan(config.getInt("max_coming_time_span_without_ticket"), TimeUnit.MINUTES);

            complainingProbability = config.getInt("average_probability_of_complaining") / 100.0;
            gettingInformationProbability = config.getInt("average_probability_of_getting_information") / 100.0;
            buyingTicketProbability = config.getInt("average_probability_of_buying_ticket") / 100.0;
            externalDelayProbability = config.getInt("average_probability_of_external_delay") / 100.0;

            distribution = config.getJSONArray("companion_count_distribution");
            companionCountDist = new double[distribution.length()];
            for (int i = 0; i < distribution.length(); i++) {
                companionCountDist[i] = Math.max(0, distribution.getDouble(i));
            }

            distribution = config.getJSONArray("visitor_coming_distribution");
            visitorComingDist = new int[distribution.length()];
            for (int i = 0; i < distribution.length(); i++) {
                visitorComingDist[i] = Math.max(distribution.getInt(i), 0);
            }

            platformCount = config.getInt("platform_count");
            infoDeskCount = config.getInt("info_desk_count");
            cashDeskCount = config.getInt("cash_desk_count");
            waitingRoomCapacity = config.getInt("waiting_room_capacity");

            minArrivingPassengerCount = config.getInt("min_arriving_passenger_count");
            maxArrivingPassengerCount = config.getInt("max_arriving_passenger_count");
            minDeparturingPassengerCount = config.getInt("min_departuring_passenger_count");
            maxDeparturingPassengerCount = config.getInt("max_departuring_passenger_count");

            internalArrivalDuration = new TimeSpan(config.getInt("internal_arrival_time"), TimeUnit.MINUTES);
            defaultPlatformWaitingTime = new TimeSpan(config.getInt("default_platform_waiting_time"), TimeUnit.MINUTES);
            minExternalDelay = new TimeSpan(config.getInt("min_external_delay"), TimeUnit.MINUTES);
            maxExternalDelay = new TimeSpan(config.getInt("max_external_delay"), TimeUnit.MINUTES);

            minSellingTicketTime = new TimeSpan(config.getInt("min_selling_ticket_time"), TimeUnit.MINUTES);
            maxSellingTicketTime = new TimeSpan(config.getInt("max_selling_ticket_time"), TimeUnit.MINUTES);
            minServingInformationTime = new TimeSpan(config.getInt("min_serving_information_time"), TimeUnit.MINUTES);
            maxServingInformationTime = new TimeSpan(config.getInt("max_serving_information_time"), TimeUnit.MINUTES);

            String schedulingAlgoName = config.getString("scheduling_algorithm");
            if(schedulingAlgoName.equals("wait")) {
                schedulingAlgorithm = new WaitSchedulingAlgorithm();
            }
            else if(schedulingAlgoName.equals("random")) {
                schedulingAlgorithm = new FirstFreeSchedulingAlgorithm();
            }
        } catch (JSONException ex) {
            System.err.println("error parsing json configuration");
        }
    }
}
