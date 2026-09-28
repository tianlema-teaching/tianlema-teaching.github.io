@title Lecture 19: Greedy algorithms
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Greedy algorithms
## Choose the best-looking step, and prove it is safe

---
# Where we are

- L17: Dijkstra settles the closest unsettled vertex, and never revisits it.
- L18: Kruskal takes the lightest safe edge; Prim the lightest crossing edge.
- All three commit to a locally best choice and never undo it.
- Today: that pattern in general, where it works and where it fails.
- L20 next: dynamic programming, for problems where greedy fails.

> In the last two lectures you met three algorithms with the same personality. Dijkstra picks the unsettled vertex with the smallest distance and declares it done. Kruskal and Prim pick the cheapest edge they are allowed to pick and keep it forever. Each time we needed a proof that this was safe. Today we name the pattern, try it on four new problems, and learn how to tell whether it will work, because for many problems it does not.

---
# By the end of today you can

- Say what makes an algorithm greedy, and name its two ingredients.
- Trace earliest-finish-time scheduling and prove it optimal.
- Break a greedy rule with one small counterexample.
- Solve fractional knapsack, and show the same rule fails for 0/1.
- Build a Huffman code and count its total bits.
- Decide when greedy coin change is safe.

> Each outcome is something you can do on paper. The two most important are opposites: proving that a greedy rule always gives an optimal answer, and showing with a single example that a rule does not. We will practise both on four problems: booking a room, filling a knapsack, compressing text with Huffman codes, and making change with coins. For each one you will trace the greedy rule on a small example, and then either prove the rule correct or break it with a counterexample. By the end, you should be able to look at a new greedy idea and say which of the two you would try first. Both are skills you will use whenever you design an algorithm, not only in this course.

---
# A problem: one room, many requests

- Eight groups ask for the same room, each for a fixed time interval.
- Two bookings cannot overlap; one may start exactly when another ends.
- Goal: accept as **many** requests as possible.
- Trying every subset of $n$ requests costs about $2^n$ checks.
- Is there a simple rule that is always optimal?

> Think of one seminar room and a list of requested time slots. We want to say yes to as many groups as possible, not to fill the most hours. Brute force would try all subsets and keep the largest one without overlaps, which is exponential. Our checks do exactly that on small inputs, to test the fast rule we will develop. The question for today is whether one sensible sorting rule can replace that search.

---
# What greedy means

- Build the answer one choice at a time.
- At each step, make the choice that looks best **right now**.
- Never undo a choice.
- Fast and simple, often one sort plus one scan.
- The hard part is the proof: locally best must lead to globally best.

> A greedy algorithm does not look ahead and does not backtrack. It sorts or queues the options by some measure, takes the best one that is still allowed, and moves on. That makes greedy algorithms short and fast. The price is that they are often wrong: a choice that looks best now can block a better overall answer. So a greedy algorithm is only as good as the proof that its particular rule is safe for its particular problem.

---
# Two ingredients

- **Greedy-choice property**: some optimal solution begins with the greedy choice.
- **Optimal substructure**: after that choice, an optimal solution of the smaller problem that remains completes an optimal solution of the whole.
- Together: choose greedily, recurse on what is left, repeat.
- Dynamic programming (L20) also needs optimal substructure, but not the greedy-choice property.

> These two properties are what a proof has to establish. The greedy-choice property says we lose nothing by committing to the greedy choice first; some best answer agrees with us. Optimal substructure says the rest of the problem is a smaller copy of the same problem, so we can apply the same reasoning again. When only the second property holds, we cannot commit early, and we have to compare several choices; that is what dynamic programming does next lecture.

---
@type section
# Interval scheduling

---
# Intervals and compatibility

- An interval $[s, f)$ starts at $s$ and finishes at $f$, with $s<f$.
- Half-open: time $f$ itself is free again.
- Two intervals are **compatible** if one finishes no later than the other starts.
- Task: choose a largest set of pairwise compatible intervals.
- Also called **activity selection**.

> We use half-open intervals, which include the start time but not the finish time. That way, a booking from one to four and another from four to six are compatible: one ends exactly as the other begins. The Java Interval record encodes this as finish less than or equal to the other's start, in either order. We want the maximum number of intervals, which the textbooks call activity selection.

---
# The running intervals

