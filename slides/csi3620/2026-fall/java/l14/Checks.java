package l14;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        runningExample();
        edgeCases();
        randomAgainstPriorityQueue();
        heapsort();
        topK();
        lazyQueue();
        System.out.println("l14 OK");
    }

    static void runningExample() {
        MinHeap<Integer> h = new MinHeap<>();
        for (int x : new int[] {2, 4, 9, 3, 1, 8, 7}) h.insert(x);
        check(h.toList().equals(List.of(1, 2, 7, 4, 3, 9, 8)), "inserts: " + h.toList());
        check(h.isHeap(), "heap order after inserts");
        check(h.peek() == 1, "peek");
        check(h.extractMin() == 1, "first extractMin");
        check(h.toList().equals(List.of(2, 3, 7, 4, 8, 9)), "after extract: " + h.toList());
        MinHeap<Integer> b = MinHeap.buildHeap(List.of(2, 4, 9, 3, 1, 8, 7));
        check(b.toList().equals(List.of(1, 2, 7, 3, 4, 8, 9)), "buildHeap: " + b.toList());
        check(b.isHeap(), "heap order after buildHeap");
        check(MinHeap.parent(4) == 1 && MinHeap.left(1) == 3 && MinHeap.right(1) == 4, "index formulas");
    }

    static void edgeCases() {
        MinHeap<Integer> h = new MinHeap<>();
        check(h.isEmpty() && h.size() == 0, "new heap is empty");
        boolean threw = false;
        try { h.extractMin(); } catch (NoSuchElementException e) { threw = true; }
        check(threw, "extractMin on empty throws");
        threw = false;
        try { h.peek(); } catch (NoSuchElementException e) { threw = true; }
        check(threw, "peek on empty throws");
        threw = false;
        try { h.insert(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null rejected");
        h.insert(3);
        check(h.extractMin() == 3 && h.isEmpty(), "one element");
        for (int i = 0; i < 5; i++) h.insert(7);
        for (int i = 0; i < 5; i++) check(h.extractMin() == 7, "duplicates");
        check(MinHeap.buildHeap(new ArrayList<Integer>()).isEmpty(), "buildHeap of empty");
        check(MinHeap.buildHeap(List.of(1)).peek() == 1, "buildHeap of one");
        // LocalDate is Comparable<ChronoLocalDate>, a supertype: needs Comparable<? super T>
        MinHeap<LocalDate> dates = new MinHeap<>();
        dates.insert(LocalDate.of(2026, 3, 1));
        dates.insert(LocalDate.of(2025, 7, 4));
        check(dates.extractMin().equals(LocalDate.of(2025, 7, 4)), "supertype Comparable");
    }

    static void randomAgainstPriorityQueue() {
        Random rnd = new Random(3620);
        for (int trial = 0; trial < 300; trial++) {
            MinHeap<Integer> h = new MinHeap<>();
            PriorityQueue<Integer> pq = new PriorityQueue<>();
            int ops = rnd.nextInt(200);
            for (int k = 0; k < ops; k++) {
                if (pq.isEmpty() || rnd.nextInt(3) > 0) {
                    int x = rnd.nextInt(50);            // small range forces duplicates
                    h.insert(x);
                    pq.offer(x);
                } else {
                    check(h.peek().equals(pq.peek()), "peek matches");
                    check(h.extractMin().equals(pq.poll()), "extractMin matches poll");
                }
                check(h.size() == pq.size(), "sizes match");
                check(h.isHeap(), "heap order holds");
            }
            List<Integer> data = new ArrayList<>();
            int n = rnd.nextInt(100);
            for (int k = 0; k < n; k++) data.add(rnd.nextInt(1000) - 500);
            MinHeap<Integer> b = MinHeap.buildHeap(data);
            PriorityQueue<Integer> q = new PriorityQueue<>(data);
            check(b.isHeap(), "buildHeap gives heap order");
            while (!q.isEmpty()) check(b.extractMin().equals(q.poll()), "buildHeap drains like PQ");
            check(b.isEmpty(), "drained");
        }
        PriorityQueue<Integer> maxPq =
                new PriorityQueue<>(Comparator.reverseOrder());
        maxPq.addAll(List.of(2, 4, 9, 3, 1, 8, 7));
        check(maxPq.poll() == 9, "Comparator gives a max-heap");
    }

    static void heapsort() {
        Integer[] ex = {2, 4, 9, 3, 1, 8, 7};
        Heapsort.sort(ex);
        check(Arrays.equals(ex, new Integer[] {1, 2, 3, 4, 7, 8, 9}), "heapsort example");
        Integer[] empty = {};
        Heapsort.sort(empty);
        check(empty.length == 0, "heapsort empty");
        Random rnd = new Random(14);
        for (int trial = 0; trial < 300; trial++) {
            Integer[] a = new Integer[rnd.nextInt(80)];
            for (int i = 0; i < a.length; i++) a[i] = rnd.nextInt(30);
            Integer[] expect = a.clone();
            Arrays.sort(expect);
            Heapsort.sort(a);
            check(Arrays.equals(a, expect), "heapsort matches Arrays.sort");
        }
        Card[] cards = {new Card(1, "a"), new Card(1, "b")};
        Heapsort.sort(cards);
        check(cards[0].tag().equals("b") && cards[1].tag().equals("a"), "heapsort is not stable");
        String[] s = {"pear", "fig", "apple"};
        Heapsort.sort(s);
        check(Arrays.equals(s, new String[] {"apple", "fig", "pear"}), "heapsort strings");
    }

    /** A record ordered by key only, to observe stability. */
    record Card(int key, String tag) implements Comparable<Card> {
        public int compareTo(Card o) { return Integer.compare(key, o.key); }
    }

    static void lazyQueue() {
        LazyQueue q = new LazyQueue(3);
        q.offerOrDecrease(0, 5);
        q.offerOrDecrease(1, 3);
        q.offerOrDecrease(0, 1);          // decrease: old entry (0, 5) goes stale
        q.offerOrDecrease(1, 4);          // not smaller: ignored
        check(q.key(0) == 1 && q.key(1) == 3, "current keys");
        check(q.pollItem() == 0 && q.pollItem() == 1, "poll order");
        check(q.pollItem() == -1 && q.staleSkipped() == 1, "stale entry skipped");
        LazyQueue big = new LazyQueue(2);
        big.offerOrDecrease(0, Integer.MAX_VALUE);   // first offer, even at the largest key
        check(big.key(0) == Integer.MAX_VALUE && big.pollItem() == 0, "key MAX_VALUE accepted");
        check(big.pollItem() == -1, "item 1 never offered");
        Random rnd = new Random(17);
        for (int trial = 0; trial < 200; trial++) {
            int n = 1 + rnd.nextInt(8);
            LazyQueue lq = new LazyQueue(n);
            int[] best = new int[n];
            Arrays.fill(best, Integer.MAX_VALUE);
            for (int k = 0; k < 20; k++) {
                int item = rnd.nextInt(n), key = rnd.nextInt(30);
                lq.offerOrDecrease(item, key);
                best[item] = Math.min(best[item], key);
            }
            int prev = Integer.MIN_VALUE, count = 0;
            for (int item = lq.pollItem(); item != -1; item = lq.pollItem()) {
                check(lq.key(item) == best[item] && best[item] >= prev, "lazy order by final key");
                prev = best[item];
                count++;
            }
            int expected = 0;
            for (int b : best) if (b != Integer.MAX_VALUE) expected++;
            check(count == expected, "each offered item polled once");
        }
    }

    static void topK() {
        check(TopK.largest(List.of(2, 4, 9, 3, 1, 8, 7), 3).equals(List.of(9, 8, 7)), "top 3");
        check(TopK.largest(List.of(6, 4), 5).equals(List.of(6, 4)), "k larger than n");
        check(TopK.largest(List.of(6, 4), 0).isEmpty(), "k = 0");
        Random rnd = new Random(7);
        for (int trial = 0; trial < 100; trial++) {
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < 60; i++) data.add(rnd.nextInt(40));
            int k = rnd.nextInt(10);
            List<Integer> sorted = new ArrayList<>(data);
            sorted.sort(Comparator.reverseOrder());
            check(TopK.largest(data, k).equals(sorted.subList(0, k)), "topK matches sort");
        }
    }
}
