@title Lecture 3: Linear and binary search
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Linear and binary search
## Look everywhere, or halve what is left

---
# Where we are

- Lecture 1: the efficiency game, linear against binary search on `1..32`.
- Lecture 2: $O$, $\Omega$, $\Theta$, and best, worst and average cases.
- Today: both searches stated precisely, proved correct, and costed.
- Next: sorting by divide and conquer, which binary search depends on.

> In the first lecture we played a game with thirty-two sorted cards and saw that looking at the middle beats looking from the left, six probes against thirty-two at worst. In the second lecture we built the language to say that precisely: bounds, cases, and the halving loop. Today we put the two together. We write both searches in Java, state their costs with the case and the bound named, prove binary search correct with a loop invariant, and ask what sorted order costs to get and to keep.

---
# By the end of today you can

- Write linear and binary search in Java and trace them on a small array.
- State best, worst and average cases, with the assumptions named.
- Explain why `mid = low + (high - low) / 2` avoids `int` overflow.
- Prove binary search correct with the invariant on `arr[low..high]`.
- Decide when sorting first pays for itself, in $\Theta$ terms.
- Find the first of several equal keys with a lower-bound search.

> Each of these is something you can do on paper. The tracing one comes first because every later claim is checked against a trace. The overflow one is a single line of code, but it is a real bug in naive versions, so you should be able to explain it with numbers. The trade-off one asks you to reason with growth rates rather than stopwatch times, which is the skill from last lecture applied to a decision you will actually face.

---
# Is this student enrolled?

- A registration system stores the ID numbers of every enrolled student.
- A request arrives: is ID `1771` in the list? Where?
- The answer is an index, or "not present".
- Small lists hide the difference; millions of IDs and many requests expose it.

> Here is the problem we solve today, in its plainest form: given an array of whole numbers and one target number, report whether the target occurs and, if so, at which index. Think of a campus system checking whether a scanned card belongs to an enrolled student. The ID numbers on these slides are made up. The question is not whether we can answer it, since any loop can, but how the work grows as the list grows and as the requests pile up.

---
# One array throughout

| index | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|---|
| `ids` (arrival order) | 1609 | 1024 | 1835 | 1330 | 1998 | 1187 | 1771 | 1452 |
| `sorted` | 1024 | 1187 | 1330 | 1452 | 1609 | 1771 | 1835 | 1998 |

- Eight made-up student IDs, $n = 8$, indices from $0$ to $7$.
- Targets we reuse: `1771` (present), `1500` (absent), `1998` (last).
- Every trace on these slides was produced by running the Java code.

> Keep these eight numbers in view; every picture today uses them. The first row is the order in which the IDs arrived, with no structure at all. The second row is the same eight numbers after sorting, which binary search needs. Three targets come back again and again: seventeen seventy-one, which is present, fifteen hundred, which is not, and nineteen ninety-eight, the largest. Each state drawn later was printed by an instrumented copy of the search and then checked by assertions.

---
@type section
# Linear search

---
# Linear search: the idea

```algorithm
function LINEAR-SEARCH(A, x):   // A has n elements, any order
  for i ← 0 to n - 1 do
    if A[i] = x then
      return i                  // first match wins
  return -1                     // looked everywhere
```

- Compare the target with each element, left to right.
- Stop at the first match; report $-1$ only after checking all $n$.

> Linear search is the method you would use without thinking. It makes no assumption about order, which is its great strength: it works on any array, sorted or not. It returns the index of the first match, so with repeated values it finds the leftmost one. The minus one is a special return value that can never be a valid index, so it safely means not present. The algorithm can only say no after it has looked at every element.

---
# Linear search in Java

```java
public static int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) {
            return i;
        }
    }
    return -1;
}
```

- `arr.length` is $n$. An empty array skips the loop and returns `-1`.
- Step through it: [Linear search visualization](../../../visualizations/algorithms/linear_search.html).

> Look at the two exits. The return inside the loop fires on the first match. The return after the loop is reached only if every comparison failed, including the case of an empty array, where the loop body never runs. For an array of int values, the double equals sign compares the numbers themselves. If the array reference itself is null, reading arr dot length throws a NullPointerException, as it does in every method today.

---
# Linear search on `1771`, step by step

```diagram
@dir TB
@reveal manual
L0[i = 0 to 5 | 1609, 1024, 1835, 1330, 1998, 1187]
focus L0
--- Six comparisons, six misses.
L0 -> L1[i = 6 | 1771 found] : match
focus L0 L1
--- The seventh comparison matches. Return 6.
L1 -> L2[i = 7 | 1452 never read].gray
focus L1 L2
--- The loop exits early; the last element is never examined.
```

