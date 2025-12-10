package no.uio.aeroscript.type;

import java.util.Random;

public class Range {
    private final float start;
    private final float end;

    public Range(float start, float end) {
        this.start = start;
        this.end = end;
    }

    public Float getRandomNumber() {
        Random r = new Random();
        return r.nextFloat((this.end-this.start)) + this.start;
    }

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }
}
