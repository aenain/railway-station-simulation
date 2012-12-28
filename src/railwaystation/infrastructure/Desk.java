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
import railwaystation.TimeTable;
import railwaystation.person.Passenger;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class Desk extends SimProcess {
    protected Region enclosingRegion;
    protected ProcessQueue<Person> peopleToServe;
    protected Person servedPerson = null;
    protected boolean idle = false;
    protected String name;
    protected int complainCount = 0, soldTicketCount = 0, servedInfoCount = 0, lastStackedCount = 0;

    public Desk(Model owner, String name, Region enclosingRegion) {
        super(owner, name, true);
        this.name = name;
        this.enclosingRegion = enclosingRegion;
        peopleToServe = new ProcessQueue(owner, name, true, true);
    }

    public void addPerson(Person person) {
        person.setWaiting(true);
        person.setCurrentDesk(this);
        peopleToServe.insert(person);
        if (idle) { activate(); }
    }

    public void removePerson(Person person) {
        if (person.equals(servedPerson)) {
            servedPerson = null;
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
        person.activate();
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
        int currentCount = count();

        if (lastStackedCount != currentCount) {
            registerPeopleChange(currentCount);
            lastStackedCount = currentCount;
        }
    }

    private void registerPeopleChange(int count) {
        JSONObject data = new JSONObject();
        try {
            data.put("region", getName());
            data.put("count", count);
            enclosingRegion.station.registerVisualizationEvent("people-change", data);
        } catch(JSONException ex) {
            System.err.println("error building event: people-change");
        }
    }
}