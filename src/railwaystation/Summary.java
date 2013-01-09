/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.infrastructure.Desk;

/**
 *
 * @author KaMyLuS
 */
public class Summary {
    RailwayStation station;
    
    // dla kas
    private long soldTickets = 0;
    private long averCashWaitTime = 0;
    private long maxCashWaitTime = 0;
    private String maxCashWaitTimeAt = null; 
    private double averCashQueueLen = 0;
    private long maxCashQueueLen = 0;
    private String maxCashQueueLenAt = null;
    
    // dla informacji
    private long servedInfos = 0;
    private long complaints = 0;
    private long averInfoWaitTime = 0;
    private long maxInfoWaitTime = 0;
    private String maxInfoWaitTimeAt = null;
    private double averInfoQueueLen = 0;
    private long maxInfoQueueLen = 0;
    private String maxInfoQueueLenAt = null;
    
    // dla ludzi
    private long arrivPassengers = 0;
    private long deparTotalPassengers = 0;
    private long deparWithoutTicketPassengers = 0;
    private long missedTrainPassengers = 0;
    private long companions = 0;
    private long visitors = 0;
    
    // dla pociagow
    private long trains = 0;
    private long platformChanges = 0;
    private long sumSemapDelay = 0;
    private long sumPlatformDelay = 0;
    private long sumExternalDelay = 0;
    private long sumTotalDelay = 0;
    private long averSemapDelay = 0;
    private long averPlatformDelay = 0;
    private long averExternalDelay = 0;
    private long averTotalDelay = 0;
    private long maxSemapDelay = 0;
    private long maxPlatformDelay = 0;
    private long maxExternalDelay = 0;
    private long maxTotalDelay = 0;

    public Summary(RailwayStation station) {
        this.station = station;
    }
    
    public void computeCashStats() {
        long sumTime = 0, maxTime = 0, sumLen = 0, maxLen = 0;
        TimeInstant maxTimeAt = new TimeInstant(0);
        TimeInstant maxLenAt = new TimeInstant(0);
        
        for(Desk desk : station.getStructure().getCashDeskRegion().getDesks()) {
            sumTime += desk.getPeopleToServe().averageWaitTime().getTimeRounded(TimeUnit.SECONDS);
            if(desk.getPeopleToServe().maxWaitTime().getTimeRounded(TimeUnit.SECONDS) > maxTime) {
                maxTime = desk.getPeopleToServe().maxWaitTime().getTimeRounded(TimeUnit.SECONDS);
                maxTimeAt = desk.getPeopleToServe().maxWaitTimeAt();
            }
            
            sumLen += desk.getPeopleToServe().averageLength();
            if(desk.getPeopleToServe().maxLength() > maxLen) {
                maxLen = desk.getPeopleToServe().maxLength();
                maxLenAt = desk.getPeopleToServe().maxLengthAt();
            }
            
            soldTickets += desk.getSoldTicketCount();
        } 
        
        int desks = station.getStructure().getCashDeskRegion().getDesks().size();
        if(desks == 0) desks = -1;
        
        averCashWaitTime = sumTime/desks;
        maxCashWaitTime = maxTime;
        maxCashWaitTimeAt = TimeTable.timeToString(maxTimeAt, "minutes");
        
        averCashQueueLen = (double)sumLen/desks;
        maxCashQueueLen = maxLen;
        maxCashQueueLenAt = TimeTable.timeToString(maxLenAt, "minutes");
    }
    
    public void computeInformationStats() {
        long sumTime = 0, maxTime = 0, sumLen = 0, maxLen = 0;
        TimeInstant maxTimeAt = new TimeInstant(0);
        TimeInstant maxLenAt = new TimeInstant(0);
        
        for(Desk desk : station.getStructure().getInformationDeskRegion().getDesks()) {
            sumTime += desk.getPeopleToServe().averageWaitTime().getTimeRounded(TimeUnit.SECONDS);
            if(desk.getPeopleToServe().maxWaitTime().getTimeRounded(TimeUnit.SECONDS) > maxTime) {
                maxTime = desk.getPeopleToServe().maxWaitTime().getTimeRounded(TimeUnit.SECONDS);
                maxTimeAt = desk.getPeopleToServe().maxWaitTimeAt();
            }
            
            sumLen += desk.getPeopleToServe().averageLength();
            if(desk.getPeopleToServe().maxLength() > maxLen) {
                maxLen = desk.getPeopleToServe().maxLength();
                maxLenAt = desk.getPeopleToServe().maxLengthAt();
            } 
            
            servedInfos += desk.getServedInfoCount();
            complaints += desk.getComplainCount();
        }
        
        int desks = station.getStructure().getInformationDeskRegion().getDesks().size();
        if(desks == 0) desks = -1;
        
        averInfoWaitTime = sumTime/desks;
        maxInfoWaitTime = maxTime;
        maxInfoWaitTimeAt = TimeTable.timeToString(maxTimeAt, "minutes");
        
        averInfoQueueLen = (double)sumLen/desks;
        maxInfoQueueLen = maxLen;
        maxInfoQueueLenAt = TimeTable.timeToString(maxLenAt, "minutes");
    }
    
    public void addArrivingPassengersAndCompanions(int passengers, int companions) {
        arrivPassengers += passengers;
        this.companions += companions;
    }
    
    public void addDeparturingPassengersAndCompanions(int passengers, int companions) {
        deparTotalPassengers += passengers;
        this.companions += companions;
    }
    
    public void addDeparturingPassengersWithoutTickets(int count) {
        deparWithoutTicketPassengers += count;
    }
    
