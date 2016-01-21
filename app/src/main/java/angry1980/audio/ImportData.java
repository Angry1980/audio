package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@Import(ImportDataConfig.class)
public class ImportData {

    @Autowired
    private DataImporter dataImporter;
    @Autowired
    private DataImporter.TrackDataEnvironment sourceEnvironment;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(ImportData.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "IMPORT"
        );
        ConfigurableApplicationContext context = sa.run(args);
        //todo: add shutdown hook
        context.getBean(ImportData.class).importData().print();
        ;
    }

    public ImportData importData(){
        dataImporter.importTo(sourceEnvironment);
        return this;
    }

    public void print(){
        getTracks()
                .peek(track -> System.out.println(track + " looks like"))
                .map(track -> sourceEnvironment.getTrackSimilarityDAO().findByTrackId(track.getId()))
                .map(list -> list.orElseGet(() -> Collections.<TrackSimilarity>emptyList()))
                .flatMap(list -> list.stream()
                        .collect(Collectors.groupingBy(ts -> ts.getTrack2()))
                        .entrySet().stream()
                )
                .forEach(System.out::println)
        ;

    }

    private Stream<Track> getTracks(){
        return sourceEnvironment.getTrackDAO().getAll().orElseGet(() -> Collections.emptyList()).stream();
    }

}
