/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;
import org.json.JSONException;
import org.json.JSONObject;
import railwaystation.person.Companion;
import railwaystation.person.Passenger;
import railwaystation.person.Person;
import railwaystation.person.Visitor;

/**
 *
 * @author artur
 */
public class Desk extends SimProcess {
    protected Region enclosingRegion;
    protected ProcessQueue<Person> peopleToServe;
    protected Person servedPerson = null;
    protected boolean idle = false, peopleChanged = false;
    protected String name;
    protected int complainCount = 0, soldTicketCount = 0, servedInfoCount = 0;
    protected int companionCount = 0, passengerCount = 0, visitorCount = 0;

    public Desk(Model owner, String name, Region enclosingRegion) {
        super(owner, name, true);
        this.name = name;
        this.enclosingRegion = enclosingRegion;
        peopleToServe = new ProcessQueue(owner, name, true, true);
        setSchedulingPriority(1);
    }

    public void addPerson(Person person) {
        person.setWaiting(true);
        person.setCurrentDesk(this);
        peopleToServe.insert(person);
        changePeopleCount(person, 1);
        if (idle) { activate(); }
    }

    public void removePerson(Person person) {
        changePeopleCount(person, -1);
        if (person.equals(servedPerson)) {
            servedPerson = null;
            cancel();
            activate();
        } else {
            peopleToServe.remove(person);
        }
        person.setWaiting(false);
        person.setCurrentDesk(null);
    }

    public int getWaitingCount() {
        return peopleToServe.size();
    }

    public ProcessQueue<Person> getPeopleToServe() {
        return peopleToServe;
    }

    public int getCompanionCount() {
        return companionCount;
    }

    public int getComplainCount() {
        return complainCount;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public boolean isPeopleChanged() {
        return peopleChanged;
    }

    public int getServedInfoCount() {
        return servedInfoCount;
    }

    public Person getServedPerson() {
        return servedPerson;
    }

    public int getSoldTicketCount() {
        return soldTicketCount;
    }

    public int getVisitorCount() {
        return visitorCount;
    }

    protected void serve(Person person) {
        switch(person.getCurrentActivityType()) {
            case COMPLAIN:
                complainCount++;
                break;
            case BUY_TICKET:
                if (person instanceof Passenger) {
                    ((Passenger)person).setTicketPossession(true);
                }
                soldTicketCount++;
                break;
            case GET_INFO:
                servedInfoCount++;
                break;
        }
        person.setWaiting(false);
        person.setCurrentDesk(null);
        person.cancel(); // removes all next scheduled events.
        person.activateAfter(this);
    }

    /*
     * @param change int -1|1 - decrement or increment
     */
    protected void changePeopleCount(Person person, int change) {
        if (person instanceof Passenger) {
            passengerCount = Math.max(passengerCount + change, 0);
            // when a passenger waits in the queue, companions stay with him but are not getting served.
            companionCount = Math.max(companionCount + change * ((Passenger)person).getFollowers().size(), 0);
        } else if (person instanceof Companion) {
            companionCount = Math.max(companionCount + change, 0);
        } else if (person instanceof Visitor) {
            visitorCount = Math.max(visitorCount + change, 0);
        }
        peopleChanged = true;
    }

    @Override
    public void lifeCycle() {
        while (true) {
            if (peopleToServe.isEmpty()) {
                idle = true;
                passivate();
                idle = false;
            }
            if (! peopleToServe.isEmpty()) {
                servedPerson = peopleToServe.removeFirst();
                hold(servingTime());
                if (servedPerson != null) {
                    changePeopleCount(servedPerson, -1);
                    serve(servedPerson);
                    servedPerson = null;
                }
            }
        }
    }

    protected TimeSpan servingTime() {
        return enclosingRegion.station.dist.servingInformationTime();
    }

    public int count() {
        int count = peopleToServe.size();
        if (servedPerson != null) {
            count++;
        }
        return count;
    }

    @Override
    public String getName() {
        return name;
    }

    public void stackPeopleChange() {
        if (peopleChanged) {
            registerPeopleChange();
            peopleChanged = false;
        }
    }

    private void registerPeopleChange() {
        JSONObject data = new JSONObject();
        try {
            data.put("region", getName());
            data.put("count", count());
            data.put("passengers", passengerCount);
            data.put("companions", companionCount);
            data.put("visitors", visitorCount);
            enclosingRegion.station.registerVisualizationEvent("people-change", data);
        } catch(JSONException ex) {
            System.err.println("error building event: people-change");
        }
    }
}