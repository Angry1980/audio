package angry1980.audio.dao;

import angry1980.audio.model.NetflixNodeType;
import angry1980.audio.model.NetflixRelationType;
import com.netflix.nfgraph.util.OrdinalMap;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Netflix<T> {

    protected NetflixData data;

    public Netflix(NetflixData data) {
        this.data = Objects.requireNonNull(data);
    }

    protected int getConnectionNode(T value, NetflixRelationType relation){
        return data.getGraph().getConnection(getNodeType().name(), getValues(data).get(value), relation.name());
    }

    protected <R> R connectionValue(OrdinalMap<R> map, Function<T, Integer> f, T from){
        return map.get(f.apply(from));
    }

    protected Stream<T> getAllNodes(){
        Iterable<T> iterable = () -> getValues(data).iterator();
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    protected void addConnection(T value, NetflixRelationType relation, int to){
        data.getGraph().addConnection(
                getNodeType().name(),
                getValues(data).add(value),
                relation.name(),
                to
        );

    }

    protected abstract NetflixNodeType getNodeType();

    protected abstract OrdinalMap<T> getValues(NetflixData data);
}
