@title Lecture 14: Priority queues and heaps
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Priority queues and heaps
## Always serve the smallest key next

---
# Where we are

- L08 to L11: search trees keep *all* keys in order.
- L12: hash tables find one key fast, with no order at all.
- L13: B-trees keep order on disk-sized nodes.
- Today: a structure that keeps only enough order to find the minimum.
- Next time (L15): graphs. Heaps return in L17, inside shortest paths.

> Over the last few lectures we built structures that answer many questions. A balanced search tree knows the full sorted order of its keys, so it can answer "what is the successor of this key" or "list everything between ten and twenty". Hash tables gave up order entirely in exchange for fast lookup. Today we give up almost all order, but keep one piece: which key is the smallest. That turns out to be exactly what schedulers, simulations and several graph algorithms need, and it is much cheaper to maintain than full sorted order.

---
# By the end of today you can

- State the priority queue operations and their costs in a binary heap.
- Compute parent and child indices in a 0-based array heap.
- Trace insert (sift-up) and extractMin (sift-down) on paper.
- Build a heap bottom-up and explain why it takes $O(n)$ time.
- Use `java.util.PriorityQueue`, knowing what it does not do.
- Pick a heap for top-k, merging sorted lists and scheduling.

> These are the things you should be able to do on paper and in Java when we finish. The tracing skills matter most: if you can draw the tree and the array side by side and move keys correctly, the code follows almost line by line. The last two outcomes are about using the library well, which includes knowing its limits, such as the missing decrease-key operation.

---
# The problem: who goes next?

- A help desk receives tickets, each with an urgency number.
- New tickets keep arriving while old ones are being served.
- Rule: always serve the most urgent ticket next.
- A queue (L07) serves by arrival time, not by urgency.
- Sorting everything again after each arrival wastes work.

> Picture a help desk, a hospital triage desk, or a print server. Items arrive at unpredictable times, each with a priority. At any moment we need the most urgent item, and we must also accept new arrivals quickly. A first-in first-out queue from lecture seven answers the wrong question: it knows who came first, not who matters most. Sorting the whole list after every arrival works, but it redoes a lot of work that the next question never needed. We want a structure that does just enough.

---
# The priority queue ADT

| Operation | Meaning |
|---|---|
| `insert(x)` | Add key `x` |
| `peek()` (findMin) | Return the smallest key, keep it |
| `extractMin()` | Remove and return the smallest key |
| `size()`, `isEmpty()` | How many keys are stored |
| `decreaseKey(x, k)` | Extra: lower the key of a stored item to `k` |

A **max** priority queue is the mirror image: `peek` and `extract` use the largest key.

> An abstract data type names operations and their meaning, not how they are stored. A min priority queue promises that peek and extract-min always see the smallest key currently stored. Duplicates are allowed; if several keys tie for smallest, any one of them may come out. Decrease-key is an extra operation that some algorithms want, such as Dijkstra's shortest paths; not every implementation offers it, and Java's does not. A max priority queue is the same idea with the comparison reversed, and we will use one for heapsort.

---
# One running example

- Keys arrive in this order: `2, 4, 9, 3, 1, 8, 7`.
- Seven distinct integers, so every tie question is set aside.
- We insert them one by one into an empty min-heap.
- Then we extract the minimum, and build a heap from the same array.
- Every state shown today comes from running the lecture's Java code.

> We will use the same seven keys all lecture. First we insert them one at a time and watch the heap grow. Then we remove the minimum once. Finally we take the original array of seven keys and turn it into a heap in one pass, which gives a different but equally valid heap. The states shown on the trace slides come from running the Java classes you will see; the method runningExample in the lecture's Checks class asserts the heap arrays after the inserts, after the extract-min and after the build, so you can reproduce them yourself.

---
# Cost table

| Structure | insert | peek | extractMin | build from $n$ keys |
|---|---|---|---|---|
| Unsorted array | $O(1)$ amortized | $\Theta(n)$ | $\Theta(n)$ | $\Theta(n)$ |
| Sorted array | $\Theta(n)$ worst | $\Theta(1)$ | $\Theta(1)$ | $O(n\log n)$ |
| Balanced BST | $\Theta(\log n)$ worst | $O(\log n)$ | $\Theta(\log n)$ worst | $O(n\log n)$ |
| Binary heap | $\Theta(\log n)$ worst | $\Theta(1)$ | $\Theta(\log n)$ worst | $\Theta(n)$ |

> Two obvious options come first. An unsorted array makes insert trivial but pays a full scan for every minimum. A sorted array, kept largest first so the minimum sits at the end, removes the minimum in constant time but may shift almost every key on insert. In the worst case, for example n inserts followed by n extractMins, both cost quadratic time. Read the rest of the table row by row. The balanced search tree from lectures ten and eleven already gives logarithmic insert and extract, because the minimum is the leftmost node. So why a new structure? The heap matches those bounds with a much simpler layout: a plain array, no pointers, no rotations, and constant-time peek. It also builds from n keys in linear time. The tree still wins when you need other ordered queries, like successor or range search, which a heap cannot answer efficiently. The sorted-array build is a sort, so it is order n log n with a comparison sort.

