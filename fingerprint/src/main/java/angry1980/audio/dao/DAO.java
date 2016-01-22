package angry1980.audio.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface DAO<T> {

    default Optional<T> get(long  id){
        return Optional.ofNullable(tryToGet(id));
    }

    //to support java versions less then 8
    T tryToGet(long id);

    //todo: use paging
    default Collection<T> getAllOrEmpty(){
        return getAll().orElseGet(() -> Collections.emptyList());
    }

    default Optional<Collection<T>> getAll(){
        return Optional.ofNullable(tryToGetAll());
    }

    //to support java versions less then 8
    Collection<T> tryToGetAll();

    default Optional<T> create(T entity){
        return Optional.of(tryToCreate(entity));
    }

    default Optional<Collection<T>> createAll(Collection<T> entities){
        return Optional.ofNullable(entities)
                .map(this::tryToCreateAll);
    }

    //to support java versions less then 8
    default Collection<T> tryToCreateAll(Collection<T> entities){
        return entities.stream()
                .map(this::tryToCreate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //to support java versions less then 8
    T tryToCreate(T entity);

}
