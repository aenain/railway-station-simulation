/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import java.util.ArrayList;
import railwaystation.infrastructure.Platform;
import railwaystation.infrastructure.Track;
import railwaystation.infrastructure.Train;

/**
 *
 * @author KaMyLuS
 */
public class FirstFreeSchedulingAlgorithm implements SchedulingAlgorithm{
    
    private ArrayList<Platform> platforms = new ArrayList<Platform>();
    static int count = 0;

    public FirstFreeSchedulingAlgorithm(ArrayList<Platform> platforms) {
        this.platforms = platforms;
    }

    public FirstFreeSchedulingAlgorithm() {
    }
    
    @Override
    public Track getTrackForTrain(Train train) {
        if(platforms.isEmpty()) {
            platforms = train.getStation().getStructure().getPlatforms();
        }
        
        if(train.getTrack().isEmpty()) {
            return train.getTrack();
        }
        
        for(Platform plat : platforms) {
            if(plat.getTrack(0).isEmpty()) {
                return plat.getTrack(0);
            }
        }
        
        if(platforms.get(platforms.size()-1).getTrack(1).isEmpty()) {
            return platforms.get(platforms.size()-1).getTrack(1);
        }
        return train.getTrack();
    }
    
}
