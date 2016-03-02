package angry1980.audio.model;

public enum ComparingType {
    CHROMAPRINT(FingerprintType.CHROMAPRINT),
    CHROMAPRINT_ER(FingerprintType.CHROMAPRINT, SimilarityType.ERROR_RATE),
    PEAKS(FingerprintType.PEAKS),
    LASTFM(FingerprintType.LASTFM),
    LASTFM_ER(FingerprintType.LASTFM, SimilarityType.ERROR_RATE),
    ;

    private FingerprintType fingerprintType;
    private SimilarityType similarityType;

    ComparingType(FingerprintType fingerprintType) {
        this(fingerprintType, SimilarityType.MASKED);
    }

    ComparingType(FingerprintType fingerprintType, SimilarityType similarityType) {
        this.fingerprintType = fingerprintType;
        this.similarityType = similarityType;
    }

    public FingerprintType getFingerprintType() {
        return fingerprintType;
    }

    public SimilarityType getSimilarityType() {
        return similarityType;
    }

}
