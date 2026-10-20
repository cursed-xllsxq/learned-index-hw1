package me.index.task.isearch;

import me.index.libs.math.Sorted;
import me.index.task.Index;

public final class ExponentialSearchIndex implements Index {
    private int[] keys;

    @Override
    public void build(int[] keys) {
        Sorted.checkKeys(keys);
        this.keys = keys;
    }

    @Override
    public int predict(int key) {
        int n = keys.length;
        if (key <= keys[0]) {
            return 0;
        }
        int prev = 0;
        int next = 1;
        while (next < n && keys[next] < key) {
            prev = next;
            next <<= 1;
        }
        int end = Math.min(next + 1, n);
        return Math.min(
                Sorted.lowerBound(keys, prev, end, key),
                n - 1
        );
    }

    @Override
    public int lower_bound(int key) {
        return Sorted.lowerBoundNear(keys, key, predict(key), maxError());
    }

    @Override
    public String id() {
        return "exponential";
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
