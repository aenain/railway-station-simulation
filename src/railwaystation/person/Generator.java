/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import java.util.Random;
import railwaystation.RailwayStation;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public class Generator {
    private RailwayStation station;
    private Random random;

    public Generator(RailwayStation station) {
        this.station = station;
        random = new Random(System.currentTimeMillis());
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
                companion.activate(station.dist.companionComingTime(train, Person.Type.ARRIVING_COMPANION));
                passenger.addCompanion(companion);
            }
            train.addPassenger(passenger);
        }
    }

    private void generateDeparturing(Train train, int count) {
        Passenger passenger;
        Companion companion;
        int companionCount;
        boolean hasTicket;

        for (int i = 1; i <= count; i++) {
            passenger = new Passenger(station, train.getName() + "-departuring-passenger-" + i, train);
            passenger.setType(Person.Type.DEPARTURING_PASSENGER);
            companionCount = station.dist.companionCount();
            for (int j = 0; j < companionCount; j++) {
                companion = new Companion(station, train.getName() + "-departuring-passenger-" + i + "-companion-" + (j + 1), passenger);
                companion.setType(Person.Type.DEPARTURING_COMPANION);
                passenger.addCompanion(companion);
            }
            hasTicket = station.dist.hasTicket();
            passenger.activate(station.dist.passengerComingTime(train, hasTicket));
        }
    }

    // returns a random integer number within <min, max>
    private int rand(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
