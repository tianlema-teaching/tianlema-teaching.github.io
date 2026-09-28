@title Lecture 4: Divide-and-conquer sorting
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Divide-and-conquer sorting
## Divide, conquer, combine

---
# Sorting, concretely

- A **record** is one row: a student, a song, a parcel.
- A **key** is the value you sort by: a score, a price, a name.
- Sorting arranges records so keys run smallest to largest.
- The puzzle: do it fast, and know *why* the method works.

> Open a contacts app and sort by name, or a shop page and sort by price: each row is a record and the sorted column is the key. Today every key is a number and every record is one of five cards. Letters like a and b only mark which card arrived first; comparisons read the numbers alone.

---
# One array throughout

- Start every algorithm with `[32a, 14, 32b, 21, 13]`.
- `32a` and `32b` share the sorting key `32`.
- Sorted keys: `[13, 14, 21, 32, 32]`.
- Stable keeps `32a` before `32b`.

```diagram
@dir LR
P0[0: 32a] -> P1[1: 14] -> P2[2: 32b] -> P3[3: 21] -> P4[4: 13]
```

Positions count from 0, so the last of five sits at 4.

> Imagine two records that share the key thirty-two. The letters tell us which one came first. Sorting compares the numbers, never the letters. Either order of the two thirty-twos is numerically sorted. A stable method promises more: the record that came first among equal keys stays first. Copy the starting array once. Every algorithm below starts from that same order.

---
# How to read these slides

```diagram
@dir LR
M[32a, 14] -> N[14, 32a] : swap
```

- Boxes show the array. Arrows show steps. The sentence below narrates.
- Amber boxes hold the pivot or the held-aside record.
- `←` stores a value. `key()` reads the number. `A[i]` is position `i`.
- Counting starts at `0`. `//` is a hint, not a command.

> Every picture builds one keypress after another, and the sentence under it says what just happened. Pseudocode slides give the exact rules; the pictures that follow trace those exact lines on our five records. When a box glows amber, look there first: it holds the pivot or the record parked aside.

---
# What counts as sorted?

- Keys end in nondecreasing order.
- Every original record appears exactly once.
- **Stable** keeps equal keys in their original order.
- Extra lists and pending calls cost auxiliary space.
- Cost analysis asks how work grows with `n`.

> A row of identical values is ordered, but it is not a sort of our input unless it holds the same records. Stability is an extra promise on top of order. Space counts everything beyond the input: helper lists plus waiting recursive calls. Work has two views: the exact steps on five records, and the growth rate that predicts larger inputs.

---
# Stable keeps arrival order

```diagram
@dir TB
@reveal manual
S[32a, 32b | a arrived first]
focus S
--- Two records share key 32. a arrived first.
S -> K[32a, 32b | stable]
S -> U[32b, 32a | unstable]
focus K U
--- Both read sorted. Only one kept arrival order.
```

> Both boxes read numerically sorted because both keys are thirty-two. Only the left box kept arrival order. Stability is a promise about every input, so one kept order proves nothing while one flip refutes the promise.

---
# Binary search and sorting

```diagram
@dir TB
@reveal edge
B[Binary search] -> O[Keep one relevant half]
O -> F[Find a target]
S[Merge sort] -> T[Sort both halves]
T -> M[Merge their results]
```

> Binary search starts sorted. One comparison tells it which half can still hold the target, so it drops the other half. Sorting starts unordered and every record must survive, so no half is dropped. The shared idea is smaller problems. The difference is that sorting finishes both halves and then combines them.

---
# Divide and conquer

```diagram
@dir TB
@reveal manual
P[one problem | 5 records]
focus P
--- One unsorted problem.
P -> H[left 2 | right 3] : divide by position
focus P H
--- Halves shrink toward single records.
H -> S[sorted 2 | sorted 3] : conquer each half
focus H S
--- Helpers return sorted halves.
S -> F[full answer] : combine
focus S F
--- Combine builds the whole answer.
```

