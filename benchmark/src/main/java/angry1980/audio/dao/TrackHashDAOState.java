package angry1980.audio.dao;

import angry1980.audio.model.ImmutableTrackHash;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Random;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class TrackHashDAOState {

    private static Random RND = new Random();

    public TrackHashDAO dao;
    private int trackSize = 2000;
    public long[][] hashes;
    public int hashCount = 10_000_000;

    public TrackHashDAOState(){
        dao = new TrackHashDAOInMemoryImpl(hashCount, -16);
        hashes = IntStream.range(0, hashCount/trackSize) //list of tracks
                        .mapToObj(track -> RND.longs(trackSize).toArray())
                            .toArray(long[][]::new);
        IntStream.range(0, hashes.length) //list of tracks
                .forEach(track -> {
                    IntStream.range(0, hashes[track].length)
                            .mapToObj(time -> ImmutableTrackHash.builder().trackId(track).time(time).hash(hashes[track][time]).build())
                            .forEach(dao::create);
                });
    }

}
