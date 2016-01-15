package angry1980.audio.dsl;

import angry1980.audio.model.FingerprintType;
import com.netflix.nfgraph.NFGraph;
import com.netflix.nfgraph.OrdinalIterator;
import com.netflix.nfgraph.compressed.NFCompressedGraph;
import com.netflix.nfgraph.util.OrdinalMap;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;

public class NetflixDataProvider implements InitializingBean {

    private File source;
    private NetflixTrackDSL tracks;
    private Vocabulary<String> stringVocabulary = new Vocabulary<>(
                                                in -> in.readUTF(),
                                                (out, node) -> out.writeUTF(node)
    );
    private Vocabulary<Long> longVocabulary = new Vocabulary<>(
                                                in -> in.readLong(),
                                                (out, node) -> out.writeLong(node)
    );
    private Vocabulary<FingerprintType> typeVocabulary = new Vocabulary<>(
                                                in -> FingerprintType.valueOf(in.readUTF()),
                                                (out, node) -> out.writeUTF(node.name())
    );

    public NetflixDataProvider(File source, NetflixTrackDSL tracks) {
        this.source = source;
        this.tracks = tracks;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(!source.exists()){
            return;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(source));
        longVocabulary.read(in, tracks.getTracks());
        longVocabulary.read(in, tracks.getClusters());
        stringVocabulary.read(in, tracks.getSimilarities());
        typeVocabulary.read(in, tracks.getTypes());
        NFGraph graph = NFCompressedGraph.readFrom(in);
        in.close();
        tracks.getSimilarities().forEach(s -> tracks.similarity(s).typeOf(graph));
        tracks.getTracks().forEach(t -> tracks.track(t).hasSimilarity(graph).is(graph));
    }

    public void save(){
        if(source.exists()) {
            try {
                source.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try(DataOutputStream out = new DataOutputStream(new FileOutputStream(source, false))) {
            longVocabulary.write(out, tracks.getTracks());
            longVocabulary.write(out, tracks.getClusters());
            stringVocabulary.write(out, tracks.getSimilarities());
            typeVocabulary.write(out, tracks.getTypes());
            tracks.getGraph().compress().writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Vocabulary<T> implements Reader<T>, Writer<T>{

        private Reader<T> reader;
        private Writer<T> writer;

        public Vocabulary(Reader<T> reader, Writer<T> writer) {
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public T readNode(DataInputStream in) throws IOException {
            return reader.readNode(in);
        }

        @Override
        public void writeNode(DataOutputStream out, T node) throws IOException {
            writer.writeNode(out, node);
        }
    }

    private interface Reader<T>{

        default void read(DataInputStream in, OrdinalMap<T> data) throws IOException{
            int size = in.readInt();
            for(int i = 0; i < size; i++) {
                data.add(readNode(in));
            }
        }

        T readNode(DataInputStream in) throws IOException;
    }

    private interface Writer<T>{

        default void write(DataOutputStream out, OrdinalMap<T> data) throws IOException {
            out.writeInt(data.size());
            for(T node: data){
                writeNode(out, node);
            }
        }

        void writeNode(DataOutputStream out, T node) throws IOException;
    }

}
