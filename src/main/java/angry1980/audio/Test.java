package angry1980.audio;

import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.ChromaprintErrorRatesCalculator;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.utils.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Test {

//todo:
//similarity type - comparing, minhash, errorrates
// spring config + props
// track cluster id
//neo4j
// observable service
//last fm
//wavelet
//autotests
//logging
//process, process waiter refactoring

    private static final String musicDir = "C:\\music";

    public static void main(String[] args){
        String dir = musicDir;
        if(args != null && args.length > 0){
            dir = args[0];
        }
        List<Path> files = FileUtils.getFiles(Paths.get(dir), ".mp3");
        TrackSimilarityDAO trackSimilarityDAO = new TrackSimilarityDaoInMemoryImpl();
        //chromaprint(files, trackSimilarityDAO);
        shazam(files, trackSimilarityDAO);
        LongStream.range(0, files.size())
                .peek(trackId -> System.out.println(files.get((int) trackId).toString() + " looks like"))
                .mapToObj(trackSimilarityDAO::findByTrackId)
                .map(list -> list.orElseGet(() -> Collections.<TrackSimilarity>emptyList()))
                .flatMap(list -> list.stream()
                                    .collect(Collectors.groupingBy(ts -> ts.getTrack2()))
                                    .entrySet().stream()
                )
                .forEach(System.out::println)
        ;

    }

    private static void chromaprint(List<Path> files, TrackSimilarityDAO trackSimilarityDAO){
        FingerprintDAO fingerprintDAO = new FingerprintDAOInMemoryImpl<>();
        GetOrCreateFingerprint getOrCreateFingerprint = new GetOrCreateFingerprint(
                fingerprintDAO,
                new TrackDAOFileImpl(files),
                new ChromaprintCalculator(),
                new ChromaprintInvertedIndex(new ChromaprintHashDAOInMemoryImpl())
        );
        FindSimilarTracks findSimilarTracks = new FindSimilarTracks(
                trackSimilarityDAO,
                getOrCreateFingerprint,
                new ChromaprintErrorRatesCalculator(fingerprintDAO),
                FingerprintType.CHROMAPRINT
        );
        LongStream.range(0, files.size())
                .peek(trackId -> System.out.println("Chromaprint " + files.get((int) trackId).toString()))
                .mapToObj(findSimilarTracks)
                .collect(Collectors.toList());

    }

    private static void shazam(List<Path> files, TrackSimilarityDAO trackSimilarityDAO){
        PeaksInvertedIndex invertedIndex = new PeaksInvertedIndex(new PeakDAOInMemoryImpl());
        GetOrCreateFingerprint getOrCreateFingerprint = new GetOrCreateFingerprint(
                new FingerprintDAOFakeImpl(),
                new TrackDAOFileImpl(files),
                new PeaksCalculator(new LocalAdapter()),
                invertedIndex
        );
        FindSimilarTracks findSimilarTracks = new FindSimilarTracks(
                trackSimilarityDAO,
                getOrCreateFingerprint,
                invertedIndex,
                FingerprintType.PEAKS
        );
        LongStream.range(0, files.size())
                .peek(trackId -> System.out.println("Shazam " + files.get((int) trackId).toString()))
                .mapToObj(findSimilarTracks)
                .collect(Collectors.toList());

    }
}