> Recursion asks a helper to run your exact instructions on a shorter list. Name the three moves as they appear: divide splits one list into smaller lists, conquer sorts each smaller list the same way, and combine builds the full answer from the solved pieces. A one-item list needs no work. The interesting moment is the return: merge sort merges two sorted lists, while quicksort arranges key ranges first so the return needs no merge. The diagrams below trace every helper call on the same five records.

---
@type section
# Merge sort

---
# Reading pseudocode in 30 seconds

```algorithm
i ← 0              // store 0 in i
while i < 3 do     // repeat while this stays true
  i ← i + 1        // grow i by one each round
```

- `←` stores a value. `//` is a hint for humans, not a command.
- `A[i]` is the record at position `i`. `key()` reads its number.
- Indented lines under `while`, `for`, `if` are the repeated (or chosen) block.

> Trace it aloud: i starts at 0, three rounds make it 1, 2, 3, then 3 < 3 is false and we stop. Read every later recipe the same way: top to bottom, do what each line says. Each picture after a recipe traces those exact lines, so when a picture confuses you, cover it and run the lines on paper first.

---
# Merge in pseudocode
## Ties take the left record

```algorithm
function MERGE(L, R):            // L and R sorted; builds a new list
  out ← empty list
  i ← 0; j ← 0
  while i < len(L) and j < len(R) do
    if key(L[i]) ≤ key(R[j]) then  // tie takes left: stable
      append L[i] to out; i ← i + 1
    else
      append R[j] to out; j ← j + 1
  append L[i..], then R[j..]     // one side is empty
  return out
```

> Read the loop guard first: it runs only while both halves still hold a record. The comparison calls key on each front record, and less-or-equal sends ties left. Exactly one side advances per round. After the loop one tail is empty, so copying both tails finishes the merge. The next picture traces these exact lines.

---
# Merge sort driver
## Halves by position, then merge

```algorithm
function MERGE-SORT(A):          // returns a new sorted list
  if len(A) ≤ 1 then
    return copy of A             // zero or one item is sorted
  mid ← len(A) ÷ 2, rounded down
  L ← MERGE-SORT(left half by position)
  R ← MERGE-SORT(right half by position)
  return MERGE(L, R)
```

> Division uses positions only: five records split into two plus three, then into singles and one pair. No keys are compared until MERGE runs. Both halves shrink, so recursion reaches the one-item base case. The returned halves are sorted, and only then does the merge walk them. Point at divide, conquer and combine in these seven lines before watching the picture.

---
# Merge sort in one picture
## Divide by position, then merge sorted lists

```diagram
@dir TB
@reveal manual
R[32a, 14, 32b, 21, 13]
focus R
--- Start with all five records.
R -> L[32a, 14] : first 2
R -> Rt[32b, 21, 13] : last 3
focus R L Rt
--- Split by position. No keys compared yet.
L -> L0[32a]
L -> L1[14]
Rt -> R0[32b]
Rt -> R1[21, 13]
focus L L0 L1 Rt R0 R1
--- Split again. Singles are sorted already.
L0 -> SL[14, 32a] : 14 first
L1 -> SL
R1 -> SR1[13, 21] : 13 first
focus SL SR1
--- Merge the small pairs by comparing first records.
R0 -> SR[13, 21, 32b]
SR1 -> SR
SL -> F[13, 14, 21, 32a, 32b]
SR -> F
focus SL SR F
--- Merge the two sorted halves. The left 32a wins the tie.
```

> Follow the picture from top to bottom. First the array splits by position into two plus three, then into singles and one pair. Small merges compare only the first unused record of each sorted half. The left pair yields fourteen before thirty-two a. The pair twenty-one and thirteen yields thirteen first. The right merge takes thirteen, twenty-one, then thirty-two b. The final merge walks two sorted halves and writes thirteen, fourteen, twenty-one, then the two thirty-twos.

---
# The tie that decides stability

```diagram
@dir TB
@reveal manual
S[left 32a | right 32b]
focus S
--- One record left on each side. Both keys are 32.
S -> T[13, 14, 21, 32a | right 32b] : take the left record
focus S T
--- Equal keys take the left record first. That is the tie rule.
T -> F[13, 14, 21, 32a, 32b] : right side empty
focus T F
--- Nothing left to compare, so copy the last record over.
```

