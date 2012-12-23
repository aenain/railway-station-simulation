/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.DiscreteDistUniform;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;
import railwaystation.infrastructure.Train;
import railwaystation.person.Person;

/**
 *
 * @author artur
 * klasa odpowiedzialna za generowanie kolejnych wartości odpowiednich zmiennych
 */
public class Distribution {
    private final Configuration config;
    private final RailwayStation station;
    private DiscreteDistUniform comingSpanWithTicket, comingSpanWithoutTicket, companionComingSpan;
    private DiscreteDistUniform companionCount, externalDelay;
    private BoolDistBernoulli havingTicket, havingCompanion, havingExternalDelay;

    public Distribution(RailwayStation station) {
        this.config = station.config;
        this.station = station;
    }

    public void initStreams() {
        comingSpanWithTicket = new DiscreteDistUniform(station, "coming-span-with-ticket", config.minComingTimeWithTicket.getTimeRounded(TimeUnit.MINUTES), config.maxComingTimeWithTicket.getTimeRounded(TimeUnit.MINUTES), true, true);
        comingSpanWithoutTicket = new DiscreteDistUniform(station, "coming-span-without-ticket", config.minComingTimeWithoutTicket.getTimeRounded(TimeUnit.MINUTES), config.maxComingTimeWithoutTicket.getTimeRounded(TimeUnit.MINUTES), true, true);
        companionComingSpan = new DiscreteDistUniform(station, "companion-coming-span", config.minCompanionComingTime.getTimeRounded(TimeUnit.MINUTES), config.maxCompanionComingTime.getTimeRounded(TimeUnit.MINUTES), true, true);
        havingTicket = new BoolDistBernoulli(station, "having-ticket", config.havingTicketProbability / 100.0, true, true);
        havingCompanion = new BoolDistBernoulli(station, "having-companion", config.havingCompanionProbability / 100.0, true, true);
        companionCount = new DiscreteDistUniform(station, "companion-count", 1, config.maxCompanionCount, true, true);

        havingExternalDelay = new BoolDistBernoulli(station, "having-external-delay", config.externalDelayProbability / 100.0, true, true);
        externalDelay = new DiscreteDistUniform(station, "external-delay", config.minExternalDelay.getTimeRounded(TimeUnit.MINUTES), config.maxExternalDelay.getTimeRounded(TimeUnit.MINUTES), true, true);
    }

    public int companionCount() {
        long count = 0;

        if (havingCompanion.sample()) {
            count = companionCount.sample();
        }

        return (int)count;
    }

    public boolean hasTicket() {
        return havingTicket.sample();
    }

    public TimeSpan externalDelay() {
        if (! havingExternalDelay.sample()) {
            return TimeSpan.ZERO;
        }

        return externalDelay.sampleTimeSpan(TimeUnit.MINUTES);
    }

    public TimeInstant passengerComingTime(Train train, boolean hasTicket) {
        TimeInstant reference = train.getDepartureAt();
        TimeSpan difference;

        if (hasTicket) {
            difference = comingSpanWithTicket.sampleTimeSpan(TimeUnit.MINUTES);
        } else {
            difference = comingSpanWithoutTicket.sampleTimeSpan(TimeUnit.MINUTES);
        }

        return TimeOperations.subtract(reference, difference);
    }

    public TimeInstant companionComingTime(Train train, Person.Type type) {
        TimeSpan difference = companionComingSpan.sampleTimeSpan(TimeUnit.MINUTES);
        TimeInstant reference;

        switch(type) {
            case ARRIVING_COMPANION:
                reference = train.getArrivalAt();
                break;
            case DEPARTURING_COMPANION:
                reference = train.getDepartureAt();
                break;
            default:
                reference = null;
                break;
        }

        return TimeOperations.subtract(reference, difference);
    }

    public TimeInstant visitorComingTime() {
        // TODO!
        return null;
    }
}