> Here is the run on the arrival-order array. Indices zero through five hold other IDs, so six comparisons fail. At index six the value is seventeen seventy-one, so the method returns six after seven comparisons. It never looks at index seven. For fifteen hundred, which is absent, the same loop makes all eight comparisons and then returns minus one: that is what not present costs.

---
# Best and worst cases

| input, $n = 8$ | comparisons | general $n$ |
|---|---|---|
| target at index 0: `1609` | 1 | $1$ |
| target at index 6: `1771` | 7 | $i + 1$ at index $i$ |
| target last: `1452` | 8 | $n$ |
| target absent: `1500` | 8 | $n$ |

- Best case $\Theta(1)$. Worst case $\Theta(n)$, reached by last or absent.

> The count is the index of the first match plus one, or n when the target is absent. The best case is a match at index zero: one comparison, a constant, so Theta of one. The worst case is a target in the last position or not there at all: n comparisons, so Theta of n. Remember the lesson from last time: Theta of one is a tight bound on the best case, not a claim about every input. For every input the cost is O of n and Omega of one.

---
# The average case needs assumptions

- **Assume** the target is present, and each of the $n$ positions is equally likely.
- Position $i$ has probability $\frac{1}{n}$ and costs $i + 1$ comparisons.

$$\frac{1}{n}\sum_{i=0}^{n-1}(i+1)=\frac{n+1}{2}$$

- For our 8 IDs: $\frac{1+2+\dots+8}{8}=\frac{36}{8}=4.5$. Average case $\Theta(n)$.
- If the target may be absent, the average moves toward $n$.

> An average is only meaningful once we say what we are averaging over. Here we assume two things: the target is in the array, and it is equally likely to be at any position. Under those assumptions the sum one plus two up to n is n times n plus one over two, and dividing by n gives an expected n plus one over two comparisons, about half the array. The Java checks confirm the total of thirty-six for our eight IDs and the formula for every n up to two hundred. Half of n is still linear, so the average case is Theta of n. If some requests are for absent IDs, each of those costs the full n, which pushes the average up.

---
# Quiz: linear search on average

```quiz
Linear search on $n$ distinct values. The target is present, and every position is equally likely. What is the expected number of comparisons?
- [ ] $\frac{n}{2}$
- [x] $\frac{n+1}{2}$
- [ ] $n$
- [ ] $\log_2 n$
```

> The answer is n plus one over two. The costs are one, two, and so on up to n, each with probability one over n, and their average is n plus one over two. One half of n is close but misses that a match at index zero still costs one comparison, not zero. The value n is the worst case, or the cost when the target is absent. Log n belongs to binary search, which needs sorted input. Try the linear search visualization: [Linear search visualization](../../../visualizations/algorithms/linear_search.html).

---
@type section
# Binary search

---
# Sorted order lets us discard half

```diagram
@dir TB
@reveal manual
M[sorted, look at index 3 | value 1452]
focus M
--- Target 1771. The middle value is 1452, which is smaller.
M -> D[indices 0 to 3 | all at most 1452].gray : discard
M -> K[indices 4 to 7 | 1609, 1771, 1835, 1998] : keep
focus D K
--- Sorted order means nothing left of the middle can be 1771.
```

- **Precondition**: `arr[0] <= arr[1] <= ... <= arr[n-1]`.
- One comparison rules out every element on one side of `mid`.

> Here is the whole idea. On sorted data, looking at one element tells us about many others. The middle value is fourteen fifty-two and the target is larger, so every element at index three or below is at most fourteen fifty-two and cannot be the target. One comparison removes four candidates, not one. A precondition is a promise the caller must keep before calling; binary search promises nothing if it is broken, as we will see near the end.

---
# Binary search in pseudocode

```algorithm
function BINARY-SEARCH(A, x):        // A sorted ascending
  low ← 0; high ← n - 1              // closed range A[low..high]
  while low ≤ high do                // range is not empty
    mid ← low + ⌊(high - low) / 2⌋
    if A[mid] = x then return mid
    else if A[mid] < x then low ← mid + 1
    else high ← mid - 1
  return -1                          // range empty: absent
```

- `A[low..high]` includes both ends. It is empty once `low > high`.

> Read the range as the set of candidates that are still possible. It starts as the whole array and includes both of its ends, which is why high starts at n minus one. Each pass looks at the middle of the range, the probe, and either finds the target or moves one end past the middle. The middle element is excluded on both sides because we have just compared it. When low passes high, no candidates remain.

---
# Binary search in Java

```java
public static int binarySearch(int[] arr, int target) {
    int low = 0;
    int high = arr.length - 1;
    while (low <= high) {               // if present: in arr[low..high]
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            low = mid + 1;              // arr[low..mid] are all too small
        } else {
            high = mid - 1;             // arr[mid..high] are all too big
        }
    }
    return -1;                          // range empty: not present
}
```

