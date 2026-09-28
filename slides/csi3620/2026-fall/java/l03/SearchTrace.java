package l03;

import java.util.ArrayList;
import java.util.List;

/** Instrumented copies of the searches, used to derive every state drawn on the slides. */
public final class SearchTrace {
    private SearchTrace() { }

    /** One line per comparison made by linear search. */
    public static List<String> linear(int[] arr, int target) {
        List<String> steps = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                steps.add("i=" + i + " arr[i]=" + arr[i] + " found");
                return steps;
            }
            steps.add("i=" + i + " arr[i]=" + arr[i] + " no");
        }
        steps.add("end not found");
        return steps;
    }

    /** One line per probe of the iterative binary search, plus a final line if the range empties. */
    public static List<String> binary(int[] arr, int target) {
        List<String> steps = new ArrayList<>();
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            String step = "low=" + low + " high=" + high + " mid=" + mid + " arr[mid]=" + arr[mid];
            if (arr[mid] == target) {
                steps.add(step + " found");
                return steps;
            } else if (arr[mid] < target) {
                steps.add(step + " go right");
                low = mid + 1;
            } else {
                steps.add(step + " go left");
                high = mid - 1;
            }
        }
        steps.add("low=" + low + " high=" + high + " empty");
        return steps;
    }

    /** Number of probes (reads of arr[mid]) the iterative binary search makes. */
    public static int binaryProbes(int[] arr, int target) {
        int probes = 0;
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            probes++;
            if (arr[mid] == target) {
                return probes;
            } else if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return probes;
    }

    /** One line per probe of lowerBound, plus the final answer. */
    public static List<String> lowerBound(int[] arr, int target) {
        List<String> steps = new ArrayList<>();
        int low = 0;
        int high = arr.length;
        while (low < high) {
            int mid = low + (high - low) / 2;
            String step = "low=" + low + " high=" + high + " mid=" + mid + " arr[mid]=" + arr[mid];
            if (arr[mid] < target) {
                steps.add(step + " low=mid+1");
                low = mid + 1;
            } else {
                steps.add(step + " high=mid");
                high = mid;
            }
        }
        steps.add("answer " + low);
        return steps;
    }

    /** One line per shift made by SortedArray.insert, then the final write. */
    public static List<String> insertShifts(int[] arr, int size, int value) {
        List<String> steps = new ArrayList<>();
        int low = Search.lowerBound(java.util.Arrays.copyOf(arr, size), value);
        for (int i = size; i > low; i--) {
            steps.add("arr[" + i + "] = " + arr[i - 1]);
            arr[i] = arr[i - 1];
        }
        arr[low] = value;
        steps.add("arr[" + low + "] = " + value);
        return steps;
    }

    /** Values linear search compares before it stops. */
    public static int linearComparisons(int[] arr, int target) {
        int i = Search.linearSearch(arr, target);
        return i < 0 ? arr.length : i + 1;
    }
}
