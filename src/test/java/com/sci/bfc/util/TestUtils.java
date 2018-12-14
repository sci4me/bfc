package com.sci.bfc.util;

import java.util.ArrayList;
import java.util.List;

public final class TestUtils {
    public static <E> List<List<E>> generatePermutations(final List<E> original) {
        if(original.size() == 0) {
            final List<List<E>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }

        final E firstElement = original.remove(0);
        final List<List<E>> returnValue = new ArrayList<>();
        final List<List<E>> permutations = TestUtils.generatePermutations(original);

        for(final List<E> smallerPermutated : permutations) {
            for(int index = 0; index <= smallerPermutated.size(); index++) {
                final List<E> temp = new ArrayList<>(smallerPermutated);
                temp.add(index, firstElement);
                returnValue.add(temp);
            }
        }

        return returnValue;
    }

    private TestUtils() {
    }
}