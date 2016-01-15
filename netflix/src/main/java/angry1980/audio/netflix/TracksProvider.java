package angry1980.audio.netflix;

import com.netflix.nfgraph.NFGraph;
import com.netflix.nfgraph.compressed.NFCompressedGraph;
import com.netflix.nfgraph.util.OrdinalMap;
import org.springframework.beans.factory.InitializingBean;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class TracksProvider implements InitializingBean {

    private File source;
    private NetflixTrackDSL tracks;

    public TracksProvider(File source, NetflixTrackDSL tracks) {
        this.source = source;
        this.tracks = tracks;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DataInputStream is = new DataInputStream(new FileInputStream(source));
        int size = is.readInt();
        OrdinalMap<String> map = new OrdinalMap<>(size);
        for(int i=0;i<size;i++) {
            map.add(is.readUTF());
        }

        //OrdinalMap<String> movieOrdinals = deserializeNodeDetails(is);
        //OrdinalMap<String> actorOrdinals = deserializeNodeDetails(is);
        //OrdinalMap<String> ratingOrdinals = deserializeNodeDetails(is);
        NFGraph graph = NFCompressedGraph.readFrom(is);
        is.close();
    }
/*
    public OrdinalMap<String> deserializeNodeDetails(DataInputStream is) {
        int size = is.readInt();
        OrdinalMap<String> map = new OrdinalMap<>(size);
        for(int i=0;i<size;i++) {
            map.add(is.readUTF());
        }
        return map;
    }
    */
}
