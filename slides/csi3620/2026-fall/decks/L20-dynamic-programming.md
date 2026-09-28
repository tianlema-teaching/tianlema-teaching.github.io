@title Lecture 20: Dynamic programming
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Dynamic programming
## Solve each subproblem once, then reuse it

---
# Where we are

- L04: divide and conquer splits a problem into independent halves.
- L19: greedy commits to one locally best choice and never looks back.
- Greedy failed for coins `{1, 3, 4}`: it paid 6 with three coins, not two.
- Today: try every last choice, but solve each smaller problem only once.

> In Lecture 4 we split problems into pieces that never share work. Last lecture we made one choice at a time and hoped it was safe, and we saw a coin system where it was not. Dynamic programming keeps the honesty of trying every option while avoiding the cost of recomputing the same smaller answers. Everything today builds on recursion, which you already know.

---
# By the end of today you can

- Recognise overlapping subproblems and optimal substructure.
- Turn a recurrence into memoized or bottom-up Java code.
- Fill a DP table by hand and read an optimal solution back out.
- Explain why $O(nW)$ for knapsack is pseudo-polynomial.
- Pick the right loop direction for 0/1 versus unbounded problems.
- Choose between DP, divide and conquer, and greedy for a new problem.

> These are the things you should be able to do on paper by the end of class. The most important one is the middle pair: given a recurrence, fill the table, and given a filled table, recover the actual answer, not just its value. We will practise each on small inputs that fit on one slide.

---
# The problem greedy got wrong

- Coins `{1, 3, 4}`, unlimited supply. Pay amount `6` with the fewest coins.
- Greedy takes the largest coin that fits: `4 + 1 + 1`, three coins.
- The optimum is `3 + 3`, two coins.
- The last coin of an optimal answer is 1, 3 or 4. We do not know which.
- So try all three, each on a smaller amount: `5`, `3` or `2`.

> Here is the idea of the whole lecture in one example. We do not know the right first move, so we try each possibility and take the best. Trying each possibility recursively looks expensive, and the naive version is. The fix is to notice that the smaller amounts keep coming back, so we remember their answers. That remembering is dynamic programming.

---
# Running examples for today

| Problem | Input used on every trace |
|---|---|
| Fibonacci | $F(5)$, and call counts up to $n = 40$ |
| Fewest coins, counting ways | coins `{1, 3, 4}`, amount `6` |
| 0/1 knapsack | items A, B, C with weights `1, 3, 4`; capacity `6` |
| Longest common subsequence | `BACDB` and `BDCB` |
| Edit distance | `CART` to `CHAT` |

Every table on later slides was printed by the tested Java code.

> The knapsack weights deliberately match the coin values, so you can compare the two problems side by side. Every number that appears in a table today was produced by running the Java classes for this lecture, and the check program asserts every table, array and call count shown: the coin, knapsack, LCS and edit-distance tables, the ways arrays, the one-row arrays and the Fibonacci call counts. When you practise, pick one of these inputs and fill the table yourself before looking.

---
@type section
# Overlapping subproblems

---
# Fibonacci by plain recursion

$$F(0)=0,\qquad F(1)=1$$

$$F(n)=F(n-1)+F(n-2)$$

for every $n\ge 2$.

```java
public static long naive(int n) {
    calls++;
    if (n <= 1) return n;                       // F(0) = 0, F(1) = 1
    return naive(n - 1) + naive(n - 2);
}
```

- The code is the definition, word for word. `calls` counts every call.

> This is the recursion you wrote in your first Java course. It is correct, and it is a direct translation of the definition. The counter line is only there so we can measure. Before the next slide, guess how many calls naive of five makes. Most people guess a number close to five.

---
# The same subproblem, again and again

| Argument | 5 | 4 | 3 | 2 | 1 | 0 | total |
|---|---|---|---|---|---|---|---|
| Calls made by `naive(5)` | 1 | 1 | 2 | 3 | 5 | 3 | **15** |

```diagram
@dir LR
F5((F5)) -> F4((F4))
F5 -> F3a((F3))
F4 -> F3b((F3))
F4 -> F2a((F2))
F3a -> F2b((F2))
F3b -> F2c((F2))
```

- $F(3)$ is solved twice and $F(2)$ three times. Each repeat redoes a whole subtree.

> The table was counted by instrumenting the recursion. The diagram shows only the top of the call tree, and already $F(3)$ appears twice and $F(2)$ three times. Each of those repeated calls rebuilds its entire subtree from scratch. Six different subproblems exist, zero through five, but the recursion makes fifteen calls. That gap is what the word overlapping means.

---
# Call counts grow exponentially

```chart
type: line
title: Calls made by naive(n)
x: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
ylabel: calls
Calls: 1, 1, 3, 5, 9, 15, 25, 41, 67, 109, 177
```

- Measured: $n=20$: 21,891 calls; $n=30$: 2,692,537; $n=40$: 331,160,281.
- The count equals $2F(n+1)-1$, and $F(n)=\Theta(\varphi^n)$ with $\varphi\approx1.618$.

