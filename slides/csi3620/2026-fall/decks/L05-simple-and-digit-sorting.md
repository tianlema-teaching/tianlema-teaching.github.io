@title Lecture 5: Sorting with simple loops and digits
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Sorting with simple loops and digits
## Grow a sorted region, then read digits

---
# The same five records

- Original input: `[32a, 14, 32b, 21, 13]`.
- Sorted keys read `[13, 14, 21, 32, 32]`.
- Stability keeps `32a` before `32b`.
- Every algorithm below restarts from the original.

```diagram
@dir LR
P0[0: 32a] -> P1[1: 14] -> P2[2: 32b] -> P3[3: 21] -> P4[4: 13]
```

Positions count from 0, so the last of five sits at 4.

> The letters mean exactly what they meant before: thirty-two a arrived before thirty-two b, and both carry key thirty-two. Comparisons read the numbers only. Ending with b first can still be numerically sorted while failing stability. Copy the original array with positions zero through four, and keep it apart from each working copy.

---
# Same pictures, same shorthand

- Boxes are array states. Arrows are steps.
- Amber boxes hold the held-aside record.
- `←` stores. `key()` reads the number. `A[i]` is position `i`.
- Counting starts at `0`. `//` is a hint, not a command.

> These shorthands match the previous lecture exactly. Pseudocode slides state the rules, and the pictures after them trace those same lines. When a box glows amber, look there first.

---
# Three ways to grow a sorted region

| Algorithm | Repeated operation | Region that finishes |
|---|---|---|
| Bubble | Swap adjacent out-of-order records | Right end |
| Selection | Find the smallest remaining record | Left end |
| Insertion | Insert the next record into sorted order | Left prefix |

> All three repeat a small action but finish different sides. Bubble settles large records right. Selection chooses the next smallest for the left. Insertion keeps everything seen so far in order. The word finished means that region sits out the later passes.

---
@type section
# Bubble sort

---
# Bubble idea in one pass

```diagram
@dir LR
N0[compare neighbours] -> N1[swap if left is bigger]
N1 -> N2[a maximum reaches the end]
N2 -> N3[next pass stops one earlier]
N3 -> N4[a pass with no swaps is done]
```

> Picture a large record walking right through its neighbours: each smaller key swaps it one step right, each larger key takes over the walk. Equal keys never swap because the test is strict. By the pass end a maximum sits at the edge, and a swap-free pass certifies the whole active region.

---
# Bubble sort in pseudocode
## Strict swap; a quiet pass stops

```algorithm
function BUBBLE-SORT(A):         // biggest record per pass
  for end ← len(A) - 1 downto 1 do
    swapped ← false
    for j ← 0 to end - 1 do
      if key(A[j]) > key(A[j+1]) then  // strict: ties never swap
        swap A[j] and A[j+1]; swapped ← true
    if swapped = false then return     // a quiet pass certifies order
```

- `end` is where the active region stops: it walks $4, 3, 2, 1$ on our five records.
- The flag answers one question: did anything move this pass?

> The outer line walks the active edge leftward while the inner line compares neighbours up to it. The flag resets each pass and trips on any swap, so a full pass with no swaps proves every neighbour pair ordered. Strictness is the stability story: equal records never exchange, so they cannot pass one another. The next three pictures trace these lines on our five records.

---
# Bubble first pass, step by step
## Start: [32a, 14, 32b, 21, 13]

```diagram
@dir TB
@reveal manual
S0[32a, 14, 32b, 21, 13]
focus S0
--- Compare neighbours from the left.
S0 -> S1[14, 32a, 32b, 21, 13] : 32a bigger, swap
focus S0 S1
--- 14 moves left past 32a.
S1 -> S2[same array] : equal 32s, keep
focus S1 S2
--- The two thirty-twos meet and stay put.
S2 -> S3[14, 32a, 21, 32b, 13] : 32b bigger, swap
focus S2 S3
--- 32b walks right past 21.
S3 -> S4[14, 32a, 21, 13, 32b | 4 done] : 32b bigger, swap
focus S3 S4
--- 32b passes 13 and parks at index 4.
```

> Chase the compared pair rightward after every step. Four comparisons, three swaps. The last record now sits in a valid final spot while the first four still need work. Mark the finished suffix apart on your page.

---
# Bubble second pass, step by step
## Start: [14, 32a, 21, 13, 32b]