---
@type section
# The binary heap

---
# Shape: a complete binary tree

- Every level is full, except possibly the last.
- The last level is filled from the left, with no gaps.
- So the shape depends only on $n$, never on the keys.
- Height counts edges (as in CLRS): $\lfloor \log_2 n \rfloor$.
- Seven keys fill levels 0, 1 and 2 exactly: height 2.

> A binary heap has two rules: one about shape and one about keys. The shape rule says the tree is complete: fill each level left to right before starting the next. Because of that, the shape is fixed by the number of keys alone. We measure height in edges on the longest root-to-leaf path, which is the CLRS convention; some books count nodes instead and get one more. A complete tree with n nodes has height floor of log base two of n, so every path from root to leaf is short.

---
# Order: the heap property

- **Min-heap**: every key is at least its parent's key.
- So the root holds a smallest key.
- **Max-heap**: every key is at most its parent's key.
- Siblings are not ordered, and levels are not sorted.
- The rule is local: check each parent against its children.

> The second rule is about keys. In a min-heap no child is smaller than its parent. Follow any path down from the root and the keys never decrease, which is why the root must be a smallest key. A max-heap reverses the inequality. Notice what the rule does not say: nothing orders the left child against the right child, and a key deep in the left subtree can be smaller than a key near the top of the right subtree. That weakness is the whole point. It is cheap to restore after a change, because only one path is disturbed.

---
# Our heap after seven inserts

```html
<svg viewBox="-4 0 348 392" role="img" style="width:100%;max-height:64cqh;font-family:var(--sans)">
<g transform="translate(0,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">After all seven inserts</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">tree and array hold the same keys</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="297" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<circle cx="297" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="297" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="308.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
</svg>
```

> Here is where our running example will end up after inserting two, four, nine, three, one, eight and seven. At the top is the tree view; below it are the same keys in an array, with each index printed under its key. Check the heap property: one is at the root; its children are two and seven; two has children four and three; seven has children nine and eight. Every parent is smaller than its children. But the array is not sorted: seven sits at index two even though three and four are smaller. A heap is only partially ordered.

---
# Array layout, 0-based

- Store the tree level by level, left to right, in `a[0..n-1]`.
- $\text{parent}(i) = \lfloor (i-1)/2 \rfloor$ for $i > 0$.
- $\text{left}(i) = 2i+1$, $\text{right}(i) = 2i+2$.
- A child index $\ge n$ means that child does not exist.
- CLRS counts from 1: parent $\lfloor i/2 \rfloor$, children $2i$ and $2i+1$.

> Because the tree is complete, we never need pointers. Number the nodes level by level and the family relations become arithmetic. In our heap, index four holds three; its parent is four minus one, divided by two, which is index one, holding two. Index one's children are three and four. In Java, integer division of a nonnegative int already rounds down, so the code is simply i minus one over two. If you read CLRS, remember its arrays start at one, which changes all three formulas. Mixing the two conventions is a classic off-by-one bug.

---
# What heap order tells you

- The minimum is at index 0: `peek` is $\Theta(1)$.
- A second-smallest key is a child of the root.
- A maximum is at a leaf, but which leaf is unknown.
- Leaves are indices $\lfloor n/2 \rfloor$ to $n-1$: about half the keys.
- Searching for an arbitrary key can take $\Theta(n)$.

> Heap order answers exactly one question fast: what is the minimum. Some second-smallest key must be a child of the root, because every deeper key has an ancestor below the root that is no larger. Some maximum must sit at a leaf, since every internal node has a child at least as large, but it can be any leaf, and roughly half the nodes are leaves. With distinct keys, as in our example, these become exact: the second smallest is a child of the root and the maximum is a leaf. Looking for an arbitrary key gives no guidance about which subtree to try, so in the worst case you scan the whole array. That is why removing an arbitrary element from Java's priority queue costs linear time.

---
# Quick check: which is a heap?

```quiz
Which array is a valid 0-based min-heap?
- [ ] `[1, 3, 2, 4, 0]`
- [x] `[1, 5, 2, 6, 7, 3]`
- [ ] `[2, 1, 3, 4, 5]`
- [ ] `[1, 2, 3, 1, 5, 6]`
```

> The answer is the second array. Check each index against its parent at i minus one over two: five and two are above one; six and seven are above five; three is above two. In the first array, index four holds zero but its parent, index one, holds three. In the third, index one holds one below its parent two. In the last, index three holds one, but its parent is index one, holding two. Always compute the parent from the formula rather than eyeballing the levels.

---
@type section
# Insert and sift-up

---
# Insert in pseudocode

```algorithm
function INSERT(A, x):           // A is a min-heap in A[0..n-1]
  append x at A[n]; n ← n + 1    // new last leaf keeps the shape
  i ← n - 1
  while i > 0 and A[i] < A[PARENT(i)] do
    swap A[i] and A[PARENT(i)]    // move the small key up
    i ← PARENT(i)
```

