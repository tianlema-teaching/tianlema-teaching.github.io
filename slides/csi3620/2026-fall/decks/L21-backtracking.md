@title Lecture 21: Backtracking
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Backtracking
## Choose, explore, unchoose

---
# Where we are

- L15: depth-first search goes deep, then returns to try the next branch.
- L20: dynamic programming reuses answers to a small set of subproblems.
- Some problems have no small set: we must search many arrangements.
- Today: search depth first over **partial solutions**, and abandon dead ends early.

> Depth-first search from the graphs lectures is the engine of today's method, but the graph is no longer given to us: it is the tree of all ways to make choices one at a time. Last lecture every problem had a modest number of distinct subproblems. Today the answers themselves can be exponentially many, so the goal is to search without wasting work on choices that can never succeed.

---
# By the end of today you can

- Draw the state-space tree for a small search problem.
- Write a backtracking method with choose, explore, unchoose.
- Generate all subsets and permutations, and count them.
- Prune N-Queens and subset sum, and measure what pruning saves.
- Explain why pruning does not remove the exponential worst case.
- Place backtracking next to brute force, DP and branch and bound.

> By the end you should be able to take a puzzle you have never seen, say what one choice is, say when a partial solution is already hopeless, and write the recursive search. Measuring matters too: we will count search-tree nodes by running the code, so you can see pruning work rather than take it on faith.

---
# Four queens on a four-by-four board

- Place 4 queens so that no two share a row, a column or a diagonal.
- Any 4 of the 16 squares: $\binom{16}{4} = 1820$ placements to test.
- One queen per row: $4^4 = 256$ complete placements.
- Backtracking, measured: **17** search nodes, and both solutions found.
- The trick: reject a queen the moment it is attacked.

> A queen attacks along its row, its column and both diagonals. Testing every set of four squares works but is wasteful. Putting one queen in each row already shrinks the space, because two queens in one row can never be right. Backtracking goes further: as soon as a newly placed queen is attacked, it abandons every completion of that partial board at once. The number seventeen was counted by running today's Java code.

---
# Running examples for today

| Problem | Input traced on the slides |
|---|---|
| All subsets | `[a, b, c]` |
| All permutations | `[1, 2, 3]` |
| N-Queens | the 4-by-4 board; counts for $n = 4$ to $8$ |
| Subset sum | sorted `[2, 3, 5, 6, 8]`, target `10` |
| Combination sum | coins `[1, 3, 4]`, target `6` (as in L20) |

Every trace and count on later slides was produced by the tested Java code.

> Four-Queens is the main example, and we will follow its search from the first queen to the last dead end. The subset-sum input is small enough to draw its whole pruned tree. The combination-sum input is the coin example from the dynamic programming lecture, so you can compare listing the answers with counting them.

---
@type section
# The idea

---
# Build, check, undo

- A **partial solution** fixes some choices and leaves the rest open.
- Extend it by **one choice**, then recurse on what remains.
- If the partial solution cannot be completed, **backtrack**: undo the last choice.
- **Pruning**: skipping a subtree because its partial solution cannot be completed.
- Then try the next option for that choice.
- Record a complete, valid solution when you reach one.

> Think of a maze walked with one hand on the wall of options. At each junction you pick the first unexplored way. At a dead end you walk back to the most recent junction with an untried way. The word backtrack refers to that walking back. The key efficiency idea is the dead-end test: the earlier you can tell a partial solution is hopeless, the more work you skip.

---
# The state-space tree

```diagram
@dir TB
R[chosen: none] -> A[a] : include a
R -> N[none] : skip a
A -> AB[a, b] : include b
A -> A2[a] : skip b
N -> B[b] : include b
N -> N2[none] : skip b
```

- Node: a partial solution. Edge: one choice. Depth: choices made so far.
- The search visits this tree **depth first**; it is never stored.

> This is the tree for subsets of two items, a and b. The root has made no choices. Each level decides one item, include or skip, so the leaves are the four subsets. Backtracking walks the tree in depth-first order and keeps only the current path in memory, which is why its extra space is proportional to the depth, not to the size of the tree.

