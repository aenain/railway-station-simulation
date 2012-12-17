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
    public Configuration config;
    
    private Infrastructure infrastructure;
    private JSONArray visualizationEvents;

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
        infrastructure.createPlatforms(config.platformCount);

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
        return config.minExternalDelay;
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

        config = new Configuration(configStream);
        config.read();
        config.setParameters();

        if (configStream != null) {
            try {
                configStream.close();
            } catch (IOException ex) {}
        }
    }
}