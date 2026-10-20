package me.index.task.isegment;

import me.index.libs.math.Frac128;
import me.index.libs.math.Int128;
import me.index.libs.math.Maths;

import java.util.Stack;

public enum CHT implements Window {
    INSTANCE;

    private static class Line {
        private long a;
        private long b;
        private Frac128 from;

        private Line(long a, long b, Frac128 from) {
            this.a = a;
            this.b = b;
            this.from = from;
        }

        private Frac128 intersection(long a, long b) {
            return new Frac128(
                    Maths.sub(b, this.b),
                    Maths.sub(this.a, a)
            );

        }
    }

    private Stack<Line> lower = new Stack<>();
    private Stack<Line> upper = new Stack<>();

    private Frac128 w;
    private int lowerAtMin;
    private int upperAtMin;
    private Frac128 resultW;
    private long resultUpperA;
    private long resultUpperB;
    private long resultLowerA;
    private long resultLowerB;

    private void addLower(long a, long b) {
        if (lower.empty()) {
            lower.push(new Line(a, b, Maths.INF_NEG));
            w = Maths.F_ZERO;
            lowerAtMin = 0;
            return;
        }

        Frac128 intersection = lower.peek().intersection(a, b);
        while (lower.size() >= 2 && Maths.lessEq(intersection, lower.peek().from)) {
            if (lowerAtMin == lower.size() - 1) {
                lowerAtMin--;
            }
            lower.pop();
            intersection = lower.peek().intersection(a, b);
        }
        if (Maths.less(intersection, w)) {
            w = intersection;
            while (upperAtMin + 1 < upper.size()
                    && Maths.great(upper.get(upperAtMin + 1).from, intersection)) {
                upperAtMin++;
            }
        }
        Line next = new Line(a, b, intersection);
        lower.push(next);
    }


    private void addUpper(long a, long b) {
        if (upper.empty()) {
            upper.push(new Line(a, b, Maths.INF_POS));
            w = Maths.F_ZERO;
            upperAtMin = 0;
            return;
        }

        Frac128 intersection = upper.peek().intersection(a, b);
        while (upper.size() >= 2 && Maths.greatEq(intersection, upper.peek().from)) {
            if (upperAtMin == upper.size() - 1) {
                upperAtMin--;
            }
            upper.pop();
            intersection = upper.peek().intersection(a, b);
        }
        if (Maths.great(intersection, w)) {
            w = intersection;
            while (lowerAtMin + 1 < lower.size()
                    && Maths.less(lower.get(lowerAtMin + 1).from, intersection)) {
                lowerAtMin++;
            }
        }
        Line next = new Line(a, b, intersection);
        upper.push(next);
    }

    private Frac128 minGap() {
        Line u = upper.get(upperAtMin);
        Line l = lower.get(lowerAtMin);

        Int128 da = Maths.sub(u.a, l.a);
        Int128 db = Maths.sub(u.b, l.b);

        Int128 numerator = Maths.sum(
                Maths.mul(w.num(), da),
                Maths.mul(w.den(), db)
        );

        return new Frac128(
                numerator,
                w.den()
        );
    }

    @Override
    public int init_skip(long key, long err) {
        lower.clear();
        upper.clear();

        w = Maths.F_ZERO;

        lowerAtMin = 0;
        upperAtMin = 0;

        resultW = Maths.F_ZERO;
        resultUpperA = 0;
        resultUpperB = 0;
        resultLowerA = 0;
        resultLowerB = 0;

        Line lowerLine = new Line(-key, 0, Maths.INF_NEG);
        Line upperLine = new Line(-key, 0, Maths.INF_POS);

        lower.push(lowerLine);
        upper.push(upperLine);

        return 1;
    }

    @Override
    public boolean can_expand(long key, long pos, int err) {
        Frac128 oldW = w;

        Line oldUpper = upper.get(upperAtMin);
        Line oldLower = lower.get(lowerAtMin);

        addLower(-key, pos);
        addUpper(-key, pos);

        Frac128 gap = minGap();

        Int128 limit = new Int128(
                0,
                2L * err
        );

        Frac128 maxGap = new Frac128(limit, Maths.ONE);

        if (Maths.lessEq(gap, maxGap)) {
            saveResult();
            return true;
        }

        resultW = oldW;
        resultUpperA = oldUpper.a;
        resultUpperB = oldUpper.b;
        resultLowerA = oldLower.a;
        resultLowerB = oldLower.b;

        return false;
    }

    private void saveResult() {
        resultW = w;

        Line upperLine = upper.get(upperAtMin);
        Line lowerLine = lower.get(lowerAtMin);

        resultUpperA = upperLine.a;
        resultUpperB = upperLine.b;

        resultLowerA = lowerLine.a;
        resultLowerB = lowerLine.b;
    }

    @Override
    public double[] get_result() {
        Int128 upperValue = Maths.sum(
                Maths.mul(resultW.num(), resultUpperA),
                Maths.mul(resultW.den(), resultUpperB)
        );

        Int128 lowerValue = Maths.sum(
                Maths.mul(resultW.num(), resultLowerA),
                Maths.mul(resultW.den(), resultLowerB)
        );

        Int128 interceptNum = Maths.sum(
                upperValue,
                lowerValue
        );

        Int128 interceptDen = Maths.mul(
                resultW.den(),
                2L
        );

        Frac128 intercept = new Frac128(
                interceptNum,
                interceptDen
        );

        return new double[]{
                Maths.toDouble(resultW),
                Maths.toDouble(intercept)
        };
    }
}