---
# The template in pseudocode

```algorithm
function BACKTRACK(partial):
  if partial is a complete solution then
    record a copy of partial
    return
  for each option for the next choice do
    if option is consistent with partial then   // pruning test
      apply option to partial                   // choose
      BACKTRACK(partial)                        // explore
      undo option                               // unchoose
```

> Every method today has this shape. The base case records a solution. The loop tries each option for the next decision. The consistency test is where pruning happens: skip options that already break a rule. Choose changes the shared partial solution, explore recurses, and unchoose restores it exactly, so the next option starts from the same state.

---
# The template in Java: all subsets

```java
private static <T> void subsets(List<T> items, int i, List<T> chosen,
        List<List<T>> out) {
    if (i == items.size()) {             // all decided: record a copy
        out.add(new ArrayList<>(chosen));
        return;
    }
    chosen.add(items.get(i));            // choose: include items[i]
    subsets(items, i + 1, chosen, out);  // explore
    chosen.remove(chosen.size() - 1);    // unchoose
    subsets(items, i + 1, chosen, out);  // explore without items[i]
}
```

- One list `chosen` is shared by every call; unchoose restores it.

> Here the next choice is item i, and there are two options, include it or skip it. Look at the three commented lines in the middle: add, recurse, remove. Removing the last element undoes exactly the add. The base case stores a copy, because chosen keeps changing after we return. No pruning is needed here, since every subset is an answer.

---
@type section
# Subsets and permutations

---
# Subsets of `[a, b, c]`, in the order produced

| Step | `chosen` when recorded | Last choices |
|---|---|---|
| 1 | `[a, b, c]` | include a, b, c |
| 2 | `[a, b]` | unchoose c, skip c |
| 3 | `[a, c]` | unchoose b, skip b, include c |
| 4 | `[a]` | unchoose c, skip c |
| 5 | `[b, c]` | back to the root; skip a |
| 6, 7, 8 | `[b]`, `[c]`, `[]` | the same pattern without a |

- Each item has 2 options, so there are $2^n$ subsets: here $2^3 = 8$.

> The order comes from trying include before skip. The first leaf includes everything. Each later leaf appears after undoing the most recent include and switching it to skip. The output list was printed by the Java code, and the check program confirms two to the n distinct subsets for every n up to seven, including one subset, the empty one, for an empty input.

---
# Permutations: choose an unused item

```java
for (int k = 0; k < items.size(); k++) {
    if (used[k]) continue;                  // each item appears once
    used[k] = true;                         // choose
    order.add(items.get(k));
    permute(items, used, order, out);       // explore
    order.remove(order.size() - 1);         // unchoose
    used[k] = false;
}
```

- The next choice is the next position; the options are the unused items.
- Unchoose must undo **both** changes: the list and the `used` flag.

> This loop sits after a base case that records a copy when order holds every item. The boolean array used is part of the state. Choosing an item sets its flag and appends it; unchoosing removes it and clears the flag. Forgetting to clear the flag is a common bug: the item would stay blocked for all later branches, and most permutations would never appear.

---
# The permutation tree for `[1, 2, 3]`

```diagram
@dir LR
@reveal manual
R[order: none]
focus R
--- Nothing placed yet. Three options for position 0.
R -> P1[1] -> P12[1, 2] -> P123[1, 2, 3]
focus R P1 P12 P123
--- First leaf: [1, 2, 3], recorded.
P1 -> P13[1, 3] -> P132[1, 3, 2]
focus P1 P13 P132
--- Undo 3, undo 2, place 3, then 2: [1, 3, 2].
R -> P2[2]
R -> P3[3]
focus R P2 P3
--- Undo back to the root. Branches 2 and 3 give the other four.
```

- Output order from the code: `123, 132, 213, 231, 312, 321`. That is $3! = 6$.

> Position zero has three options, position one has the two items left, and position two has one. That product, three times two times one, is three factorial. The tree drawn here shows only the first branch in full; the branches starting with two and with three have the same shape. The six orderings listed at the bottom were printed by the Java code in this order.

---
# How much work is listing everything?

