/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import railwaystation.RailwayStation;

/**
 *
 * @author artur
 */
public class Visitor extends Person {
    
    protected static final int MAX_ACTIVITIES = 5;
    
    public Visitor(RailwayStation station, String name) {
        super(station, name, null);
    }
    
    @Override
    public void createScenario() {
        
        int activitiesCount = Generator.rand(0, MAX_ACTIVITIES);
        Activity lastActivity = new Activity(this, null);
        for(int i = 0; i < activitiesCount; i++) {
            int nextActivityNo = Generator.rand(0, 2);
            switch(nextActivityNo) {
                case 0:
                    futureActivities.add(Activity.Type.BUY_TICKET);
                    lastActivity.setType(Activity.Type.BUY_TICKET);
                    break;
                case 1:
                    futureActivities.add(Activity.Type.COMPLAIN);
                    lastActivity.setType(Activity.Type.COMPLAIN);
                    break; 
                case 2:
                    futureActivities.add(Activity.Type.GET_INFO);
                    lastActivity.setType(Activity.Type.GET_INFO);
                    break; 
            }
        }
        
        futureActivities.add(Activity.Type.LEAVE_STATION);
    }
    
}
