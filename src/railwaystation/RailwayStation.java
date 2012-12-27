/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
import railwaystation.person.Generator;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class RailwayStation extends Model {
    public static final String CONFIG_FILENAME = "config.json",
                               OUTPUT_FILENAME = "output.json",
                               SCHEDULE_FILENAME = "schedule.json";

    public static final TimeInstant START_TIME = new TimeInstant(0, TimeUnit.HOURS),
                                    STOP_TIME = new TimeInstant(24, TimeUnit.HOURS);

    public Configuration config;
    public Distribution dist;

    private TimeTable timeTable;
    private Infrastructure infrastructure;
    private JSONArray visualizationEvents;
    private JSONObject visualizationSummary;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Generator peopleGenerator;

    public RailwayStation() {
        super(null, "railway-station", true, true);
        visualizationEvents = new JSONArray();
        visualizationSummary = new JSONObject();
        timeTable = new TimeTable();
        infrastructure = new Infrastructure(this);
        peopleGenerator = new Generator(this);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RailwayStation model = new RailwayStation();
        model.parseOptions(args);

        Experiment experiment = new Experiment("Railway Station Daily Simulation");
        model.connectToExperiment(experiment);

        experiment.setShowProgressBar(false);
        experiment.setSilent(true);
        experiment.tracePeriod(START_TIME, STOP_TIME);
        experiment.stop(STOP_TIME);

        experiment.start();
        experiment.report();
        experiment.finish();

        model.prepareVisualizationSummary();
        model.saveVisualizationResult();
    }

    protected void prepareVisualizationSummary() {
        JSONObject data, trains, delays, averageDelays;

        try {
            // cash desks
            data = new JSONObject();
            data.put("soldTickets", 0);
            data.put("averageWaitingTime", 0); // seconds
            visualizationSummary.put("cashDesks", data);

            // info desks
            data = new JSONObject();
            data.put("servedInformations", 0);
            data.put("complaints", 0);
            data.put("averageWaitingTime", 0); // seconds
            visualizationSummary.put("infoDesks", data);

            // people
            data = new JSONObject();
            data.put("arriving", 0);
            data.put("departuring", 0);
            visualizationSummary.put("passengers", data);
            visualizationSummary.put("companions", 0);
            visualizationSummary.put("visitors", 0);

            // trains
            trains = new JSONObject();
            trains.put("count", 0);
            trains.put("platformChanges", 0);
                delays = new JSONObject();
                    averageDelays = new JSONObject();
                    averageDelays.put("semaphore", 0); // seconds
                    averageDelays.put("platform", 0); // seconds
                    averageDelays.put("external", 0); // seconds
                delays.put("average", averageDelays);
            trains.put("delay", delays);
            visualizationSummary.put("trains", trains);
        } catch (JSONException ex) {
            System.err.println("error preparing json summary");
        }
    }

    protected void saveVisualizationResult() {
        JSONObject data = new JSONObject();

        try {
            data.put("events", visualizationEvents);
            data.put("summary", visualizationSummary);
            railwaystation.io.JSONWriter.write(outputStream, data);
        } catch (JSONException ex) {
            System.err.println("error saving json output");
        }
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
        timeTable.generateTrains(this);
        generatePeople();

        new CyclicPeopleChangeEvent(this).schedule(CyclicPeopleChangeEvent.INTERVAL);
    }

    @Override
    public void init() {
        readConfig();
        initDistribution();

        buildInfrastructure();
        readSchedule();
    }

    private void readConfig() {
        config = new Configuration(inputStream);
        config.read();
        config.setParameters();

        try {
            inputStream.close();
        } catch (IOException ex) {}
    }

    private void initDistribution() {
        dist = new Distribution(this);
        dist.initStreams();
    }

    protected void buildInfrastructure() {
        infrastructure.createPlatforms(config.platformCount);

        Region hall = infrastructure.createRegion("hall", Infrastructure.MAX_CAPACITY);
        infrastructure.setEntryRegion(hall);

        ServingRegion informationDeskRegion = infrastructure.createServingRegion("info", Infrastructure.MAX_CAPACITY, config.infoDeskCount);
        infrastructure.setInformationDeskRegion(informationDeskRegion);

        CashDeskRegion cashDeskRegion = infrastructure.createCashDeskRegion("cash", Infrastructure.MAX_CAPACITY, config.cashDeskCount);
        infrastructure.setCashDeskRegion(cashDeskRegion);

        Region waitingRoom = infrastructure.createRegion("waiting-room", config.waitingRoomCapacity);
        infrastructure.setWaitingRoom(waitingRoom);

        // polaczenia miedzy elementami infrastruktury dworcowej
        infrastructure.bindRegions(hall, waitingRoom);
        infrastructure.bindWithPlatforms(hall);
        infrastructure.bindWithPlatforms(waitingRoom);
        infrastructure.bindRegions(hall, informationDeskRegion);
        infrastructure.bindRegions(hall, cashDeskRegion);
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }

    public void readSchedule() {
        File schedule = new File(SCHEDULE_FILENAME);
        timeTable.readSchedule(schedule);
    }

    public void generatePeople() {
        for (Train train : timeTable.trains) {
            peopleGenerator.generate(train);
        }
    }

    public void sendDelayNotification(Train train, LinkedList<Person> listeners) {
        // TODO! przekaz informacje zainteresowanym pasazerom
        
    }

    public void sendPlatformChangeNotification(Train train, LinkedList<Person> listeners) {
        // TODO! przekaz informacje zainteresowanym pasazerom
    }

    public Track getBestTrack(Train train) {
        return train.getTrack(); // TODO! zwroc najlepszy tor (algorytm powinien brac pod uwage przypisany tor :P)
    }

    public void registerVisualizationEvent(String type, JSONObject data) {
        registerVisualizationEvent(type, data, presentTime());
    }

    public void registerVisualizationEvent(String type, JSONObject data, TimeInstant at) {
        JSONObject event = new JSONObject();

        try {
            event.put("at", at.getTimeRounded(TimeUnit.SECONDS));
            event.put("type", type);
            event.put("data", data);
            visualizationEvents.put(event);
        } catch (JSONException ex) {
            System.err.println("error adding visualization event to the array");
        }
    }

    public void parseOptions(String[] args) {
        List<String> arguments = Arrays.asList(args);
        ListIterator<String> it = arguments.listIterator();
        boolean expectInput = false, expectOutput = false;
        String input = null, output = null, token;

        while (it.hasNext()) {
            token = it.next();

            if (expectInput) {
                input = token;
                expectInput = false;
            } else if (expectOutput) {
                output = token;
                expectOutput = false;
            } else if (token.equals("-i")) {
                expectInput = true;
                expectOutput = false;
            } else if (token.equals("-o")) {
                expectOutput = true;
                expectInput = false;
            }
        }

        setStreams(input, output);
    }

    private void setStreams(String input, String output) {
        if ("STDIN".equals(input)) {
            inputStream = System.in;
        } else {
            if (input == null) { input = CONFIG_FILENAME; }
            try {
                File configFile = new File(input);
                inputStream = new FileInputStream(configFile);
            } catch (FileNotFoundException ex) {
                System.err.println("input file not found!");
            }
        }

        if ("STDOUT".equals(output)) {
            outputStream = System.out;
        } else {
            if (output == null) { output = OUTPUT_FILENAME; }
            try {
                File outputFile = new File(output);
                outputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException ex) {
                System.err.println("output file not found!");
            }
        }
    }
}