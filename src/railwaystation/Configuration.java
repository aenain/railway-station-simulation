/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.TimeSpan;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author artur
 */
public class Configuration {
    protected TimeSpan minGoToWaitingRoom, maxGoToPlatform, externalDelayInfoSpan;
    protected TimeSpan minComingTimeWithTicket, maxComingTimeWithTicket, minComingTimeWithoutTicket, maxComingTimeWithoutTicket;
    protected double complainingProbability, havingCompanionProbability, havingTicketProbability, shareOfVisitors, externalDelayProbability;
    protected int platformCount, cashDeskCount, infoDeskCount, waitingRoomCapacity;
    protected int minArrivingPassengerCount, maxArrivingPassengerCount, minDeparturingPassengerCount, maxDeparturingPassengerCount; // per train
    protected TimeSpan internalArrivalDuration, defaultPlatformWaitingTime, minExternalDelay, maxExternalDelay; // ride from semaphore to the platform | how long should a train wait for passengers on a platform
    protected TimeSpan minSellingTicketTime, maxSellingTicketTime, minServingInformationTime, maxServingInformationTime;

    protected CrowdSpeed crowdSpeed;
    protected SchedulingAlgorithm schedulingAlgorithm;

    private InputStream stream;
    private JSONObject config;

    public Configuration(InputStream stream) {
        this.stream = stream;
        this.config = null;
    }

    public void read() {
        this.config = railwaystation.io.JSONReader.read(stream);
    }

    public void setParameters() {
        if (config == null) { read(); }

        try {
            minGoToWaitingRoom = new TimeSpan(config.getInt("go_to_waiting_room_min_time_span"), TimeUnit.MINUTES);
            maxGoToPlatform = new TimeSpan(config.getInt("go_to_platform_max_time_span"), TimeUnit.MINUTES);
            externalDelayInfoSpan = new TimeSpan(config.getInt("external_delay_info_time_span"), TimeUnit.MINUTES);

            minComingTimeWithTicket = new TimeSpan(config.getInt("min_coming_time_span_with_ticket"), TimeUnit.MINUTES);
            maxComingTimeWithTicket = new TimeSpan(config.getInt("max_coming_time_span_with_ticket"), TimeUnit.MINUTES);
            minComingTimeWithoutTicket = new TimeSpan(config.getInt("min_coming_time_span_without_ticket"), TimeUnit.MINUTES);
            maxComingTimeWithoutTicket = new TimeSpan(config.getInt("max_coming_time_span_without_ticket"), TimeUnit.MINUTES);

            complainingProbability = config.getInt("average_probability_of_complaining") / 100.0;
            havingCompanionProbability = config.getInt("average_probability_of_having_companion") / 100.0;
            havingTicketProbability = config.getInt("average_probability_of_having_ticket") / 100.0;
            shareOfVisitors = config.getInt("average_share_of_visitors") / 100.0;
            externalDelayProbability = config.getInt("average_probability_of_external_delay") / 100.0;

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

            /*
                crowdSpeed, schedulingAlgorithm
             */
        } catch (JSONException ex) {
            System.err.println("error parsing json configuration");
        }
    }
}
