package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

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
                FingerprintType.LASTFM.name()
        );
        ConfigurableApplicationContext context = sa.run(args);
        //todo: add shutdown hook
        context.getBean(ImportData.class).importData();
    }

    public void importData(){
        dataImporter.importTo(sourceEnvironment);
    }

}