> Every count here was measured by running the code, and the check program confirms the formula two F of n plus one, minus one, for every n up to twenty-five. Because Fibonacci numbers grow like the golden ratio to the n, so does the running time: Theta of phi to the n. Going from thirty to forty multiplies the work by more than a hundred.

---
# Two properties that make DP work

- **Overlapping subproblems**: a recursive solution asks for the same smaller instance many times.
- **Optimal substructure**: an optimal solution is built from optimal solutions to subproblems.
- Fibonacci has the first; coin change and knapsack have both.
- The number of distinct subproblems is small (polynomial), so we can store every answer.

> Dynamic programming needs both properties for optimisation problems. Overlap makes storing answers pay off. Optimal substructure makes the recurrence correct: if the best way to pay six ends with a three, then the rest must be a best way to pay three, otherwise we could swap in a better way and improve the whole. Fibonacci is not an optimisation problem, so it only illustrates the overlap.

---
# Top-down: memoization

```java
public static long memo(int n) {
    long[] memo = new long[n + 1];
    Arrays.fill(memo, -1);                      // -1 means "not computed yet"
    return memo(n, memo);
}

private static long memo(int n, long[] memo) {
    calls++;
    if (n <= 1) return n;
    if (memo[n] != -1) return memo[n];          // reuse a stored answer
    memo[n] = memo(n - 1, memo) + memo(n - 2, memo);
    return memo[n];
}
```

> Memoization keeps the recursive code and adds a table of answers. Look at the third line of the helper: if we have seen this n before, return the stored value immediately. The sentinel minus one is safe because no Fibonacci number is negative. The first call for each n does the real work, and every later call for that n costs constant time.

---
# Memoized calls: linear

```diagram
@dir LR
F5((F5)) -> F4((F4)) -> F3((F3)) -> F2((F2)) -> F1((F1))
F2 -> F0((F0))
F5 ..> F3
F4 ..> F2
F3 ..> F1
```

- Solid: first request. Dotted: a repeat request, answered at once (from the table; F1 is a base case).
- Measured calls: $n=5$: 9; $n=10$: 19; in general $2n-1$ for $n\ge1$.
- The recursion still goes $n$ levels deep: $\Theta(n)$ stack.

> The picture shows every call memo of five makes: six nodes, one per subproblem, instead of a tree with exponentially many nodes. Solid arrows are first requests, which do the real work; dotted arrows are repeat requests, which return at once. The count two n minus one splits as n minus one real calls for F of two up to F of n, plus n calls that return at once, either a base case or a table lookup. For n equal to five that is four plus five, nine. The stack is still n deep, which matters for very large n.

---
# Bottom-up: tabulation

```java
public static long table(int n) {
    if (n <= 1) return n;
    long[] f = new long[n + 1];
    f[0] = 0;
    f[1] = 1;
    for (int i = 2; i <= n; i++) {
        f[i] = f[i - 1] + f[i - 2];
    }
    return f[n];
}
```

- Fill the table from the base cases upward. No recursion, no stack.

> Tabulation turns the dependency graph around. We know F of i needs F of i minus one and F of i minus two, so we compute in increasing order of i, and by the time we reach i both are already in the array. For n equal to five the array fills as zero, one, one, two, three, five. The loop does n minus one additions.

---
# Keep only what the next step needs

```java
public static long twoVariables(int n) {
    if (n == 0) return 0;
    long prev = 0, curr = 1;                    // F(i-1), F(i) for i = 1
    for (int i = 2; i <= n; i++) {
        long next = prev + curr;
        prev = curr;
        curr = next;
    }
    return curr;
}
```

- Each $F(i)$ reads only the two previous values, so two variables suffice.

> Look at which array entries the loop reads: only the last two. Once F of i is computed, F of i minus two is never read again, so we can forget it. This is the simplest case of a space optimisation we will reuse for knapsack. One caution that applies to all four versions: a Java long holds Fibonacci numbers only up to F of ninety-two.

---
# Four versions compared

| Version | Time | Extra space |
|---|---|---|
| Plain recursion | $\Theta(\varphi^n)$ | $\Theta(n)$ stack |
| Memoized (top-down) | $\Theta(n)$ | $\Theta(n)$ table + $\Theta(n)$ stack |
| Tabulated (bottom-up) | $\Theta(n)$ | $\Theta(n)$ table |
| Two variables | $\Theta(n)$ | $\Theta(1)$ |

Each addition counts as one step, which holds while values fit in a `long`.

> These are worst-case bounds in terms of the value n, and for every n there is only one input, so best and worst agree. The unit-cost assumption matters: if you switch to BigInteger for huge n, each addition costs time proportional to the number of digits, and the analysis changes. The jump from exponential to linear comes entirely from not recomputing.

---
# Quick check

```quiz
Counted by running the code, how many calls does `naive(6)` make?
- [ ] 11
- [ ] 13
- [x] 25
- [ ] 64
```

