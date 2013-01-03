/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.utilities;

import java.util.HashMap;
import railwaystation.RailwayStation;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public class Logger {
    public static enum Level { DEBUG, INFO, WARN, ERROR };

    private HashMap<Person.Type, Integer> threads = new HashMap();
    private boolean threadChanged = false;
    private Level level;

    public Logger(RailwayStation station, Level level) {
        this.level = level;
        threads.put(Person.Type.ARRIVING_PASSENGER, new Integer(0));
        threads.put(Person.Type.ARRIVING_COMPANION, new Integer(0));
        threads.put(Person.Type.DEPARTURING_PASSENGER, new Integer(0));
        threads.put(Person.Type.DEPARTURING_COMPANION, new Integer(0));
        threads.put(Person.Type.VISITOR, new Integer(0));
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public void changeThreads(Person.Type type, Integer change) {
        if (isSevereEnough(Level.DEBUG)) {
            Integer value = threads.get(type);
            threads.put(type, value + change);
            threadChanged = true;
        }
    }

    public void dumpThreads() {
        if (threadChanged && isSevereEnough(Level.DEBUG)) {
            System.err.println(threads);
            threadChanged = false;
        }
    }

    private boolean isSevereEnough(Level messageLevel) {
        return numericLevel(messageLevel) >= numericLevel(level);
    }

    public static int numericLevel(Level level) {
        int numericLevel = 0;

        switch(level) {
            case ERROR:
                numericLevel++;
            case WARN:
                numericLevel++;
            case INFO:
                numericLevel++;
        }

        return numericLevel;
    }
}
