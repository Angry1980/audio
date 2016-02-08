package angry1980.audio.dao;

import angry1980.audio.model.ImmutableTrackHash;
import angry1980.audio.model.TrackHash;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrackHashDAOInMemoryImplTest {

    private static Random RND = new Random();

    private TrackHashDAOInMemoryImpl dao;

    @Before
    public void init(){
        dao = new TrackHashDAOInMemoryImpl();
    }

    @Test
    public void testCreation() {
        TrackHash original = createTrackHash(10, 1, 1);
        Optional<TrackHash> result = dao.create(original);
        assertTrue(result.isPresent());
        assertTrue(original.equals(result.get()));
    }

    @Test
    public void testCreationOfEmptyHash() {
        assertFalse(dao.create(null).isPresent());
    }

    @Test
    public void testSearching(){
        dao.create(createTrackHash(1, 1, 1));
        dao.create(createTrackHash(1, 2, 1));
        dao.create(createTrackHash(1, 3, 10));
        dao.create(createTrackHash(1, 4, 10));
        dao.create(createTrackHash(2, 1, 1));
        dao.create(createTrackHash(2, 2, 1));
        dao.create(createTrackHash(2, 3, 10));
        dao.create(createTrackHash(2, 4, 3));
        checkSearchingResult(dao.findByHash(1),
                createTrackHash(1, 1, 1),
                createTrackHash(1, 2, 1),
                createTrackHash(2, 1, 1),
                createTrackHash(2, 2, 1)
        );
        checkSearchingResult(dao.findByHash(10),
                createTrackHash(1, 3, 10),
                createTrackHash(1, 4, 10),
                createTrackHash(2, 3, 10)
        );
        checkSearchingResult(dao.findByHash(3),
                createTrackHash(2, 4, 3)
        );
    }

    @Test
    public void testWithMask(){
        dao = new TrackHashDAOInMemoryImpl(-16);
        dao.create(createTrackHash(1, 1, 1));
        dao.create(createTrackHash(1, 2, 8));
        dao.create(createTrackHash(1, 3, 128));
        dao.create(createTrackHash(1, 4, 136));
        checkSearchingResult(dao.findByHash(4),
                createTrackHash(1, 1, 1),
                createTrackHash(1, 2, 8)
        );
        checkSearchingResult(dao.findByHash(132),
                createTrackHash(1, 3, 128),
                createTrackHash(1, 4, 136)
        );

    }
    @Test
    public void testWithZeroMask(){
        dao = new TrackHashDAOInMemoryImpl(0);
        RND.ints(10).forEach(v -> dao.create(createTrackHash(1, 1, v)));
        RND.ints(10).forEach(v -> assertTrue(dao.findByHash(v).size() == 10));
    }

    private void checkSearchingResult(List<TrackHash> result, TrackHash ... hashes){
        assertTrue(result.size() == hashes.length);
        Arrays.stream(hashes).forEach(hash -> assertTrue(result.contains(hash)));
    }

    private TrackHash createTrackHash(long trackId, int time, int value){
        return ImmutableTrackHash.builder().trackId(trackId).time(time).hash(value).build();
    }
}