```diagram
@dir TB
@reveal manual
P0[14, 32a, 21, 13 | 32b done]
focus P0
--- The last record sits out. Three comparisons remain.
P0 -> P1[same array] : 14 and 32a in order
focus P0 P1
--- First pair needs nothing.
P1 -> P2[14, 21, 32a, 13 | 32b done] : 32a bigger, swap
focus P1 P2
--- 32a walks right past 21.
P2 -> P3[14, 21, 13, 32a | 32b done] : 32a bigger, swap
focus P2 P3
--- 32a passes 13. Indices 3 and 4 are finished.
```

> The finished record is never compared again. Fourteen and thirty-two a stand ordered, then thirty-two a swaps twice toward the active edge. Both thirty-twos now sit in final spots in their original order.

---
# Bubble final passes, step by step

```diagram
@dir TB
@reveal manual
F0[14, 21, 13 | 32a, 32b done]
focus F0
--- Two pairs left, then one.
F0 -> F1[same array] : 14 and 21 in order
focus F0 F1
--- First pair of pass 3 needs nothing.
F1 -> F2[14, 13, 21 | 32a, 32b done] : 21 bigger, swap
focus F1 F2
--- 21 and 13 swap.
F2 -> F3[13, 14, 21 | 32a, 32b done] : 14 bigger, swap
focus F2 F3
--- Pass 4 swaps 14 and 13. Ten comparisons and seven swaps total.
```

> Pass three checks two pairs, pass four checks one, adding up with the earlier passes to four plus three plus two plus one comparisons. Swaps total three plus two plus one plus one. This input never produces a swap-free pass, so the early-stop flag saves nothing here.

---
# Why bubble sort works

- Each comparison pushes the bigger key right.
- A full pass parks an active-region maximum at its edge.
- Later passes leave the finished suffix alone.
- A swap-free pass certifies the region is ordered.
- Equal keys never swap, so equal order survives.

> Within a pass the largest key seen so far always reaches the right of the compared pair. Across passes the suffix holds finished large records, so shrinking the active region is safe. Adjacent strict swaps are the only motion, and equal records can never pass each other without one.

---
# Bubble sort costs

```diagram
@dir LR
P1[pass 1: 4 checks] -> P2[pass 2: 3] -> P3[pass 3: 2] -> P4[pass 4: 1] -> T[10 total]
```

| Input situation | Work for this arrangement |
|---|---|
| Already sorted | $\Theta(n)$ |
| Random distinct order | Average $\Theta(n^2)$ |
| Reverse distinct order | Worst $\Theta(n^2)$ |

Auxiliary space: $\Theta(1)$. The linear best case needs the swap flag.

> A sorted input still needs one full pass of $n-1$ comparisons before the flag certifies order: linear, not constant. Hard inputs sum the shrinking pass lengths quadratically. The arrangement keeps a few indices plus one flag, with no list growing in `n`. Average here means uniformly random distinct keys.

---
@type section
# Selection sort

---
# Selection idea in one pass

```diagram
@dir LR
K0[scan the suffix] -> K1[remember the smallest index]
K1 -> K2[swap it into place]
K2 -> K3[prefix grows by one]
```

> Looking and moving stay separate: the scan only updates a remembered index while the array sits still, and one swap at the end places the minimum. That swap can leap across many records, which is why equal-key order is at risk even though comparisons are strict.

---
# Selection sort in pseudocode
## Remember the minimum, then one leap

```algorithm
function SELECTION-SORT(A):            // finished prefix grows by one
  for i ← 0 to len(A) - 2 do
    m ← i                              // m remembers the minimum index
    for j ← i + 1 to len(A) - 1 do
      if key(A[j]) < key(A[m]) then    // strict: first minimum wins
        m ← j
    if m ≠ i then swap A[i] and A[m]   // a single leap per pass
```

- `m` remembers a *position*, not a value; the array sits still until the final swap.

> Looking and moving stay apart: the inner scan only rewrites m while the array sits still, and the swap lands after the whole suffix has been seen. Strictness keeps the first minimum, yet the leap can jump across an equal record, which is exactly how our run reverses the two thirty-twos. The next three pictures trace these lines on our five records.

---
# Selection search, step by step

