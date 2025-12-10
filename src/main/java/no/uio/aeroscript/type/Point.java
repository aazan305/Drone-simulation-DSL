package no.uio.aeroscript.type;

public class Point {
    private final float x;
    private final float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void print() {
        System.out.println("Point: (" + this.x + ", " + this.y + ")");
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
