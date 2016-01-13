package angry1980.utils;

public class Numbered<T> {

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