> The answer is twenty-five. Use the recurrence on call counts: calls of n equals one plus calls of n minus one plus calls of n minus two, with calls of zero and one equal to one. Calls of four is nine and calls of five is fifteen, so calls of six is one plus fifteen plus nine. Eleven is the memoized count, two times six minus one. Sixty-four is two to the sixth, a common guess that ignores how uneven the tree is.

---
@type section
# A recipe for DP

---
# Five steps

1. **Subproblem in words**: "`best[a]` is the fewest coins that sum to `a`".
2. **Recurrence with base cases**: how an answer uses smaller answers.
3. **Evaluation order**: every entry is computed after the entries it reads.
4. **Answer**: which entry holds the result, e.g. `best[amount]`.
5. **Reconstruction**: walk back through the table to recover the choices.

> Step one is where most of the thinking happens. If you cannot say in one English sentence what a table entry means, you cannot write a correct recurrence. Step two asks: what is the last decision an optimal solution makes, and what smaller problem remains? Steps three to five are mechanical once the first two are right. We now run the recipe four times.

---
# Top-down or bottom-up?

| | Memoized (top-down) | Tabulated (bottom-up) |
|---|---|---|
| Code shape | Recursion plus a lookup | Loops in a chosen order |
| Order of work | Found by the recursion | You must choose it |
| Subproblems solved | Only those reachable | Usually all of them |
| Risk | Deep recursion, stack overflow | Wrong loop order |
| Space savings | Hard | Often easy (keep a row) |

> Both give the same asymptotic time for the problems today. Memoization is often the quickest way from a recurrence to working code, because you never think about order. Tabulation avoids recursion depth and makes the space optimisation visible. A reasonable habit is to write the memoized version first as a correctness reference, then the loops.

---
@type section
# Coin change

---
# Fewest coins: subproblem and recurrence

- Subproblem: `best[a]` = fewest coins summing to `a`, for `a = 0..amount`.
- The last coin is some `c ≤ a`; the rest is a best way to make `a - c`.

$$\mathit{best}[a]=\min_{c\,\le\, a}\bigl(\mathit{best}[a-c]+1\bigr)$$

- Base case: `best[0] = 0`.
- If no coin fits, or every `a - c` is impossible, `best[a]` is infinite.
- Order: increasing `a`, since every `a - c` is smaller.

> Say the subproblem aloud: best of a is the fewest coins that sum exactly to a. The optimal substructure argument is the exchange we mentioned: if the rest were not optimal, a better rest plus the same last coin would beat the optimum. The base case is best of zero equals zero coins, not one, and not infinity. Impossible amounts get a special infinite value.

---
# Fewest coins in pseudocode

```algorithm
function FEWEST(coins, amount):
  best[0..amount] ← ∞; best[0] ← 0
  for a ← 1 to amount do
    for each c in coins do
      if c ≤ a and best[a - c] ≠ ∞ then
        best[a] ← min(best[a], best[a - c] + 1)
  return best[amount]          // ∞ means impossible
```

- Two nested loops, $A$ amounts times $k$ coin values: $\Theta(kA)$ time, $\Theta(A)$ space.

> The outer loop walks amounts in increasing order, which is the evaluation order from the recipe. The inner loop tries every possible last coin. The test best of a minus c not equal to infinity skips amounts that cannot be made. The table has amount plus one entries, because amount zero is a real entry.

---
# Fewest coins in Java

```java
public static int[] fewestTable(int[] coins, int amount) {
    int[] best = new int[amount + 1];
    Arrays.fill(best, INF);
    best[0] = 0;                                // zero coins make amount 0
    for (int a = 1; a <= amount; a++) {
        for (int c : coins) {
            if (c <= a && best[a - c] != INF) {
                best[a] = Math.min(best[a], best[a - c] + 1);
            }
        }
    }
    return best;
}
```

> INF is Integer MAX VALUE. Look at the guard: without the test against INF, INF plus one overflows to a large negative number, and min would pick it. That is a classic Java bug in DP code. The method returns the whole table, because the next slides fill it and then read a solution out of it.

---
# The coin table, filled step by step

```html
<table style="border-collapse:collapse;margin:0 auto;font-variant-numeric:tabular-nums">
<tr style="border-bottom:2px solid var(--rule)"><th style="padding:4px 12px">a</th><th style="padding:4px 12px;text-align:left">try last coin 1, 3, 4</th><th style="padding:4px 12px">best[a]</th></tr>
<tr><td style="padding:4px 12px;text-align:center">0</td><td style="padding:4px 12px">base case</td><td style="padding:4px 12px;text-align:center"><b>0</b></td></tr>
<tr data-step="1"><td style="padding:4px 12px;text-align:center">1</td><td style="padding:4px 12px">1+best[0]=1</td><td style="padding:4px 12px;text-align:center"><b>1</b></td></tr>
<tr data-step="2"><td style="padding:4px 12px;text-align:center">2</td><td style="padding:4px 12px">1+best[1]=2</td><td style="padding:4px 12px;text-align:center"><b>2</b></td></tr>
<tr data-step="3"><td style="padding:4px 12px;text-align:center">3</td><td style="padding:4px 12px">1+best[2]=3, 1+best[0]=1</td><td style="padding:4px 12px;text-align:center"><b>1</b></td></tr>
<tr data-step="4"><td style="padding:4px 12px;text-align:center">4</td><td style="padding:4px 12px">1+best[3]=2, 1+best[1]=2, 1+best[0]=1</td><td style="padding:4px 12px;text-align:center"><b>1</b></td></tr>
<tr data-step="5"><td style="padding:4px 12px;text-align:center">5</td><td style="padding:4px 12px">1+best[4]=2, 1+best[2]=3, 1+best[1]=2</td><td style="padding:4px 12px;text-align:center"><b>2</b></td></tr>
<tr data-step="6"><td style="padding:4px 12px;text-align:center">6</td><td style="padding:4px 12px">1+best[5]=3, 1+best[3]=2, 1+best[2]=3</td><td style="padding:4px 12px;text-align:center"><b>2</b></td></tr>
</table>
```

