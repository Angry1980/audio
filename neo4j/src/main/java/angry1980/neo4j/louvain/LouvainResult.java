package angry1980.neo4j.louvain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collections;
import java.util.List;

public class LouvainResult {

    private Int2ObjectMap<LouvainLayer> layerMap;

    public LouvainResult() {
        this.layerMap = new Int2ObjectOpenHashMap<>();
    }

    public LouvainLayer layer(int level) {
        if (!layerMap.containsKey(level)) {
            this.layerMap.put(level, new LouvainLayer(level));
        }
        return this.layerMap.get(level);
    }

    public List<Integer> layers() {
        List<Integer> keys = new IntArrayList(layerMap.keySet());
        Collections.sort(keys);
        return keys;
    }

}
