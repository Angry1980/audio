package angry1980.audio;

import angry1980.audio.model.*;

public class Entities {

    private static FingerprintType defaultFingerprintType = FingerprintType.CHROMAPRINT;

    private Entities(){};

    public static TrackHash trackHash(long trackId, int time, int value){
        return ImmutableTrackHash.builder().trackId(trackId).time(time).hash(value).build();
    }

    public static TrackSimilarity trackSimilarity(long track1, long track2, int value){
        return trackSimilarity(track1, track2, defaultFingerprintType, value);
    }

    public static TrackSimilarity trackSimilarity(long track1, long track2, FingerprintType type, int value){
        return ImmutableTrackSimilarity.builder().track1(track1).track2(track2).fingerprintType(type).value(value).build();
    }

    public static Fingerprint hashFingerprint(long trackId, TrackHash ... hashes){
        return hashFingerprint(trackId, defaultFingerprintType, hashes);
    }

    public static Fingerprint hashFingerprint(long trackId, FingerprintType type, TrackHash ... hashes){
        return ImmutableFingerprint.builder().trackId(trackId).type(type).addHashes(hashes).build();
    }
}