> Either thirty-two could go first without breaking numeric order. The rule takes the left half record on ties. Splitting kept input order across halves, so left-on-ties keeps equal records in order through the whole sort. Once the left side runs out, the remaining right record is copied with no more comparisons.

---
# Why merge sort works

- Singles are already sorted.
- Small results return sorted, by the same argument.
- The smaller front record is the smallest one left.
- Each merge copies every record exactly once.
- Left-on-ties keeps equal records in order.

> During a merge the output always holds the smallest consumed records in order. The next smallest unused record must sit at the front of one sorted half, so copying the smaller front keeps the output sorted. Each step consumes exactly one record, so none is lost or doubled. Base cases plus correct small results plus a correct merge give a correct full sort.

---
@type section
# Lomuto partitioning

---
# Lomuto in one idea

```diagram
@dir LR
S[small keys | at most pivot] -> B[big keys | above pivot] -> U[unseen] -> P[pivot waits].amber
```

> Picture three zones sliding right as the scan index advances: small-or-equal keys, bigger keys, then records not yet checked. A qualifying record swaps into the next small slot. A non-qualifying record only advances the scan. When nothing is unseen, the pivot swaps into the gap between small and big. That index is finished and sits out the later work.

---
# Lomuto partition in pseudocode
## Pivot last; a finished index returns

```algorithm
function LOMUTO(A, lo, hi):      // inclusive ends; pivot is A[hi]
  p ← key(A[hi]); i ← lo         // pivot waits at hi
  for j ← lo to hi - 1 do        // scan every other record
    if key(A[j]) ≤ p then        // tie joins the small side
      swap A[i] and A[j]; i ← i + 1
  swap A[i] and A[hi]            // park the pivot
  return i                       // pivot now finished at i
```

> Name the three zones while reading: positions from lo up to i hold small-or-equal keys, from i up to j hold bigger keys, from j onward waits unseen with the pivot at the far end. A qualifying record swaps into the small zone and i advances; otherwise only j advances. The last swap parks the pivot at i, finished.

---
# Lomuto driver in pseudocode
## The finished pivot sits out both sides

```algorithm
function QUICK-LOMUTO(A, lo, hi):
  if lo ≥ hi then return         // zero or one record
  q ← LOMUTO(A, lo, hi)          // pivot finished at q
  QUICK-LOMUTO(A, lo, q - 1)     // pivot sits out both sides
  QUICK-LOMUTO(A, q + 1, hi)
```

> The driver sorts ever smaller ranges around the finished pivot: the base case stops at zero or one record, and both recursive calls skip q because that index stands finished. The next two pictures trace both recipes on our five records.

---
# Lomuto first partition, step by step

```diagram
@dir TB
@reveal manual
S0[32a, 14, 32b, 21, 13 | pivot 13 waits].amber
focus S0
--- The pivot is the last key, 13. It waits at the end.
S0 -> S1[same array | 4 checks, no swaps].amber : all four keys bigger
focus S0 S1
--- The scan tests each record against 13. None qualifies.
S1 -> S2[13, 14, 32b, 21, 32a | q = 0] : swap index 0 and 4
focus S1 S2
--- The pivot swaps into index 0. Only index 0 is finished.
```

> Test the four scan comparisons one by one: each key against thirteen fails, so the array never changes during the scan. The final swap moves thirteen to the front and thirty-two a to the end. The right region still holds thirty-two b before twenty-one, which is out of order, so more work remains.

---
# Lomuto finish, step by step

```diagram
@dir TB
@reveal manual
T0[13, 14, 32b, 21, 32a | pivot 32a].amber
focus T0
--- New range 1 to 4. The pivot is the last key, 32a.
T0 -> T1[same array | q = 4].amber : 3 checks pass
focus T0 T1
--- Every scanned key is at most 32a, so each swaps with itself.
T1 -> T2[13, 14, 21, 32b, 32a | q = 2] : keep 14, park 21
focus T1 T2
--- Range 1 to 3, pivot 21. 14 stays, 32b is skipped, 21 lands at 2.
```