```html
<svg viewBox="0 0 560 250" width="100%" style="max-width:560px" role="img" aria-label="Eight intervals on a time line from 0 to 11">
<text x="22" y="22" fill="var(--ink)" font-size="14" text-anchor="middle">a</text>
<rect x="84" y="8" width="132" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="48" fill="var(--ink)" font-size="14" text-anchor="middle">b</text>
<rect x="172" y="34" width="88" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="74" fill="var(--ink)" font-size="14" text-anchor="middle">c</text>
<rect x="40" y="60" width="264" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="100" fill="var(--ink)" font-size="14" text-anchor="middle">d</text>
<rect x="260" y="86" width="88" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="126" fill="var(--ink)" font-size="14" text-anchor="middle">e</text>
<rect x="172" y="112" width="220" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="152" fill="var(--ink)" font-size="14" text-anchor="middle">f</text>
<rect x="260" y="138" width="176" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="178" fill="var(--ink)" font-size="14" text-anchor="middle">g</text>
<rect x="304" y="164" width="176" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="204" fill="var(--ink)" font-size="14" text-anchor="middle">h</text>
<rect x="392" y="190" width="132" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<line x1="40" y1="220" x2="524" y2="220" stroke="var(--ink)"/>
<line x1="40" y1="220" x2="40" y2="225" stroke="var(--ink)"/><text x="40" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">0</text>
<line x1="84" y1="220" x2="84" y2="225" stroke="var(--ink)"/><text x="84" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">1</text>
<line x1="128" y1="220" x2="128" y2="225" stroke="var(--ink)"/><text x="128" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">2</text>
<line x1="172" y1="220" x2="172" y2="225" stroke="var(--ink)"/><text x="172" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">3</text>
<line x1="216" y1="220" x2="216" y2="225" stroke="var(--ink)"/><text x="216" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">4</text>
<line x1="260" y1="220" x2="260" y2="225" stroke="var(--ink)"/><text x="260" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">5</text>
<line x1="304" y1="220" x2="304" y2="225" stroke="var(--ink)"/><text x="304" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">6</text>
<line x1="348" y1="220" x2="348" y2="225" stroke="var(--ink)"/><text x="348" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">7</text>
<line x1="392" y1="220" x2="392" y2="225" stroke="var(--ink)"/><text x="392" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">8</text>
<line x1="436" y1="220" x2="436" y2="225" stroke="var(--ink)"/><text x="436" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">9</text>
<line x1="480" y1="220" x2="480" y2="225" stroke="var(--ink)"/><text x="480" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">10</text>
<line x1="524" y1="220" x2="524" y2="225" stroke="var(--ink)"/><text x="524" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">11</text>
</svg>
```

a [1,4) · b [3,5) · c [0,6) · d [5,7) · e [3,8) · f [5,9) · g [6,10) · h [8,11)

> These eight intervals are the running example for the first half of the lecture. They are listed, top to bottom, in order of finish time, which will matter in a moment. Copy the list: a from one to four, b three to five, c zero to six, d five to seven, e three to eight, f five to nine, g six to ten, and h eight to eleven. Before the next slide, try to find the largest compatible set by eye.

---
# Three plausible rules

- **Earliest start first**: take the interval that begins soonest.
- **Shortest first**: take the interval that uses the least time.
- **Earliest finish first**: take the interval that frees the room soonest.
- Each rule: sort, then keep every interval compatible with those kept.
- Only one of them is always optimal.

> All three rules sound reasonable, and each is a greedy algorithm: sort by a key, scan, and keep an interval when it fits with everything kept so far. Before we prove anything, guess which one is right. The earliest-finish rule has a simple intuition: whatever you pick first, you want it to leave as much of the day free as possible, and finishing early does exactly that.

---
# Earliest finish first in pseudocode

```algorithm
function EARLIEST-FINISH(I):
  sort I by finish time
  chosen ← empty list
  free ← −∞                      // time the room is free again
  for each interval x in I do
    if x.start ≥ free then       // compatible with everything chosen
      append x to chosen
      free ← x.finish
  return chosen
```

> Sorting by finish time makes the compatibility test cheap. Every chosen interval finishes no later than the one we are looking at, and the last chosen finishes latest, so the new interval fits with all of them exactly when it starts at or after the last chosen finish. That is one comparison per interval. The variable free holds that finish time, starting below every possible start.

---
# Earliest finish trace

| Order | Interval | Room free at | Decision |
|---|---|---|---|
| 1 | a [1,4) | $-\infty$ | take; free at 4 |
| 2 | b [3,5) | 4 | skip: starts at 3 |
| 3 | c [0,6) | 4 | skip |
| 4 | d [5,7) | 4 | take; free at 7 |
| 5 | e [3,8) | 7 | skip |
| 6 | f [5,9) | 7 | skip |
| 7 | g [6,10) | 7 | skip |
| 8 | h [8,11) | 7 | take; free at 11 |

> This is the log printed by our Java method on the running intervals. Interval a finishes first, so it is taken and the room is busy until four. Intervals b and c start before four and are skipped. Interval d starts at five, so it is taken, and the room is busy until seven. Intervals e, f and g all start before seven. Interval h starts at eight and is taken. The answer has three intervals, and the brute-force check confirms that no compatible set of four exists.

---
# The chosen intervals

