/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.infrastructure.CashDeskRegion;
import railwaystation.infrastructure.Infrastructure;
import railwaystation.infrastructure.Region;
import railwaystation.infrastructure.ServingRegion;
import railwaystation.infrastructure.Track;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class RailwayStation extends Model {
    public TimeSpan minGoToWaitingRoom, maxGoToPlatform, externalDelayInfoSpan;
    public TimeSpan minComingTimeWithTicket, maxComingTimeWithTicket, minComingTimeWithoutTicket, maxComingTimeWithoutTicket;
    public double complainingProbability, havingCompanionProbability, havingTicketProbability, shareOfVisitors;
    public int platformCount, cashDeskCount, infoDeskCount, waitingRoomCapacity;
    public int minArrivingPassengerCount, maxArrivingPassengerCount, minDeparturingPassengerCount, maxDeparturingPassengerCount; // per train
    public TimeSpan internalArrivalDuration, defaultPlatformWaitingTime; // ride from semaphore to the platform | how long should a train wait for passengers on a platform
    public TimeSpan minSellingTicketTime, maxSellingTicketTime, minServingInformationTime, maxServingInformationTime;
    
    public CrowdSpeed crowdSpeed;
    public SchedulingAlgorithm schedulingAlgorithm;
    
    private Infrastructure infrastructure;
    private JSONArray visualizationEvents;
    
    private class ConfigurationManager {
        protected InputStream stream;
        protected JSONObject config;

        public ConfigurationManager(InputStream stream) {
            this.stream = stream;
            this.config = null;
        }

        public void read() {
            this.config = railwaystation.io.JSONReader.read(stream);
        }

        public void setParameters() {
            if (config != null) {
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

                minSellingTicketTime = new TimeSpan(config.optInt("min_selling_ticket_time", 5), TimeUnit.MINUTES);
                maxSellingTicketTime = new TimeSpan(config.optInt("max_selling_ticket_time", 10), TimeUnit.MINUTES);
                minServingInformationTime = new TimeSpan(config.optInt("min_serving_information_time", 5), TimeUnit.MINUTES);
                maxServingInformationTime = new TimeSpan(config.optInt("max_serving_information_time", 15), TimeUnit.MINUTES);
                
                /*
                    crowdSpeed, schedulingAlgorithm
                 */
            }
        }
    }

    public RailwayStation() {
        super(null, "railway-station", true, true);
        visualizationEvents = new JSONArray();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RailwayStation station = new RailwayStation();
        station.readConfig(args);
    }

    protected void buildInfrastructure() {
        infrastructure = new Infrastructure(this);
        infrastructure.createPlatforms(Infrastructure.PLATFORM_COUNT);

        Region hall = infrastructure.createRegion("hall", Infrastructure.HALL_CAPACITY);
        infrastructure.setEntryRegion(hall);

        ServingRegion informationDeskRegion = infrastructure.createServingRegion("info", Infrastructure.INFO_DESKS_CAPACITY, Infrastructure.INFO_DESK_COUNT);
        infrastructure.setInformationDeskRegion(informationDeskRegion);

        CashDeskRegion cashDeskRegion = infrastructure.createCashDeskRegion("cash", Infrastructure.CASH_DESKS_CAPACITY, Infrastructure.CASH_DESK_COUNT);
        infrastructure.setCashDeskRegion(cashDeskRegion);

        Region waitingRoom = infrastructure.createRegion("waiting-room", Infrastructure.WAITING_ROOM_CAPACITY);
        infrastructure.setWaitingRoom(waitingRoom);

        // polaczenia miedzy elementami infrastruktury dworcowej
        infrastructure.bindRegions(hall, waitingRoom);
        infrastructure.bindWithPlatforms(hall);
        infrastructure.bindWithPlatforms(waitingRoom);
        infrastructure.bindRegions(hall, informationDeskRegion);
        infrastructure.bindRegions(hall, cashDeskRegion);
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    @Override
    public String description() {
        return "TODO! description of the simulation.";
    }

    @Override
    public void doInitialSchedules() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        buildInfrastructure();
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public TimeSpan generateExternalDelay() {
        // TODO! sparametryzowac
        return new TimeSpan(20, TimeUnit.MINUTES);
    }

    public void sendDelayNotification(Train train) {
        // TODO! przekaz informacje zainteresowanym pasazerom
        
    }

    public void sendPlatformChangeNotification(Train train) {
        // TODO! przekaz informacje zainteresowanym pasazerom
    }

    public Track getBestTrack(Train train) {
        return train.getTrack(); // TODO! zwroc najlepszy tor (algorytm powinien brac pod uwage przypisany tor :P)
    }

    public void registerVisualizationEvent(String type, JSONObject data) {
        JSONObject event = new JSONObject();

        try {
            event.put("at", presentTime().getTimeRounded(TimeUnit.SECONDS));
            event.put("type", type);
            event.put("data", data);
            visualizationEvents.put(event);
        } catch (JSONException ex) {
            System.err.println("error adding visualization event to the array");
        }
    }

    public void readConfig(String[] args) {
        InputStream configStream = null;

        if (args.length == 2 && args[0].equals("-i") && args[1].equals("STDIN")) {
            configStream = System.in;
        } else {
            try {
                File configFile = new File("config.json");
                configStream = new FileInputStream(configFile);
            } catch (FileNotFoundException ex) {
                System.err.println("configuration file not found!");
            }
        }

        ConfigurationManager config = new ConfigurationManager(configStream);
        config.read();
        if (configStream != null) {
            try {
                configStream.close();
            } catch (IOException ex) {}
        }
        config.setParameters();
    }
}