> Replay the middle partition slowly: fourteen, thirty-two b and twenty-one each meet the test, so the array looks unchanged and the pivot swaps with itself. The next range covers indices one through three with pivot twenty-one. Fourteen qualifies, thirty-two b does not, and twenty-one swaps into index two. The remaining ranges hold at most one record each. The keys end sorted with the two thirty-twos reversed.

---
# Why Lomuto works

- The scan keeps small keys left of big keys.
- The final swap parks the pivot between them.
- Left keys are at most the pivot, right keys above it.
- Both sides shrink because the pivot index leaves.
- Swaps keep records but can flip equal keys.

> Partition ends with every left key at most the pivot and every right key above it. Sorting both smaller sides therefore sorts the whole range. Our run ends with thirty-two b before thirty-two a, which shows this arrangement can reverse equal keys.

---
@type section
# Hoare partitioning

---
# Hoare in one idea

```diagram
@dir LR
@reveal manual
V[pivot value 32].amber
focus V
--- The first key sets the value both scans obey.
V -> L[i walks right | stops at 32 or more]
V -> R[j walks left | stops at 32 or less]
focus L R
--- Stopped pairs swap. Crossing ends the pass.
```

> The left scan skips records that already belong left, and the right scan skips records that already belong right. When both stop short of crossing, the pair is on the wrong sides and swaps. Both scans stop on equal keys, and each round moves the pointers first, so equal values cannot stall the pass. There is no final pivot swap: the saved value guided the scans even if its record moved.

---
# Hoare pass in pseudocode
## Pivot value first; a boundary returns

```algorithm
function HOARE(A, lo, hi):  // pivot value is key(A[lo])
  p ← key(A[lo])
  i ← lo - 1; j ← hi + 1         // parked outside both ends
  repeat
    repeat i ← i + 1 until key(A[i]) ≥ p  // stops on ties
    repeat j ← j - 1 until key(A[j]) ≤ p  // stops on ties
    if i ≥ j then return j       // crossed: j is the boundary
    swap A[i] and A[j]           // wrong sides: exchange
```

> Contrast every line with Lomuto: the pivot supplies a value, not a waiting record, and both scans halt on equality, so tied keys stop each pointer. Each round moves the pointers before checking, so equal values cannot stall the pass. Crossing ends the pass with no final swap.

---
# Hoare driver in pseudocode
## The boundary stays inside the left side

```algorithm
function QUICK-HOARE(A, lo, hi):
  if lo ≥ hi then return
  q ← HOARE(A, lo, hi)           // boundary, not a finished pivot
  QUICK-HOARE(A, lo, q)          // left side keeps q
  QUICK-HOARE(A, q + 1, hi)
```

> No record stands finished, so the driver keeps q inside the left side: ranges `lo..q` and `q+1..hi`. Lomuto drops its finished pivot instead. The next three pictures trace both recipes on our five records.

---
# Hoare first swap, step by step

```diagram
@dir TB
@reveal manual
S0[32a, 14, 32b, 21, 13 | pivot value 32].amber
focus S0
--- Fresh array. The pivot value is the first key, 32.
S0 -> S1[i stops at 0 | j stops at 4] : equality stops scans
focus S0 S1
--- Left stops on 32a at once. Right stops on 13 at once.
S1 -> S2[13, 14, 32b, 21, 32a] : swap 0 and 4
focus S1 S2
--- Swap the stopped pair. The pass is not over yet.
```

> This reset starts from the original order, not from the Lomuto result. Equality stops a scan, so both pointers halt immediately. After the exchange the saved pivot value is still thirty-two, even though thirty-two a now sits at the far end. The picture matches the Lomuto state by coincidence, but the promise differs: no index is finished yet.

---
# Hoare cross, step by step