```html
<svg viewBox="0 0 560 250" width="100%" style="max-width:560px" role="img" aria-label="The same eight intervals with a, d and h highlighted in turn">
<text x="22" y="22" fill="var(--ink)" font-size="14" text-anchor="middle">a</text>
<rect x="84" y="8" width="132" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<rect data-step="1" x="84" y="8" width="132" height="18" rx="3" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<text x="22" y="48" fill="var(--ink)" font-size="14" text-anchor="middle">b</text>
<rect x="172" y="34" width="88" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="74" fill="var(--ink)" font-size="14" text-anchor="middle">c</text>
<rect x="40" y="60" width="264" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="100" fill="var(--ink)" font-size="14" text-anchor="middle">d</text>
<rect x="260" y="86" width="88" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<rect data-step="2" x="260" y="86" width="88" height="18" rx="3" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<text x="22" y="126" fill="var(--ink)" font-size="14" text-anchor="middle">e</text>
<rect x="172" y="112" width="220" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="152" fill="var(--ink)" font-size="14" text-anchor="middle">f</text>
<rect x="260" y="138" width="176" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="178" fill="var(--ink)" font-size="14" text-anchor="middle">g</text>
<rect x="304" y="164" width="176" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<text x="22" y="204" fill="var(--ink)" font-size="14" text-anchor="middle">h</text>
<rect x="392" y="190" width="132" height="18" rx="3" fill="var(--mist)" stroke="var(--rule)"/>
<rect data-step="3" x="392" y="190" width="132" height="18" rx="3" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<line x1="40" y1="220" x2="524" y2="220" stroke="var(--ink)"/>
<line x1="40" y1="220" x2="40" y2="225" stroke="var(--ink)"/><text x="40" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">0</text>
<line x1="84" y1="220" x2="84" y2="225" stroke="var(--ink)"/><text x="84" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">1</text>
<line x1="128" y1="220" x2="128" y2="225" stroke="var(--ink)"/><text x="128" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">2</text>
<line x1="172" y1="220" x2="172" y2="225" stroke="var(--ink)"/><text x="172" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">3</text>
<line x1="216" y1="220" x2="216" y2="225" stroke="var(--ink)"/><text x="216" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">4</text>
<line x1="260" y1="220" x2="260" y2="225" stroke="var(--ink)"/><text x="260" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">5</text>
<line x1="304" y1="220" x2="304" y2="225" stroke="var(--ink)"/><text x="304" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">6</text>
<line x1="348" y1="220" x2="348" y2="225" stroke="var(--ink)"/><text x="348" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">7</text>
<line x1="392" y1="220" x2="392" y2="225" stroke="var(--ink)"/><text x="392" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">8</text>
<line x1="436" y1="220" x2="436" y2="225" stroke="var(--ink)"/><text x="436" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">9</text>
<line x1="480" y1="220" x2="480" y2="225" stroke="var(--ink)"/><text x="480" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">10</text>
<line x1="524" y1="220" x2="524" y2="225" stroke="var(--ink)"/><text x="524" y="239" fill="var(--ink)" font-size="12" text-anchor="middle">11</text>
</svg>
```

- Taken: a [1,4), then d [5,7), then h [8,11). Three intervals.

> Watch the three highlighted bars appear in the order the algorithm takes them. Each one begins after the previous one ends, and each is the earliest-finishing interval among those that still fit. Notice the gaps: the room is empty from four to five and from seven to eight. The goal is the number of bookings, not the hours used, so the gaps do not matter.

---
# Earliest finish in Java

```java
List<Interval> sorted = new ArrayList<>(intervals);
sorted.sort(Comparator.comparingInt(Interval::finish));
List<Interval> chosen = new ArrayList<>();
int free = Integer.MIN_VALUE;        // time when the room is free again
for (Interval x : sorted) {
    if (x.start() >= free) {
        chosen.add(x);
        free = x.finish();
        log.add("take " + x);
    } else {
        log.add("skip " + x);
    }
}
return chosen;
```

> The method sorts a copy, so the caller's list is never changed. Comparator comparing int with a method reference sorts by finish time. The loop is the pseudocode line for line, plus two log calls that produced the trace table. Integer MIN VALUE plays the role of minus infinity, since every start is at least that.

---
@type section
# Proving earliest finish

---
# Proof 1: an exchange argument

- Let $g$ be the interval that finishes first overall.
- Take any optimal solution $O$; let $o$ be its earliest-finishing interval.
- $f(g)\le f(o)$, and every other interval of $O$ starts at or after $f(o)$.
- So $O - o + g$ is still compatible and just as large: it is optimal and contains $g$.
- What remains after $g$ is the same problem on intervals starting at or after $f(g)$. Repeat.

> This is the greedy-choice property, proved by swapping. Start from any best answer. Its first interval finishes no earlier than g does, because g finishes first overall. Replace that first interval with g. Nothing else in the answer overlaps g, because everything else started after the old first interval finished, which is no earlier than g finishes. So we get another best answer that begins with g. After committing to g, we face the same problem on the intervals that start at or after g's finish, which is the optimal substructure. Induction on the number of intervals finishes the proof.

---
# Proof 2: greedy stays ahead

- Greedy picks $g_1,\dots,g_k$; an optimal solution has $o_1,\dots,o_m$, both sorted by finish.
- Claim: $f(g_i)\le f(o_i)$ for every $i\le k$.
- Step: $o_{i+1}$ starts at or after $f(o_i)\ge f(g_i)$, so it was still available to greedy.
- Greedy took the earliest finish among available intervals, so $f(g_{i+1})\le f(o_{i+1})$.
- If $m>k$, then $o_{k+1}$ would still fit after $g_k$, and greedy would not have stopped. So $k=m$.

> Here is a second proof style. Instead of changing the optimal answer, we compare the two answers position by position and show greedy is never behind. After i choices, greedy's last finish is no later than the optimal answer's i-th finish. The induction step uses the greedy rule directly: the optimal solution's next interval was still available to greedy, and greedy picked something finishing no later. At the end, if the optimal answer had an extra interval, that interval would still have fit after greedy's last one, which contradicts greedy stopping.

---
# Two rules that fail

- **Earliest start** on the running intervals: takes c [0,6), then g [6,10). Only 2.
- **Shortest first** on p [0,5), q [4,6), r [5,10): takes q only. Optimal: p and r.
- **Shortest first** happens to find 3 on the running intervals.
- One success proves nothing; one failure disproves the rule.

