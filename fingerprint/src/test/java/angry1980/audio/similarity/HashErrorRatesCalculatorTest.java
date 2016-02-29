package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashErrorRatesCalculatorTest {

    private Track firstTrack;
    private Fingerprint firstTrackFingerprint;
    private int[] firstTrackFingerprintHashes;
    private TrackDAO trackDAO;
    private FingerprintDAO fingerprintDAO;
    private HashErrorRatesCalculator calculator;

    @Before
    public void init(){
        trackDAO = mock(TrackDAO.class);
        fingerprintDAO = mock(FingerprintDAO.class);
        firstTrack = track(1, 1);
        firstTrackFingerprintHashes = new int[]{
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
        };
        firstTrackFingerprint = fingerprint(1, firstTrackFingerprintHashes);
        calculator = new HashErrorRatesCalculator(
                FingerprintType.CHROMAPRINT,
                new HashErrorRatesCalculatorTrackSourceImpl(trackDAO),
                fingerprintDAO,
                25,
                8
        );
    }

    @Test
    public void testEmptySourceResult(){
        when(trackDAO.findByCluster(1)).thenReturn(Collections.emptyList());
        checkEmptyResult(calculator.calculate(firstTrackFingerprint));
    }

    @Test
    public void testTrackSourceResult(){
        setFilterResult(1, firstTrack);
        checkEmptyResult(calculator.calculate(firstTrackFingerprint));
    }

    @Test
    public void testEmptyFingerprintResult(){
        Track another = track(2, 1);
        setFilterResult(1, firstTrack, another);
        checkEmptyResult(calculator.calculate(firstTrackFingerprint));
    }

    @Test
    public void testEqualFingerprints(){
        Track another = track(2, 1);
        fingerprint(2, firstTrackFingerprintHashes);
        setFilterResult(1, firstTrack, another);
        checkResult(calculator.calculate(firstTrackFingerprint), 1,
                    ImmutableTrackSimilarity.builder()
                            .track1(1)
                            .track2(2)
                            .value(firstTrackFingerprintHashes.length)
                            .fingerprintType(FingerprintType.CHROMAPRINT)
                                .build()
                );
    }

    @Test
    public void testSameHashesLimit(){
        Track another = track(2, 1);
        int[] anotherHashes = new int[19];
        System.arraycopy(firstTrackFingerprintHashes, 0, anotherHashes, 0, 19);
        Fingerprint anotherFingerprint = fingerprint(2, anotherHashes);
        setFilterResult(1, firstTrack, another);
        checkEmptyResult(calculator.calculate(anotherFingerprint));
    }

    @Test
    public void testErrorBitCountLimit(){
        //8 different bits
        Track another = track(2, 1);
        int[] anotherHashes = Arrays.stream(firstTrackFingerprintHashes)
                .map(hash -> hash ^ 255)
                .toArray();
        Fingerprint anotherFingerprint = fingerprint(2, anotherHashes);
        setFilterResult(1, firstTrack, another);
        checkResult(calculator.calculate(firstTrackFingerprint), 1,
                ImmutableTrackSimilarity.builder()
                        .track1(1)
                        .track2(2)
                        .value(firstTrackFingerprintHashes.length)
                        .fingerprintType(FingerprintType.CHROMAPRINT)
                        .build());
        //9 different bits
        anotherHashes = Arrays.stream(firstTrackFingerprintHashes)
                .map(hash -> hash ^ 511)
                .toArray();
        when(anotherFingerprint.getHashes()).thenReturn(hashes(2, anotherHashes));
        checkEmptyResult(calculator.calculate(firstTrackFingerprint));
    }

    private List<TrackSimilarity> checkResult(List<TrackSimilarity> result, int length, TrackSimilarity ... expected){
        assertNotNull(result);
        assertTrue(result.size() == length);
        Arrays.stream(expected)
                .forEach(ts -> assertTrue(result.contains(ts)));
        return result;
    }

    private List<TrackSimilarity> checkEmptyResult(List<TrackSimilarity> result){
        assertNotNull(result);
        assertTrue(result.size() == 0);
        return result;
    }

    private void setFilterResult(long cluster, Track ... tracks){
        when(trackDAO.findByCluster(cluster)).thenReturn(Arrays.asList(tracks));
    }

    private void setFingerprintDAOResult(long[] tracks, Fingerprint ... fingerprints){
        when(fingerprintDAO.findByTrackIds(eq(tracks))).thenReturn(Arrays.asList(fingerprints));
    }

    private Track track(long id, long cluster){
        Track track = mock(Track.class);
        when(track.getId()).thenReturn(id);
        when(track.getCluster()).thenReturn(cluster);
        when(trackDAO.get(id)).thenReturn(Optional.of(track));
        return track;
    }

    private Fingerprint fingerprint(long trackId, int[] hashes){
        Fingerprint fingerprint = mock(Fingerprint.class);
        when(fingerprint.getTrackId()).thenReturn(trackId);
        when(fingerprint.getHashes()).thenReturn(hashes(trackId, hashes));
        when(fingerprintDAO.findByTrackId(trackId)).thenReturn(Optional.of(fingerprint));
        setFingerprintDAOResult(new long[]{trackId}, fingerprint);
        return fingerprint;
    }

    private List<TrackHash> hashes(long trackId, int[] hashes){
        AtomicInteger counter = new AtomicInteger();
        return Arrays.stream(hashes)
                .mapToObj(hash -> ImmutableTrackHash.builder().hash(hash).time(counter.getAndIncrement()).trackId(trackId).build())
                .collect(Collectors.toList());
    }
}