```diagram
@dir TB
@reveal manual
C0[13, 14, 32b, 21, 32a | resume inside].amber
focus C0
--- Pointers resume inside the array.
C0 -> C1[i stops at 2 | j stops at 3] : 14 skipped
focus C0 C1
--- Left passes 14 and halts on 32b. Right halts on 21.
C1 -> C2[13, 14, 21, 32b, 32a] : swap 2 and 3
focus C1 C2
--- Swap the second stopped pair.
C2 -> C3[left 0 to 2 | right 3 to 4, q = 2] : cross at 3 and 2
focus C2 C3
--- The next scans cross. Return boundary 2. No pivot swap.
```

> Move both pointers before comparing again, then exchange the stopped records. The following scans reach index three from the left and index two from the right, so they have crossed. The boundary is two, while the original pivot record sits at index four: direct evidence that this boundary is not a finished pivot position.

---
# Hoare finish, step by step

```diagram
@dir TB
@reveal manual
H0[left 13, 14, 21 | right 32b, 32a]
focus H0
--- Boundary 2 splits the work into 0 to 2 and 3 to 4.
H0 -> H1[left sorted | 13, 14, 21] : no swaps
focus H0 H1
--- The left part partitions twice around 13, then 14, with no swaps.
H1 -> H2[13, 14, 21, 32a, 32b | q = 3] : swap equal 32s
focus H1 H2
--- Equal keys stop both scans, so the pair swaps before crossing.
```

> The left region is already ordered but still partitions: pivot thirteen gives boundary zero, then fourteen and twenty-one give boundary one. On the right both keys equal thirty-two, both scans stop, the records swap, and the next round crosses at boundary three. This run happens to keep thirty-two a first, but that single outcome proves nothing general about equal keys.

---
# Why Hoare works

- Passed left spots hold keys at most the value.
- Passed right spots hold keys at least the value.
- Each swap puts both stopped records on good sides.
- At the cross, every left key is at most every right key.
- Sorting both smaller sides sorts the whole range.

> No single record must reach its final spot during the pass. The scans plus swaps build a boundary with all left keys at most all right keys. Recursive sorting then orders each side. Swaps keep every record while shrinking both ranges through inward motion.

---
# The two promises side by side

| Property | Lomuto shown here | Hoare shown here |
|---|---|---|
| Pivot key | Last record | First record |
| Returned `q` | Finished pivot index | Split boundary |
| Left range | `lo..q-1` | `lo..q` |
| Right range | `q+1..hi` | `q+1..hi` |
| Equal keys | Accept with `<=` | Both scans stop |

> Read each column as one complete rule set. The returned index means different things, so the callers recurse differently: Lomuto drops its finished pivot, Hoare keeps the boundary record inside the left side. Explain the table to a neighbour by pointing at the two finish diagrams and saying which index Lomuto finished and why Hoare finished none.

---
# One good run proves little

```diagram
@dir TB
@reveal manual
E0[32a, 32b | pivot value 32].amber
focus E0
--- Two equal records. Both scans stop at once.
E0 -> E1[32b, 32a] : swap, then cross
focus E0 E1
--- The equal pair swaps. Order is reversed.
```

> Our five-record Hoare run kept thirty-two a first, yet this two-record input flips them. One preserving run cannot certify a general promise, while one flipping run refutes it. Think of swans: one white swan proves nothing about all swans, but one black swan refutes the claim that all swans are white. Both arrangements shown in this course reverse equal keys on some input.

---
@type section
# Costs and guarantees

---
# Every level costs n

```diagram
@dir TB
L0[Level 0: sort 8 | work 8] -> L1[Level 1: two sorts of 4 | work 8] -> L2[Level 2: four sorts of 2 | work 8] -> L3[Level 3: eight singles | base cases]
```

Halving reaches singles after $\log_2 n$ levels: linear work per level, $\Theta(n\log n)$ total.

> Eight is a tidy size for the picture, not a new trace. Each level merges every record once across its boxes. Halving reaches singles after log base two of n levels, so linear work per level across logarithmically many levels gives theta n log n.

---
# What does $\log_2 n$ count?

```diagram
@dir LR
H8[8] -> H4[4] -> H2[2] -> H1[1]
```

- Halvings to reach $1$: $8 \to 4 \to 2 \to 1$ is $3$ steps, so $\log_2 8 = 3$.
- $16$ needs $4$ halvings; $1024$ needs only $10$.
- Doubling `n` adds just one level: logs grow very slowly.

