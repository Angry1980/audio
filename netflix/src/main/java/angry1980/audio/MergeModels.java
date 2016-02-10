package angry1980.audio;

import angry1980.audio.dao.*;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import com.google.common.collect.ImmutableMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MergeModels {

    public static void main(String[] args){
        new MergeModels(
                ImmutableMap.of(
                        FingerprintType.PEAKS, Paths.get("C://work//ts.local.peaks.data")
                ),
                Paths.get("C://work//merged.data")
        ).init()
        .save();
    }

    private Map<FingerprintType, Path> models;
    private Path resultModel;
    private Map<Path, NetflixData> cached;
    private NetflixData data;
    private TrackDAO trackDAO;
    private AtomicBoolean inited;

    public MergeModels(Map<FingerprintType, Path> models, Path resultModel) {
        this.models = models;
        this.cached = new HashMap<>(models.size());
        this.resultModel = resultModel;
        this.data = new NetflixData();
        this.trackDAO = new TrackDAONetflixImpl(data);
        this.inited = new AtomicBoolean(false);
    }

    public MergeModels init(){
        if (!inited.compareAndSet(false, true)){
            return this;
        }
        TrackSimilarityDAO dao = new TrackSimilarityDAONetflixImpl(data);
        Collection<TrackSimilarity> existed = new ArrayList<>();
        models.entrySet().stream()
                .flatMap(entry -> getSimilarities(entry.getKey(), entry.getValue()).stream())
                .filter(ts -> !existed.contains(ts) && !existed.contains(ts.reverse()))
                .forEach(ts -> {
                    try{
                        dao.create(ts);
                        existed.add(ts);
                    } catch(Exception e){
                        System.err.println(e);
                    }
                });
        return this;
    }

    public void save(){
        new NetflixDataProvider(resultModel.toFile(), data).save();
    }

    private List<TrackSimilarity> getSimilarities(FingerprintType type, Path source){
        return new TrackSimilarityDAONetflixImpl(getOrCreateData(source)).findByFingerprintType(type)
                .orElseGet(() -> Collections.emptyList());
    }

    private NetflixData getOrCreateData(Path source){
        return cached.computeIfAbsent(source, this::createData);
    }

    private NetflixData createData(Path source){
        NetflixData data = new NetflixData();
        new NetflixDataProvider(source.toFile(), data).init();
        new TrackDAONetflixImpl(data).getAll().ifPresent(
                    tracks -> tracks.stream().forEach(track -> {
                        try{
                            trackDAO.create(track);
                        } catch(Exception e){
                            System.err.println(e);
                        }
                    })
            );
        return data;
    }
}
