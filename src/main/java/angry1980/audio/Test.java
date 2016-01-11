package angry1980.audio;

import angry1980.audio.dao.*;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.FindSimilarTracks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@ComponentScan(value = {"angry1980.audio.config"})
public class Test {

//todo:
//similarity type - comparing, minhash, errorrates
// parameters for different implementations to props file
// track cluster id
//neo4j
// observable service
//last fm
//wavelet
//autotests
//logging
//process, process waiter refactoring

    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private List<FindSimilarTracks> findSimilarTracks;

    public static void main(String[] args){
        ApplicationContext context = new SpringApplication(Test.class).run(args);
        //todo: add shutdown hook
        Test test = context.getBean(Test.class);
        test.init();
        test.print();
    }

    public void init(){
        for(FindSimilarTracks handler : findSimilarTracks){
            trackDAO.getAll().stream()
                    .peek(track -> System.out.println(handler + " " + track))
                    .map(track -> handler.apply(track.getId()))
                    .collect(Collectors.toList());
        }
    }

    public void print(){
        trackDAO.getAll().stream()
                .peek(track -> System.out.println(track + " looks like"))
                .map(track -> trackSimilarityDAO.findByTrackId(track.getId()))
                .map(list -> list.orElseGet(() -> Collections.<TrackSimilarity>emptyList()))
                .flatMap(list -> list.stream()
                                    .collect(Collectors.groupingBy(ts -> ts.getTrack2()))
                                    .entrySet().stream()
                )
                .forEach(System.out::println)
        ;

    }
}
