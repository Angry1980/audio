package angry1980.audio.fingerprint;

import angry1980.audio.Entities;
import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.model.TrackSimilarity;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashInvertedIndexTest {

    private TrackHashDAO dao;
    private HashInvertedIndex index;
    private HashFingerprint fingerprint;

    @Before
    public void init(){
        dao = mock(TrackHashDAO.class);
        index = new HashInvertedIndex(dao, 1, 1);
        fingerprint = Entities.hashFingerprint(1,
                Entities.trackHash(1, 1, 1),
                Entities.trackHash(1, 2, 1),
                Entities.trackHash(1, 3, 1)
        );
    }

    @Test
    public void testEmptyHashDAO(){
        when(dao.findByHash(anyInt())).thenReturn(Collections.emptyList());
        List<TrackSimilarity> result = index.calculate(fingerprint);
        assertNotNull(result);
        assertTrue(result.size() == 0);
    }

    @Test
    public void testSourceTrack(){
        when(dao.findByHash(anyInt())).thenReturn(Arrays.asList(
                Entities.trackHash(2, 1, 1),
                Entities.trackHash(2, 2, 1)
        ));
        index.calculate(fingerprint).stream()
                .forEach(ts -> assertFalse(ts.getTrack2() == 1));

    }

    @Test
    public void testEmptyResult(){
        when(dao.findByHash(1)).thenReturn(Arrays.asList(
                Entities.trackHash(2, 1, 1)
        ));
        List<TrackSimilarity> result = index.calculate(fingerprint);
        assertNotNull(result);
        assertTrue(result.size() == 0);
    }

    @Test
    public void testResult1(){
        when(dao.findByHash(1)).thenReturn(Arrays.asList(
                Entities.trackHash(2, 1, 1),
                Entities.trackHash(2, 2, 1)
        ));
        checkResult(
                index.calculate(fingerprint),
                Entities.trackSimilarity(1, 2, 2),
                2
        );
    }

    @Test
    public void testResult2(){
        when(dao.findByHash(1)).thenReturn(Arrays.asList(
                Entities.trackHash(2, 1, 1),
                Entities.trackHash(2, 3, 1)
        ));
        List<TrackSimilarity> result = index.calculate(fingerprint);
        assertNotNull(result);
        assertTrue(result.size() == 0);
    }

    private void checkResult(List<TrackSimilarity> result, TrackSimilarity ts, int value){
        int index = result.indexOf(ts);
        assertTrue(index >= 0);
        assertTrue(result.get(index).getValue() == value);

    }
}
