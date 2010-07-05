public class bigpower {
    private static final int pow = 26;

    public static void main(String[] args) {
        float size = pow / 30f;
        for (int i = pow - 1; --i >= 0;) {
            size *= pow;
        }
        System.out.println(size);
    }
}