    public void addVisitors(int count) {
        visitors += count;
    }
    
    public void addTrain(int count, TimeSpan semaphoreDelay, TimeSpan platformDelay, TimeSpan externalDelay, TimeSpan totalDelay) {
        trains += count;
        
        if(semaphoreDelay != null) {
            sumSemapDelay += semaphoreDelay.getTimeRounded(TimeUnit.SECONDS);
            if(semaphoreDelay.getTimeRounded(TimeUnit.SECONDS) > maxSemapDelay)
                maxSemapDelay = semaphoreDelay.getTimeRounded(TimeUnit.SECONDS);
        }
        
        if(platformDelay != null) {
            sumPlatformDelay += Math.max(0, platformDelay.getTimeRounded(TimeUnit.SECONDS));
            if(platformDelay.getTimeRounded(TimeUnit.SECONDS) > maxPlatformDelay)
                maxPlatformDelay = platformDelay.getTimeRounded(TimeUnit.SECONDS);
        }
        
        if(externalDelay != null) {
            sumExternalDelay += externalDelay.getTimeRounded(TimeUnit.SECONDS);
            if(externalDelay.getTimeRounded(TimeUnit.SECONDS) > maxExternalDelay)
                maxExternalDelay = externalDelay.getTimeRounded(TimeUnit.SECONDS);
        }
        
        if(totalDelay != null) {
            sumTotalDelay += totalDelay.getTimeRounded(TimeUnit.SECONDS);
            if(totalDelay.getTimeRounded(TimeUnit.SECONDS) > maxTotalDelay)
                maxTotalDelay = totalDelay.getTimeRounded(TimeUnit.SECONDS);
        }
    }
    
    public void addPlatformChanges(int count) {
        platformChanges += count;
    }
    
    public void addPassengersMissedTrain(int count) {
        missedTrainPassengers += count;
    }
    
    public void computeTrainStats() {
        long count = (trains == 0 ? -1 : trains);
        averSemapDelay = sumSemapDelay/count;
        averPlatformDelay = sumPlatformDelay/count;
        averExternalDelay = sumExternalDelay/count;
        averTotalDelay = sumTotalDelay/count;
    }
    
    public void computeAll() {
        computeCashStats();
        computeInformationStats();
        computeTrainStats();
    }
    
    protected JSONObject prepareVisualizationSummary() {
        JSONObject visualizationSummary = new JSONObject();
        
        JSONObject data, trainsJS, delay, averageDelays, cashQueues, cashDesks, 
                passengers, arriving, depart;

        try {
            // cash desks
            cashDesks = new JSONObject();
            cashDesks.put("soldTickets", soldTickets);
            cashQueues = new JSONObject();
            
            data = new JSONObject();
            data.put("average", averCashWaitTime);
            data.put("max", maxCashWaitTime);
            data.put("maxAt", maxCashWaitTimeAt);
            cashQueues.put("waitingTime", data);
            
            data = new JSONObject();
            data.put("average", averCashQueueLen);
            data.put("max", maxCashQueueLen);
            data.put("maxAt", maxCashQueueLenAt);
            cashQueues.put("length", data);
            
            cashDesks.put("queues", cashQueues);
            visualizationSummary.put("cashDesks", cashDesks);
            
            // info desks
            cashDesks = new JSONObject();
            cashDesks.put("servedInformations", servedInfos);
            cashDesks.put("complaints", complaints);
            cashQueues = new JSONObject();
            
            data = new JSONObject();
            data.put("average", averInfoWaitTime);
            data.put("max", maxInfoWaitTime);
            data.put("maxAt", maxInfoWaitTimeAt);
            cashQueues.put("waitingTime", data);
            
            data = new JSONObject();
            data.put("average", averInfoQueueLen);
            data.put("max", maxInfoQueueLen);
            data.put("maxAt", maxInfoQueueLenAt);
            cashQueues.put("length", data);
            
            cashDesks.put("queues", cashQueues);
            visualizationSummary.put("infoDesks", cashDesks);
            
            // passengers
            passengers = new JSONObject();
            
            data = new JSONObject();
            data.put("total", arrivPassengers);
            passengers.put("arriving", data);
            
            data = new JSONObject();
            data.put("total", deparTotalPassengers);
            data.put("withoutTicket", deparWithoutTicketPassengers);
            data.put("missedTrain", missedTrainPassengers);
            passengers.put("departuring", data);
            
            visualizationSummary.put("passengers", passengers);
            visualizationSummary.put("companions", companions);
            visualizationSummary.put("visitors", visitors);
            
            // trains
            trainsJS = new JSONObject();
            trainsJS.put("count", trains);
            trainsJS.put("platformChanges", platformChanges);
            
            delay = new JSONObject();
            averageDelays = new JSONObject();
            averageDelays.put("semaphore", averSemapDelay);
            averageDelays.put("platform", averPlatformDelay);
            averageDelays.put("external", averExternalDelay);
            averageDelays.put("total", averTotalDelay);
            delay.put("average", averageDelays);
            
            averageDelays = new JSONObject();
            averageDelays.put("semaphore", maxSemapDelay);
            averageDelays.put("platform", maxPlatformDelay);
            averageDelays.put("external", maxExternalDelay);
            averageDelays.put("total", maxTotalDelay);
            delay.put("max", averageDelays);
            
            trainsJS.put("delay", delay);
            visualizationSummary.put("trains", trainsJS); 
            
        } catch (JSONException ex) {
            System.err.println("error preparing json summary");
        }
        
        return visualizationSummary;
    }
}