| Generator | Outputs | Time, including copies |
|---|---|---|
| Subsets of $n$ items | $2^n$ | $\Theta(n\,2^n)$ |
| Permutations of $n$ items | $n!$ | $\Theta(n\cdot n!)$ |

- Each output is a list of up to $n$ items, and copying it costs $\Theta(n)$.
- No method that lists them all can be faster than the total output size.
- Extra space besides the output: $\Theta(n)$ for the path and the stack.

> For subsets the tree has two to the n plus one, minus one nodes, each doing constant work, plus a copy of length up to n at each leaf. For permutations the internal nodes number at most a small constant times n factorial, and each loops over n items. When the task is to list every arrangement, exponential time is unavoidable because the answer itself is exponentially long.

---
# Quick check

```quiz
How many lists does `permutations` return for four distinct items?
- [ ] 16
- [x] 24
- [ ] 64
- [ ] 256
```

> Twenty-four, which is four factorial: four choices for the first position, three for the second, two for the third and one for the last. Sixteen is two to the fourth, the number of subsets. Two hundred fifty-six is four to the fourth, which would allow an item to repeat. The check program confirms n factorial distinct lists for every n up to seven.

---
@type section
# N-Queens: method

---
# One queen per row

- `col[r]` is the column of the queen in row `r`. Rows are filled 0, 1, 2, ...
- One queen per row is built in; we only check columns and diagonals.
- Squares on one **diagonal** share $r - c$; on one **anti-diagonal** they share $r + c$.
- Keep three boolean arrays: `colUsed[n]`, `diagUsed[2n-1]`, `antiUsed[2n-1]`.
- Then "is this square attacked?" takes constant time.

> Choosing the representation is half the design. Because each row gets exactly one queen, the row rule can never be broken. Moving down-right along a diagonal adds one to both r and c, so r minus c stays the same; moving down-left keeps r plus c the same. The value r minus c ranges from minus n plus one to n minus one, so we shift it by n minus one to get an array index.

---
# Checking and marking in Java

```java
private boolean safe(int r, int c) {
    return !colUsed[c] && !diagUsed[r - c + n - 1] && !antiUsed[r + c];
}

private void mark(int r, int c, boolean value) {
    colUsed[c] = value;
    diagUsed[r - c + n - 1] = value;
    antiUsed[r + c] = value;
}
```

- `mark(r, c, true)` is part of choose; `mark(r, c, false)` is unchoose.

> The safety test reads three flags. Marking and unmarking write the same three flags, so unchoose is exact. Could two queens ever share a diagonal flag incorrectly? No: each flag belongs to exactly one line of the board, and at most one queen stands on any line in a consistent partial solution, so clearing a flag on unchoose never frees a line that another queen still uses.

---
# PLACE in pseudocode

```algorithm
function PLACE(r):                   // rows 0..r-1 hold safe queens
  if r = n then
    record a copy of col; return
  for c ← 0 to n - 1 do
    if square (r, c) is attacked then continue   // prune
    col[r] ← c; mark (r, c)          // choose
    PLACE(r + 1)                     // explore
    unmark (r, c)                    // unchoose
```

- Invariant: on entry, the queens in rows `0..r-1` attack no one.

> The comment on the first line is the invariant that makes the method correct. It holds for row zero because no queens are placed. If it holds on entry, the only queens added are on unattacked squares, so it holds for the recursive call. So when r reaches n, all n queens are mutually safe. And no solution is missed, because we only skip squares that are already attacked.

---
# PLACE in Java

```java
private void place(int r) {
    nodes++;
    if (r == n) {                               // all rows filled: a solution
        solutions.add(col.clone());
        return;
    }
    for (int c = 0; c < n; c++) {
        if (!allowed(r, c)) continue;           // prune: attacked square
        col[r] = c;                             // choose
        mark(r, c, true);
        place(r + 1);                           // explore
        mark(r, c, false);                      // unchoose
    }
}
```

> The method allowed is the safety test from the previous slide; in the tested file it also writes a step log, used for the traces on the next slides, and the logging lines are left out here. The counter nodes counts every call, including the root, and it is how we will measure pruning. Note col dot clone in the base case: the array keeps changing as the search continues, so we store a copy.

