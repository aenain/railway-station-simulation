/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import railwaystation.infrastructure.Track;
import railwaystation.infrastructure.Train;

/**
 *
 * @author KaMyLuS
 */
public class WaitSchedulingAlgorithm implements SchedulingAlgorithm {

    @Override
    public Track getTrackForTrain(Train train) {
        return train.getTrack();
    }
    
}
