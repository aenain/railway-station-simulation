/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.RailwayStation;
import railwaystation.person.Companion;
import railwaystation.person.Passenger;
import railwaystation.person.Person;
import railwaystation.person.Visitor;

/**
 *
 * @author artur
 * Klasa reprezentująca część infrastruktury dworca,
 * do której może wejść człowiek odwiedzający dworzec.
 */
public class Region extends SimProcess implements Visitable {
    protected ProcessQueue<Person> people;
    protected int companionCount, passengerCount, visitorCount;

    protected LinkedList<Region> adjacentRegions;
    protected String name;
    protected RailwayStation station;
    protected boolean peopleChanged;
    protected TimeSpan walkingTime;

    public Region(RailwayStation station, String name, Integer capacity) {
        super(station, name, true);
        this.name = name;
        people = new ProcessQueue(station, name + "-people", true, true);
        people.setQueueCapacity(capacity);
        adjacentRegions = new LinkedList();
        this.station = station;
        peopleChanged = false;
        walkingTime = new TimeSpan(1, TimeUnit.MINUTES);
    }

    @Override
    public void lifeCycle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWalkingTime(TimeSpan walkingTime) {
        this.walkingTime = walkingTime;
    }

    public TimeSpan getWalkingTime() {
        return walkingTime;
    }

    @Override
    public void personLeaves(Person person) {
        people.remove(person);
        changePeopleCount(person, -1);
    }

    @Override
    public void peopleLeave(LinkedList<Person> leavingPeople) {
        for (Person person : leavingPeople) {
            personLeaves(person);
        }
    }

    @Override
    public boolean canPersonEnter() {
        return canPeopleEnter(1);
    }

    @Override
    public boolean canPeopleEnter(int count) {
        return (people.size() + count) <= people.getQueueLimit();
    }

    @Override
    public void personEnters(Person person) {
        people.insert(person);
        changePeopleCount(person, 1);
    }

    @Override
    public void peopleEnter(LinkedList<Person> enteringPeople) {
        for (Person person : enteringPeople) {
            personEnters(person);
        }
    }

    public void bind(Region other) {
        adjacentRegions.add(other);
    }

    public LinkedList<Region> getAdjacentRegions() {
        return adjacentRegions;
    }

    @Override
    public String getName() {
        return name;
    }

    public int count() {
        return people.length();
    }

    public void stackPeopleChange() {
        if (peopleChanged) {
            registerPeopleChange();
            peopleChanged = false;
        }
    }

    protected void changePeopleCount(Person person, int change) {
        if (person instanceof Passenger) {
            passengerCount = Math.max(passengerCount + change, 0);
        } else if (person instanceof Companion) {
            companionCount = Math.max(companionCount + change, 0);
        } else if (person instanceof Visitor) {
            visitorCount = Math.max(visitorCount + change, 0);
        }
        peopleChanged = true;
    }

    private void registerPeopleChange() {
        JSONObject data = new JSONObject();
        try {
            data.put("region", getName());
            data.put("count", count());
            data.put("passengers", passengerCount);
            data.put("companions", companionCount);
            data.put("visitors", visitorCount);
            station.registerVisualizationEvent("people-change", data);
        } catch(JSONException ex) {
            System.err.println("error building event: people-change");
        }
    }
}