---
@type section
# N-Queens: trace and cost

---
# 4-Queens: the first queen in column 0

```diagram
@dir LR
@reveal manual
R[empty board]
focus R
--- Start: row 0 tries column 0 first.
R -> A[0] -> A2[0, 2]
focus R A A2
--- Row 1: columns 0 and 1 are attacked; place at 2.
A2 -> D1[dead end].rose : row 2: all 4 attacked
focus A2 D1
--- Every square in row 2 is attacked. Undo row 1.
A -> A3[0, 3] -> A31[0, 3, 1]
focus A A3 A31
--- Row 1 at column 3; row 2 at column 1 (column 0 is attacked).
A31 -> D2[dead end].rose : row 3: all 4 attacked
focus A31 D2
--- Row 3 is dead, row 2 columns 2 and 3 are attacked, row 1 has no more columns.
```

> Labels list the queens' columns by row, so zero, three, one means rows zero, one and two hold queens in columns zero, three and one. Follow the step log printed by the code. With a queen in the corner, row one rejects columns zero and one and places at column two. Row two then finds all four squares attacked, so the search backs up and moves the row one queen to column three. Row two rejects column zero and places at column one, and row three is completely attacked. Nothing else fits, so the corner queen itself is removed.

---
# Why `[0, 2]` is a dead end

```html
<div style="display:flex;gap:32px;justify-content:center;align-items:center;flex-wrap:wrap">
<table style="border-collapse:collapse;flex:none"><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"><b>Q</b></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td></tr><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"><b>Q</b></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td></tr><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"><span style="color:var(--rose-ink)">&times;</span></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"><span style="color:var(--rose-ink)">&times;</span></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"><span style="color:var(--rose-ink)">&times;</span></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"><span style="color:var(--rose-ink)">&times;</span></td></tr><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td></tr></table>
<div style="max-width:22em">
<p>Row 2, column 0: same column as the queen in row 0.</p>
<p>Row 2, column 1: anti-diagonal of row 1 ($2+1 = 1+2$).</p>
<p>Row 2, column 2: same column as the queen in row 1.</p>
<p>Row 2, column 3: diagonal of row 1 ($2-3 = 1-2$).</p>
</div>
</div>
```

> Here is the board behind the first dead end. The crosses mark the four squares of row two, and each one is attacked for the reason listed. Checking a square is three array reads, so the whole row is rejected in constant time per square. Without pruning, the search would still place queens on these squares and try all sixteen ways to fill rows two and three before rejecting them.

---
# 4-Queens: the first solution

```diagram
@dir LR
@reveal manual
R[empty board]
focus R
--- Back at the root. Row 0 tries column 1.
R -> B[1] -> B3[1, 3]
focus R B B3
--- Row 1: columns 0, 1 and 2 are attacked; place at 3.
B3 -> B30[1, 3, 0]
focus B3 B30
--- Row 2: column 0 is safe.
B30 -> B302[1, 3, 0, 2].green : solution
focus B30 B302
--- Row 3: columns 0 and 1 attacked; column 2 is safe. Record [1, 3, 0, 2].
```

> This time every row finds a safe square on the first safe try. Row one needs column three, the only column not attacked by the queen at column one. Rows two and three fall into place, and the base case records the solution one, three, zero, two. The search does not stop here: it unchooses row three and continues, because we asked for all solutions.

---
# The first solution on the board

```html
<div style="display:flex;gap:32px;justify-content:center;align-items:center;flex-wrap:wrap">
<table style="border-collapse:collapse;flex:none"><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"><b>Q</b></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td></tr><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"><b>Q</b></td></tr><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"><b>Q</b></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td></tr><tr><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--gray)"><b>Q</b></td><td style="width:56px;height:56px;padding:0;text-align:center;font-size:1.3em;border:1px solid var(--rule);background:var(--paper)"></td></tr></table>
<div style="max-width:22em">
<p><code>col = [1, 3, 0, 2]</code></p>
<p>Columns used: 1, 3, 0, 2, all different.</p>
<p>Values of $r-c$: $-1, -2, 2, 1$, all different.</p>
<p>Values of $r+c$: $1, 4, 2, 5$, all different.</p>
</div>
</div>
```

