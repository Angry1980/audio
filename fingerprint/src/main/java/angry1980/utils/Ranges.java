package angry1980.utils;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Ranges {

    private final int lowerLimit;
    private final int upperLimit;
    private final int[] ranges;

    public Ranges(int lowerLimit, int upperLimit, int rangesCount) {
        if(lowerLimit >= upperLimit){
            throw new IllegalArgumentException();
        }
        if(rangesCount < 1){
            rangesCount = 1;
        }
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        int delta = Math.floorDiv((upperLimit - lowerLimit), rangesCount);
        this.ranges = IntStream.range(0, rangesCount + 1)
                .map(i -> lowerLimit + i * delta)
                .toArray();
    }

    public int getLowerLimit() {
        return lowerLimit;
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public int getWidth(){
        return upperLimit - lowerLimit;
    }

    public IntStream stream(){
        return IntStream.range(lowerLimit, upperLimit);
    }

    public int getIndex(int value) {
        int i = 0;
        while (i < ranges.length && ranges[i] < value) {
            i++;
        }
        return i;
    }

    @Override
    public String toString() {
        return "Ranges{" +
                "lowerLimit=" + lowerLimit +
                ", upperLimit=" + upperLimit +
                ", ranges=" + Arrays.toString(ranges) +
                '}';
    }
}