> Read the chain rightward: 8 halves to 4, then 2, then 1, which is three halvings, so log base 2 of 8 is 3. Each halving is one level of the merge tree, which is why doubling the input adds only one more level. 1024 sounds big but needs just 10 halvings.

---
# Merge work by level

| Level | Sizes for `n = 8` | Merge work |
|---|---|---|
| 0 | `8` | Proportional to `8` |
| 1 | `4 + 4` | Proportional to `8` |
| 2 | `2 + 2 + 2 + 2` | Proportional to `8` |
| 3 | Eight singles | Base cases |

Total $=$ work per level $\times$ levels $=$ $\Theta(n) \times \log_2 n$.

> Size eight shows a perfectly balanced tree; it is a work illustration, not a new trace. Each level touches all `n` records across its merges. Halving reaches singles after $\log_2 n$ levels, so linear work per level times logarithmically many levels gives $\Theta(n\log n)$.

---
# Reading a recurrence

```diagram
@dir TB
W[sort n] -> H[two half sorts] : first term
W -> M[split and merge] : last term
```

- $T(n)$ is the work for input size `n` (think: minutes to sort `n` records).
- Merge sort: $T(n)=2T(n/2)+\Theta(n)$: two half sorts plus linear split-and-merge.
- $\Theta$ is a tight rate: an upper bound plus a matching lower one.

> A recurrence defines a function through smaller inputs. The two halves give the first term, and everything outside them gives the linear term. Odd sizes use floor and ceiling halves without changing the growth result. $\Theta$ states a tight rate, both an upper and a matching lower growth bound.

---
# Master theorem: matching work

```diagram
@dir LR
F[a branches | size n over b | extra f(n)] -> P[p means log of a base b] -> C[compare f(n) with n to the p] -> E[match adds log n]
```

- Form: $T(n)=aT(n/b)+f(n)$, with $a\ge1$, $b>1$; let $p=\log_b a$.
- Match: if $f(n)=\Theta(n^p)$, then $T(n)=\Theta(n^p\log n)$.
- Merge sort: $a=2$, $b=2$, so $p=1$; $f(n)=\Theta(n)$ matches: $\Theta(n\log n)$.
- In words: equal work on every level piles up one extra $\log n$.