> Verify the solution the way the flags do. Four different columns means no column is shared. Four different values of r minus c means no diagonal is shared, and four different values of r plus c means no anti-diagonal is shared. The check program runs an equivalent pairwise test, independently of the search, on every solution for n up to ten.

---
# 4-Queens: the rest of the search

```diagram
@dir LR
R[root] -> C[2] -> C0[2, 0] -> C03[2, 0, 3] -> C031[2, 0, 3, 1].green
R -> D[3] -> D0[3, 0] -> D02[3, 0, 2]
D -> D1[3, 1]
```

- Column 2 gives the mirror image `[2, 0, 3, 1]`, the second solution.
- Column 3 dies at `[3, 0, 2]` and `[3, 1]`.
- Nodes visited: $1 + 4 + 6 + 4 + 2 = 17$, as counted by the code.

> The second half of the search is a reflection of the first half. Column two leads to the second solution, the mirror image of the first. Column three first tries row one at column zero, reaches a third queen and dies in row three, then tries column one and dies in row two. Counting nodes level by level: one root, four first queens, six second queens, four third queens and the two solutions.

---
# What pruning saves, measured

| $n$ | Solutions | Pruned | Distinct columns | Any column |
|---|---|---|---|---|
| 4 | 2 | 17 | 65 | 341 |
| 5 | 10 | 54 | 326 | 3,906 |
| 6 | 4 | 153 | 1,957 | 55,987 |
| 7 | 40 | 552 | 13,700 | 960,800 |
| 8 | 92 | 2,057 | 109,601 | 19,173,961 |

Search-tree nodes (calls, root included); the last two columns check only full boards. Counts exclude the per-node safety checks.

> Every number here came from running three searches in the Java file, and all three find the same solutions. The middle baseline puts one queen in each row and each column, so it walks the permutation tree and checks diagonals only on full boards. The last column allows any column in every row: one plus n plus n squared and so on up to n to the n nodes. The check program confirms both formulas. The counts measure tree size, not exact running time, since each node also does safety checks. For eight queens, pruning visits about two thousand nodes instead of about a hundred thousand.

---
# Pruning does not remove the exponential worst case

- Pruning can shrink the tree, but for subset sum some inputs prune nothing, so the worst case stays $\Theta(2^n)$ nodes.
- Example: target $\ge$ the total of all numbers. The size test never fires.
- Pruned N-Queens nodes still grow by a factor of about 2.8 to 3.7 per extra row here.
- Listing all subsets or permutations needs exponential time for the output alone.

> A pruning rule is only as good as the inputs let it be. For subset sum, if the target is at least the sum of every number, no partial sum ever exceeds it, so the break never happens and the search visits every subset. For the searches in this lecture, the safe worst-case bound is the size of the unpruned tree. Measured counts on particular inputs, like the N-Queens table, can be far smaller, and good pruning is often what makes a search practical.

---
# Quick check

```quiz
Queens sit at row 0, column 1 and row 1, column 3 of a 4-by-4 board. Which square in row 2 is safe?
- [x] Column 0
- [ ] Column 1
- [ ] Column 2
- [ ] Column 3
```

> Column zero is the only safe square. Column one is the same column as the row zero queen. Column two has r plus c equal to four, the same as the row one queen at one plus three, so it is on that queen's anti-diagonal. Column three is the same column as the row one queen. This is the partial board one, three from the first solution.

---
@type section
# Subset sum

---
# Subset sum on sorted input

- Given positive integers, find every subset with sum exactly `target`.
- Input `[2, 3, 5, 6, 8]`, target `10`. Answers: `[2, 3, 5]` and `[2, 8]`.
- Choice at each step: the next number to add, taken left to right.
- Pruning: if `a[i] > remaining`, stop the loop.
- Sorted input makes that a `break`: every later number is at least as large.