> Step through the rows. Amounts one and two can only use the coin one. Amount three can end with a three, leaving zero, so one coin. Amount four ends with a four. Amount five ties between ending in one and ending in four, two coins either way. Amount six: ending with three leaves three, which needs one coin, so two coins in total. The table was printed by the Java code, and the final entry, two, is the answer greedy missed.

---
# Reading the coins back out

```java
int a = amount;
while (a > 0) {
    for (int c : coins) {
        if (c <= a && best[a - c] != INF && best[a - c] + 1 == best[a]) {
            used.add(c);                    // c was a last coin of some optimum
            a -= c;
            break;
        }
    }
}
```

- At `a = 6`: coin 1 gives `best[5]+1 = 3`, not 2; coin 3 gives `best[3]+1 = 2`. Take 3.
- At `a = 3`: coin 3 gives `best[0]+1 = 1`. Take 3. Result: `[3, 3]`.

> The table holds values, not choices, but a choice can be recovered: a coin c is a valid last coin exactly when best of a minus c plus one equals best of a. Walk back from six, take any coin that passes this test, subtract, and repeat. This costs k checks per coin used. An alternative is to store the chosen coin in a second array while filling.

---
# Counting ways is a different question

- Question: how many **multisets** of coins sum to `6`? Answer: `4`.
- `1+1+1+1+1+1`, `3+1+1+1`, `3+3`, `4+1+1`.
- Recurrence adds instead of taking a minimum; base case `ways[0] = 1`.

```java
for (int c : coins) {  // coins outside: each multiset once
    for (int a = c; a <= amount; a++) {
        ways[a] += ways[a - c];
    }
}
```

- `ways` after coin 1: `[1,1,1,1,1,1,1]`; after 3: `[1,1,1,2,2,2,3]`; after 4: `[1,1,1,2,3,3,4]`.

> Fewest coins minimises; counting sums over the choices. The base case changes meaning too: there is exactly one way to make zero, the empty collection, so ways of zero is one, not zero. Putting coins in the outer loop means each multiset is built in one fixed coin order, so it is counted once. The three arrays under the code were printed by the Java code.

---
# Loop order decides what is counted

| Outer loop | Counts | For `{1, 3, 4}` and `6` |
|---|---|---|
| Coins, then amounts | Multisets: order ignored | `4` |
| Amounts, then coins | Sequences: `1+3+1+1` differs from `3+1+1+1` | `9` |

- Swapping two loops silently changes the problem you solve.

> With amounts outside, every amount tries every coin as the last one, so the same coins in a different order count as different ways. That gives nine sequences, as the method countSequences confirms. Neither answer is wrong; they answer different questions. Before writing the loops, decide which question you mean, then write the order that matches it.

---
@type section
# 0/1 knapsack

---
# The 0/1 knapsack problem

| Item | A | B | C |
|---|---|---|---|
| Weight | 1 | 3 | 4 |
| Value | 1 | 5 | 6 |

- Capacity `6`. Each item is taken **once or not at all**. Maximise total value.
- Weights and capacity are non-negative integers (they index the table).
- Greedy by value per weight takes B, then A: value `6`.
- Optimum: A and C, weight `5`, value `7`.
- L19: greedy by ratio is optimal for the *fractional* version only.

> Each item is either packed whole or left behind. Value per unit of weight is highest for B, so the ratio rule from last lecture takes B first, skips C because it no longer fits, then takes A: value six. Packing A and C gives seven. Last lecture proved the ratio rule for fractional knapsack, where we could take part of C. With whole items that proof breaks, so we need DP.

---
# Subproblem and recurrence

- `dp[i][c]`: best value using only the first `i` items with capacity `c`, for integers `c = 0..W`.
- Item `i-1` (the `i`-th item) is either skipped or taken.

$$dp[i][c]=\max(\mathit{skip},\ \mathit{take})$$

- $\mathit{skip}=dp[i-1][c]$.
- $\mathit{take}=dp[i-1][c-w_{i-1}]+v_{i-1}$, only when $w_{i-1}\le c$.
- Base: $dp[0][c]=0$. Answer: `dp[n][W]`. Order: row by row, increasing `i`.

