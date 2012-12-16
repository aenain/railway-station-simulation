/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import java.util.ArrayList;
import railwaystation.RailwayStation;

/**
 *
 * @author artur
 */
public class Platform extends Region {
    protected ArrayList<Track> tracks;
    protected int number;

    public Platform(RailwayStation station, int number) {
        super(station, "platform-" + Integer.toString(number), Infrastructure.PLATFORM_CAPACITY);
        tracks = new ArrayList();
    }

    public void addTracks() {
        for (int i = 0; i < 2; i++) {
            tracks.add(new Track(this, i+1));
        }
    }

    // 1..tracks.count
    public Track getTrack(int i) {
        return tracks.get(i + 1);
    }
}