- The shape is fixed first; only heap order may break.
- It can break only between `i` and its parent.

> Insert has two phases. First keep the shape rule: the only place a new node can go in a complete tree is the next free slot, the end of the array. Now the shape is right, but the new key may be smaller than its parent. So we compare it with its parent and swap while it is smaller, walking up one level at a time. We stop when the parent is not larger, or when we reach the root. Using strict less-than means equal keys stop the walk early, which saves swaps.

---
# Seven inserts, one line each

| Insert | Array after the insert | Swaps |
|---|---|---|
| 2 | `[2]` | 0 |
| 4 | `[2, 4]` | 0 |
| 9 | `[2, 4, 9]` | 0 |
| 3 | `[2, 3, 9, 4]` | 1 |
| 1 | `[1, 2, 9, 4, 3]` | 2 |
| 8 | `[1, 2, 8, 4, 3, 9]` | 1 |
| 7 | `[1, 2, 7, 4, 3, 9, 8]` | 1 |

Replay it in the [binary heap visualization](../../../visualizations/algorithms/binary_heap.html).

> This table is the whole insert phase at a glance, taken from running the Java code. The first three keys need no swaps: four and nine are both larger than their parent two. Three lands under four and swaps once. One is the interesting insert: it lands at index four and climbs all the way to the root. Eight and seven each swap once with the nine or eight above them. The next slides draw the last three rows as trees. After class, enter the same seven keys in the binary heap visualization and predict each swap before you press step.

---
# Insert 1: all the way to the root

```html
<svg viewBox="-4 0 1088 392" role="img" style="width:100%;max-height:64cqh;font-family:var(--sans)">
<g transform="translate(0,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">1 lands at index 4</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">parent 3 is larger</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="1" transform="translate(370,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">Swap: 1 is at index 1</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">parent 2 is larger</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="2" transform="translate(740,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">Swap: 1 is the root</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">stop at index 0</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
</svg>
```

> One lands at index four, a child of index one. It is smaller than three, so they swap, and one moves to index one. Its new parent is the root, holding two. One is smaller again, so they swap once more, and one becomes the root. Two swaps, which equals the height of this five-node tree. That is the worst case for sift-up: a new minimum always climbs to the root.

---
# Insert 8, then 7

```html
<svg viewBox="-4 0 718 392" role="img" style="width:100%;max-height:64cqh;font-family:var(--sans)">
<g transform="translate(0,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">8 lands at index 5,</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">swaps with parent 9</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="255" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="212" cy="232" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="1" transform="translate(370,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">7 lands at index 6,</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">swaps with parent 8</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="297" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="255" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<circle cx="297" cy="232" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="297" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="308.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
</svg>
```

> Eight goes to index five, whose parent is index two, holding nine. Eight is smaller, so they swap, and then eight's parent is the root, one, so it stops. Seven goes to index six, the right child of index two, now holding eight. Seven is smaller, so they swap; its next parent is one, so it stops. The tree is now complete with seven keys, and this is the heap we showed at the start of the section.

---
# Insert in Java

```java
public void insert(T x) {
    Objects.requireNonNull(x, "heap keys must not be null");
    a.add(x);
    siftUp(a.size() - 1);
}

private void siftUp(int i) {
    while (i > 0 && less(i, parent(i))) {
        swap(i, parent(i));
        i = parent(i);
    }
}
```

> Here is the Java version. The keys live in an ArrayList named a, so appending is add, amortized constant time. We reject null keys up front, just as Java's PriorityQueue does, because compareTo on null would throw later in a confusing place. Sift-up is the pseudocode loop almost word for word. The helper less compares the keys at two indices with compareTo, and swap exchanges two slots. Look at the loop condition: the i greater than zero test comes first, so we never compute the parent of the root.

---
# Why sift-up is correct, and its cost

- Invariant: heap order holds everywhere except maybe between `i` and its parent.
- The key that moves down was an ancestor of all its new descendants.
- The loop ends at the root or when the parent is not larger.
- At most one swap per level: $O(\log n)$ worst case, $\Theta(1)$ best case.
- Extra space: $\Theta(1)$ beyond the array.

> Why does this restore the heap? Before each swap, the only possible violation is between position i and its parent. When we swap, the new key moves up, and the old parent moves down into position i. Every key now below that position was below the old parent before the insert, so it is no larger than any of them. So the only possible violation moves one level up. When the loop stops, there is no violation left. Each iteration climbs one level, and the height is floor of log n, so insert is O of log n in the worst case; if the new key is not smaller than its parent, it costs one comparison.

---
@type section
# extractMin and sift-down

---
# extractMin in pseudocode

```algorithm
function EXTRACT-MIN(A):          // requires n ≥ 1
  min ← A[0]
  A[0] ← A[n-1]; n ← n - 1        // last leaf fills the root
  i ← 0
  while LEFT(i) < n do
    c ← child of i with the smaller key
    if A[c] ≥ A[i] then break     // heap order holds
    swap A[i] and A[c]; i ← c
  return min
```

