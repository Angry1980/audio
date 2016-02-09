package angry1980.utils;

import java.util.function.Function;

public class Numbered<T> {

    public static <L1, L2> Function<Numbered<L1>, Numbered<L2>> transformator(Function<L1, L2> f){
        return (numbered) -> new Numbered(numbered.getNumber(), f.apply(numbered.getValue()));
    }

    private final long number;
    private final T value;

    public Numbered(long number, T value) {
        this.number = number;
        this.value = value;
    }

    public long getNumber() {
        return number;
    }

    public int getNumberAsInt(){
        return (int)number;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Numbered{" +
                "number=" + number +
                ", value=" + value +
                '}';
    }
}