```diagram
@dir TB
@reveal manual
C0[candidate 0 | 32a]
focus C0
--- Start by pointing at index 0.
C0 -> C1[candidate 1 | 14] : 14 smaller
focus C0 C1
--- 14 replaces 32a.
C1 -> C2[still candidate 1] : 32b bigger
focus C1 C2
--- 32b cannot replace 14.
C2 -> C3[still candidate 1] : 21 bigger
focus C2 C3
--- 21 cannot replace 14 either.
C3 -> C4[candidate 4 | 13] : 13 smaller
focus C3 C4
--- 13 becomes the minimum. No swaps happened yet.
```

> Four comparisons, zero motion: the original order sits untouched while only the remembered index changes. The scan now knows enough to place the minimum, and only now does the array change.

---
# Selection first swaps, step by step

```diagram
@dir TB
@reveal manual
D0[32a, 14, 32b, 21, 13]
focus D0
--- Minimum of the whole array sits at index 4.
D0 -> D1[13, 14, 32b, 21, 32a] : swap 0 and 4
focus D0 D1
--- 13 lands at 0. 32a leaps past 32b.
D1 -> D2[13, 14 | rest to scan] : 14 beats all three
focus D1 D2
--- Position 1 scans 32b, 21, 32a and keeps 14. Skip the self-swap.
```

> The first exchange already moves thirty-two a across thirty-two b, so instability is on the table early. The second pass finds fourteen smaller than every later key, so the arrangement skips its swap and the prefix `[13, 14]` is finished.

---
# Selection rest, step by step

```diagram
@dir TB
@reveal manual
E0[13, 14, 32b, 21, 32a]
focus E0
--- Position 2 starts with 32b.
E0 -> E1[13, 14, 21, 32b, 32a] : 21 wins, swap 2 and 3
focus E0 E1
--- 21 replaces 32b as candidate, then swaps into place.
E1 -> E2[same array] : equal 32s keep first
focus E1 E2
--- Position 3 ties, so it keeps its candidate and skips the swap.
```

> Pass lengths stay four plus three plus two plus one comparisons: ten total. Only the first and third passes swap different positions, hence two swaps. The strict test keeps the first equal candidate, yet the long leaps can still reverse equal order, as the final `[13, 14, 21, 32b, 32a]` shows.

---
# Correctness and instability

- The candidate is the smallest key seen so far.
- Its final pick is the minimum of the whole suffix.
- Placing it extends the finished prefix by one.
- A long swap can carry a record past an equal one.
- This run ends `[13, 14, 21, 32b, 32a]`: unstable.

> Assume the prefix holds the smallest records in order. The scan's minimum belongs next, so placing it extends the invariant by one and repeating finishes the sort. Stability fails independently of the strict test: one leap can jump an equal record.

---
# Selection sort costs

- Comparisons: $(n-1)+(n-2)+\cdots+1=n(n-1)/2$.
- Best, average and worst work: $\Theta(n^2)$.
- At most `n-1` swaps, skipping self-swaps.
- Auxiliary space: $\Theta(1)$.
- Ordered input still scans every suffix.

> Loop bounds ignore input order, so the comparison count is fixed for a given length. Skipped swaps save motion, never scans. Few swaps matter when moving a record costs far more than comparing keys.

---
@type section
# Insertion sort

---
# Insertion idea in one card

```diagram
@dir LR
I[hold the next record] -> S[shift bigger prefix right]
S -> P[drop it into the gap]
P -> G[sorted prefix grows by one]
```

> Picture adding a card to a sorted hand: hold the new card aside, slide bigger cards right until its slot opens, then place it. The hold matters because shifting overwrites the card's old cell. Shifting stops on equal keys, which is what keeps equal order.

---
# Insertion sort in pseudocode
## Hold the card, shift, drop it in

```algorithm
function INSERTION-SORT(A):      // prefix before i stays sorted
  for i ← 1 to len(A) - 1 do
    item ← A[i]; j ← i - 1       // hold the next record aside
    while j ≥ 0 and key(A[j]) > key(item) do  // strict: ties stop
      A[j+1] ← A[j]; j ← j - 1   // slide bigger records right
    A[j+1] ← item                // drop into the gap
```

> The hold exists because shifting overwrites the record's old cell: item keeps it safe while the scan walks left. Only strictly bigger keys slide, so a newcomer lands after earlier equals and stability survives. The guard order matters: the boundary test runs first, so the scan never reads past the left edge. The next three pictures trace these lines on our five records.

---
# Insertion adds 14 and 32b, step by step

