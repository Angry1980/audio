package angry1980.audio.fingerprint;

import angry1980.audio.model.ComparingType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChromaprintProcessCreator implements ProcessCalculator.ProcessCreator{

    @Override
    public ProcessBuilder create(File file) {
        List<String> params = new ArrayList<>();
        params.add("fpcalc");
        params.add(file.getAbsolutePath());
        params.add("-length");
        params.add("1024");
        return new ProcessBuilder().command(params).directory(new File("C:\\utils\\chromaprint"));
    }

    @Override
    public ComparingType getType() {
        return ComparingType.CHROMAPRINT;
    }

}