> This line-for-line translation of the pseudocode is the version we trace and prove. Look at three lines. The loop test uses less-than-or-equal, because a range with one element still has a candidate. The two updates move past mid, using plus one and minus one, which guarantees progress. Java integer division rounds toward zero, and since high minus low is never negative here, it rounds down, so mid is the lower middle of an even-length range.

---
# Why `low + (high - low) / 2`?

```java
int low = 1_500_000_000;
int high = 2_000_000_000;
check(low + high == -794_967_296, "low + high overflows int");
check((low + high) / 2 == -397_483_648, "naive midpoint is negative");
int mid = low + (high - low) / 2;
check(mid == 1_750_000_000, "safe midpoint");
```

- An `int` holds at most $2^{31}-1 = 2{,}147{,}483{,}647$; larger sums wrap around.
- `high - low` never overflows here, and `low + (high - low) / 2` stays between them.
- Only arrays with more than about $2^{30}$ elements can trigger it, but Java allows them.

> Mathematically, low plus high over two and low plus half the gap are the same number. In Java they are not, because int arithmetic wraps silently past about two point one billion. Both values could be indices into a large enough array, yet their sum wraps to a negative number, and reading a negative index throws an ArrayIndexOutOfBoundsException. The safe form subtracts first, and the difference of two non-negative ints cannot overflow. The asserts shown are from the checker, so these exact values were computed by the JVM.

---
# Tracing `1771`: found in 2 probes

```diagram
@dir TB
@reveal manual
S0[low 0, high 7 | mid 3 reads 1452]
focus S0
--- 1452 is smaller than 1771: go right.
S0 -> S1[low 4, high 7 | mid 5 reads 1771] : low = 4
focus S0 S1
--- Match at index 5 after 2 probes.
```

- A **probe** is one pass of the loop: one read of `arr[mid]`.
- Step through it: [Binary search visualization](../../../visualizations/algorithms/binary_search.html).

> This is the sorted array with target seventeen seventy-one. The first range is zero to seven, so mid is zero plus seven over two rounded down, three, which holds fourteen fifty-two. That is smaller, so low jumps to four. The new middle is four plus three over two rounded down, five, which holds the target. Two probes, where linear search on the unsorted array needed seven comparisons. Each probe may compare twice, equal and then less, but we count probes because each one reads one element.

---
# Tracing `1500`: absent after 3 probes

```diagram
@dir TB
@reveal manual
A0[low 0, high 7 | mid 3 reads 1452]
focus A0
--- 1452 is smaller than 1500: go right.
A0 -> A1[low 4, high 7 | mid 5 reads 1771] : low = 4
focus A0 A1
--- 1771 is bigger: go left.
A1 -> A2[low 4, high 4 | mid 4 reads 1609] : high = 4
focus A1 A2
--- One candidate left, 1609. Bigger again: go left.
A2 -> A3[low 4, high 3 | empty range].gray : high = 3
focus A2 A3
--- low passed high: return -1. low = 4 is where 1500 would go.
```

> Now a target that is not there. The first probe goes right as before. At index five we meet seventeen seventy-one, which is too big, so high drops to four. The range is a single element, sixteen oh nine, and it is still too big, so high drops to three. Now low is four and high is three: the range is empty and the loop stops. Notice where low ended: index four, exactly where fifteen hundred would have to be inserted to keep the array sorted. We will use that fact later.

---
# Tracing `1998`: the worst case for $n = 8$

```diagram
@dir TB
@reveal manual
W0[low 0, high 7 | mid 3 reads 1452]
focus W0
--- Go right.
W0 -> W1[low 4, high 7 | mid 5 reads 1771] : low = 4
focus W0 W1
--- Go right.
W1 -> W2[low 6, high 7 | mid 6 reads 1835] : low = 6
focus W1 W2
--- Go right. The lower middle of two is the left one.
W2 -> W3[low 7, high 7 | mid 7 reads 1998] : low = 7
focus W2 W3
--- Found at index 7 after 4 probes.
```

> The largest ID takes the longest path. Each probe sends us right: to the range four to seven, then six to seven, then seven to seven. With two candidates, six and seven, mid rounds down to six, so the right one needs one more probe. Four probes in all, the most any target needs on eight elements. Searching for two thousand, which is larger than everything, also takes four probes and ends with low equal to eight, one past the last index.

---
# Every probe path on the 8 IDs

```diagram
@dir TB
P3[1452 | index 3] -> P1[1187 | index 1] : left
P3 -> P5[1771 | index 5] : right
P1 -> P0[1024 | index 0] : left
P1 -> P2[1330 | index 2] : right
P5 -> P4[1609 | index 4] : left
P5 -> P6[1835 | index 6] : right
P6 -> P7[1998 | index 7] : right
```