```diagram
@dir TB
@reveal manual
A0[32a, 14, 32b, 21, 13 | hold 14].amber
focus A0
--- Hold 14 aside before shifting.
A0 -> A1[32a, 32a, 32b, 21, 13] : shift 32a right
focus A0 A1
--- 32a appears twice. 14 is safe in the hold.
A1 -> A2[14, 32a, 32b, 21, 13] : place 14 at 0
focus A1 A2
--- The gap closes at index 0.
A2 -> A3[same array] : 32b meets equal 32a
focus A2 A3
--- Equality stops the shift. Prefix of three is sorted.
```

> The doubled thirty-two a shows real array contents mid-shift, not an extra record: the hold plus the gap explain the pending state. Thirty-two b faces an equal left neighbour, the strict test fails, and it lands back where it started.

---
# Insertion adds 21, step by step

```diagram
@dir TB
@reveal manual
B0[14, 32a, 32b, 21, 13 | hold 21].amber
focus B0
--- Hold 21. Scan the sorted prefix right to left.
B0 -> B1[14, 32a, 32b, 32b, 13] : shift 32b
focus B0 B1
--- 32b is bigger and slides right.
B1 -> B2[14, 32a, 32a, 32b, 13] : shift 32a
focus B1 B2
--- 32a is bigger and slides right too.
B2 -> B3[14, 21, 32a, 32b, 13] : 14 smaller, place here
focus B2 B3
--- 14 stops the scan. 21 lands right after it. Both 32s kept order.
```

> The two thirty-twos each moved right without changing their relative order. Try covering the last row and inserting thirteen yourself first: hold it aside, write every middle array, then compare with the next picture.

---
# Insertion adds 13, step by step

```diagram
@dir TB
@reveal manual
G0[14, 21, 32a, 32b, 13 | hold 13].amber
focus G0
--- Hold 13. Everything left of it is bigger.
G0 -> G1[14, 21, 32a, 32b, 32b] : shift 32b
focus G0 G1
--- 32b slides right first.
G1 -> G2[14, 21, 32a, 32a, 32b] : shift 32a
focus G1 G2
--- Then 32a slides right.
G2 -> G3[14, 21, 21, 32a, 32b] : shift 21
focus G2 G3
--- Then 21 slides right.
G3 -> G4[14, 14, 21, 32a, 32b] : shift 14
focus G3 G4
--- Finally 14 slides right. The gap sits at index 0.
G4 -> G5[13, 14, 21, 32a, 32b] : place 13 at 0
focus G4 G5
--- Drop 13 into the gap. Seven shifts and four placements total.
```

> Shift right to left so no unread record is overwritten. The doubled entries are genuine middle states; the hold explains what is still missing. Across all four insertions the shift counts were one, zero, two and four, with one placement each. Equal records kept their order throughout.

---
# Why insertion sort works

- The prefix before `i` is already sorted.
- Shifting keeps bigger records in their relative order.
- Placement lands after all smaller or equal keys.
- The sorted prefix grows by one record.
- The hold saves the record that shifting overwrites.

> A one-record prefix starts the invariant. Sliding bigger records right opens exactly one slot without reordering them, and the held record fills it, giving a longer sorted prefix. Equal keys never shift, so each newcomer lands after earlier equals while no record is lost.

---
# One inversion, one shift

```diagram
@dir TB
@reveal manual
I[32a, 14 | wrong order]
focus I
--- One pair out of order: one inversion.
I -> F[14, 32a] : one shift erases it
focus F
--- Each shift erases exactly one inversion.
```

- An **inversion** is one pair out of order: $[3, 1, 2]$ has two ($3>1$, $3>2$).
- Each shift erases exactly one inversion: cost is $\Theta(n+I)$ for inversion count `I`.
- Our input carries seven inversions and seven shifts.

> An inversion is a pair out of order relative to position. Each shift removes exactly one, so this arrangement costs Theta of n plus I for inversion count I. Our input carries seven inversions and seven shifts.

---
# Insertion sort costs

- Sorted input: one failed test per insertion, $\Theta(n)$.
- Reverse distinct order: $\Theta(n^2)$ shifts and work.
- Random distinct order: average $\Theta(n^2)$.
- Auxiliary space: $\Theta(1)$. This version is stable.

> Sorted input runs each insertion into one failed test: linear. Reverse order shifts every earlier record each time: quadratic. Random distinct keys average a quadratic pile of inversions, hence quadratic work. Nearly sorted inputs feel cheap because nearly sorted means few inversions.

---
@type section
# Radix sort

---
# Radix idea with two digits