> Removing the root leaves a hole in the worst place. The shape rule tells us which node must disappear: the last leaf. So we move the last leaf's key into the root and shrink the array. Now the shape is right, but the root key is probably too large. Sift-down walks it down: compare it with the smaller of its children and swap if that child is smaller. The loop ends at a leaf, which is when the left child index is out of range, or when both children are at least as large.

---
# extractMin on our heap

```html
<svg viewBox="-4 0 1088 392" role="img" style="width:100%;max-height:64cqh;font-family:var(--sans)">
<g transform="translate(0,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">Remove 1; last leaf 8</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">moves to the root</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="1" transform="translate(370,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">Children 2 and 7:</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">swap with smaller 2</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="2" transform="translate(740,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">Children 4 and 3:</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">swap with smaller 3</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="127" cy="232" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="none" stroke="var(--ink)" stroke-opacity="0.2" stroke-dasharray="4 4"/>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
</svg>
```

> Start from our seven-key heap. The minimum, one, is removed, and the last leaf, eight, moves to the root. Its children are two and seven; the smaller is two, and it is smaller than eight, so they swap. Now eight is at index one with children four at index three and three at index four. The smaller is three, so they swap again. Eight is now at index four, a leaf, and the loop stops. The result is two, three, seven, four, eight, nine.

---
# Why the smaller child?

- Swap with the smaller child `c`: it becomes the parent of its sibling.
- It is no larger than its sibling, so that edge is fine.
- Swapping with the larger child would put it above a smaller key.
- In our trace: swapping 8 with 7 would put 7 above 2.
- Invariant: the only possible violation is between `i` and its children.

> This is the one decision in sift-down, and it is where most bugs hide. After the swap, the child we promoted becomes the parent of its former sibling. If we promoted the smaller child, it is no larger than that sibling, so the new edge satisfies heap order. If we promoted the larger child, the smaller sibling would sit below a larger parent, breaking the heap in a place we will never visit again. In our trace, promoting seven instead of two would have placed seven above two.

---
# extractMin in Java

```java
public T extractMin() {
    T min = peek();                   // throws if empty
    T last = a.remove(a.size() - 1);  // detach the last leaf
    if (!a.isEmpty()) {
        a.set(0, last);               // it replaces the root
        siftDown(0);
    }
    return min;
}
```

- `peek()` throws `NoSuchElementException` on an empty heap.
- With one key, removing the last leaf removes the root itself.

> Two edge cases are handled here. On an empty heap, peek throws NoSuchElementException, which is the same exception java.util.Queue's remove and element methods use. With exactly one key, the last leaf is the root; after removing it the list is empty, and writing it back into index zero would resurrect it, so the if statement skips that. Removing the last element of an ArrayList is constant time because nothing shifts.

---
# siftDown in Java

```java
private void siftDown(int i) {
    int n = a.size();
    while (left(i) < n) {
        int c = left(i);                  // pick the smaller child
        if (right(i) < n && less(right(i), c)) c = right(i);
        if (!less(c, i)) return;          // heap order holds here
        swap(i, c);
        i = c;
    }
}
```

At most one swap and two comparisons per level: $O(\log n)$ worst case.

> The loop runs while i has a left child. In a complete tree, a node with no left child has no right child either, so this is the leaf test. Then pick the smaller child, checking first that the right child exists. If the smaller child is not less than the current key, heap order holds and we return; using not-less means ties stop the walk. Otherwise swap and continue from the child's position. Each level costs at most two comparisons and one swap, and there are at most floor of log n levels, so extract-min is Theta of log n in the worst case, for example when the last leaf is the largest key and sinks back to the bottom.

---
# Quick check: one more extractMin

```quiz
After the extractMin above, the heap is `[2, 3, 7, 4, 8, 9]`. What is the array after one more extractMin?
- [ ] `[3, 4, 7, 8, 9]`
- [x] `[3, 4, 7, 9, 8]`
- [ ] `[3, 7, 4, 9, 8]`
- [ ] `[9, 3, 7, 4, 8]`
```

> Remove two and move the last leaf, nine, to the root, giving nine, three, seven, four, eight. Nine's children are three and seven; the smaller is three, so they swap. Now nine is at index one, whose children are index three holding four and index four holding eight. The smaller is four, so nine swaps with four. The result is three, four, seven, nine, eight. The first option looks sorted, but heaps do not sort their arrays; the last option forgot to sift down at all.

---
@type section
# Build-heap and heapsort

---
# Two ways to build from $n$ keys

- **Repeated insert**: $n$ sift-ups, $O(n \log n)$ in total.
- Worst case: keys arrive in decreasing order.
- Each new key is the new minimum and climbs to the root.
- **Bottom-up build**: put all keys in the array, then fix it.
- Sift down each internal node, from the last one to the root.