> This shortcut handles recurrences with equal-size branches. In the matching case the extra work equals the branching benchmark at every level, which adds one logarithm. Identify `a`, `b` and `f` before applying the formula. Reference: [MIT Master theorem notes](https://people.csail.mit.edu/thies/6.046-web/master.pdf).

---
# Master theorem: the other cases

- Let $p=\log_b a$ and pick a constant $\epsilon>0$.
- If $f(n)=O(n^{p-\epsilon})$, the result is $\Theta(n^p)$.
- If $f(n)=\Omega(n^{p+\epsilon})$, check regularity.
- Regularity: $af(n/b)\le c f(n)$ for some $c<1$.
- With that check, the result is $\Theta(f(n))$.
- In words: tiny extra work means the leaves win; huge extra work means the top wins.

> When extra work is polynomially smaller, the leaves dominate. When it is polynomially larger and regularity holds, the top dominates: two halves plus $n^2$ extra work is $\Theta(n^2)$ because each deeper level halves the square work. Reference: [MIT Master theorem notes](https://people.csail.mit.edu/thies/6.046-web/master.pdf).

---
# Quicksort recursion shape

```diagram
@dir TB
@reveal manual
Q[partition n | work n]
focus Q
--- Every partition scans its whole range.
Q -> G[halves | log many levels] : balanced
Q -> B[n minus 1, then n minus 2, ..] : chained
focus G B
--- Same linear pass. Balanced totals n log n. Chained totals n squared.
```

- One partition of size `m` costs $\Theta(m)$.
- Balanced regions give $\Theta(n\log n)$ total work.
- Chained sizes `n-1` then `0` give $\Theta(n^2)$.

> Partitioning is linear, but the same records may be partitioned again and again. A bad chain adds $n$, $n-1$, $n-2$ and onward, which sums quadratically. Balanced levels each cost linear total across logarithmically many levels. On uniformly random distinct keys these fixed-pivot versions average $\Theta(n\log n)$; that average is not a promise for every input. Fixed end pivots can fail on already ordered input, and lopsided splits skip the Master form because their branches differ in size.

---
# Equal keys change the story

| All keys equal | Lomuto with `<=` | Hoare, both scans stop |
|---|---|---|
| One partition | Pivot finishes at `hi` | Pointers meet near the middle |
| Recursive sizes | `n-1` and `0` | Roughly equal halves |
| Total work | $\Theta(n^2)$ | $\Theta(n\log n)$ |

> With five thirty-twos, every Lomuto scan test passes, so one record peels off per pass. Hoare stops both scans on equality, swaps inward, and splits near the middle. Shuffling the pivot helps ordered distinct inputs but does not fix this all-equal Lomuto pattern.

---
# Memory and stability of these versions

| Arrangement | Peak extra space | Stable? |
|---|---|---|
| Merge sort with slices | $\Theta(n)$ | Yes, left on ties |
| Lomuto quicksort | $\Theta(\log n)$ balanced, $\Theta(n)$ worst | No |
| Hoare quicksort | $\Theta(\log n)$ balanced, $\Theta(n)$ worst | No |

> Both partitions rearrange in place with constant local storage, but the waiting recursive calls still stack up: logarithmic when balanced, linear along a bad chain. Merge sort adds helper lists plus its stack for linear peak extra memory.

---
# How deep must comparisons go?

```diagram
@dir TB
Top[compare two keys] -> L[first answer]
Top -> R[second answer]
L -> LL[n! orders need n! leaves]
R -> RL[height h gives at most 2 to the h leaves]
```

> Each comparison is one yes-or-no question with two answers. Ask h questions in a row and the answers branch into at most 2 to the h endings, which are the leaves on screen. Sorting must separate every possible input order, and the next slide counts how many orders that is.

---
# Count the questions

- Each comparison is one yes/no question; $h$ questions give at most $2^h$ answers.
- $n$ distinct keys have $n!$ possible orders to tell apart.
- Example: $3$ records have $6$ orders, and $2^2 = 4 < 6 \le 8 = 2^3$: need $3$ questions.
- In general $h\ge\log_2(n!)=\Omega(n\log n)$ in the worst case.

> Play twenty questions with the input order: each answer halves the candidates, so telling n factorial orders apart needs log base two of n factorial questions. That is Omega n log n, and merge sort meets this bound. The next lecture reads digits under extra assumptions, outside this comparison-only model.

---
@type section
# Wrap-up

---
# Which arrangement fits?

| Requirement | Choice among these versions | Reason |
|---|---|---|
| Stable ties plus guaranteed work | Merge sort | Stable, worst $\Theta(n\log n)$ |
| Rearrange inside the array | Either quicksort | In place: constant partition storage |
| Many equal keys, picking a partition | Hoare over this Lomuto | Better all-equal splitting |

> Justify one row from the diagrams, not from slogans. Merge sort guarantees work but builds lists. Both quicksort versions can stack linearly in the worst case. Hoare handles the all-equal input better yet stays unstable with quadratic worst cases elsewhere. No version wins everywhere.

---
# Check both first partitions

```diagram
@dir TB
@reveal manual
S[32a, 14, 32b, 21, 13]
focus S
--- Both partitions start from this same array.
S -> L[13, 14, 32b, 21, 32a | Lomuto q = 0 | pivot was last key 13]
S -> H[13, 14, 21, 32b, 32a | Hoare q = 2 | pivot was first key 32]
focus L H
--- Lomuto finishes a pivot record. Hoare returns a split boundary.
```

> Draw indices zero through four above each result. Circle the Lomuto pivot at zero and cross it out of later work: its ranges are `0..-1` and `1..4`. Hoare divides at boundary two with ranges `0..2` and `3..4`, and the record at the boundary still needs sorting. In the next lecture the same input returns with simpler loops and digit passes.