> Two indices are needed because a subproblem must remember both which items are still available and how much room is left. The recurrence has exactly two cases, skip or take. Taking item i minus one uses row i minus one, the row without that item, which is what forbids taking it twice. Indices start at zero for items, so row i talks about item i minus one.

---
# Knapsack in pseudocode

```algorithm
function KNAPSACK(w, v, W):        // n items, integer weights
  dp[0][c] ← 0 for every c ← 0 to W
  for i ← 1 to n do
    for c ← 0 to W do
      dp[i][c] ← dp[i-1][c]                    // skip item i-1
      if w[i-1] ≤ c then                       // or take it
        dp[i][c] ← max(dp[i][c], dp[i-1][c - w[i-1]] + v[i-1])
  return dp[n][W]
```

- Row 0 is the base case; every other row reads only the row above it.

> This is the recurrence written as loops. The outer loop adds one item at a time, and the inner loop tries every integer capacity from zero to W. Because row i reads only row i minus one, any order of the inner loop works here; that freedom disappears when we later squeeze the table into one row. The Java on the next slide is a line-by-line translation.

---
# Knapsack table in Java

```java
public static int[][] table(int[] w, int[] v, int cap) {
    int n = w.length;
    int[][] dp = new int[n + 1][cap + 1];       // row 0: no items, value 0
    for (int i = 1; i <= n; i++) {
        for (int c = 0; c <= cap; c++) {
            dp[i][c] = dp[i - 1][c];            // skip item i-1
            if (w[i - 1] <= c) {                // or take it, if it fits
                int take = dp[i - 1][c - w[i - 1]] + v[i - 1];
                dp[i][c] = Math.max(dp[i][c], take);
            }
        }
    }
    return dp;
}
```

> Java initialises the new array to zeros, which is exactly the base row: no items, no value. The comments label the two cases of the recurrence. The table has n plus one rows and capacity plus one columns, so the loops never step outside it. Watch the two indices minus one: they are where most off-by-one errors in this problem live.

---
# The knapsack table, row by row

```html
<table style="border-collapse:collapse;margin:0 auto;font-variant-numeric:tabular-nums;text-align:center">
<tr style="border-bottom:2px solid var(--rule)"><th style="padding:4px 8px;text-align:left">items \ c</th><th style="padding:4px 8px">0</th><th style="padding:4px 8px">1</th><th style="padding:4px 8px">2</th><th style="padding:4px 8px">3</th><th style="padding:4px 8px">4</th><th style="padding:4px 8px">5</th><th style="padding:4px 8px">6</th></tr>
<tr><td style="padding:4px 8px;text-align:left">none</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td></tr>
<tr data-step="1"><td style="padding:4px 8px;text-align:left">+A (1, 1)</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td></tr>
<tr data-step="2"><td style="padding:4px 8px;text-align:left">+B (3, 5)</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">5</td><td style="padding:4px 10px">6</td><td style="padding:4px 10px">6</td><td style="padding:4px 10px">6</td></tr>
<tr data-step="3"><td style="padding:4px 8px;text-align:left">+C (4, 6)</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">5</td><td style="padding:4px 10px">6</td><td style="padding:4px 10px">7</td><td style="padding:4px 10px">7</td></tr>
</table>
```

- Row B, `c = 4`: skip gives 1; take B gives `dp[1][1] + 5 = 6`.
- Row C, `c = 6`: skip gives 6; take C gives `dp[2][2] + 6 = 7`.

> Row A is simple: once the capacity is at least one, A fits and gives value one. In row B, at capacity four, taking B leaves one unit of room, and the best we can do in one unit with A alone is one, so six. In row C, at capacity six, taking C leaves two units, worth one using A and B, so seven beats the six from skipping. The bottom right entry, seven, is the answer.

---
# Which items? Walk back up the table

```java
for (int i = dp.length - 1; i >= 1; i--) {
    if (dp[i][c] != dp[i - 1][c]) {  // changed: item i-1 was taken
        items.add(i - 1);
        c -= w[i - 1];
    }
}
```

- Row C, `c = 6`: `7 ≠ 6`, so C was taken; room left `2`.
- Row B, `c = 2`: `1 = 1`, so B was skipped.
- Row A, `c = 2`: `1 ≠ 0`, so A was taken. Items: **A, C**.

> If the value in row i equals the value just above it, skipping item i minus one was optimal, so we skip it. If it changed, taking the item was the only way to reach that value, so we record it and subtract its weight. Each row is visited once, so reconstruction costs Theta of n. The method returns indices zero and two, which are A and C.

---
# Knapsack costs

- Time: $\Theta(nW)$ for $n$ items and capacity $W$, every input.
- Space: $\Theta(nW)$ for the table; $\Theta(W)$ with one row (later slide).
- Reconstruction: $\Theta(n)$ given the full table.
- Brute force tries all $2^n$ subsets; the checks compare both on random inputs.