> If we record which index every search probes first, second, third and fourth, the paths form this tree. The first probe is always index three. Then either index one or index five, and so on down. Each value is found at the level where it sits, counting the top level as one: indices zero through seven need three, two, three, one, three, two, three and four probes, and those counts were printed by the code for every one of the eight IDs. Absent targets fall off the bottom of the tree. This picture is a preview of lecture eight: a sorted array searched this way behaves like a balanced binary search tree.

---
@type section
# Why it works

---
# The loop invariant

- A **loop invariant** is a statement true before every test of the loop condition.
- Ours: **if `target` is present, it lies in `arr[low..high]`.**
- It says nothing when the target is absent; that case needs no help.
- Three checks: it starts true, each pass keeps it true, and it gives the answer at the end.

> An invariant is the tool that turns "it worked on my examples" into "it works on every sorted input". We claim that whenever the loop is about to test its condition, the target, if it is anywhere in the array, is somewhere between low and high inclusive. The statement is conditional on purpose. If the target is absent it is vacuously true, and the method returns minus one, which is right. So the proof only needs to follow a target that is present.

---
# Proving the invariant

- **Start**: `low = 0`, `high = n - 1`: the whole array.
- **Go right**: `arr[mid] < target`, so all of `arr[low..mid]` is too small.
- **Go left**: `arr[mid] > target`, so all of `arr[mid..high]` is too big.
- Either way, the discarded part cannot hold the target.
- **End**: a match is correct; an empty range means absent, so `-1`.

> Walk through it once. At the start the range is everything, so the claim is trivially true. In the go-right branch the middle value is too small, and because the array is sorted every value to its left is at most that, so all of them are too small too; discarding them cannot discard the target. The go-left branch is the mirror image. At the end, either we returned mid after seeing it equals the target, or the range is empty, and an empty range cannot hold the target, so by the invariant the target is not in the array.

---
# Why the loop stops

- The range holds `high - low + 1` candidates.
- `low <= mid <= high` always, because `mid` is the lower middle.
- Each non-matching probe moves `low` above `mid` or `high` below `mid`.
- So the range shrinks by at least 1 every pass, and hits empty after at most $n$ passes.

> Correct answers are only half of the proof; we also need the loop to end. The range size is high minus low plus one. Because mid lies inside the range, setting low to mid plus one or high to mid minus one removes at least mid itself, so the size strictly decreases. A strictly decreasing non-negative whole number cannot decrease forever. That bound of n passes is weak, and the next slide sharpens it to about log n, but it already rules out an infinite loop.

---
# At most $\lfloor \log_2 n \rfloor + 1$ probes

- With $m$ candidates: $\lfloor (m-1)/2 \rfloor$ left of `mid`, $\lfloor m/2 \rfloor$ right.
- So after a miss, at most $\lfloor m/2 \rfloor$ remain.
- After $k$ misses at most $\lfloor n/2^k \rfloor$ remain; probing needs at least one.

$$\text{probes} \le \lfloor \log_2 n \rfloor + 1 \quad (n \ge 1)$$

- Checked by code for $n = 1..2048$: never exceeded, always reached.
- Worst case for $n = 8, 1000, 1024$: $4, 10, 11$ probes. For $n = 10^6$ the formula gives $20$.

> Here is the sharp count. With m candidates, mid leaves the floor of m minus one over two on its left and the floor of m over two on its right, so either way at most half survive, rounded down. Halving n repeatedly, the range is still non-empty only while n over two to the k is at least one, that is, for k up to log base two of n. That allows floor of log n misses plus one final probe. The checker searched every present and absent target for every n up to two thousand forty-eight, and the worst case always equalled this bound exactly. Look at the numbers: eight elements need four probes, a thousand need ten, and one thousand twenty-four need eleven, because crossing a power of two adds a probe; doubling n adds exactly one. For a million elements the formula gives twenty, while linear search needs up to a million comparisons. That ratio, not any particular timing, is the reason binary search matters.

---
# Binary search costs

- **Best** $\Theta(1)$: the first `mid` matches, as for `1452`.
- **Worst** $\Theta(\log n)$: $\lfloor \log_2 n \rfloor + 1$ probes, as for `1998`.
- **Average**, present and uniform: $\Theta(\log n)$; our IDs: $\frac{21}{8} \approx 2.6$.
- Every input: $O(\log n)$ time. Extra space $\Theta(1)$ for the loop.

> Name the case and the bound each time. The best case is a lucky first probe. The worst case is the path down to the deepest level, as for nineteen ninety-eight, or for two thousand, which is above the largest value. The average under the same two assumptions as for linear search is still logarithmic: in the tree, more than half of the values need at least floor of log n probes, so the average is at least half of that, and the checker confirms this lower bound for every n up to two thousand forty-eight. Every level but the last is full, because the two sides of mid differ by at most one; and the average is at most the worst case, so it is Theta of log n. The iterative version keeps three int variables, so its extra space is constant.

