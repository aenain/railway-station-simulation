/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.zip.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.infrastructure.*;
import railwaystation.person.GenerateVisitorsEvent;
import railwaystation.person.Generator;
import railwaystation.person.TrainOrientedPerson;
import railwaystation.utilities.Logger;

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
    private boolean compressOutput = false;
    public Configuration config;
    public Distribution dist;
    public Infrastructure structure;
    public Logger logger;
    public Summary summary;
    private TimeTable timeTable;
    private JSONArray visualizationEvents;
    private JSONObject visualizationSummary;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Generator peopleGenerator;

    public RailwayStation() {
        super(null, "railway-station", true, true);

        visualizationEvents = new JSONArray();
        visualizationSummary = new JSONObject();

        logger = new Logger(this, Logger.Level.ERROR);
        timeTable = new TimeTable();
        structure = new Infrastructure(this);
        peopleGenerator = new Generator(this);
        summary = new Summary(this);
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

        try {
            experiment.start();
        } catch (OutOfMemoryError error) {
            System.err.println(TimeTable.timeToString(model.presentTime(), "seconds"));
        }

        experiment.report();
        experiment.finish();
        
        model.computeSummary();
        model.saveVisualizationResult();
    }

    protected void computeSummary() {
        summary.computeAll();
    }

    protected void saveVisualizationResult() {
        JSONObject data = new JSONObject();
        JSONObject result = new JSONObject();

        visualizationSummary = summary.prepareVisualizationSummary();

        try {
            data.put("events", visualizationEvents);
            data.put("summary", visualizationSummary);
            result.put("simulation", data);
            railwaystation.io.JSONWriter.write(outputStream, result);
        } catch (JSONException ex) {
            System.err.println("error saving json output");
        }
    }

    @Override
    public String description() {
        return "TODO! description of the simulation.";
    }

    @Override
    public void doInitialSchedules() {
        timeTable.generateTrains(this);
        structure.activateDesks();
        generatePeople();
        CyclicPeopleChangeEvent event = new CyclicPeopleChangeEvent(this);
        event.setSchedulingPriority(-1); // lower priority
        event.schedule(CyclicPeopleChangeEvent.INTERVAL);
        
        GenerateVisitorsEvent genVisEvent = new GenerateVisitorsEvent(this, dist);
        genVisEvent.schedule(new TimeSpan(0));
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
        } catch (IOException ex) {
        }
    }

    private void initDistribution() {
        dist = new Distribution(this);
        dist.initStreams();
    }

    protected void buildInfrastructure() {
        structure.createPlatforms(config.platformCount);

        Region hall = structure.createRegion("hall", Infrastructure.MAX_CAPACITY);
        structure.setEntryRegion(hall);

        ServingRegion informationDeskRegion = structure.createServingRegion("info", Infrastructure.MAX_CAPACITY, config.infoDeskCount);
        structure.setInformationDeskRegion(informationDeskRegion);

        CashDeskRegion cashDeskRegion = structure.createCashDeskRegion("cash", Infrastructure.MAX_CAPACITY, config.cashDeskCount);
        structure.setCashDeskRegion(cashDeskRegion);

        Region waitingRoom = structure.createRegion("waiting-room", config.waitingRoomCapacity);
        waitingRoom.setWalkingTime(TimeSpan.ZERO);
        structure.setWaitingRoom(waitingRoom);

        // polaczenia miedzy elementami infrastruktury dworcowej
        structure.bindRegions(hall, waitingRoom);
        structure.bindWithPlatforms(hall);
        structure.bindWithPlatforms(waitingRoom);
        structure.bindRegions(hall, informationDeskRegion);
        structure.bindRegions(hall, cashDeskRegion);
    }

    public Generator getPeopleGenerator() {
        return peopleGenerator;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }

    public Infrastructure getStructure() {
        return structure;
    }

    public Summary getSummary() {
        return summary;
    }

    public void readSchedule() {
        File schedule = new File(SCHEDULE_FILENAME);
        timeTable.readSchedule(schedule);
    }

    public void generatePeople() {
        peopleGenerator.findGreatestComingTimeSpan();
        for (Train train : timeTable.trains) {
            peopleGenerator.generateDelayed(train);
        }
    }

    public void sendDelayNotification(Train train, LinkedList<TrainOrientedPerson> listeners) {
        for (TrainOrientedPerson listener : listeners) {
            listener.setTrainDelay(train.getDelay());
        }
    }

    public void sendPlatformChangeNotification(Train train, LinkedList<TrainOrientedPerson> listeners) {
        for (TrainOrientedPerson listener : listeners) {
            listener.setTrainRealPlatform(train.getRealPlatform());
        }
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
        compressOutput = true;

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
            } else {
                expectOutput = false;
                expectInput = false;

                if (token.equals("--debug")) {
                    logger.setLevel(Logger.Level.DEBUG);
                } else if (token.equals("--no-compress")) {
                    compressOutput = false;
                }
            }
        }

        setStreams(input, output);
    }

    private void setStreams(String input, String output) {
        if ("STDIN".equals(input)) {
            inputStream = System.in;
        } else {
            if (input == null) {
                input = CONFIG_FILENAME;
            }
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
            if (output == null) {
                output = OUTPUT_FILENAME;
            }
            try {
                File outputFile;

                if (compressOutput) {
                    outputFile = new File(output + ".gz");
                    outputStream = new GZIPOutputStream(new FileOutputStream(outputFile));
                } else {
                    outputFile = new File(output);
                    outputStream = new FileOutputStream(outputFile);
                }
            } catch (IOException ex) {
                System.err.println("output file not found!");
            }
        }
    }

    public void d(SimProcess process, String message) {
        System.err.print(TimeTable.timeToString(process.presentTime(), "seconds") + " " + process.getName());
        System.err.print(" scheduled: " + process.isScheduled() + "\t\t");
        System.err.println(message);
    }
}