> Each call tries every later number as the next one to add, so each subset is built in increasing order exactly once. The pruning test is simple: if the next number alone exceeds what remains, it cannot be used. On sorted input a stronger conclusion follows for free, because every number after it is at least as big, so the whole rest of the loop can be skipped.

---
# Subset sum in Java

```java
private static void search(int[] a, int start, int remaining, List<Integer> chosen,
                           List<List<Integer>> out, boolean prune, boolean reuse) {
    nodes++;
    if (remaining == 0) {
        out.add(new ArrayList<>(chosen));
        return;
    }
    for (int i = start; i < a.length; i++) {
        if (prune && a[i] > remaining) break;   // sorted: later a[i] are too big
        chosen.add(a[i]);                       // choose
        search(a, reuse ? i : i + 1, remaining - a[i], chosen, out, prune, reuse);
        chosen.remove(chosen.size() - 1);       // unchoose
    }
}
```

> The two flags let one method serve three searches: with and without pruning, and with reuse for combination sum. Without reuse, the recursive call starts at i plus one, so each number is used at most once. The base case fires when nothing remains; because all numbers are positive, no extension of a solution can also be a solution.

---
# The pruned tree: branch `[2]`

```diagram
@dir LR
@reveal manual
R[none, need 10]
focus R
--- Start. Try 2 first.
R -> A[2, need 8] -> B[2, 3, need 5] -> C[2, 3, 5].green : solution
focus A B C
--- 2, 3, 5 reaches 10. Back at [2, 3], 6 > 5: break.
A -> D[2, 5, need 3]
A -> E[2, 6, need 2]
focus A D E
--- After [2, 5] and [2, 6], the next number is too big: break.
A -> F[2, 8].green : solution
focus A F
--- 2 + 8 = 10. The branch under 2 is finished.
```

> Follow the need values. After two and three we need five, and five itself finishes the sum. Back at two, three, the next candidate six exceeds five, so the loop breaks at once. Two, five needs three, but the next candidate is six, too big. Two, six needs two, and the next candidate eight is too big. Two, eight is the second solution. Seven nodes so far, including the root.

---
# The pruned tree: the other branches

```diagram
@dir LR
R[none, need 10] -> A[3, need 7]
A -> B[3, 5, need 2]
A -> C[3, 6, need 1]
R -> D[5, need 5]
R -> E[6, need 4]
R -> F[8, need 2]
```

- Under `[3]`, 8 > 7 breaks the loop; `[5]`, `[6]`, `[8]` stop at once.
- Total **13** nodes. Same answers without the `break`: **29** nodes.

> Under three we need seven. Adding five or six leaves too little for anything later, and eight alone is too big, so the loop breaks. The first-level nodes five, six and eight each need a number too big to follow, or have no numbers left. The full count, thirteen, and the unpruned count, twenty-nine, were measured by the code; the unpruned search visits every subset except the three that extend the solution two, three, five.

---
# Combination sum: reuse allowed

- Change one argument: recurse on `i` instead of `i + 1`.
- Coins `[1, 3, 4]`, target `6` gives 4 multisets, printed by the code:
- `[1,1,1,1,1,1]`, `[1,1,1,3]`, `[1,1,4]`, `[3,3]`. The search visited 15 nodes.
- L20 counted the same 4 with `countWays` in $\Theta(kA)$ time.
- Listing all answers costs at least their total size; counting does not.

> Passing i rather than i plus one lets the same number be chosen again, while never going back to a smaller one, so each multiset appears once, in nondecreasing order. That is exactly the question the coin-counting loop answered in the last lecture. The difference is what we ask for: DP counts the four ways without building them, while backtracking produces each one, which is necessary when you need the actual lists.

---
# More problems with the same shape

- **Sudoku**: choice = a digit for the next empty cell; prune on row, column and box.
- **Graph coloring**: choice = a color for the next vertex; prune if a neighbor has it.
- **Word search, mazes**: choice = the next step; prune on walls and visited cells.
- The pattern: what is one choice, and when is a partial solution hopeless?