---
# Quiz: count the probes

```quiz
Binary search as written, on a sorted array of $n = 100$ distinct values. What is the largest number of probes any target can need?
- [ ] 6
- [x] 7
- [ ] 50
- [ ] 100
```

> Seven. The floor of log base two of a hundred is six, because two to the sixth is sixty-four and two to the seventh is one hundred twenty-eight, and one more probe for the last candidate makes seven. The checker confirms that some target needs exactly seven at n equal to one hundred. Six forgets the final probe on a one-element range, the same off-by-one as in the halving loop from last lecture. Fifty is one halving, and one hundred is linear search. Try it: [Binary search visualization](../../../visualizations/algorithms/binary_search.html).

---
@type section
# Recursion and trade-offs

---
# Binary search, recursively

```java
private static int search(int[] arr, int target, int low, int high) {
    if (low > high) {
        return -1;                      // base case: empty range
    }
    int mid = low + (high - low) / 2;
    if (arr[mid] == target) {
        return mid;
    } else if (arr[mid] < target) {
        return search(arr, target, mid + 1, high);
    } else {
        return search(arr, target, low, mid - 1);
    }
}
```

- Same probes and time; but $\Theta(\log n)$ stack frames, as Java does not promise tail-call elimination (reusing the frame of a call in the last position).

> The recursive version says the same thing in a different shape: search the whole range by probing the middle and searching one half. The public wrapper starts it with low zero and high the length minus one. The empty range is the base case, and each recursive call receives a range at least one smaller, which is why the recursion ends. Every call does the same probe as one pass of the loop, so the probe counts are identical; the checker confirms that the two versions return the same index on every test input. Space differs, though. Each call waits for the call it makes, so the stack holds one frame per probe, plus one for the empty range when the target is absent, about log n frames at the deepest point. For a million elements that is around twenty frames, which is harmless, but it is still Theta of log n extra space against Theta of one for the loop. Some languages turn a call in the last position into a jump; Java does not guarantee that, so count the frames.

---
# Linear and binary search compared

| | linear search | binary search |
|---|---|---|
| precondition | none | sorted, constant-time indexing |
| best case | $\Theta(1)$ | $\Theta(1)$ |
| worst case | $\Theta(n)$ | $\Theta(\log n)$ |
| average, present and uniform | $\frac{n+1}{2}$, $\Theta(n)$ | $\Theta(\log n)$ |
| worst case, our 8 IDs | 8 comparisons | 4 probes |
| extra space | $\Theta(1)$ | $\Theta(1)$ loop |

> This table is the summary of the first half. Binary search wins every row except the first, and the first row is the catch. It needs the data sorted, and it needs to jump to any index in constant time, which an array gives and a linked list, coming right after the sorting lectures, does not: reaching the middle of a linked list means walking half of it. Linear search needs nothing, which is why it remains the right tool for small or unsorted collections, or for a single lookup.

---
# When does sorting first pay?

- $k$ searches on the same $n$ values, worst case. Linear every time: $\Theta(kn)$.
- Sort once with a $\Theta(n \log n)$ worst-case sort such as merge sort (lecture 4), then binary search: $\Theta(n \log n + k \log n)$.

| number of searches $k$ | linear every time | sort, then binary |
|---|---|---|
| constant, e.g. $k = 1$ | $\Theta(n)$ | $\Theta(n \log n)$: worse |
| $k = \Theta(\log n)$ | $\Theta(n \log n)$ | $\Theta(n \log n)$: same growth |
| $k = \Theta(n)$ | $\Theta(n^2)$ | $\Theta(n \log n)$: better |

- Sorting wins asymptotically once $k$ grows faster than $\log n$; near $k \approx \log n$ only measurement decides.

> Sorting is not free: next lecture shows that comparison sorting needs on the order of n log n work in the worst case. So sorting is an investment, paid once, that makes every later search cheap. Read the rows from top to bottom. For one search, sorting is a waste: n log n to save a single linear scan. When k is about log n the two totals have the same growth, and the hidden constants, which we have not measured, decide. When the number of searches is proportional to n, as in checking every student in one list against another, linear search is quadratic and sorting first is n log n. So the rule is qualitative but reliable: many searches on stable data justify sorting; one or two do not. One caution: sorting moves the elements, so binary search then reports positions in the sorted copy; if original positions matter, sort (ID, position) pairs.

---
# Keeping the array sorted

```java
public static int insert(int[] arr, int size, int value) {
    // ...
    for (int i = size; i > low; i--) {  // O(n): shift the tail right
        arr[i] = arr[i - 1];
    }
    arr[low] = value;
    return low;
}
```

