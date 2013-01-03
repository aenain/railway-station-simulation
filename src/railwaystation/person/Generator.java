/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.Random;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class Generator {
    private RailwayStation station;
    static private Random random;
    private TimeSpan comingTimeSpan;

    public Generator(RailwayStation station) {
        this.station = station;
        random = new Random(System.currentTimeMillis());
    }

    public void findGreatestComingTimeSpan() {
        comingTimeSpan = station.config.getMaxComingTimeWithoutTicket();
        if (station.config.getMaxComingTimeWithTicket().compareTo(comingTimeSpan) > 0) {
            comingTimeSpan = station.config.getMaxComingTimeWithTicket();
        }
        if (station.config.getMaxCompanionComingTime().compareTo(comingTimeSpan) > 0) {
            comingTimeSpan = station.config.getMaxCompanionComingTime();
        }
    }

    public void generateDelayed(Train train) {
        new GenerationEvent(station, train).schedule(TimeOperations.subtract(train.getArrivalAt(), comingTimeSpan));
    }

    public void generate(Train train) {
        int arrivingCount, departuringCount;

        arrivingCount = rand(train.getMinArrivingCount(), train.getMaxArrivingCount());
        departuringCount = rand(train.getMinDeparturingCount(), train.getMaxDeparturingCount());

        generateArriving(train, arrivingCount);
        generateDeparturing(train, departuringCount);
        // TODO! tu mozna dodac tranzytowych pasazerow
    }

    private void generateArriving(Train train, int count) {
        Passenger passenger;
        Companion companion;
        int companionCount;

        for (int i = 1; i <= count; i++) {
            passenger = new Passenger(station, train.getName() + "-arriving-passenger-" + i, train);
            passenger.setType(Person.Type.ARRIVING_PASSENGER);
            companionCount = station.dist.companionCount();
            for (int j = 0; j < companionCount; j++) {
                companion = new Companion(station, train.getName() + "-arriving-passenger-" + i + "-companion-" + (j + 1), passenger);
                companion.setType(Person.Type.ARRIVING_COMPANION);
                train.addNotifyListener(companion);
                companion.activate(station.dist.companionComingTime(train, Person.Type.ARRIVING_COMPANION));
                passenger.addCompanion(companion);
            }
            train.addPassenger(passenger);
            station.logger.changeThreads(Person.Type.ARRIVING_COMPANION, companionCount);
        }

        station.logger.changeThreads(Person.Type.ARRIVING_PASSENGER, count);
    }

    private void generateDeparturing(Train train, int count) {
        Passenger passenger;
        Companion companion;
        int companionCount;
        boolean hasTicket;

        for (int i = 1; i <= count; i++) {
            passenger = new Passenger(station, train.getName() + "-departuring-passenger-" + i, train);
            train.addNotifyListener(passenger);
            hasTicket = station.dist.hasTicket();
            passenger.setType(Person.Type.DEPARTURING_PASSENGER);
            passenger.setTicketPossession(hasTicket);
            passenger.activate(station.dist.passengerComingTime(train, hasTicket));
            companionCount = station.dist.companionCount();
            for (int j = 0; j < companionCount; j++) {
                companion = new Companion(station, train.getName() + "-departuring-passenger-" + i + "-companion-" + (j + 1), passenger);
                companion.setType(Person.Type.DEPARTURING_COMPANION);
                train.addNotifyListener(companion);
                companion.activateBefore(passenger);
                passenger.addCompanion(companion);
            }
            station.logger.changeThreads(Person.Type.DEPARTURING_COMPANION, companionCount);
        }

        station.logger.changeThreads(Person.Type.DEPARTURING_PASSENGER, count);
    }

    // returns a random integer number within <min, max>
    static public int rand(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
