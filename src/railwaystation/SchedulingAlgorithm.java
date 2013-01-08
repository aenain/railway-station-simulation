/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import railwaystation.infrastructure.Track;
import railwaystation.infrastructure.Train;

/**
 *
 * @author artur
 */
public interface SchedulingAlgorithm {
    Track getFreeTrackForTrain(Train train);
}
