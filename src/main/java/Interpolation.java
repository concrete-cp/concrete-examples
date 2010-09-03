public class Interpolation {
    public static void main(String[] args) {
        final double x0 = 225;
        final double y0 = 8;

        final double x1 = x0 + 5;
        final double y1 = 20;

        final double y = 10;

        System.out.println(x0 + (y - y0) * (x0 - x1) / (y0 - y1));

    }
}
