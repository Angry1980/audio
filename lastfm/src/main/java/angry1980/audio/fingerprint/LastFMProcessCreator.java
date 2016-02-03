package angry1980.audio.fingerprint;

import angry1980.audio.model.FingerprintType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LastFMProcessCreator implements ProcessCalculator.ProcessCreator {

    @Override
    public ProcessBuilder create(File file) {
        List<String> params = new ArrayList<>();
        params.add("lastfm-fpclient");
        params.add(file.getAbsolutePath());
        return new ProcessBuilder().command(params);

    }

    @Override
    public FingerprintType getType() {
        return FingerprintType.LASTFM;
    }
}