> The table has n plus one times W plus one entries and each costs constant time. The one-row version saves space but loses the table needed for reconstruction, so you trade one for the other. The check program runs the table, the one-row version and a brute-force subset search on three hundred random inputs and requires the same best value.

---
# Why $\Theta(nW)$ is only pseudo-polynomial

- Input size counts **bits**: capacity $W$ is written with about $\log_2 W$ bits.
- With $b$ bits, $W$ can be as large as about $2^b$.
- So $nW$ can be about $n\cdot 2^b$: exponential in the length of the input.
- Adding one bit to $W$ can double the table; adding one item adds one row.
- **Pseudo-polynomial**: polynomial in the numeric values, not in the input length.

> This is a subtle point. An algorithm is polynomial when its time is bounded by a polynomial in the number of bits needed to write the input. Writing a capacity of one billion takes about thirty bits, yet the table has a billion columns. The 0/1 knapsack problem is NP-hard, and no polynomial-time algorithm for it is known; the P versus NP visualization gives the background. Coin change has the same pseudo-polynomial table.

---
# Quick check

```quiz
Why is the $\Theta(nW)$ knapsack algorithm not polynomial in the input size?
- [ ] The table has two dimensions.
- [ ] Reconstruction takes extra time.
- [x] $W$ is written in about $\log_2 W$ bits, so $W$ can be exponential in the input length.
- [ ] The number of items $n$ can be exponential.
```

> The third option is right. Input size is measured in bits. The number n of items already contributes at least n to the input size, so a factor of n is fine. The capacity contributes only about log W bits, while the table grows with W itself. Two dimensions are not the problem: LCS has a two-dimensional table and is polynomial, because both lengths are counts, not magnitudes.

---
@type section
# Sequences: LCS and edit distance

---
# Longest common subsequence

- A **subsequence** keeps some characters of a string, in order, not necessarily adjacent.
- `BCB` is a subsequence of `BACDB` (positions 0, 2, 4) and of `BDCB`.
- **LCS**: a longest string that is a subsequence of both. Here its length is `3`.
- The LCS need not be unique: `BDB` also has length 3.
- Uses: comparing files line by line, aligning biological sequences.

> A subsequence may skip characters but never reorders them. That distinguishes it from a substring, which must be contiguous. Check that B D B is also common to both strings: in B A C D B take the first B, the D and the last B. So we will ask for one longest common subsequence, not the longest. File-comparison tools use related ideas when they report added and removed lines.

---
# LCS recurrence

- `len[i][j]`: LCS length of the prefixes `x[0..i-1]` and `y[0..j-1]`.
- Base: an empty prefix shares nothing, so row 0 and column 0 are 0.

- If $x_{i-1}=y_{j-1}$: $\mathit{len}[i][j]=\mathit{len}[i-1][j-1]+1$.
- Otherwise: $\mathit{len}[i][j]=\max(\mathit{len}[i-1][j],\ \mathit{len}[i][j-1])$.
- Order: rows top to bottom, each row left to right.

> Look at the last characters of the two prefixes. If they match, some longest common subsequence ends with that character, so we add one to the answer for both shorter prefixes. If they differ, at least one of them is not the last character of the common subsequence, so drop one or the other and take the better result. Every entry reads entries above, left, or diagonally up-left.

---
# LCS table in Java

```java
public static int[][] table(String x, String y) {
    int m = x.length(), n = y.length();
    int[][] len = new int[m + 1][n + 1];        // row 0 and column 0: empty prefix
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (x.charAt(i - 1) == y.charAt(j - 1)) {
                len[i][j] = len[i - 1][j - 1] + 1;  // last characters match
            } else {
                len[i][j] = Math.max(len[i - 1][j], len[i][j - 1]);
            }
        }
    }
    return len;
}
```

> The structure is the same as knapsack: an extra row and column for the base case, two nested loops, and indices shifted by one so that row i describes a prefix of length i. The if statement is the two cases of the recurrence. The check program compares its length with a brute-force search over all subsequences on random strings.

---
# The LCS table for `BACDB` and `BDCB`

```html
<table style="border-collapse:collapse;margin:0 auto;font-variant-numeric:tabular-nums;text-align:center">
<tr style="border-bottom:2px solid var(--rule)"><th style="padding:4px 10px"></th><th style="padding:4px 10px">""</th><th style="padding:4px 10px">B</th><th style="padding:4px 10px">D</th><th style="padding:4px 10px">C</th><th style="padding:4px 10px">B</th></tr>
<tr><th style="padding:4px 10px">""</th><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td><td style="padding:4px 10px">0</td></tr>
<tr data-step="1"><th style="padding:4px 10px">B</th><td style="padding:4px 10px">0</td><td style="padding:4px 10px"><b>1</b></td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td></tr>
<tr data-step="2"><th style="padding:4px 10px">A</th><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td></tr>
<tr data-step="3"><th style="padding:4px 10px">C</th><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px"><b>2</b></td><td style="padding:4px 10px">2</td></tr>
<tr data-step="4"><th style="padding:4px 10px">D</th><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">2</td><td style="padding:4px 10px">2</td><td style="padding:4px 10px">2</td></tr>
<tr data-step="5"><th style="padding:4px 10px">B</th><td style="padding:4px 10px">0</td><td style="padding:4px 10px">1</td><td style="padding:4px 10px">2</td><td style="padding:4px 10px">2</td><td style="padding:4px 10px"><b>3</b></td></tr>
</table>
```