- `arr[0..size-1]` is sorted. Omitted lines check for room and find `low` by lower bound, in $O(\log n)$ probes.
- New ID `1500` goes at index 4, where the failed search ended; four elements shift right.
- Worst case, a value smaller than all: $n$ shifts, so insertion is $\Theta(n)$.

> Sorted order has an upkeep cost. When a new student enrolls, the ID has to go into its place, and the unsuccessful search for fifteen hundred ended with low equal to four, which is exactly that place. Finding it is logarithmic. But an array stores its elements in consecutive slots, so opening a gap means moving every later element one slot to the right. The omitted part does two things: it throws an exception if there is no free slot, and it runs a binary search variant to set low to the first index whose value is at least the new one. Then look at the loop: it walks from the end of the used part down to low, copying each element one slot to the right, and finally writes the new value into the gap. The loop runs size minus low times, which is zero when the value goes at the end and size when it goes at the front. The fast find does not rescue the slow shift.

---
# Inserting `1500`, step by step

```diagram
@dir TB
@reveal manual
I0[1024, 1187, 1330, 1452, 1609, 1771, 1835, 1998 | size 8, one free slot]
focus I0
--- Eight sorted IDs and one free slot at index 8.
I0 -> I1[spot found: index 4 | 3 probes read 1609, 1330, 1452] : lower bound
focus I0 I1
--- Three probes find the first value at least 1500.
I1 -> I2[1998, 1835, 1771, 1609 | each moves one slot right] : 4 shifts
focus I1 I2
--- Copy from the end backward, so nothing is overwritten.
I2 -> I3[1024, 1187, 1330, 1452, 1500, 1609, 1771, 1835, 1998]
focus I2 I3
--- Write 1500 into index 4. The array is sorted again.
```

> The instrumented run shows each stage. The lower-bound search reads sixteen oh nine, then thirteen thirty, then fourteen fifty-two, and settles on index four. Then the shift loop copies nineteen ninety-eight into slot eight, eighteen thirty-five into slot seven, seventeen seventy-one into slot six and sixteen oh nine into slot five, in that order. Copying from the back matters: copying from the front would overwrite values before they moved. Three probes, four shifts; with a million elements it would be about twenty probes and up to a million shifts.

---
# Quiz: many searches

```quiz
You must answer $k = n$ membership queries on the same $n$ unsorted IDs. Which statement about the worst-case total is correct?
- [ ] Linear search for each query costs $\Theta(n)$ in total.
- [x] Sorting once with a $\Theta(n \log n)$ sort such as merge sort (lecture 4), then binary searching each query, costs $\Theta(n \log n)$.
- [ ] Sorting before every query, then binary searching, costs $\Theta(n \log n)$.
- [ ] Linear search wins, because it never pays for sorting.
```

> Sort once, then search. A worst-case n log n sort such as merge sort costs n log n, and n binary searches cost n log n more, so the total is Theta of n log n. Linear search costs up to n per query, so n queries cost Theta of n squared, not Theta of n; that also rules out the last option, since n squared grows faster than n log n. Sorting before every query repeats the n log n investment n times, which is n squared log n, worse than doing nothing clever. The choice of sort matters for a worst-case claim: plain quicksort can take quadratic time in its worst case, as next lecture shows. The lesson is to sort once, with a sort whose worst case is n log n, and reuse the order.

---
@type section
# Duplicates, edge cases, library

---
# Duplicates: which match?

| index | 0 | 1 | 2 | 3 | 4 |
|---|---|---|---|---|---|
| `dup` | 1024 | 1330 | 1330 | 1330 | 1609 |

- `binarySearch(dup, 1330)` returns `2`: the first probe lands on a match.
- Linear search returns `1`, the first match.
- Binary search promises **some** matching index, not the first one.

> Real data has repeats: the same ID scanned twice, or scores rather than IDs. Take five values with thirteen thirty three times. The first probe is index two, which matches, so binary search stops there. It is a correct answer to "is thirteen thirty present", but if you wanted the first occurrence, for example to count how many copies there are, it is the wrong index. Which match it returns depends on the length of the array and the positions of the repeats, so do not rely on any particular one.

---
# Lower bound: the first possible position

```java
public static int lowerBound(int[] arr, int target) {
    int low = 0;
    int high = arr.length;          // undecided: [low, high); answer may be high
    while (low < high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] < target) {
            low = mid + 1;          // mid and everything left of it are too small
        } else {
            high = mid;             // mid might be the answer: keep it
        }
    }
    return low;
}
```

- Returns the first index with `arr[i] >= target`, or `arr.length` if none.