> Each of these fits the template with a different choice and a different test. In Sudoku a digit that already appears in the same row, column or three-by-three box is rejected immediately. In graph coloring with k colors a vertex may not take a color already used by an adjacent vertex, using the adjacency lists from the graphs lectures. The same two questions design every one of them.

---
@type section
# Choosing a method

---
# Backtracking among its neighbours

| Method | Explores | Uses |
|---|---|---|
| Brute force | Every complete candidate | Tiny inputs, test oracles |
| Backtracking | Partial candidates, prunes dead ends | Constraint problems, listing all answers |
| Dynamic programming | Each distinct subproblem once | Subproblems overlap |
| Branch and bound | Partial candidates, prunes by a bound | Optimisation |

> Brute force builds complete candidates and checks them at the end, which is what our unpruned N-Queens did. Backtracking checks as it builds. If the same subproblem appears on many branches, memoizing it turns the search into dynamic programming. Branch and bound, on the next slide, adapts pruning to optimisation, where many candidates are valid and we want the best.

---
# When subproblems overlap, memoize

- Decide the numbers in order, take or skip, and ask: can `a[i..]` reach `remaining`?
- That answer depends only on the pair (`i`, `remaining`), not on the path.
- Store it: $(n+1)(T+1)$ distinct pairs for target $T$, each solved once.
- Deciding or counting takes $\Theta(nT)$ time: pseudo-polynomial, so still exponential in the number of bits of $T$.
- Listing every answer can still take exponential time: there may be exponentially many.

> Different paths can reach the same index with the same amount remaining. Taking two and three and skipping five, or skipping two and three and taking five, both arrive at index three needing five. The rest of the search from that point is identical, so its answer can be stored. With a memo keyed by the pair, each pair is solved once, which is the same pseudo-polynomial behaviour as the knapsack table, and for the same reason: the table grows with the value of T, not with the few bits needed to write it.

---
# Branch and bound, briefly

- For **optimisation**: maximise or minimise over the valid solutions.
- Keep the best complete solution found so far.
- At each node compute an optimistic **bound** on any completion.
- If the bound cannot beat the best so far, prune the subtree.
- 0/1 knapsack: the fractional-knapsack value from L19 is such a bound.

> Suppose we search 0/1 knapsack by deciding item by item. At a partial choice, the best fractional completion is an upper bound on any whole-item completion, because allowing fractions can only help. If that bound is no better than a packing we already have, the subtree cannot contain a better answer and is skipped. How much this saves depends on the input; the worst case is still exponential.

---
# Common mistakes

- Forgetting to unchoose, or undoing only part of the state.
- Recording `chosen` itself instead of a copy: every stored answer ends up the same list.
- Using `break` for pruning on unsorted input; there only `continue` is safe.
- Duplicate values in the input give duplicate answers unless you skip equal neighbours.
- Pruning too eagerly: a test that rejects a completable partial solution loses answers.

> Most backtracking bugs come from shared mutable state. If unchoose is missing, later branches start from a wrong state. If you store the list itself, all stored references point to the one list that is empty when the search ends. The break in subset sum is valid only because the input is sorted. And a pruning test must be sound: it may only reject partial solutions that truly cannot be completed.

---
# Quick check

```quiz
The subsets method records `out.add(chosen)` instead of `out.add(new ArrayList<>(chosen))`. What does it return for `[a, b, c]`?
- [ ] The 8 subsets, in the same order.
- [ ] Only the subset `[a, b, c]`.
- [x] 8 references to one list, which is empty when the search ends.
- [ ] It throws an exception.
```

> Eight references to the same list. Every recorded entry is the one shared list chosen, not a snapshot of it. Each unchoose removes what the matching choose added, so when the search finishes, the shared list is empty, and printing the result shows eight empty lists. The copy in the base case is what freezes each answer.

---
# In Java

- `ArrayList` works well as the path: `add` at the end is amortized constant time.
- `remove(size() - 1)` removes the last element without shifting others.
- A `boolean[]` or `BitSet` records which items or lines are used.
- Recursion depth equals the number of choices, so the default stack is ample for these inputs.
- An explicit stack such as `ArrayDeque` turns the search iterative if needed.

