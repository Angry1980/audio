package angry1980.audio.dsl;

import angry1980.audio.model.FingerprintType;
import com.netflix.nfgraph.NFGraph;
import com.netflix.nfgraph.compressed.NFCompressedGraph;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;

public class NetflixDataProvider implements InitializingBean {

    private File source;
    private NetflixTrackDSL tracks;

    public NetflixDataProvider(File source, NetflixTrackDSL tracks) {
        this.source = source;
        this.tracks = tracks;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /* todo: implement
        DataInputStream is = new DataInputStream(new FileInputStream(source));
        for(int i = 0; i < is.readInt(); i++) {
            tracks.addTrack(is.readLong());
        }
        for(int i = 0; i < is.readInt(); i++) {
            tracks.addCluster(is.readLong());
        }
        for(int i = 0; i < is.readInt(); i++) {
            tracks.addSimilarity(is.readUTF());
        }
        for(int i = 0; i < is.readInt(); i++) {
            tracks.addType(FingerprintType.valueOf(is.readUTF()));
        }

        NFGraph graph = NFCompressedGraph.readFrom(is);
        for(String s : tracks.getSimilarities()){
            tracks.similarity(s).typeOf();
        }
        is.close();
        */
    }

    public void save(){
        DataOutputStream out = null;
        //todo: refactor
        try {
            if(source.exists()) {
                source.createNewFile();
            }
            out = new DataOutputStream(new FileOutputStream(source, false));
            out.writeInt(tracks.getTracks().size());
            for(long node: tracks.getTracks()){
                out.writeLong(node);
            }
            out.writeInt(tracks.getClusters().size());
            for(long node: tracks.getClusters()){
                out.writeLong(node);
            }
            out.writeInt(tracks.getSimilarities().size());
            for(String node: tracks.getSimilarities()){
                out.writeUTF(node);
            }
            out.writeInt(tracks.getTypes().size());
            for(FingerprintType node: tracks.getTypes()){
                out.writeUTF(node.name());
            }
            tracks.getGraph().compress().writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