> Often we have all the keys at once, for example to start heapsort, or to load a scheduler with today's jobs. Inserting them one by one works, but in the worst case every insert climbs all the way: if the keys arrive in decreasing order, each new key is smaller than everything before it. The total is the sum of floor of log two of i for i up to n, which is Theta of n log n. The bottom-up method does something smarter: it leaves the array as is and repairs it from the bottom, where most nodes are.

---
# Bottom-up build

```algorithm
function BUILD-HEAP(A):             // any array A[0..n-1]
  for i ← ⌊n/2⌋ - 1 downto 0 do     // last internal node first
    SIFT-DOWN(A, i)
```

```java
for (int i = h.a.size() / 2 - 1; i >= 0; i--) {
    h.siftDown(i);
}
```

> The algorithm is two lines. Indices from floor of n over two to n minus one are leaves, and a single node is already a heap, so we skip them. We start at the last internal node and move left toward the root, sifting each one down. The Java version is the loop inside the static buildHeap method; h is a new heap whose list was filled with the input keys in their original order. Going from right to left is essential: when we sift down node i, both of its subtrees have already been made into heaps.

---
# Build from `[2, 4, 9, 3, 1, 8, 7]`, part 1

```html
<svg viewBox="-4 0 718 392" role="img" style="width:100%;max-height:64cqh;font-family:var(--sans)">
<g transform="translate(0,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">Start: the input array</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">indices 3 to 6 are leaves</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="297" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="297" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="297" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="308.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="1" transform="translate(370,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">siftDown(2): children 8, 7</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">9 swaps with 7</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="297" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="255" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="297" cy="232" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="297" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="308.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
</svg>
```

> Here is bottom-up build on the same seven keys, now in their original arrival order. Seven keys means internal nodes zero, one and two; we start at index two. Nine has children eight and seven. The smaller is seven, and it is smaller than nine, so they swap. Index two's subtree is now a heap. The leaves at indices three through six were never touched.

---
# Build from `[2, 4, 9, 3, 1, 8, 7]`, part 2

```html
<svg viewBox="-4 0 718 392" role="img" style="width:100%;max-height:64cqh;font-family:var(--sans)">
<g transform="translate(0,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">siftDown(1): children 3, 1</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">4 swaps with 1</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="297" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="85" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="127" cy="232" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="297" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="297" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="308.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
<g data-step="1" transform="translate(370,0)">
<text x="170.0" y="24" text-anchor="middle" font-size="22" fill="var(--ink)">siftDown(0): 2 swaps with 1</text>
<text x="170.0" y="52" text-anchor="middle" font-size="22" fill="var(--ink)">then 2 ≤ 3: stop</text>
<line x1="170" y1="100" x2="85" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="170" y1="100" x2="255" y2="166" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="42" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="85" y1="166" x2="127" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="212" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<line x1="255" y1="166" x2="297" y2="232" stroke="var(--ink)" stroke-opacity="0.45" stroke-width="2"/>
<circle cx="170" cy="100" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="170" y="108" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<circle cx="85" cy="166" r="23" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="85" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<circle cx="255" cy="166" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="255" y="174" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<circle cx="42" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="42" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<circle cx="127" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="127" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<circle cx="212" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="212" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<circle cx="297" cy="232" r="23" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="297" y="240" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<rect x="9.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="32.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">1</text>
<text x="32.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">0</text>
<rect x="55.0" y="318" width="46" height="44" fill="var(--amber)" stroke="var(--amber-ink)" stroke-width="2"/>
<text x="78.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">2</text>
<text x="78.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">1</text>
<rect x="101.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="124.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">7</text>
<text x="124.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">2</text>
<rect x="147.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="170.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">3</text>
<text x="170.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">3</text>
<rect x="193.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="216.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">4</text>
<text x="216.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">4</text>
<rect x="239.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="262.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">8</text>
<text x="262.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">5</text>
<rect x="285.0" y="318" width="46" height="44" fill="var(--paper)" stroke="var(--ink)" stroke-width="2"/>
<text x="308.0" y="349" text-anchor="middle" font-size="24" fill="var(--ink)">9</text>
<text x="308.0" y="384" text-anchor="middle" font-size="17" fill="var(--ink)" fill-opacity="0.65">6</text>
</g>
</svg>
```

> Next is index one, holding four, with children three and one. The smaller is one, so four and one swap, and four lands at a leaf. Last is the root, holding two, with children one and seven. One is smaller, so they swap, and two moves to index one. Its children there are three and four; three is not smaller than two, so sift-down stops. The result is one, two, seven, three, four, eight, nine, built with three swaps.

---
# Heaps are not unique

| Built by | Array |
|---|---|
| Seven inserts in order | `[1, 2, 7, 4, 3, 9, 8]` |
| Bottom-up build | `[1, 2, 7, 3, 4, 8, 9]` |

- Both satisfy heap order on the same seven keys.
- Many arrangements are valid; tests should check the property, not one array.