```html
<svg viewBox="0 0 560 124" width="100%" style="max-width:560px" role="img" aria-label="Three intervals: p from 0 to 5, q from 4 to 6, r from 5 to 10">
<text x="22" y="22" fill="var(--ink)" font-size="14" text-anchor="middle">p</text>
<rect x="40" y="8" width="220" height="18" rx="3" fill="var(--blue)" stroke="var(--blue-ink)"/>
<text x="266" y="22" fill="var(--ink)" font-size="12">[0,5)</text>
<text x="22" y="48" fill="var(--ink)" font-size="14" text-anchor="middle">q</text>
<rect x="216" y="34" width="88" height="18" rx="3" fill="var(--rose)" stroke="var(--rose-ink)"/>
<text x="310" y="48" fill="var(--ink)" font-size="12">[4,6)</text>
<text x="22" y="74" fill="var(--ink)" font-size="14" text-anchor="middle">r</text>
<rect x="260" y="60" width="220" height="18" rx="3" fill="var(--blue)" stroke="var(--blue-ink)"/>
<text x="486" y="74" fill="var(--ink)" font-size="12">[5,10)</text>
<line x1="40" y1="92" x2="524" y2="92" stroke="var(--ink)"/>
<line x1="40" y1="92" x2="40" y2="97" stroke="var(--ink)"/><text x="40" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">0</text>
<line x1="84" y1="92" x2="84" y2="97" stroke="var(--ink)"/><text x="84" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">1</text>
<line x1="128" y1="92" x2="128" y2="97" stroke="var(--ink)"/><text x="128" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">2</text>
<line x1="172" y1="92" x2="172" y2="97" stroke="var(--ink)"/><text x="172" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">3</text>
<line x1="216" y1="92" x2="216" y2="97" stroke="var(--ink)"/><text x="216" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">4</text>
<line x1="260" y1="92" x2="260" y2="97" stroke="var(--ink)"/><text x="260" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">5</text>
<line x1="304" y1="92" x2="304" y2="97" stroke="var(--ink)"/><text x="304" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">6</text>
<line x1="348" y1="92" x2="348" y2="97" stroke="var(--ink)"/><text x="348" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">7</text>
<line x1="392" y1="92" x2="392" y2="97" stroke="var(--ink)"/><text x="392" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">8</text>
<line x1="436" y1="92" x2="436" y2="97" stroke="var(--ink)"/><text x="436" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">9</text>
<line x1="480" y1="92" x2="480" y2="97" stroke="var(--ink)"/><text x="480" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">10</text>
<line x1="524" y1="92" x2="524" y2="97" stroke="var(--ink)"/><text x="524" y="111" fill="var(--ink)" font-size="12" text-anchor="middle">11</text>
</svg>
```

> Our Java checks verify each of these. Earliest start grabs c because it begins at zero, but c occupies the room until six and blocks a, b and d. Shortest first grabs q, the rose bar, because it lasts only two units, but q overlaps both p and r, the blue bars, which fit together because p ends at five exactly when r starts. Notice that shortest first gives the right answer on our running intervals. That lucky run tells us nothing about correctness, while the three-interval example is enough to show the rule is wrong.

---
# Cost of interval scheduling

- Sorting by finish: $O(n\log n)$ comparisons.
- The scan: one comparison per interval, $\Theta(n)$.
- Total $O(n\log n)$; $\Theta(n)$ if the input is already sorted by finish.
- Extra space: $\Theta(n)$ for the sorted copy and the result.
- Compare brute force: $2^n$ subsets, each checked for overlaps.

> The greedy algorithm spends almost all its time sorting. With input already sorted by finish time, it is a single linear pass. The brute-force checker in our tests tries all two to the n subsets, which is fine for ten intervals and hopeless for fifty. This gap, from exponential search to a sort, is the payoff when a greedy rule is correct.

---
# Quiz: pick the safe rule

```quiz
Which rule always gives a largest set of compatible intervals?
- [ ] Earliest start first
- [ ] Shortest interval first
- [x] Earliest finish first
- [ ] Fewest overlaps with other intervals first
```

> Earliest finish first is the rule with a proof, and we saw two proofs of it. Earliest start and shortest first both fail on small examples we verified with code. The fewest-overlaps rule is also wrong in general, although its counterexamples are larger; we did not include one here, so if you want to be sure, try to build one and check it by brute force. The lesson is that sounding sensible is not a proof.

---
@type section
# Knapsack: split or whole

---
# Fractional knapsack

- A bag holds total weight at most $W$.
- Item $i$ has value $v_i$ and weight $w_i>0$.
- **Fractional**: you may take any fraction $x_i\in[0,1]$ of each item.
- Maximize total value $\sum x_i v_i$ with $\sum x_i w_i\le W$.
- Greedy rule: take items in decreasing **value per weight**, $v_i/w_i$.

> Think of buying spices by weight from open bins: you can take all of one bin and part of another. The obvious measure is value per unit of weight, the ratio. Fill the bag with the best ratio first, then the next, and cut the last item to fit. The fraction variable x i is between zero and one for each item.

---
# The running items

| Item | Value | Weight | Value per weight |
|---|---|---|---|
| X | 30 | 6 | 5 |
| Y | 20 | 5 | 4 |
| Z | 15 | 5 | 3 |

- Capacity $W=10$. Ratio order: X, Y, Z.
- Greedy: all of X (weight 6, value 30), then 4 of Y's 5 units (value 16). Total $46$.

