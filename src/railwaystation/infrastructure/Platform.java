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
        super(station, "platform-" + Integer.toString(number), Infrastructure.MAX_CAPACITY);
        this.tracks = new ArrayList();
        this.number = number;
    }

    public void buildTracks() {
        for (int i = 1; i <= 2; i++) {
            tracks.add(new Track(this, i));
        }
    }

    // 1..tracks.count
    public Track getTrack(int i) {
        if (i == 0) { i = 1; }
        return tracks.get(i - 1);
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}
