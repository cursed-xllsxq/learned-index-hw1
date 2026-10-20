package me.index.task.isegment;

import me.index.libs.math.Frac128;
import me.index.libs.math.Int128;
import me.index.libs.math.Maths;

public enum RSpline implements Window {
    INSTANCE;

    private long firstKey;

    private Frac128 lower = Maths.INF_NEG;
    private Frac128 upper = Maths.INF_POS;

    private int size;

    @Override
    public int init_skip(long key, long err) {
        firstKey = key;
        lower = Maths.INF_NEG;
        upper = Maths.INF_POS;
        size = 1;

        return 1;
    }

    @Override
    public boolean can_expand(long key, long pos, int err) {
        long dx = key - firstKey;

        Frac128 newLower = new Frac128(
                toInt128(pos - err),
                toInt128(dx)
        );

        Frac128 newUpper = new Frac128(
                toInt128(pos + err),
                toInt128(dx)
        );

        Frac128 nextLower =
                Maths.great(newLower, lower)
                        ? newLower
                        : lower;

        Frac128 nextUpper =
                Maths.less(newUpper, upper)
                        ? newUpper
                        : upper;

        if (Maths.great(nextLower, nextUpper)) {
            return false;
        }

        lower = nextLower;
        upper = nextUpper;
        size++;

        return true;
    }

    @Override
    public double[] get_result() {
        if (size == 1) {
            return new double[]{0.0, 0.0};
        }

        Int128 numerator = Maths.sum(
                Maths.mul(lower.num(), upper.den()),
                Maths.mul(upper.num(), lower.den())
        );

        Int128 denominator = Maths.mul(
                lower.den(),
                upper.den()
        );

        denominator = Maths.mul(denominator, 2L);

        Frac128 slope = new Frac128(
                numerator,
                denominator
        );

        double s = Maths.toDouble(slope);
        double b = -s * firstKey;

        return new double[]{s, b};
    }

    private static Int128 toInt128(long value) {
        return new Int128(
                value < 0 ? -1L : 0L,
                value
        );
    }
}