> These three items and a capacity of ten are our knapsack example for both versions of the problem. X has the best ratio, five per unit, so the greedy rule takes all six units of it. That leaves room for four units, and Y is next with ratio four, so we take four fifths of Y, worth sixteen. The bag is full at total value forty-six, which is the number our Java method returns.

---
# Fractional knapsack in Java

```java
public static double fractional(List<Item> items, int capacity) {
    if (capacity < 0)
        throw new IllegalArgumentException("negative capacity " + capacity);
    double total = 0;
    int room = capacity;
    for (Item it : byRatio(items)) {
        if (room == 0) break;
        int take = Math.min(it.weight(), room);
        total += (double) it.value() * take / it.weight();
        room -= take;
    }
    return total;
}
```

- `byRatio` sorts a copy by decreasing $v/w$: $O(n\log n)$.

> The loop takes as much of each item as fits, which is the whole item until the last one. The value added is computed as value times amount taken, divided by weight, in that order; multiplying before dividing avoids an extra rounding in general, although here zero point eight times twenty would also give exactly sixteen. A negative capacity is rejected at entry. Sorting dominates the cost. For testing, our checks cut every item into unit-weight pieces and take the most valuable pieces directly, an independent way to reach the same optimum.

---
# Why the ratio rule is optimal here

- Suppose an optimal load uses some weight on item $j$ while a better-ratio item $i$ is not fully taken.
- Move $\delta$ units of weight from $j$ to $i$: weight is unchanged.
- Value changes by $\delta\,(v_i/w_i - v_j/w_j)\ge 0$.
- Repeat until the load follows ratio order: no value lost.
- So the greedy load is optimal. Splitting is what makes the swap possible.

> This is again an exchange argument. If a supposedly optimal load contains a lower-ratio item while a higher-ratio item still has some left, we trade a small amount of weight from the worse item to the better one. The bag weighs the same and the value does not decrease. Repeating these trades turns any optimal load into the greedy load without losing value. The key step is that we can trade a tiny amount delta, which requires that items be divisible.

---
# 0/1 knapsack: the same rule fails

| Choice (whole items) | Weight | Value |
|---|---|---|
| X alone (greedy by ratio) | 6 | 30 |
| Y and Z | 10 | 35 |
| X with Y or with Z | 11 | too heavy |

- Greedy takes X; then Y and Z, weight 5 each, do not fit in the 4 units left.
- Brute force over all 8 subsets: the best is Y and Z, value $35$.

> Now each item must be taken whole or not at all. The ratio rule still takes X first, and then the remaining four units cannot hold Y or Z, so greedy stops at thirty. But Y and Z together weigh exactly ten and are worth thirty-five. Our checks try all eight subsets and confirm that thirty-five is the best. The exchange argument from the previous slide breaks because we can no longer move a small amount of weight; the swap must move a whole item.

---
# What failed, and what fixes it

- Greedy-choice property fails: no optimal 0/1 load contains X.
- Optimal substructure still holds: after deciding one item, the rest is a smaller knapsack.
- So we must compare "take it" against "leave it", not commit early.
- L20 solves 0/1 knapsack exactly with dynamic programming.
- The fractional value $46$ is an upper bound on any 0/1 value.

> The failure is specific. The rest of the problem is still a smaller knapsack, so optimal substructure is fine. What fails is committing to the best-ratio item first, since the only optimal answer leaves X out. When only optimal substructure holds, we need to consider both branches of each decision and reuse overlapping work, which is dynamic programming. The fractional value is always at least the 0/1 optimum, because every whole-item load is also a legal fractional load; our random tests check that too.

---
@type section
# Huffman coding

---
# Codes for symbols

- A **code** maps each symbol to a string of bits.
- **Fixed-length**: every symbol uses $\lceil\log_2 n\rceil$ bits. Six symbols need 3 bits.
- **Variable-length**: frequent symbols get short codes.
- **Prefix-free**: no code is a prefix of another, so a bit stream decodes without separators.
- Goal: a prefix-free code with the fewest total bits for given frequencies.

> Compression starts from a simple observation: if some symbols appear far more often than others, give them shorter codes. But variable lengths create a decoding problem, since we must know where each code ends. A prefix-free code solves it: reading bits from left to right, the moment the bits read so far match a code, that code is the symbol, because no longer code starts with it. Prefix-free codes correspond to binary trees, with symbols at the leaves, left edges as zero and right edges as one.

---
# The running frequencies

| Symbol | A | B | C | D | E | F | Total |
|---|---|---|---|---|---|---|---|
| Count | 14 | 10 | 7 | 5 | 3 | 1 | 40 |

- Fixed-length code: $40\times 3=120$ bits.
- Can a prefix-free code do better? Huffman's algorithm finds an optimal one.

> Imagine a message of forty symbols drawn from six letters, with A appearing fourteen times and F only once. A fixed-length code needs three bits per symbol, because two bits give only four patterns. That costs one hundred twenty bits for the whole message. We will build a Huffman code for these counts and compare.

---
# Huffman's algorithm

```algorithm
function HUFFMAN(freq):
  Q ← min-priority queue of one-leaf trees, keyed by frequency
  while Q holds more than one tree do
    a ← EXTRACT-MIN(Q); b ← EXTRACT-MIN(Q)
    z ← new node with left a, right b, freq(a) + freq(b)
    INSERT(Q, z)
  return the only tree in Q      // left edge 0, right edge 1
```