> The lower-bound search asks a sharper question: what is the first index whose value is at least the target? That is the first copy when the target is present, and its insertion point when it is not. Look at the differences from binary search. High starts at the length, because the answer may be one past the end. The loop runs while low is less than high. And when the middle value is big enough we keep mid with high equals mid, because it might be the answer. There is no early exit; the loop narrows the range until low equals high, and that single position is the answer.

---
# Lower bound on `1330`, step by step

```diagram
@dir TB
@reveal manual
B0[low 0, high 5 | mid 2 reads 1330]
focus B0
--- 1330 is not smaller than 1330: it might be the first. high = 2.
B0 -> B1[low 0, high 2 | mid 1 reads 1330] : high = 2
focus B0 B1
--- Again a match. Keep it, look further left. high = 1.
B1 -> B2[low 0, high 1 | mid 0 reads 1024] : high = 1
focus B1 B2
--- 1024 is too small: low = 1.
B2 -> B3[low 1, high 1 | answer 1] : low = 1
focus B2 B3
--- low meets high at index 1, the first 1330.
```

> Trace it on the array with three copies. The first probe at index two sees thirteen thirty; that is not smaller, so it could be the first copy, and high moves to two, keeping index two as a possible answer. The next probe at index one sees another thirteen thirty, so high moves to one. The last probe at index zero sees ten twenty-four, too small, so low moves to one. Low and high meet at one, the first copy.

---
# Why lower bound works

