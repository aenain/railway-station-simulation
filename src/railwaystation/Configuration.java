/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.TimeSpan;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

/**
 *
 * @author artur
 */
public class Configuration {
    protected TimeSpan minGoToWaitingRoom, maxGoToPlatform, externalDelayInfoSpan;
    protected TimeSpan minComingTimeWithTicket, maxComingTimeWithTicket, minComingTimeWithoutTicket, maxComingTimeWithoutTicket;
    protected double complainingProbability, havingCompanionProbability, havingTicketProbability, shareOfVisitors;
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

        minGoToWaitingRoom = new TimeSpan(config.optInt("go_to_waiting_room_min_time_span", 20), TimeUnit.MINUTES);
        maxGoToPlatform = new TimeSpan(config.optInt("go_to_platform_max_time_span", 10), TimeUnit.MINUTES);
        externalDelayInfoSpan = new TimeSpan(config.optInt("external_delay_info_time_span", 30), TimeUnit.MINUTES);

        minComingTimeWithTicket = new TimeSpan(config.optInt("min_coming_time_span_with_ticket", 15), TimeUnit.MINUTES);
        maxComingTimeWithTicket = new TimeSpan(config.optInt("max_coming_time_span_with_ticket", 45), TimeUnit.MINUTES);
        minComingTimeWithoutTicket = new TimeSpan(config.optInt("min_coming_time_span_without_ticket", 25), TimeUnit.MINUTES);
        maxComingTimeWithoutTicket = new TimeSpan(config.optInt("max_coming_time_span_without_ticket", 50), TimeUnit.MINUTES);

        complainingProbability = config.optInt("average_probability_of_complaining", 15) / 100.0;
        havingCompanionProbability = config.optInt("average_probability_of_having_companion", 25) / 100.0;
        havingTicketProbability = config.optInt("average_probability_of_having_ticket", 50) / 100.0;
        shareOfVisitors = config.optInt("average_share_of_visitors", 25) / 100.0;

        platformCount = config.optInt("platform_count", 4);
        infoDeskCount = config.optInt("info_desk_count", 2);
        cashDeskCount = config.optInt("cash_desk_count", 6);
        waitingRoomCapacity = config.optInt("waiting_room_capacity", 1000);

        minArrivingPassengerCount = config.optInt("min_arriving_passenger_count", 10);
        maxArrivingPassengerCount = config.optInt("max_arriving_passenger_count", 800);
        minDeparturingPassengerCount = config.optInt("min_departuring_passenger_count", 10);
        maxDeparturingPassengerCount = config.optInt("max_departuring_passenger_count", 800);

        internalArrivalDuration = new TimeSpan(config.optInt("internal_arrival_time", 5), TimeUnit.MINUTES);
        defaultPlatformWaitingTime = new TimeSpan(config.optInt("default_platform_waiting_time", 10), TimeUnit.MINUTES);
        minExternalDelay = new TimeSpan(config.optInt("min_external_delay", 0), TimeUnit.MINUTES);
        maxExternalDelay = new TimeSpan(config.optInt("max_external_delay", 60), TimeUnit.MINUTES);

        minSellingTicketTime = new TimeSpan(config.optInt("min_selling_ticket_time", 5), TimeUnit.MINUTES);
        maxSellingTicketTime = new TimeSpan(config.optInt("max_selling_ticket_time", 10), TimeUnit.MINUTES);
        minServingInformationTime = new TimeSpan(config.optInt("min_serving_information_time", 5), TimeUnit.MINUTES);
        maxServingInformationTime = new TimeSpan(config.optInt("max_serving_information_time", 15), TimeUnit.MINUTES);

        /*
            crowdSpeed, schedulingAlgorithm
         */
    }
}