> Start with one tiny tree per symbol. Repeatedly take the two trees with the smallest total frequencies and join them under a new parent whose frequency is their sum. Each merge pushes every symbol in the two trees one level deeper, adding one bit to each of their codes, so we always charge that extra bit to the rarest symbols available. With n symbols there are exactly n minus one merges.

---
# Huffman trace

| Merge | Take the two smallest | New tree |
|---|---|---|
| 1 | F:1 and E:3 | (FE):4 |
| 2 | (FE):4 and D:5 | (FED):9 |
| 3 | C:7 and (FED):9 | (CFED):16 |
| 4 | B:10 and A:14 | (BA):24 |
| 5 | (CFED):16 and (BA):24 | root:40 |

> This is the merge log from our Java code. The two rarest symbols, F and E, merge first into a tree of weight four. That tree is now the smallest and merges with D. In merge three, C at seven and the tree at nine are the two smallest. In merge four, the remaining queue holds ten, fourteen and sixteen, so B and A merge. The last merge joins sixteen and twenty-four at the root. In each merge, the first tree taken becomes the left child.

---
# The finished tree

```diagram
@dir TB
@reveal all
R((40)) -> L((16)) : 0
R -> RR((24)) : 1
RR -> B[B:10] : 0
RR -> A[A:14] : 1
L -> C[C:7] : 0
L -> M((9)) : 1
M -> K((4)) : 0
M -> D[D:5] : 1
K -> F[F:1] : 0
K -> E[E:3] : 1
```

> The root has weight forty. Its left child is the tree of weight sixteen and its right child the tree of weight twenty-four. Reading edge labels from the root, A is one one, B is one zero, and C is zero zero. The tree of weight nine hangs at the path zero then one, so every code inside it starts with zero one: D is zero one one, and F and E, one level lower under the tree of weight four, are zero one zero zero and zero one zero one. The rarest symbols have the longest codes, exactly as intended, and the next slide lists all six codes.

---
# Counting the bits

| Symbol | Count | Code | Bits |
|---|---|---|---|
| A | 14 | `11` | 28 |
| B | 10 | `10` | 20 |
| C | 7 | `00` | 14 |
| D | 5 | `011` | 15 |
| E | 3 | `0101` | 12 |
| F | 1 | `0100` | 4 |

- Total **93** bits versus 120 fixed-length: 27 bits fewer, 22.5% less.
- Shortcut: the merge weights $4+9+16+24+40$ also sum to 93.

> Multiply each count by its code length and add: ninety-three bits, against one hundred twenty for the fixed-length code. The shortcut works because each merge adds one bit to every symbol beneath it, so the new node's weight is exactly the number of bits that merge adds. Our checks confirm the total both ways, and they compare it against a brute-force search over all code lengths a prefix-free code can have, which also finds ninety-three as the minimum.

---
# Huffman in Java

```java
PriorityQueue<Node> pq = new PriorityQueue<>((a, b) ->
        a.freq() != b.freq() ? Long.compare(a.freq(), b.freq())
                             : Integer.compare(a.seq(), b.seq()));
int seq = 0;
for (var e : new TreeMap<>(freq).entrySet()) {
    pq.add(new Node(e.getKey(), e.getValue(), null, null, seq++));
}
while (pq.size() > 1) {
    Node a = pq.poll(), b = pq.poll();          // two smallest
    Node merged = new Node('\0', a.freq() + b.freq(), a, b, seq++);
    log.add("merge " + label(a) + " + " + label(b)
            + " = " + merged.freq());
    pq.add(merged);
}
```

> Node is a small record with a symbol, a frequency, two children and a sequence number. The comparator orders by frequency and breaks ties by sequence number, so every run builds the same tree; different tie-breaking can give different codes with the same total. The loop is the pseudocode: poll two, merge, offer the result. A separate recursive method then walks the tree, appending zero for left and one for right.

---
@type section
# Why Huffman is optimal

---
# Why Huffman is optimal, and its cost

- Exchange: some optimal tree has the two rarest symbols as siblings at the deepest level.
- Merging them into one symbol of their summed frequency leaves a smaller instance.
- Cost of the tree $=$ cost of the smaller tree $+$ the two rarest frequencies.
- So an optimal tree for the smaller instance gives an optimal tree for the original.
- Time: $n-1$ merges, each $O(\log n)$ with a binary heap: $O(n\log n)$.

> The proof has our two ingredients. The greedy-choice step is an exchange: in any optimal tree, swap the two rarest symbols down into the deepest pair of sibling leaves; moving a rarer symbol deeper and a more frequent one up cannot increase the cost. The substructure step says that after merging those two into one symbol, the cost differs by exactly their two frequencies, whatever the rest of the tree looks like. So solving the smaller instance optimally solves the original. The heap holds at most n trees.

---
# Huffman edge cases

- **One symbol**: the tree is a single leaf; our code gives it `0`, one bit per symbol.
- **No symbols**: an empty code.
- **Ties** may give different codes (even different code lengths), always with the same optimal total.
- **Decoding** needs the tree (or the code table) as well as the bits.
- Huffman is optimal among codes that encode **one symbol at a time**.

> A single symbol makes the tree a lone leaf with an empty path, so the code assigns it one bit by convention, and our checks test that case. Ties change which trees merge first, so the codes, and even the lengths of the codes, can differ, but the total is always the optimal one. In practice the code table must travel with the data, which costs space for short messages. Finally, the optimality claim is only about codes that give each symbol its own fixed bit string; methods that encode groups of symbols can do better.