> Compare the two results. Both have one at the root and every parent below its children, but the leaves differ: the insert version has four before three and nine before eight. Neither is more correct. This matters when you test heap code: compare against the heap property, or compare the sequence that repeated extract-min produces, not one specific array. The checks for this lecture compare our heap's extract-min sequence with java.util.PriorityQueue on random data for exactly this reason.

---
# Why bottom-up build is correct

- Invariant: before handling index `i`, every node after `i` roots a heap.
- Initially true: every node after $\lfloor n/2 \rfloor - 1$ is a leaf.
- Sift-down at `i` merges two heaps under one root into one heap.
- Moving left keeps the invariant for `i - 1`.
- At the end, index 0 roots a heap: the whole array.

> The proof is a loop invariant, as in CLRS chapter six. Before the loop body runs for index i, every node with a larger index is the root of a valid heap. At the start, those nodes are all leaves, and a leaf alone is a heap. When we sift down i, both of its children are roots of heaps, which is exactly the situation sift-down was designed for, so i becomes the root of a heap too. Nodes after i are unaffected except inside i's own subtree. When i reaches zero, the whole array is a heap.

---
# Why the build is $O(n)$

- Sift-down from a node of height $h$ costs $O(h)$.
- At most $\lceil n/2^{h+1} \rceil$ nodes have height $h$.
- Most nodes are near the bottom, where sift-down is short.

$$\sum_{h=0}^{\lfloor \log_2 n \rfloor} \left\lceil \frac{n}{2^{h+1}} \right\rceil O(h) = O\Big(n \sum_{h \ge 0} \frac{h}{2^h}\Big) = O(n)$$

- The series $\sum_{h\ge 0} h/2^h$ equals $2$, a constant.

> Here is the proof sketch. A naive bound says n over two sift-downs, each up to log n, so n log n. But that overcounts: about half the nodes are leaves and do no work, a quarter have height one and do at most one swap, an eighth have height two, and so on. Only the root has the full height. Weighting each height by the number of nodes at that height gives a sum whose terms shrink geometrically. The series of h over two to the h converges to two, so the total is linear. Since every key must be read at least once, the build is Theta of n. Compare repeated insert: sift-up cost depends on depth, and most nodes are deep, while sift-down cost depends on height, and most nodes are short.

---
# Heapsort in Java

```java
public static <T extends Comparable<? super T>> void sort(T[] a) {
    int n = a.length;
    for (int i = n / 2 - 1; i >= 0; i--) {
        siftDown(a, i, n);            // build a max-heap in O(n)
    }
    for (int end = n - 1; end > 0; end--) {
        swap(a, 0, end);              // largest key to its final slot
        siftDown(a, 0, end);          // restore the heap on a[0..end-1]
    }
}
```

> Heapsort reuses everything from today, with the comparison flipped. First build a max-heap in place, bottom-up, in linear time. Then the largest key sits at index zero. Swap it with the last slot of the heap region: that key is now in its final sorted position. Shrink the heap region by one and sift down the new root. Repeat until one key is left. This helper siftDown takes the heap size as a parameter, because the sorted suffix grows while the heap shrinks. On our seven keys it produces one, two, three, four, seven, eight, nine.

---
# Heapsort properties

- Worst case $\Theta(n \log n)$: $n-1$ sift-downs of $O(\log n)$ each.
- In place: $\Theta(1)$ extra space, no recursion.
- Not stable: long-distance swaps can reorder equal keys.
- Example: records `1a, 1b` sort to `1b, 1a` with this code.
- Visualization: [heapsort](../../../visualizations/algorithms/heapsort.html).

> Compared with lecture four, heapsort combines merge sort's worst-case guarantee with quicksort's in-place memory use. The n minus one sift-downs give the upper bound of order n log n, and no comparison sort can beat n log n comparisons in the worst case, so heapsort's worst case is Theta of n log n. Unlike quicksort it has no bad pivot inputs, and unlike merge sort it needs no extra array. The price is stability. With two equal keys, the build phase does nothing, and the extraction phase swaps the root with the last slot, reversing their order; our tests check that exact case. Link to the visualization to watch the sorted suffix grow from the right.

---
@type section
# Priority queues in practice

---
# `java.util.PriorityQueue`

- Implemented as a heap (a binary heap stored in an array in current OpenJDK).
- Head is the least element by natural order, or by a `Comparator`.
- Ties for the least element are broken arbitrarily.
- `offer`, `poll`, `add`, `remove()`: $O(\log n)$.
- `peek`, `element`, `size`: constant time.
- `remove(Object)` and `contains`: linear time.

> Java's standard library already has what we built. The class is built on a heap; in current OpenJDK that is a binary heap in an array, just like ours. Its API documentation says the head is the least element according to natural ordering or the comparator you pass, and that ties are broken arbitrarily. The costs on the slide come from the implementation note in that API documentation: logarithmic for offer, poll, add and remove with no argument; constant for peek, element and size; linear for removing or finding an arbitrary object. That last line should not surprise you now: heap order gives no guidance for finding an arbitrary key.

---
# Using it

