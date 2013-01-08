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
    
    protected static final int MAX_ACTIVITIES = 3;
    
    public Visitor(RailwayStation station, String name) {
        super(station, name);
    }
    
    @Override
    public void createScenario() {
        
        futureActivities.add(Activity.Type.ENTER_STATION);
        
        int activitiesCount = Generator.rand(0, MAX_ACTIVITIES);
        for(int i = 0; i < activitiesCount; i++) {
            int nextActivityNo = Generator.rand(0, 1);
            switch(nextActivityNo) {
                case 0:
                    futureActivities.add(Activity.Type.BUY_TICKET);
                    break;
                case 1:
                    futureActivities.add(Activity.Type.GET_INFO);
                    break; 
            }
        }
        
        futureActivities.add(Activity.Type.LEAVE_STATION);
    }
    
}