---
# Quiz: Huffman merges

```quiz
Frequencies are P:2, Q:3, R:4, S:9. What does Huffman merge first, and second?
- [ ] P with Q, then R with S
- [x] P with Q, then the tree of weight 5 with R
- [ ] R with S, then P with Q
- [ ] P with S, then Q with R
```

> Always merge the two smallest weights in the queue. P and Q, weights two and three, merge into five. The queue now holds four, five and nine, so R at four merges with the tree of weight five into nine. Finally the two nines merge. The code lengths are three, three, two and one for P, Q, R and S, for a total of six plus nine plus eight plus nine, thirty-two bits, compared with thirty-six for a two-bit fixed code.

---
@type section
# When greedy fails, and how to tell

---
# Greedy change making

- Coins of given values, unlimited supply. Pay an amount with the **fewest** coins.
- Greedy: repeatedly take the largest coin that does not exceed what is left.
- US coins $\{1, 5, 10, 25\}$: 67 cents $= 25+25+10+5+1+1$, six coins.
- For these coins greedy is optimal for every amount; our check confirms 0 to 500.

```java
for (int i = sorted.length - 1; i >= 0; i--) {
    while (sorted[i] <= left) {
        used.add(sorted[i]);
        left -= sorted[i];
    }
}
```

> This is how most people make change without thinking. Take quarters while you can, then dimes, then nickels, then pennies. The Java loop walks the sorted coins from largest to smallest and takes each coin as many times as it fits. For the US coin set this rule always uses the fewest coins; our tests compare it with an exhaustive breadth-first search over amounts for every amount from zero to five hundred cents.

---
# A coin system where greedy fails

- Coins $\{1, 3, 4\}$, amount 6.
- Greedy: $4+1+1$, **3 coins**.
- Optimal: $3+3$, **2 coins**.
- Coins $\{3,4\}$: greedy takes 4, is stuck at 2, though $3+3$ works.

```diagram
@dir LR
@reveal all
G0[6] -> G1[2] : take 4
G1 -> G2[1] : take 1
G2 -> G3[0] : take 1
O0[6] -> O1[3] : take 3
O1 -> O2[0] : take 3
```

> The greedy rule takes the four because it is the biggest coin that fits, and then only ones fit. Two threes would have been better. The top chain in the picture is greedy's path through the remaining amounts; the bottom chain is the optimal path. Our Java checks confirm both counts, and they also show that without a one-coin, greedy can fail to make change at all. Whether greedy works depends on the coin values, not on the name of the problem. General coin change is a classic dynamic-programming problem for L20.

---
# Greedy algorithms you have already seen

| Algorithm | Greedy choice | Why it is safe |
|---|---|---|
| Dijkstra (L17) | settle the closest unsettled vertex | nonnegative weights: no later path is shorter |
| Prim (L18) | lightest edge leaving the tree | cut property |
| Kruskal (L18) | lightest edge joining two trees | cut property |
| Earliest finish | interval that ends first | exchange, or stays ahead |
| Huffman | merge the two rarest trees | exchange plus substructure |

> Each row pairs a greedy rule with the fact that makes it correct. For Dijkstra, the argument is a stays-ahead style argument that needs nonnegative weights; with a negative edge the greedy choice can be wrong, which is why Bellman-Ford exists. For Prim and Kruskal the safe-choice fact is the cut property, itself an exchange argument. Without such a fact, a greedy algorithm is only a heuristic.

---
# Two proof styles

| | Greedy stays ahead | Exchange argument |
|---|---|---|
| Idea | greedy is never behind on a progress measure | edit an optimal $O$ into greedy's $G$ |
| Step | induction: optimal's next choice was open to greedy | swap $O$'s choice at the first difference for $G$'s |
| Show | greedy's measure is at least as good after each step | each swap stays valid and is no worse |
| Examples | earliest finish (Proof 2) | cut property, earliest finish, fractional knapsack, Huffman |

> In a stays-ahead proof you do not modify the optimal solution. You pick a measure of progress, such as the finish time of the i-th chosen interval, and show by induction that greedy is always at least as far along; the step uses the greedy rule directly, because whatever the optimal solution did next was also available to greedy. An exchange proof instead edits an optimal solution until it looks like greedy's, one swap at a time, proving that no swap makes it worse. It is the most widely used technique, and we have used it several times. When you cannot make the swap work, look at why: often the failed swap points straight at a counterexample, as it did for 0/1 knapsack.

---
# Disproving: one counterexample

- A single input where greedy is not optimal disproves the rule.
- Passing many tests does not prove a rule; it only fails to disprove it.
- Search small inputs: 2 or 3 items, tiny numbers, near-ties.
- Compare greedy with brute force on random small inputs, as our checks do.
- Keep the smallest failure you find; it explains the flaw best.

> To show a greedy rule is wrong you need exactly one input where it loses, and small ones are the most convincing. A practical way to find one is what our Java checks do: generate thousands of tiny random inputs and compare the greedy answer with brute force. For 0/1 knapsack this random search found failures. For earliest-finish scheduling it found none in three thousand tries, which is consistent with the proof but is not itself a proof.

---
# Quiz: evidence

```quiz
A greedy rule matches brute force on 10,000 random small inputs. What have you shown?
- [ ] The rule is optimal on all inputs
- [ ] The rule is optimal on inputs up to that size
- [x] No counterexample among those inputs; a proof is still needed
- [ ] The problem has the greedy-choice property
```

