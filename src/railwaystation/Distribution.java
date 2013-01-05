/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.dist.BoolDistBernoulli;
import desmoj.core.dist.DiscreteDistEmpirical;
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
    private DiscreteDistUniform sellingTicketTime, servingInformationTime;
    private DiscreteDistUniform externalDelay;
    private DiscreteDistEmpirical companionCount;
    private int[] visitorComingDist;
    private double[] crowdSpeedDist;
    private BoolDistBernoulli buyingTicket, gettingInformation, complaining, havingExternalDelay;

    public Distribution(RailwayStation station) {
        this.config = station.config;
        this.station = station;
    }

    public void initStreams() {
        comingSpanWithTicket = new DiscreteDistUniform(station, "coming-span-with-ticket", config.minComingTimeWithTicket.getTimeRounded(TimeUnit.MINUTES), config.maxComingTimeWithTicket.getTimeRounded(TimeUnit.MINUTES), true, true);
        comingSpanWithoutTicket = new DiscreteDistUniform(station, "coming-span-without-ticket", config.minComingTimeWithoutTicket.getTimeRounded(TimeUnit.MINUTES), config.maxComingTimeWithoutTicket.getTimeRounded(TimeUnit.MINUTES), true, true);
        companionComingSpan = new DiscreteDistUniform(station, "companion-coming-span", config.minCompanionComingTime.getTimeRounded(TimeUnit.MINUTES), config.maxCompanionComingTime.getTimeRounded(TimeUnit.MINUTES), true, true);
        buyingTicket = new BoolDistBernoulli(station, "buying-ticket", config.buyingTicketProbability, true, true);
        gettingInformation = new BoolDistBernoulli(station, "getting-information", config.gettingInformationProbability, true, true);
        complaining = new BoolDistBernoulli(station, "complaining", config.complainingProbability, true, true);
        companionCount = new DiscreteDistEmpirical(station, "companion-count", true, true);
        populateCompanionCountDist(config.companionCountDist);

        visitorComingDist = config.visitorComingDist;
        crowdSpeedDist = config.crowdSpeedDist;

        havingExternalDelay = new BoolDistBernoulli(station, "having-external-delay", config.externalDelayProbability, true, true);
        externalDelay = new DiscreteDistUniform(station, "external-delay", config.minExternalDelay.getTimeRounded(TimeUnit.MINUTES), config.maxExternalDelay.getTimeRounded(TimeUnit.MINUTES), true, true);
        sellingTicketTime = new DiscreteDistUniform(station, "selling-ticket-time", config.minSellingTicketTime.getTimeRounded(TimeUnit.MINUTES), config.maxSellingTicketTime.getTimeRounded(TimeUnit.MINUTES), true, true);
        servingInformationTime = new DiscreteDistUniform(station, "serving-information-time", config.minServingInformationTime.getTimeRounded(TimeUnit.MINUTES), config.maxServingInformationTime.getTimeRounded(TimeUnit.MINUTES), true, true);
    }

    public void populateCompanionCountDist(double[] frequencies) {
        for (int i = 0; i < frequencies.length; i++) {
            companionCount.addEntry(i, frequencies[i]);
        }
    }

    public int companionCount() {
        return companionCount.sample().intValue();
    }

    // peopleOnSquareMeter zawiera też osobę, dla której będziemy sprawdzać
    public double crowdSpeed(double peopleOnSquareMeter) {
        if (peopleOnSquareMeter >= crowdSpeedDist.length - 1) {
            return crowdSpeedDist[crowdSpeedDist.length - 1];
        }

        double x0 = Math.floor(peopleOnSquareMeter),
               x1 = Math.ceil(peopleOnSquareMeter),
               y0 = crowdSpeedDist[(int)x0],
               y1 = crowdSpeedDist[(int)x1];

        return linearInterpolation(x0, y0, x1, y1, peopleOnSquareMeter);
    }

    // zwraca liczbę visitorów, którzy w przeciągu 1h od zadanej godziny przyszli na dworzec
    public int hourlyVisitorCount(int startHour) {
        // tablica zawiera liczbę ludzi w przedziałach dwugodzinowych
        return Math.round(visitorComingDist[(int)Math.floor(startHour/2)] / 2);
    }

    public boolean buysTicket() {
        return buyingTicket.sample();
    }

    public boolean needsInformation() {
        return gettingInformation.sample();
    }

    public boolean complains() {
        return complaining.sample();
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

    public TimeSpan sellingTicketTime() {
        return sellingTicketTime.sampleTimeSpan(TimeUnit.MINUTES);
    }

    public TimeSpan servingInformationTime() {
        return servingInformationTime.sampleTimeSpan(TimeUnit.MINUTES);
    }

    public double linearInterpolation(double x0, double y0, double x1, double y1, double x) {
        if (x1 == x0) {
            return y0;
        }

        double a = (y1 - y0) / (x1 - x0),
               b = y0 - a*x0;

        return a*x + b;
    }
}