- Bold cells are the matches on the traceback path: `B`, then `C`, then `B`.

> Fill one row per step. Row B matches the first column B, and that one carries right. Row A matches nothing, so it copies the row above. Row C matches column C, one plus the diagonal one, giving two. Row D matches column D, giving two in that column. The last row B matches the last column, one plus the diagonal two, so three. These numbers were printed by the Java code.

---
# Traceback: recover one LCS

```java
while (i > 0 && j > 0) {
    if (x.charAt(i - 1) == y.charAt(j - 1)) {
        out.append(x.charAt(i - 1));        // part of the answer: go diagonal
        i--;
        j--;
    } else if (len[i - 1][j] >= len[i][j - 1]) {
        i--;                                // up (ties go up)
    } else {
        j--;                                // left
    }
}
```

- Path from `(5,4)`: match `B`, up, match `C`, up, left, match `B`. Reversed: `BCB`.

> Start at the bottom right. A match means that character belongs to the answer, so record it and move diagonally. Otherwise move toward the larger neighbour; on a tie this code goes up. The characters come out last first, so the method reverses them. Breaking ties to the left instead would produce B D B, the other longest common subsequence. The walk takes at most m plus n steps.

---
# Edit distance, briefly

- Fewest single-character **insertions, deletions, substitutions** turning `x` into `y`.
- `d[i][j]` for prefixes; base `d[i][0] = i`, `d[0][j] = j`.
- $d[i][j]=\min(d[i-1][j-1]+[x_{i-1}\ne y_{j-1}],\ d[i-1][j]+1,\ d[i][j-1]+1)$
- Same shape as LCS: $\Theta(mn)$ time and space.

> The three terms are the three possible last operations: match or substitute the last characters, delete the last character of x, or insert the last character of y. The bracket is one when the characters differ and zero otherwise. The base cases say that turning a prefix into the empty string takes one deletion per character, and building a prefix from nothing takes one insertion per character.

---
# Edit distance: `CART` to `CHAT`

| | "" | C | H | A | T |
|---|---|---|---|---|---|
| **""** | 0 | 1 | 2 | 3 | 4 |
| **C** | 1 | 0 | 1 | 2 | 3 |
| **A** | 2 | 1 | 1 | 1 | 2 |
| **R** | 3 | 2 | 2 | 2 | 2 |
| **T** | 4 | 3 | 3 | 3 | 2 |

- Distance `2`: insert `H` after `C`, then delete `R`.

> Read the bottom right cell: two. One optimal script inserts H after C, giving C H A R T, then deletes R. Two substitutions, A to H and R to A, also cost two, so the optimal edit script is not unique.

---
@type section
# Choosing a paradigm

---
# DP, divide and conquer, greedy

| | Divide and conquer | Dynamic programming | Greedy |
|---|---|---|---|
| Subproblems | Independent | Overlapping | One remains after each choice |
| Choices tried | Split is fixed | All, best kept | One, never undone |
| Correct when | The combine step is right | Optimal substructure | A greedy-choice proof exists |
| Example | Merge sort | Knapsack, LCS | Activity selection, Huffman |

L19 counterexample: coins `{1, 3, 4}`, amount `6`. Greedy returns 3 coins; DP returns 2.

> Merge sort's two halves never share a subproblem, so remembering answers would not help. DP fits when the same smaller instances recur. Greedy is the cheapest of the three, but it tries only one option, so it needs a proof that the local choice is safe. When no such proof exists, DP is the safe fallback that tries every option while sharing work.

---
@type section
# Space and pitfalls

---
# Keep one or two rows

- Row `i` of knapsack reads only row `i-1`: keep two rows, or even one.
- LCS and edit distance: two rows give the **value** in $\Theta(\min(m,n))$ space.
- Coin change is already one array of size $A+1$.
- The price: the full table is gone, so the simple traceback is gone.

> Look at which entries each recurrence reads. If a row depends only on the previous row, older rows can be discarded. For sequence problems, choose the shorter string for the columns to make the row as short as possible. If you need the actual solution, not just its value, either keep the full table or use a more advanced technique beyond this course.

---
# One row: the loop direction matters

```java
// downward: best[c - w[i]] is still old
for (int c = cap; c >= w[i]; c--) {
    best[c] = Math.max(best[c], best[c - w[i]] + v[i]);
}
// ...
// upward: best[c - w[i]] may include item i
for (int c = w[i]; c <= cap; c++) {
    best[c] = Math.max(best[c], best[c - w[i]] + v[i]);
}
```

- Item B alone, downward (0/1): `[0,0,0,5,5,5,5]`. Upward: `[0,0,0,5,5,5,10]`.
- On all three items: 0/1 gives `7`; unbounded gives `10` (B twice).