> Testing can only show the presence of a counterexample, never its absence. Ten thousand random inputs of a given size need not include every input of that size, so even the second option claims too much. The correct conclusion is modest: we found no failure. That is good evidence and a reason to look for a proof, but the proof is what makes the claim true for every input.

---
# A checklist before trusting greedy

- Can you state the greedy choice in one sentence?
- After making it, is the rest a smaller copy of the same problem?
- Can you swap an optimal solution's first choice for the greedy one?
- Does brute force agree on many small inputs?
- If any answer is no, try to build a counterexample, then consider DP (L20).

> Use this list whenever a greedy idea comes to mind. The first question forces a precise rule. The second checks optimal substructure. The third is the start of an exchange proof and checks the greedy-choice property. The fourth is cheap insurance that catches most wrong rules quickly. If the rule survives all four, write the proof; if it fails any, you have probably found either a counterexample or a sign that the problem needs dynamic programming.

---
# Common mistakes

- Picking a rule because it sounds right, with no proof.
- Trusting one worked example; ours shows shortest first "working".
- Using the fractional ratio rule on 0/1 knapsack.
- Assuming greedy change is optimal for any coin set.
- Forgetting ties and edge cases: empty input, touching intervals, one symbol.

> Almost every greedy bug is a missing proof. The shortest-first rule found the optimum on our running intervals, which is exactly the trap: one good run feels like evidence. The knapsack and coin examples show that a rule correct for one version of a problem can fail on a close relative. Edge cases matter too: our interval code treats touching intervals as compatible, so the choice of half-open intervals changes answers.

---
# In the Java library

- `PriorityQueue` is based on a priority heap (a binary heap in current OpenJDK); its implementation note gives $O(\log n)$ `offer` and `poll`. Used for Huffman.
- `Comparator.comparingInt` builds a sort key from a method reference, e.g. `Interval::finish`.
- A comparator's `reversed()` method gives decreasing order, as for value per weight.
- `List.sort` is guaranteed stable, so equal keys keep their input order.
- A `TreeMap` iterates its keys in sorted order, which makes our Huffman runs repeatable.

> Greedy algorithms in Java are mostly a matter of choosing the right order. Comparator factory methods let you name the sort key directly. Stability matters when a rule has ties: the stable sort keeps input order among equal keys, so results do not change from run to run. The priority queue does the repeated take-the-smallest work for Huffman, just as it did for Prim and Dijkstra.

---
# Watch them run

- [Activity selection](../../../visualizations/algorithms/activity_selection.html): earliest finish, step by step.
- [Fractional knapsack](../../../visualizations/algorithms/fractional_knapsack.html): items by value per weight.
- [Huffman coding](../../../visualizations/algorithms/huffman.html): merging the two rarest trees.
- [Coin change](../../../visualizations/algorithms/coin_change.html): dynamic programming on coins $\{1, 5, 6\}$, amount 11.

> These visualizations use their own examples. For each one, before pressing play, predict the greedy choice at the next step, and ask which argument from today shows that choice is safe. The coin change visualization is a fixed demonstration: it runs dynamic programming on coins {1, 5, 6} for amount 11, where greedy also finds 6 + 5. By hand, try amount 10: greedy gives 6 + 1 + 1 + 1 + 1, five coins, but 5 + 5 uses two.

---
@type section
# Wrap-up

---
# Summary

- Greedy: take the locally best choice and never undo it.
- It works when the greedy-choice property and optimal substructure both hold.
- Correct: earliest finish, fractional knapsack by ratio, Huffman, US-coin change.
- Wrong: earliest start, shortest first, ratio for 0/1 knapsack, $\{1,3,4\}$ change.
- Prove with stays-ahead or exchange; disprove with one small counterexample.

> Today's pattern is short to state and hard to trust. Every correct greedy algorithm we saw came with a proof, either by staying ahead or by exchange, and every wrong one fell to a tiny counterexample that our code confirmed. When the greedy-choice property fails but optimal substructure holds, as in 0/1 knapsack and general coin change, we need to consider several choices at each step. That is dynamic programming, next lecture.

---
# Check yourself

- Prove that "latest start first" is also optimal for interval scheduling.
- Find a coin set containing 1, other than $\{1,3,4\}$, where greedy change fails.
- If each of the six symbols appears once, how many bits does Huffman use, compared with 18 for a 3-bit code?

> For the first question, notice that latest start first is earliest finish first with time running backwards. For the second, look at how the counterexample with one, three and four works and try to build a similar one. For the third, build the tree by hand, list the six code lengths, and compare them with three bits each; think about what happens when the number of symbols is not a power of two.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 15 (greedy algorithms: activity selection, the knapsack contrast, Huffman codes).
- CLRS Chapter 14 (dynamic programming), the topic of L20.
- D. A. Huffman, "A method for the construction of minimum-redundancy codes", *Proceedings of the IRE* 40(9), 1952, 1098–1101.
- Java SE API documentation: `java.util.PriorityQueue`, `java.util.Comparator`, `java.util.List`.

> The structure of this lecture, the two ingredients and the main proofs follow CLRS chapter fifteen, rewritten in our own words with our own examples. Huffman's original paper is listed for history. All intervals, items, frequencies and coin examples were chosen for this lecture, and every number on the slides was produced or checked by the Java code for this lecture.
