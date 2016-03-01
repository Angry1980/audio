package angry1980.audio.dao;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.NetflixNodeType;
import angry1980.audio.model.NetflixRelationType;
import com.netflix.nfgraph.NFGraph;
import com.netflix.nfgraph.OrdinalIterator;
import com.netflix.nfgraph.compressed.NFCompressedGraph;
import com.netflix.nfgraph.util.OrdinalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;

public class NetflixDataProvider implements InitializingBean {

    private static Logger LOG = LoggerFactory.getLogger(NetflixDataProvider.class);

    private File source;
    private NetflixData data;
    private Vocabulary<String> stringVocabulary = new Vocabulary<>(
                                                in -> in.readUTF(),
                                                (out, node) -> out.writeUTF(node)
    );
    private Vocabulary<Long> longVocabulary = new Vocabulary<>(
                                                in -> in.readLong(),
                                                (out, node) -> out.writeLong(node)
    );
    private Vocabulary<ComparingType> typeVocabulary = new Vocabulary<>(
                                                in -> ComparingType.valueOf(in.readUTF()),
                                                (out, node) -> out.writeUTF(node.name())
    );

    public NetflixDataProvider(File source, NetflixData data) {
        this.source = source;
        this.data = data;
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    public void init(){
        if(!source.exists()){
            LOG.debug("Source file {} does not exist", source.getAbsolutePath());
            return;
        }
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(source));
            longVocabulary.read(in, data.getTracks());
            longVocabulary.read(in, data.getClusters());
            stringVocabulary.read(in, data.getPaths());
            stringVocabulary.read(in, data.getSimilarities());
            typeVocabulary.read(in, data.getTypes());
            NFGraph graph = NFCompressedGraph.readFrom(in);
            in.close();
            copyConnections(graph, data.getSimilarities(), NetflixNodeType.SIMILARITY, NetflixRelationType.TYPE_OF);
            copyConnections(graph, data.getTracks(), NetflixNodeType.TRACK, NetflixRelationType.HAS);
            copyConnections(graph, data.getTracks(), NetflixNodeType.TRACK, NetflixRelationType.IS);
            copyConnections(graph, data.getTracks(), NetflixNodeType.TRACK, NetflixRelationType.SITUATED);
            LOG.debug("Import of data from {} was successfully finished", source.getAbsolutePath());
        } catch(Exception e){
            //todo: clean data
            LOG.error("Error while trying to restore data", e);
        }
    }

    private <T> void copyConnections(NFGraph graph, OrdinalMap<T> values, NetflixNodeType nodeType, NetflixRelationType relationType){
        values.forEach(
                t -> {
                    int ordinal = values.get(t);
                    OrdinalIterator it = graph.getConnectionIterator(nodeType.name(), ordinal, relationType.name());
                    int s;
                    while((s = it.nextOrdinal()) != OrdinalIterator.NO_MORE_ORDINALS) {
                        data.getGraph().addConnection(nodeType.name(), ordinal, relationType.name(), s);
                    }
                }
        );
    }

    public void save(){
        if(!source.exists()) {
            try {
                source.createNewFile();
            } catch (IOException e) {
                LOG.error("Error while trying to create file to save data", e);
                return;
            }
        }
        try(DataOutputStream out = new DataOutputStream(new FileOutputStream(source, false))) {
            longVocabulary.write(out, data.getTracks());
            longVocabulary.write(out, data.getClusters());
            stringVocabulary.write(out, data.getPaths());
            stringVocabulary.write(out, data.getSimilarities());
            typeVocabulary.write(out, data.getTypes());
            data.getGraph().compress().writeTo(out);
        } catch (IOException e) {
            LOG.error("Error while saving data", e);
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