```java
PriorityQueue<Integer> maxPq =
        new PriorityQueue<>(Comparator.reverseOrder());
```

- Default: a min-heap on `compareTo`. A reversed `Comparator` gives a max-heap.
- Nulls are not permitted; it is not synchronized (not thread-safe).
- Iterating or printing it does **not** give sorted order.
- Changing a stored element's key silently breaks the heap.
- To drain in order: call `poll()` until it returns `null`.

> By default PriorityQueue is a min-heap on natural ordering; pass Comparator.reverseOrder to get a max-heap, or any comparator for objects. The API says the iterator is not guaranteed to traverse the elements in any particular order, so printing the queue shows the heap array, not sorted output. Beginners often read that output and think the queue is broken. Another trap is mutating a field that the comparator reads while the object is inside the queue: the heap is never told, so later polls can return the wrong element. Remove it, change it, and offer it again.

---
# No decreaseKey: the lazy workaround

- `PriorityQueue` cannot lower the key of a stored element.
- Workaround: **insert a new entry** with the smaller key.
- The old entry stays behind, now **stale**.
- When polled, skip any entry that no longer matches the current key.
- Cost: the queue may hold more entries than items.

> Some algorithms, most famously Dijkstra's shortest paths in lecture seventeen, need to lower the key of an item already in the queue. Java's PriorityQueue has no such operation, and removing the object first costs linear time. The standard trick is to not remove anything. Keep the current best key for each item in an array, push a new entry whenever the key improves, and when an entry comes out of the queue, check it against the array. If it does not match, it is a leftover from before the improvement, so skip it.

---
# The lazy workaround in Java: offer

```java
private record Entry(int item, int key) { }
private final int[] best;       // current key of each item
private final boolean[] done;   // item already polled
private final boolean[] seen;   // item offered at least once

public void offerOrDecrease(int item, int key) {
    if (done[item] || (seen[item] && key >= best[item])) return;
    seen[item] = true;
    best[item] = key;
    pq.offer(new Entry(item, key));   // any older entry is now stale
}
```

- `pq` is a `PriorityQueue<Entry>` ordered by `key`.

> Items are numbered zero to n minus one. Each entry in the queue pairs an item with the key it had when the entry was pushed. The best array holds each item's current key, seen marks items offered at least once, and done marks items already returned. Offer-or-decrease ignores keys that are not an improvement on an item already seen. Checking seen, rather than comparing with a sentinel such as Integer.MAX_VALUE, means even that largest key is accepted on a first offer. Otherwise it records the new key and pushes a fresh entry, leaving the old entry in the heap, where it has become stale. Nothing is searched for or removed, so this costs one logarithmic offer.

---
# The lazy workaround in Java: poll

```java
public int pollItem() {
    while (!pq.isEmpty()) {
        Entry e = pq.poll();
        if (done[e.item()] || e.key() != best[e.item()]) {
            staleSkipped++;           // stale: a newer entry replaced it
            continue;
        }
        done[e.item()] = true;
        return e.item();
    }
    return -1;
}
```

> Poll-item pops entries until it finds one whose key still matches best and whose item is not done; anything else is a leftover and is skipped. Stale entries always have a larger key than the current one, so the current entry comes out first and the stale ones trail behind. With m successful offers in total, the queue holds at most m entries, so each offer costs O of log m. Each entry is polled at most once, so all polls together cost O of m log m, which is O of log m amortized per operation. In Dijkstra's algorithm, m is bounded by the number of edges plus one, which we will use in lecture seventeen.

---
# Quick check: printing a queue

```quiz
You add `3, 1, 2, 5` to a `java.util.PriorityQueue<Integer>` and print it with `System.out.println(pq)`. What does the API guarantee about the printed order?
- [ ] It prints `[1, 2, 3, 5]`, sorted ascending
- [ ] It prints the insertion order `[3, 1, 2, 5]`
- [x] Only that all four elements appear; the order is not specified
- [ ] It prints `[5, 3, 2, 1]`, sorted descending
```

> Printing uses the iterator, and the API documentation says that the iterator is not guaranteed to traverse the elements in any particular order. In current OpenJDK this prints one, three, two, five: the heap array, which starts with the minimum but is not sorted. The only order the class promises is that each poll returns a least remaining element. If you need sorted output, poll repeatedly, or copy the elements into a list and sort it.

---
# Scheduling and event simulation

- **Scheduler**: jobs keyed by priority; run `extractMin`, insert new arrivals.
- **Event simulation**: events keyed by timestamp.
- Loop: poll the earliest event, advance the clock, handle it.
- Handling an event may insert future events.
- Each event costs $O(\log m)$ with $m$ pending events.

> Two classic uses share one loop. A scheduler holds ready jobs keyed by priority and always runs the most urgent; new jobs are inserted as they arrive. A discrete-event simulation, such as modelling customers at a bank or packets in a network, holds future events keyed by time. It repeatedly removes the earliest event, sets the simulated clock to its time, and handles it, which often schedules new events later in time. Because we always take the minimum timestamp, the simulation processes events in time order without ever sorting the whole list.