- Half-open range `[low, high)`: `low` is included, `high` is not.
- Left of `low`: too small. From `high` on: at least the target.
- `mid < high` always, so `high = mid` and `low = mid + 1` both shrink the range.
- When `low == high`, nothing is undecided: `low` is the answer.
- Lesson: [Find the first possible position](https://learn.tianlema.com/lessons/02-search-and-invariants/).

> The invariant for lower bound has two sides. Everything strictly left of low is known to be smaller than the target, and everything at or after high is known to be at least the target. At the start both claims are empty, so they hold. Each probe decides at least mid, and moves one boundary past it. Because mid is the lower middle of a non-empty range, it is always less than high, so high equals mid still shrinks the range. Mid can equal low, though, which is why the other update must be mid plus one, never mid. When the boundaries meet, the first index that is at least the target is low. The public lesson walks through the same half-open range in more detail.

---
# Unsorted input: wrong, and silent

```diagram
@dir TB
@reveal manual
U0[unsorted ids, target 1609 | mid 3 reads 1330]
focus U0
--- 1330 is smaller: discard indices 0 to 3, including 1609 at index 0.
U0 -> U1[low 4, high 7 | mid 5 reads 1187] : go right
focus U0 U1
--- Smaller again: go right.
U1 -> U2[low 6, high 7 | mid 6 reads 1771] : go right
focus U1 U2
--- Bigger: go left. The range empties. Return -1, with no warning.
```

> What happens if the caller breaks the precondition? Run binary search on the arrival-order array, looking for sixteen oh nine, which sits at index zero. The first probe reads thirteen thirty, which is smaller, so the method throws away the left half, and with it the target. Two more probes and the range is empty. The method returns minus one. Nothing crashes. Checking sortedness on every call would cost a linear scan and destroy the point, so the precondition is the caller's responsibility.

---
# Edge cases and common mistakes

- **Empty** array: `high = -1`, no probe, `-1`. **One element**: one probe.
- **Beyond the ends**: `1000` ends with `high = -1`, `2000` with `low = 8`; no read is out of range.
- `while (low < high)` on a closed range: `{7}` never finds `7`.
- `low = mid`: at `low 6, high 7`, target `1998` loops forever.
- `high = arr.length` with `<=`: target `2000` reads `arr[8]` and throws `ArrayIndexOutOfBoundsException`.
- `(low + high) / 2` overflows on huge arrays; a `null` array throws `NullPointerException`.

> The first two lines are the edge cases worth testing, and the checker contains each of them. Empty is the classic: high starts at minus one and the loop test fails at once. Targets below the smallest or above the largest value push one end past the array, which is fine because the loop stops before reading out of range. The rest are mistakes, and each is a boundary mistake with a tiny counterexample. With less-than instead of less-than-or-equal, a single-element range is never probed. Setting low to mid instead of mid plus one can stop progress: on our array, the range six to seven has mid six, and if the target is larger, low stays six forever. Mixing the half-open start with the closed loop test reads one past the end, and Java throws. For null we simply let Java throw; the contract assumes a real array. The lesson is to pick one range convention, closed or half-open, and make the start, the test and the updates agree with it.

---
# The library's binary search

- `Arrays.binarySearch(int[] a, int key)` in `java.util`.
- The array must be sorted; if it is not, **the results are undefined**.
- With several equal elements, there is **no guarantee which one** is found.
- Absent key: returns $-(\text{insertion point}) - 1$.
- Insertion point: index of the first element greater than the key, or `a.length`.
- So the result is $\ge 0$ exactly when the key is found.

> These facts come straight from the Java SE API documentation for the int array version, and there are versions for the other primitive types and for objects. Undefined means the method may return anything; it does not promise an exception. The encoding for absent keys is clever: it hands back the insertion point, so you do not need a second search to insert, while keeping every not-found result negative. The minus one is there because an insertion point of zero would otherwise look like a found index zero.

---
# The library on our IDs

```java
check(Arrays.binarySearch(sorted, 1771) == 5, "library found");
check(Arrays.binarySearch(sorted, 1500) == -5, "absent: -(4)-1");
check(Arrays.binarySearch(sorted, 1000) == -1, "absent: -(0)-1");
check(Arrays.binarySearch(sorted, 2000) == -9, "absent: -(8)-1");
```

- `1500` has insertion point `4`, the same index our trace ended on.
- To decode: `insertionPoint = -result - 1`.
- Current OpenJDK writes `(low + high) >>> 1`, also overflow-safe.

> These are assertions from the checker, run against the real library. Seventeen seventy-one is found at index five, as in our own trace. Fifteen hundred returns minus five: its insertion point is four, minus four minus one. A key smaller than everything returns minus one, and a key larger than everything returns minus nine, since the insertion point is the length, eight. The checker also confirms, for every target in a range around the values, on strictly increasing arrays of 0 to 300 elements, that the library agrees with our lowerBound on every absent key. Last, the midpoint: current OpenJDK source shifts the sum right as an unsigned number. Two non-negative ints always sum to less than two to the thirty-second, so reading the wrapped bits as unsigned recovers the true sum, and the checker confirms, on this example, that it gives the same midpoint as our form. That is an implementation detail, not part of the API contract.

---
# Quiz: decode the library

```quiz
`sorted` holds 1024, 1187, 1330, 1452, 1609, 1771, 1835, 1998. What does `Arrays.binarySearch(sorted, 1100)` return?
- [ ] `-1`
- [x] `-2`
- [ ] `1`
- [ ] `0`
```

> Minus two. Eleven hundred is absent. The first element greater than it is eleven eighty-seven at index one, so the insertion point is one, and the method returns minus one minus one, which is minus two. Minus one would mean insertion point zero, a key below ten twenty-four. One and zero are non-negative, and the documentation guarantees non-negative results only for keys that are present. The checker asserts this exact value.

---
@type section
# Wrap-up

---
# Summary

- Linear search: any order, $\Theta(n)$ worst. Binary search: sorted input, $\lfloor \log_2 n \rfloor + 1$ probes worst.
- Correctness: the target, if present, stays in `arr[low..high]`; the range shrinks every pass.
- Sorting once pays when the number of searches grows faster than $\log n$.
- Sorted arrays find fast but insert in $\Theta(n)$ in the worst case; lower bound finds the first of several equal keys.

> If you keep one picture from today, keep the halving range with its invariant: the target, if present, is always inside, and the range at least halves at every miss. That one sentence gives both correctness and the log n bound. The rest is about the price: sorted order has to be paid for once by sorting and again on every insertion, and binary search promises some matching index, not the first. Lower bound is the tool when you need the first.

---
# Check yourself

- For $n = 16$, which targets in a sorted array need the most probes, and how many?
- Rewrite `binarySearch` with a half-open range `[low, high)`. What changes?
- A sorted array receives $n$ inserts and $n$ searches. What is the worst-case total?

> For the first, draw the probe tree for sixteen elements as we did for eight, and compare your answer with the formula floor of log n plus one. For the second, change the start, the loop test and one update, and check the empty array and a single element by hand. For the third, add the insertion cost, which is linear each time, to the search cost, and decide which term dominates.

---
# Next time

- Sorting, the investment binary search needs: merge sort and quicksort.
- Divide and conquer: binary search keeps one half; sorting must finish both.
- The $\Omega(n \log n)$ lower bound for comparison sorting.

> Today we assumed a sorted array and asked what it buys. Next lecture pays the bill: merge sort and quicksort, both of which split the problem the way binary search does, but must solve both halves instead of discarding one. We will also see why no comparison-based sort can beat n log n in the worst case, which makes today's trade-off table exact. Before then, step through the two search visualizations linked earlier and predict each probe before it happens.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 2 (loop invariants and their three-part proofs) and Chapter 3 (characterizing running times).
- Java SE API documentation: `java.util.Arrays.binarySearch`.
- All traces, probe counts and library results on these slides were produced by running the Java code written for this deck.

> The loop-invariant method of proof, with its three parts, follows the textbook's presentation in the getting-started chapter. The notation follows the chapter on running times. The library facts about binary search are quoted in substance from the Java SE API documentation. Every number on these slides was computed and asserted by the accompanying Java checker rather than taken from a published table.