> Both loops are the same line; only the direction differs. Going downward, when we update capacity six we read capacity three before it has been updated for this item, so B can be used at most once. Going upward, capacity three already includes B when capacity six reads it, so B is used twice, value ten. That is correct for unbounded knapsack and wrong for 0/1. Every array here was printed by the code.

---
# Common mistakes

- Wrong base case: `best[0]` must be `0`; `ways[0]` must be `1`.
- `INF + 1` overflows in Java; test for `INF` before adding.
- Table sized `n` instead of `n + 1`: the empty prefix needs its own row.
- Wrong loop direction for 0/1 versus unbounded; wrong loop nesting when counting.
- Memo sentinel that is also a legal answer, e.g. `-1` when answers can be `-1`.
- Deep memoized recursion can throw `StackOverflowError`; bottom-up avoids it.

> Most DP bugs are in the edges, not the recurrence. Test each method on the empty input, a single item, capacity or amount zero, and an impossible amount, as the check program does. When a result looks off by one, print the table for a tiny input and compare it with a hand-filled one. The last two mistakes are specific to top-down code.

---
# Quick check

```quiz
In the one-row 0/1 knapsack, why does the capacity loop run downward?
- [ ] It is faster than running upward.
- [x] So `best[c - w[i]]` still holds the value without item `i`.
- [ ] So that larger items are considered first.
- [ ] Java arrays must be filled from the end.
```

> The second option is right. The one-row array stands in for two rows of the table. Going downward, the entry we read at a smaller capacity has not yet been overwritten in this pass, so it is still the old row, the row without item i. Going upward reads an entry that may already include item i, which allows reuse. Both directions cost the same time.

---
# Memo tables in Java

- Dense integer subproblems: a plain array, filled with a sentinel by `Arrays.fill`.
- Sparse or composite keys: a `HashMap` from a key object to the answer.
- `HashMap.computeIfAbsent` must not modify the map from inside its function.
- A recursive memo inside `computeIfAbsent` does exactly that; use `get` then `put`.

> Arrays are the fastest memo when subproblems are numbered zero to n. When the subproblems are scattered, a hash map from lecture twelve is the natural memo. One trap: the Java API documentation says the mapping function of computeIfAbsent should not modify the map, and HashMap may throw ConcurrentModificationException if it detects that. A recursive memoized function written inside computeIfAbsent breaks this rule, so look up with get and store with put instead.

---
@type section
# Wrap-up

---
# Try the visualizations

- [Fibonacci recursion](../../../visualizations/algorithms/fibonacci.html)
- [Fibonacci with DP](../../../visualizations/algorithms/dp_fibonacci.html)
- [Coin change](../../../visualizations/algorithms/coin_change.html)
- [0/1 knapsack](../../../visualizations/algorithms/knapsack.html)
- [Longest common subsequence](../../../visualizations/algorithms/lcs.html)
- [Edit distance](../../../visualizations/algorithms/edit_distance.html)

> Each visualization fills a table step by step, much like the slides. The pages use their own fixed inputs; predict each table before you step through it. The coin and knapsack pages use different coins, items and targets from ours, so they are fresh practice rather than a replay of today's tables. They may also break ties differently from our Java code.

---
# Summary

- DP = recursion + reuse, justified by overlapping subproblems.
- Optimal substructure makes the recurrence correct for optimisation.
- Recipe: subproblem, recurrence and base cases, order, answer, reconstruction.
- Coins $\Theta(kA)$, knapsack $\Theta(nW)$: pseudo-polynomial. LCS, edit distance $\Theta(mn)$.
- Loop order and direction change the problem being solved.
- Next: backtracking, for searches with no small table of subproblems.

> If you remember one sentence, remember the recipe's first step: say in words what a table entry means. Everything else follows from that sentence. The costs are all table size times the work per entry. Next time we meet problems where no small table of subproblems exists, and we have to search.

---
# Check yourself

- Coins `{1, 5, 6, 9}` and amount `11`: fill `best[0..11]`. Does greedy agree?
- Add item D (weight 2, value 3) to today's knapsack. Which row changes, and what is the new answer?
- Why does storing an LCS length in two rows lose the traceback?

> Work these on paper before checking them with the Java code. For the first, find a small amount where greedy and DP disagree. For the second, only one new row appears, but think about whether earlier rows change. For the third, look at which cells the traceback visits.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 14: Dynamic programming.
- CLRS, 4th ed., Chapter 15: Greedy algorithms (0/1 versus fractional knapsack).
- Java SE API documentation: `java.util.HashMap`, `java.util.Map.computeIfAbsent`, `java.util.Arrays`.
- All examples, tables and code are original to this lecture and were produced by its tested Java files.

> The textbook chapter on dynamic programming develops the same recipe with longer examples, including the longest common subsequence. The greedy chapter contrasts the fractional and 0/1 knapsack problems. The Java documentation is the reference for the library behaviour mentioned on the memo slide.