---
# Top-k with a size-$k$ heap

```java
PriorityQueue<Integer> heap = new PriorityQueue<>();   // min-heap
for (int x : values) {
    if (k == 0) break;
    if (heap.size() < k) {
        heap.offer(x);
    } else if (x > heap.peek()) {                      // beats the weakest kept
        heap.poll();
        heap.offer(x);
    }
}
```

- To keep the $k$ **largest**, use a **min**-heap: its root is the weakest kept.
- $O(n \log k)$ time and $O(k)$ space, versus $O(n \log n)$ to sort.

> Suppose you want the ten highest scores out of millions, arriving as a stream. Keep a min-heap of at most k values. Its root is the smallest value you are keeping, the one most likely to be pushed out. Each new value is compared with the root; if it is larger, the root is replaced. Every heap operation is on at most k items, so the total is O of n log k, and memory is O of k no matter how long the stream. On our seven keys with k equal to three, the result is nine, eight, seven. The counterintuitive part is using a min-heap to find the largest values.

---
# Merging $k$ sorted lists

- Put the first element of each list in a min-heap: $k$ entries.
- Repeat: `extractMin`, output it, insert the next element of its list.
- The heap never holds more than $k$ entries.
- $N$ total elements: $O(N \log k)$ time, $O(k)$ extra space.
- Uses: combining sorted files, sorted runs in external sorting.

> Merge sort from lecture four merged two sorted lists by comparing their fronts. With k lists, comparing all k fronts each time would cost k per output element. A min-heap of the k fronts does the same job in log k per element. Each heap entry needs to remember which list it came from, so that after extracting it we can insert that list's next element. This is how large sorted files are combined when they do not fit in memory: sort chunks, write them out, then merge the chunks with a heap.

---
# Common mistakes and edge cases

- `peek` or `extractMin` on an empty heap: throw, or return `null` as `poll` does.
- One element: the last leaf is the root; do not write it back.
- Mixing 0-based formulas with CLRS 1-based ones.
- Sift-down with the larger child, or forgetting the right child may not exist.
- Duplicates are fine; ties may come out in any order.
- Expecting `PriorityQueue` iteration or `toString` to be sorted.

> Each of these has cost students hours. Decide how the empty case behaves, and be consistent: our heap throws, while Java's poll returns null and its remove throws. Handle the single-element extract, or you will resurrect the removed key. Write the index formulas once, in helpers, and test them. In sift-down, always pick the smaller child and check that the right child index is in range. Remember that heaps are not stable: equal keys come out in no guaranteed order, so if order among ties matters, add a tie-breaking field such as an arrival counter.

---
@type section
# Wrap-up

---
# Summary

| Idea | Takeaway |
|---|---|
| Priority queue | insert, peek, extractMin; decreaseKey is extra |
| Binary heap | Complete tree plus heap order, stored in an array |
| 0-based indices | parent $\lfloor (i-1)/2 \rfloor$, children $2i+1$, $2i+2$ |
| insert / extractMin | One path: $O(\log n)$ worst case |
| Bottom-up build | $\Theta(n)$, by the sum of heights |
| `PriorityQueue` | Min-heap; linear `remove(Object)`; unsorted iteration |

> A priority queue is the question "what is most urgent right now". A binary heap answers it with a complete tree in an array and a local ordering rule. Insert and extract-min each repair one path, so they cost logarithmic time; building from scratch costs linear time because most nodes are near the bottom. Java's PriorityQueue is a ready-made binary heap, and the lazy trick fills its one gap. Next time we start graphs; the heap comes back in lecture seventeen, inside Dijkstra's algorithm.

---
# Check yourself

- Insert `5, 3, 8, 1` into an empty min-heap. Draw the tree and array after each insert.
- Why does bottom-up build start at index $\lfloor n/2 \rfloor - 1$ and move left, not right?
- You need the 100 smallest of $10^7$ numbers. Which heap, what size, and what cost?

> Work these without looking at the slides, then check them with the visualizer or with the Java code. For the first, predict every swap before drawing it. For the second, think about what sift-down assumes about the two subtrees it is given. For the third, mirror the top-k slide: to keep the smallest values, the root should be the largest value you are keeping.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 6: Heapsort (heaps, build-heap, heapsort, priority queues).
- Java SE API documentation: `java.util.PriorityQueue`, `java.util.Comparator`.
- Visualizations: [binary heap](../../../visualizations/algorithms/binary_heap.html), [heapsort](../../../visualizations/algorithms/heapsort.html).
- Code: the `l14` Java package for this lecture (`MinHeap`, `Heapsort`, `TopK`, `LazyQueue`).

> The heap material, including the build-heap analysis and the loop-invariant proof, follows CLRS chapter six, adapted here to 0-based indices. Library facts about PriorityQueue, including its costs and its unspecified iteration order, come from the Java SE API documentation. The examples and code are original to this lecture and were checked against java.util.PriorityQueue on random inputs.
