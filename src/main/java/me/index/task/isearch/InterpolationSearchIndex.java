package me.index.task.isearch;

import me.index.libs.math.Sorted;
import me.index.task.Index;

public final class InterpolationSearchIndex implements Index {
    int[] keys;

    @Override
    public void build(int[] keys) {
        Sorted.checkKeys(keys);
        this.keys = keys;
    }

    @Override
    public int predict(int key) {
        int lo = 0;
        int hi = keys.length - 1;

        while (lo <= hi && key >= keys[lo] && key <= keys[hi]) {
            long numerator = ((long) key - keys[lo]) * (hi - lo);
            long denominator = (long) keys[hi] - keys[lo];

            int pos = lo + (int) (numerator / denominator);

            if (keys[pos] == key) {
                return pos;
            }

            if (keys[pos] < key) {
                lo = pos + 1;
            } else {
                hi = pos - 1;
            }
        }

        return Math.min(lo, keys.length - 1);
    }
    @Override
    public int lower_bound(int key) {
        return Sorted.lowerBoundNear(keys, key, predict(key), maxError());
    }

    @Override
    public String id() {
        return "interpolation";
    }

    @Override
    public int maxError() {
        return 0;
    }

    @Override
    public long sizeInBytes() {
        return 0;
    }
}