```diagram
@dir LR
K[key 32] -> O[ones column first] -> T[tens column next]
```

- Keys are nonnegative integers with at most `d` base-`b` digits.
- Handle the least significant digit first.
- Drop records into digit buckets in current order.
- Collect buckets in digit order, keeping order inside each.
- Here base `10` with exactly `2` passes.

> This method reads digits instead of comparing keys against each other. First group by ones, then by tens. Picture a post office sorting mail into ten bins by the last zip digit, stacking the bins in order, then sorting that stack again by the next digit. Stability inside each bucket is load-bearing: later passes must preserve the order earlier digits built. Buckets are plain lists that keep encounter order.

---
# What ÷ and mod read from 32

| `place` | `32 ÷ place`, dropping the rest | `mod 10` keeps | Digit read |
|---|---|---|---|
| `1` (ones column) | `32` | `2` | Ones digit is `2` |
| `10` (tens column) | `3` | `3` | Tens digit is `3` |

- `÷` slides the wanted column down to the ones place.
- `mod 10` throws away everything left of that place.

> Work both rows by hand: dividing by 1 changes nothing, and 32 mod 10 is the remainder 2. Dividing by 10 gives 3 with the 2 dropped, and 3 mod 10 is 3. So the recipe `(key ÷ place) mod base` reads exactly one column, and each pass multiplies place by 10 to step one column left.

---
# One digit pass in pseudocode
## A stable column, ones first

```algorithm
function DIGIT-PASS(A, place, base):  // one stable column
  buckets ← base empty lists
  for each item in A, in order do
    d ← (key(item) ÷ place) mod base  // this column's digit
    append item to buckets[d]         // encounter order kept
  return buckets[0] + .. + buckets[base-1]
```

> The digit line reads one column: dividing by place drops the lower columns and mod base keeps the wanted digit. Appending in encounter order is what makes the pass stable, and collecting buckets smallest-digit upward preserves it.

---
# All digit passes in pseudocode
## One call per column, right to left

```algorithm
function RADIX-SORT(A, digits, base):  // one call per column
  out ← copy of A; place ← 1
  for k ← 1 to digits do
    out ← DIGIT-PASS(out, place, base)
    place ← place × base
  return out
```

> Place starts at 1 for the ones column, then multiplies by the base to step one column left per pass. The least significant column goes first so later columns group records whose lower columns already stand ordered. The next two pictures trace both recipes on our five records.

---
# The ones-digit pass, step by step

```diagram
@dir TB
@reveal manual
A[32a, 14, 32b, 21, 13]
focus A
--- Read records left to right.
A -> B1[Bucket 1: 21]
A -> B2[Bucket 2: 32a, 32b]
A -> B3[Bucket 3: 13]
A -> B4[Bucket 4: 14]
focus B1 B2 B3 B4
--- Each record joins its digit bucket. The rest stay empty.
B1 -> R[21, 32a, 32b, 13, 14]
B2 -> R
B3 -> R
B4 -> R
focus R
--- Collect buckets 0 through 9 in order.
```

> Thirty-two a joins bucket two, fourteen joins four, thirty-two b follows a in bucket two, twenty-one joins one, thirteen joins three. Collecting smallest digit upward gives twenty-one, thirty-two a, thirty-two b, thirteen, fourteen: sorted by ones digit only.

---
# The tens-digit pass, step by step

```diagram
@dir TB
@reveal manual
T[21, 32a, 32b, 13, 14]
focus T
--- Read the order the ones pass left behind.
T -> B1[Bucket 1: 13, 14]
T -> B2[Bucket 2: 21]
T -> B3[Bucket 3: 32a, 32b]
focus B1 B2 B3
--- Tied tens digits keep their ones order inside the bucket.
B1 -> F[13, 14, 21, 32a, 32b]
B2 -> F
B3 -> F
focus F
--- Collect buckets 0 through 9 in order. Keys are sorted.
```

> Twenty-one lands in bucket two, the thirty-twos share bucket three in current order, thirteen and fourteen share bucket one in that order. Thirteen stays before fourteen because their tens digits tie and the stable pass preserves their ones order.

---
# Why stable digit passes work

- One pass orders records by its digit.
- Assume the last `k` digits are already ordered.
- The next pass groups by digit `k + 1`.
- Inside each group, stability keeps the lower-digit order.
- After `d` passes the full keys stand sorted and stable.