> The standard library has no backtracking framework; the method is a pattern, not a class. The Java API documents that ArrayList add runs in amortized constant time. Removing the last element shifts nothing, since there is nothing after it. Recursion depth is the number of rows or items, at most a few dozen here, far below what overflows the call stack.

---
@type section
# Course recap

---
# Course recap: data structures

| When the problem needs to... | Use | Lectures |
|---|---|---|
| Find an item in a list, sorted or not | Linear or binary search | L03 |
| Put records in order | Merge sort, quicksort, insertion, radix | L04, L05 |
| Insert and delete at known positions | Linked lists | L06 |
| Process in LIFO or FIFO order | Stacks and queues | L07 |
| Keep keys ordered; $O(\log n)$ worst case when balanced | BSTs; AVL and red-black trees | L08, L10, L11 |
| Look up by key in expected $O(1)$ | Hash tables | L12 |
| Index data stored in large blocks on disk | B-trees and B+ trees | L13 |
| Repeatedly take the smallest or largest | Heaps, priority queues | L14 |

> This table is a map of the course by question rather than by chapter. Read each row as: if your problem needs this, reach for that structure first. The costs in the middle column carry their assumptions: logarithmic worst case needs a balanced tree, and constant expected time for hashing needs a good hash function and a bounded load factor.

---
# Course recap: algorithms and paradigms

| When the problem is... | Use | Lectures |
|---|---|---|
| Hierarchical, visit every node | Tree traversals | L09 |
| A network of relationships | Graphs: BFS, DFS | L15 |
| Tasks with dependencies | Topological sort, cycle detection | L16 |
| Cheapest routes, cheapest connection | Shortest paths, minimum spanning trees | L17, L18 |
| Solved by a provably safe local choice | Greedy | L19 |
| Built from overlapping subproblems | Dynamic programming | L20 |
| A search over choices under constraints | Backtracking | L21 |

> The last three rows are ways of thinking rather than single algorithms, and they form a ladder. Try greedy when you can prove the local choice is safe. Use dynamic programming when the subproblems overlap. Fall back on backtracking with good pruning when the space of answers is truly large. Many real programs combine several rows of both tables.

---
# Summary

- Backtracking: extend a partial solution one choice at a time, depth first.
- Choose, explore, unchoose; record copies at complete solutions.
- Subsets $2^n$, permutations $n!$: listing costs at least the output size.
- Pruning, measured: 8-Queens in 2,057 nodes, versus 109,601 (distinct columns) or 19,173,961.
- The worst case stays exponential; overlap means memoize, bounds mean branch and bound.

> The template is short, and nearly every bug lives in the state it shares between calls. The measured N-Queens numbers count search-tree nodes, not the safety checks each node performs, and they show why pruning matters in practice even against the one-queen-per-column baseline, while the subset-sum example with a large target shows why it does not change the worst case. When you meet a new search problem, name the choice, name the dead-end test, and check whether subproblems repeat.

---
# Check yourself

- Draw the pruned subset-sum tree for `[1, 2, 4, 7]` with target `7`. How many nodes?
- Why is `break` wrong in subset sum when the input is not sorted? Give an input.
- For N-Queens, which change would find only the first solution, and how many nodes would $n = 4$ visit?

> Try these on paper first, then run the Java code to check your counts. For the first, remember that the root counts as a node. For the second, find an input where a large number comes before a small one that would complete a solution. For the third, think about returning a flag from the recursive call.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 20: Elementary graph algorithms (depth-first search).
- CLRS, 4th ed., Chapter 14: Dynamic programming; Chapter 15: Greedy algorithms.
- CLRS has no chapter on backtracking; the template and examples here are standard.
- Java SE API documentation: `java.util.ArrayList`, `java.util.ArrayDeque`, `java.util.BitSet`.
- All traces and counts were produced by this lecture's tested Java files.

> The depth-first search chapter describes the traversal order that backtracking follows. The dynamic programming and greedy chapters are the comparison points used in the last section. For library facts, the Java documentation is the authority. The node counts and traces are original measurements from the Java code for this lecture, not figures from a book.
