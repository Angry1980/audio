package angry1980.audio.fingerprint;

import angry1980.audio.Entities;
import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.dao.TrackHashDAOInMemoryImpl;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.TrackHash;
import angry1980.audio.model.TrackSimilarity;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashInvertedIndexTest {

    private TrackHashDAO dao;
    private HashInvertedIndex index;
    private Fingerprint fingerprint;

    @Before
    public void init(){
        dao = new TrackHashDAOInMemoryImpl();
        index = new HashInvertedIndex(0.34, 0.68, dao);
        fingerprint = Entities.hashFingerprint(1,
                Entities.trackHash(1, 1, 1),
                Entities.trackHash(1, 2, 1),
                Entities.trackHash(1, 3, 1)
        );
    }

    @Test
    public void testEmptyHashDAO(){
        List<TrackSimilarity> result = index.calculate(fingerprint, Entities.defaultComparingType);
        assertNotNull(result);
        assertTrue(result.size() == 0);
    }

    @Test
    public void testSourceTrack(){
        fillDAO(dao, Entities.trackHash(1, 1, 1), Entities.trackHash(1, 2, 1), Entities.trackHash(2, 1, 1));
        index.calculate(fingerprint, Entities.defaultComparingType).stream()
                .forEach(ts -> assertFalse(ts.getTrack2() == 1));

    }

    @Test
    public void testWeightLimit(){
        fillDAO(dao, Entities.trackHash(2, 1, 1), Entities.trackHash(3, 1, 1), Entities.trackHash(3, 2, 1));
        List<TrackSimilarity> result = index.calculate(fingerprint, Entities.defaultComparingType);
        assertNotNull(result);
        assertTrue(result.size() == 0);
    }

    @Test
    public void testResult1(){
        fillDAO(dao, Entities.trackHash(2, 1, 1), Entities.trackHash(2, 2, 1), Entities.trackHash(2, 3, 1));
        checkResult(
                index.calculate(fingerprint, Entities.defaultComparingType),
                Entities.trackSimilarity(1, 2, 3),
                3
        );
    }

    @Test
    public void testResult2(){
        fillDAO(dao, Entities.trackHash(2, 1, 1), Entities.trackHash(2, 2, 1), Entities.trackHash(2, 4, 1));
        List<TrackSimilarity> result = index.calculate(fingerprint, Entities.defaultComparingType);
        assertNotNull(result);
        assertTrue(result.size() == 0);
    }

    @Test
    public void testResult3(){
        fillDAO(dao, Entities.trackHash(2, 1, 1), Entities.trackHash(2, 2, 1), Entities.trackHash(2, 4, 1), Entities.trackHash(2, 5, 1));
        List<TrackSimilarity> result = index.calculate(fingerprint, Entities.defaultComparingType);
        checkResult(
                index.calculate(fingerprint, Entities.defaultComparingType),
                Entities.trackSimilarity(1, 2, 4),
                4
        );
    }

    @Test
    public void testResult4(){
        fillDAO(dao, Entities.trackHash(2, 1, 1), Entities.trackHash(2, 2, 1), Entities.trackHash(2, 3, 1));
        fillDAO(dao, Entities.trackHash(3, 1, 1), Entities.trackHash(3, 2, 1), Entities.trackHash(3, 3, 1));
        List<TrackSimilarity> result = index.calculate(fingerprint, Entities.defaultComparingType);
        checkResult(result, Entities.trackSimilarity(1, 2, 3), 3);
        checkResult(result, Entities.trackSimilarity(1, 3, 3), 3);
    }

    private void checkResult(List<TrackSimilarity> result, TrackSimilarity ts, int value){
        int index = result.indexOf(ts);
        assertTrue(index >= 0);
        assertTrue(result.get(index).getValue() == value);

    }

    private void fillDAO(TrackHashDAO dao, TrackHash... hashes){
        Arrays.stream(hashes).forEach(dao::create);
    }
}