> Major digits group correctly while minor order survives inside ties. Repeating the argument through ones, tens and beyond carries the invariant to the full key. Fully equal keys ride every pass in their original relative order.

---
# Radix costs and assumptions

- One pass spreads `n` records across `b` buckets.
- For `d` passes, work is $\Theta(d(n+b))$.
- Peak extra space is $\Theta(n+b)$.
- Fixed `b` gives $\Theta(dn)$ on nonempty input.
- Linear in `n` only while `b` and `d` stay bounded.

> Every pass builds buckets, spreads records, then collects them, regardless of input order. Bucket references plus one output list coexist at peak. Digit count is not automatically constant as keys grow, and digit arithmetic is assumed constant-cost, as with bounded-width keys.

---
# Why the lower bound still holds

- The $\Omega(n\log n)$ bound covers comparison sorting.
- Radix reads digits as bucket indices instead.
- Its speed leans on the allowed key shape.
- Digit buckets do not sort arbitrary objects directly.

> The earlier bound assumed order is learned only through key-to-key comparisons. Computing an index from part of a key changes the model, and adds assumptions: bounded nonnegative integer keys with a fixed digit count. Other key shapes need a different design, so state the key model with any linear-work claim.

---
@type section
# Wrap-up

---
# Work comparison

| Arrangement shown | Best | Average* | Worst |
|---|---|---|---|
| Bubble with flag | $\Theta(n)$ | $\Theta(n^2)$ | $\Theta(n^2)$ |
| Selection, skipping self-swaps | $\Theta(n^2)$ | $\Theta(n^2)$ | $\Theta(n^2)$ |
| Insertion with strict shifts | $\Theta(n)$ | $\Theta(n^2)$ | $\Theta(n^2)$ |
| Merge with slices | $\Theta(n\log n)$ | $\Theta(n\log n)$ | $\Theta(n\log n)$ |
| Lomuto / Hoare quicksort | $\Theta(n\log n)$ | $\Theta(n\log n)$ | $\Theta(n^2)$ |
| Radix with `d` stable passes | $\Theta(d(n+b))$ | $\Theta(d(n+b))$ | $\Theta(d(n+b))$ |

> Read one row at a glance. The comparison-sort average assumes uniformly random distinct keys. Duplicate-heavy quicksort needs the sharper story from the previous lecture: all-equal input is quadratic for the Lomuto form and $n\log n$ for the Hoare form. Radix rows assume its digit arithmetic and exactly `d` passes.

---
# Space and stability comparison

| Arrangement shown | Peak extra space | Stable? |
|---|---|---|
| Bubble | $\Theta(1)$ | Yes |
| Selection | $\Theta(1)$ | No |
| Insertion | $\Theta(1)$ | Yes |
| Merge with slices | $\Theta(n)$ | Yes |
| Lomuto / Hoare quicksort | $\Theta(\log n)$ balanced, $\Theta(n)$ worst | No |
| Radix with stable buckets | $\Theta(n+b)$ | Yes |

> The three loop sorts rearrange inside the array with constant extra storage. Quicksort partitions the same way but stacks recursive calls. Strict adjacent swaps and strict shifts preserve equal order; long selection and quicksort leaps can break it. Merge leans on left-on-ties, radix on encounter order inside buckets.

---
# Work on our five records

| Algorithm | Key comparisons | Array-changing work |
|---|---|---|
| Bubble | `10` | `7` swaps |
| Selection | `10` | `2` swaps |
| Insertion | `9` | `7` shifts, `4` placements |

Counts describe this input and these exact arrangements.

> Exact counts for the shared input, not formulas. Insertion compares once for fourteen, once for thirty-two b, thrice for twenty-one and four times for thirteen: nine total, with the boundary stop excluded. A placement can land back home. Different operations cost differently, so two swaps alone do not crown selection fastest.

---
# Which sort fits?

| Requirement | Choice | Reason |
|---|---|---|
| Few inversions, stable ties, little memory | Insertion | Shifts only obstructing records |
| Costly moves, few swaps wanted | Selection | At most `n-1` swaps |
| Stable general sorting with a guarantee | Merge | Worst-case $\Theta(n\log n)$ |
| Bounded nonnegative integer keys | Radix | Stable digit passes |

> Answer each row with a name plus a reason tied to an operation. Insertion shifts little when inversions are few. Selection caps swaps but still scans. Merge spends memory for a stable guarantee. Radix exploits bounded integer shape. Finish by replaying one diagram above with the captions covered.
