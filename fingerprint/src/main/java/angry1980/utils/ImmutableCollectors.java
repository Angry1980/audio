package angry1980.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.stream.Collector;

public class ImmutableCollectors {

    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toList() {
        return Collector.of(
                ImmutableList.Builder::new,
                ImmutableList.Builder::add,
                (b1, b2) -> b1.addAll(b2.build()),
                (builder) -> builder.build()
        );
    }

    public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toSet() {
        return Collector.of(
                ImmutableSet.Builder::new,
                ImmutableSet.Builder::add,
                (b1, b2) -> b1.addAll(b2.build()),
                (builder) -> builder.build()
        );
    }


}
