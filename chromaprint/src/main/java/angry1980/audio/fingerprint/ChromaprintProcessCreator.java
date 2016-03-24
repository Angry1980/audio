package angry1980.audio.fingerprint;

import angry1980.audio.model.ComparingType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChromaprintProcessCreator implements ProcessCalculator.ProcessCreator{

    private Optional<File> pathToCommand;

    public ChromaprintProcessCreator(Optional<File> pathToCommand) {
        this.pathToCommand = Objects.requireNonNull(pathToCommand);
    }

    @Override
    public ProcessBuilder create(File file) {
        List<String> params = new ArrayList<>();
        params.add("fpcalc");
        params.add(file.getAbsolutePath());
        params.add("-length");
        params.add("1024");
        ProcessBuilder pb = new ProcessBuilder().command(params);
        pathToCommand.ifPresent(d -> pb.directory(d));
        return pb;
    }

    @Override
    public ComparingType getType() {
        return ComparingType.CHROMAPRINT;
    }

}
