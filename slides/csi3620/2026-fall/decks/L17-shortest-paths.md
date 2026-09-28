@title Lecture 17: Shortest paths
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Shortest paths
## Dijkstra, Bellman-Ford and Floyd-Warshall

---
# Where we are

- L15: BFS finds fewest-edge paths. L16: DFS and Kahn order a DAG.
- Today edges carry **weights**, and we want the lightest route.
- By the end of today you can:
  - trace Dijkstra's algorithm with its distance table and priority queue;
  - show on a small graph why Dijkstra fails with a negative edge;
  - trace Bellman-Ford and Floyd-Warshall, and detect a negative cycle;
  - choose among BFS, Dijkstra, Bellman-Ford and Floyd-Warshall.

> BFS from L15 answers "how few edges", which is right when every step costs the same. Real networks rarely work that way: a road segment has a length, a network link has a delay, a currency trade has a cost. Today we generalize to weighted directed graphs and meet three classic algorithms. Each one relies on the same basic move, called relaxation, and each makes different assumptions about the weights.

---
# A routing problem

- A warehouse robot moves between six stations along one-way aisles.
- Each aisle has a travel time in seconds.
- The robot starts at station `S`. For every station: what is the fastest route, and how long is it?
- Fewest aisles is not the answer: a detour through short aisles can be faster.
- We want all six answers from one computation.

> The example is invented, but the pattern is everywhere: map routing, packet forwarding, and scheduling all ask for cheapest routes from one starting point. Notice two features of the question. It asks for every destination at once, which turns out to cost no more than asking for one in the worst case. And it asks for the route itself, not only its length, so the algorithm must remember how it reached each vertex.

---
# The running example

```diagram Travel time on each one-way aisle; source S
@dir LR
@reveal all
S -> A : 4
S -> B : 1
B -> A : 2
A -> C : 1
B -> C : 5
B -> D : 8
C -> D : 3
C -> E : 6
D -> E : 1
```

- Six vertices `S, A, B, C, D, E` (numbered 0 to 5), nine directed edges, all weights non-negative.

> Here is the graph we use for BFS, relaxation and Dijkstra. Edges were added in the order you read them in the source listing, starting with S to A of weight four and S to B of weight one. That order decides the order in which neighbors are scanned, so it affects ties and the exact contents of the priority queue. Look at A already: the direct edge costs four, but going through B costs one plus two, which is three. All the states on later slides come from the Java code run on this graph.

---
# Path weight and distance

- A path $p = v_0 \to v_1 \to \dots \to v_k$ has **weight** $w(p) = \sum_{i=1}^{k} w(v_{i-1}, v_i)$.
- The **shortest-path distance** $\delta(s, v)$ is the minimum weight over all paths from $s$ to $v$.
- If no path exists, $\delta(s, v) = \infty$.
- Example: `S -> B -> A` has weight $1 + 2 = 3$, less than the direct edge of weight 4.
- The **single-source shortest-path problem**: compute $\delta(s, v)$ for every $v$.

> Weight is just the sum of edge weights along the path, and distance is the best weight you can achieve. "Shortest" therefore means lightest, not fewest edges; the textbook convention keeps the word shortest. Unreachable vertices get infinity, and in Java we represent that with Long.MAX_VALUE and take care never to add to it. There are also single-pair and all-pairs versions of the problem. No known single-pair algorithm beats the best single-source algorithms in the worst case, so we solve the single-source problem.

---
# Negative weights and negative cycles

- Weights may be negative: a refund, a gain, a downhill segment.
- A **negative cycle** is a cycle whose edge weights sum to less than 0.
- If one is reachable from $s$ and can reach $v$, going around it again lowers the weight forever.
- Then $\delta(s, v)$ is not a finite number: we say $-\infty$, and algorithms must **report** it.
- With no reachable negative cycle, some shortest path is **simple**: no repeated vertex.

> Negative edges alone are fine: they appear later in the lecture, and Bellman-Ford and Floyd-Warshall handle them. A negative cycle is different, because a path can loop around it any number of times and each loop makes it lighter, so there is no minimum. When there is no such cycle, any path that repeats a vertex contains a cycle of non-negative weight, and cutting that cycle out cannot make the path heavier. That is why we may restrict attention to simple paths, which have at most V minus one edges.

---
# Distances and parent pointers

- Each algorithm keeps `dist[v]`: the weight of the best path to $v$ found **so far**.
- It also keeps `parent[v]`: the vertex just before $v$ on that path.
- Start: `dist[s] = 0`, every other `dist[v]` $= \infty$, all parents none.
- Invariant: `dist[v]` $\ge \delta(s, v)$ always, since it is the weight of a real path or $\infty$.
- At the end the parent pointers form a **shortest-path tree** rooted at $s$.

> Two arrays of length V carry the whole state. dist is an upper estimate: it only ever records weights of paths that really exist, so it can never drop below the true distance. The algorithms differ only in the order in which they improve these estimates. parent is how we recover the route later, exactly like the parent array in last lecture's cycle report. Following parents from any vertex leads back to the source along a shortest path.

---
# Relaxation

```algorithm
function RELAX(u, v, w):          // w is the weight of edge u → v
  if dist[u] + w < dist[v] then   // a shorter way to reach v, through u
    dist[v] ← dist[u] + w
    parent[v] ← u
```

- Relaxing $S \to A$ first sets `dist[A]` to 4. Relaxing $B \to A$ later lowers it to $1 + 2 = 3$.
- A relaxation never makes an estimate worse, and never drops it below $\delta(s, v)$.

> Relaxation asks one question about one edge: is the best known way to reach u, followed by this edge, better than the best known way to reach v? If so, update both arrays. That is the only way any of today's algorithms changes a distance. They differ in which edges they relax and how often. In code we must also skip u when dist of u is infinity, otherwise adding a weight to Long.MAX_VALUE would overflow into a negative number.

---
# BFS already solves the unweighted case

- If every edge has weight 1, distance equals the number of edges.
- BFS visits vertices in nondecreasing edge count, so its first visit is final.
- On our graph BFS gives $A=1, B=1, C=2, D=2, E=3$ edges, and path `S, A, C, E`.
- With weights, `S, A, C, E` weighs $4 + 1 + 6 = 11$. The true distance to $E$ is 8.
- Cost $\Theta(V+E)$: when weights are all equal, use BFS, not Dijkstra.

> The BFS numbers are the Java output of an unweighted BFS on the same graph. BFS is correct for counting edges because its queue processes all vertices at one edge from the source before any at two edges, and so on. Weights break that picture: the fewest-edge route to E is heavy, while a route with five edges weighs only eight. Dijkstra's algorithm keeps the BFS idea of "closest first", but measures closeness by weight, which needs a priority queue instead of a FIFO queue.

---
@type section
# Dijkstra's algorithm

---
# Dijkstra's idea

- Requires every edge weight to be **non-negative**.
- Maintain a set of **finalized** vertices, whose `dist` is known to be exact.
- Repeatedly pick the unfinalized vertex $u$ with the smallest `dist[u]`.
- Finalize $u$, then relax every edge leaving $u$.
- A priority queue keyed by `dist` finds that $u$ quickly.

> Think of a wave spreading from the source at constant speed along the aisles: it reaches stations in order of travel time, and the first time it reaches a station is the fastest time. Dijkstra's algorithm simulates that wave. It is a greedy algorithm: at each step it commits to the closest unfinished vertex and never revisits that decision. The non-negativity requirement is what makes that commitment safe, as we prove shortly.

---
# Dijkstra in pseudocode

```algorithm
function DIJKSTRA(G, s):                  // all weights ≥ 0
  dist[v] ← ∞, parent[v] ← none, done[v] ← false for every v
  dist[s] ← 0
  PQ ← priority queue holding (s, 0)
  while PQ is not empty do
    (u, d) ← REMOVE-MIN(PQ)
    if done[u] then continue              // stale entry
    done[u] ← true                        // dist[u] is final
    for each edge u → v with weight w do
      if dist[u] + w < dist[v] then       // RELAX
        dist[v] ← dist[u] + w; parent[v] ← u
        INSERT(PQ, (v, dist[v]))
  return dist, parent
```

> This is the lazy-deletion form of Dijkstra. When a vertex's distance improves we do not update its old queue entry; we insert a new entry with the smaller key. The old entry stays behind, and when it is eventually removed, the done flag shows that the vertex was already finalized with a smaller key, so we skip it. CLRS presents the version with a decrease-key operation; both finalize vertices in the same order, up to ties.

---
# Dijkstra in Java: setup

```java
record Entry(int vertex, long dist) {
}
```

```java
long[] dist = new long[n];
Arrays.fill(dist, ShortestPaths.INF);
int[] parent = new int[n];
Arrays.fill(parent, -1);
boolean[] done = new boolean[n];
PriorityQueue<Entry> pq = new PriorityQueue<>(
        Comparator.comparingLong(Entry::dist)
                .thenComparingInt(Entry::vertex));
dist[source] = 0;
pq.add(new Entry(source, 0));
```

> An Entry records which vertex was reached and with what distance at the moment it was inserted; a record gives us the constructor and accessors for free. INF is Long.MAX_VALUE. The comparator orders entries by distance, and breaks ties by vertex number so the run is deterministic and the trace on the next slides is reproducible. java.util.PriorityQueue always removes the smallest entry according to that comparator.

---
# Dijkstra in Java: main loop

```java
while (!pq.isEmpty()) {
    Entry top = pq.remove();
    int u = top.vertex();
    if (done[u]) {
        // ...
        continue; // stale entry: u was finalized earlier
    }
    done[u] = true;
    // ...
}
```

- `remove` returns the entry with the smallest key.
- An entry for a vertex that is already done is skipped: lazy deletion.
- The second gap is the relaxation loop, on the next slide.

> remove takes the entry with the smallest key. The done test implements lazy deletion: an entry whose vertex is already finalized is thrown away, and the next entry is taken. Otherwise the vertex is marked done, and its outgoing edges are relaxed; that loop fills the second gap and is on the next slide. The tested file also has two trace-recording lines, one in the skip branch and one after the relaxation loop; they are omitted here.

---
# Dijkstra in Java: relaxing u's edges

```java
for (WeightedDigraph.Edge e : g.edgesFrom(u)) {
    if (dist[u] + e.weight() < dist[e.to()]) {
        dist[e.to()] = dist[u] + e.weight();
        parent[e.to()] = u;
        pq.add(new Entry(e.to(), dist[e.to()]));
    }
}
```

- This loop runs right after `done[u] = true`.
- Relaxation is written inline; each improvement adds a fresh entry.
- No `INF` guard is needed: `dist[u]` is finite here.

> This is the loop that follows done of u equals true: relaxation written inline, where the new entry carries the improved distance. dist of u is finite here because u entered the queue with a finite key, so the addition cannot overflow. Every successful relaxation inserts a fresh entry, which is why the queue can hold several entries for one vertex. The old entries stay in the queue until the done test discards them.

---
# Why lazy deletion?

- `java.util.PriorityQueue` has no decrease-key operation.
- Its `remove(Object)` takes linear time, per the Java SE documentation.
- So we insert a new entry and let the old one go **stale**.
- A stale entry has a key larger than its vertex's final `dist`; the `done` check skips it.
- Each successful relaxation adds one entry: at most $E + 1$ entries in total.

> The API documentation promises logarithmic time for add, poll and remove of the head, linear time for remove of an arbitrary object and for contains, and says the iterator does not guarantee any particular order. Removing and reinserting would therefore cost linear time per update. Lazy deletion trades a slightly larger queue for simple code. Since each edge can cause at most one insertion, the queue never holds more than E plus one entries.

---
# Dijkstra trace, part 1

```html
<table class="tbl" style="font-size:0.75em">
<tr><th>Removed</th><th>S</th><th>A</th><th>B</th><th>C</th><th>D</th><th>E</th><th>Queue after, by key</th></tr>
<tr><td>start</td><td>0</td><td>∞</td><td>∞</td><td>∞</td><td>∞</td><td>∞</td><td>(S,0)</td></tr>
<tr data-step="1"><td>(S,0) finalize S</td><td><b>0</b></td><td>4</td><td>1</td><td>∞</td><td>∞</td><td>∞</td><td>(B,1) (A,4)</td></tr>
<tr data-step="2"><td>(B,1) finalize B</td><td><b>0</b></td><td>3</td><td><b>1</b></td><td>6</td><td>9</td><td>∞</td><td>(A,3) (A,4) (C,6) (D,9)</td></tr>
<tr data-step="3"><td>(A,3) finalize A</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td>4</td><td>9</td><td>∞</td><td>(A,4) (C,4) (C,6) (D,9)</td></tr>
<tr data-step="4"><td>(A,4) stale, skip</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td>4</td><td>9</td><td>∞</td><td>(C,4) (C,6) (D,9)</td></tr>
</table>
```

Bold distances are final.

> Start with only the source in the queue. Removing S relaxes its two edges: A gets four and B gets one. B is closest, so it is finalized next; through B, A improves to three, C gets six and D gets nine. Notice the queue now holds two entries for A. Removing A with key three finalizes it and improves C to four. Now the old entry for A, key four, reaches the front: it ties with C at four and wins the tie by vertex number, and the done check discards it.

---
# Dijkstra trace, part 2

```html
<table class="tbl" style="font-size:0.75em">
<tr><th>Removed</th><th>S</th><th>A</th><th>B</th><th>C</th><th>D</th><th>E</th><th>Queue after, by key</th></tr>
<tr data-step="1"><td>(C,4) finalize C</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td><b>4</b></td><td>7</td><td>10</td><td>(C,6) (D,7) (D,9) (E,10)</td></tr>
<tr data-step="2"><td>(C,6) stale, skip</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td><b>4</b></td><td>7</td><td>10</td><td>(D,7) (D,9) (E,10)</td></tr>
<tr data-step="3"><td>(D,7) finalize D</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td><b>4</b></td><td><b>7</b></td><td>8</td><td>(E,8) (D,9) (E,10)</td></tr>
<tr data-step="4"><td>(E,8) finalize E</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td><b>4</b></td><td><b>7</b></td><td><b>8</b></td><td>(D,9) (E,10)</td></tr>
<tr data-step="5"><td>(D,9), (E,10) stale</td><td><b>0</b></td><td><b>3</b></td><td><b>1</b></td><td><b>4</b></td><td><b>7</b></td><td><b>8</b></td><td>empty</td></tr>
</table>
```

Ten removals: six finalize a vertex, four discard stale entries.

> C is finalized at four, improving D to seven and E to ten. The stale entry for C with key six is skipped. D is finalized at seven and improves E to eight through the edge of weight one. E is finalized at eight. The last two removals are stale entries for D and E, and then the queue is empty. The finalization order S, B, A, C, D, E is also the order of increasing distance, just like the spreading wave.

---
@type section
# Why Dijkstra works

---
# The result: a shortest-path tree

```diagram Parent pointers after Dijkstra from S
@dir LR
@reveal all
S[S 0] -> B[B 1] : 1
B -> A[A 3] : 2
A -> C[C 4] : 1
C -> D[D 7] : 3
D -> E[E 8] : 1
```

- Every vertex's parent: `B <- S`, `A <- B`, `C <- A`, `D <- C`, `E <- D`.
- Here the tree happens to be a single path. In general it branches.
- Four of the nine edges, such as `S -> A` and `B -> D`, are on no shortest path.

> Keep only the edge from each vertex's parent, and you get a tree rooted at the source that contains one shortest path to every reachable vertex. On this graph every shortest path extends the previous one, so the tree is a chain: S, B, A, C, D, E. Reading the route to E off the parent pointers gives five edges with weights one, two, one, three and one, totalling eight. Edges like S to A of weight four exist, but no shortest route uses them.

---
# Why Dijkstra is correct

- Claim: when $u$ is finalized, `dist[u]` $= \delta(s, u)$.
- Take a shortest path $P$ from $s$ to $u$. Let $y$ be its first unfinalized vertex, $x$ just before it.
- $x$ was finalized with its true distance, and relaxed $x \to y$: `dist[y]` $\le \delta(s, y)$.
- Weights are $\ge 0$, so the rest of $P$ cannot make it lighter: $\delta(s, y) \le \delta(s, u)$.
- $u$ was chosen with the smallest key: `dist[u]` $\le$ `dist[y]`. Chain them:

$$\begin{aligned}\delta(s,u) &\le \text{dist}[u] \le \text{dist}[y]\\ &\le \delta(s,y) \le \delta(s,u)\end{aligned}$$

> The argument is by induction on the finalization order: assume every earlier finalized vertex was exact, and show the new one is too. The key step is the fourth line. The part of the path after y consists of edges of non-negative weight, so the prefix up to y is no heavier than the whole path. Then every term in the chain is squeezed between the true distance to u on both ends, so they are all equal. Remove the non-negativity assumption and exactly that step breaks, as the counterexample section shows.

---
# Dijkstra's cost

- Each vertex is finalized once: $V$ passes through the edge loop, $E$ relaxations in total.
- At most $E + 1$ insertions and removals, each $O(\log(E+1))$ on a binary heap.
- $\log E \le 2 \log V$ when there are no parallel edges, since then $E \le V^2$.
- Total: $O((V + E)\log V)$ time with adjacency lists and a binary heap.
- Extra space: $\Theta(V)$ for the arrays plus $O(E)$ for queue entries.

> Count the heap operations: one insertion per successful relaxation, plus the source, and one removal per insertion. Each costs logarithmic time in the queue size. Everything else is linear in V plus E. The bound is an upper bound; on graphs where few relaxations succeed, the run is cheaper. Other heap structures give better theoretical bounds; we stay with the heap that Java provides.

---
# Quiz: reading the queue

```quiz
In the trace, why does the entry (A,4) come out of the queue after A was already finalized?
- [ ] A's distance was wrong and needed a second update
- [x] it was inserted when S relaxed S -> A, before B improved A to 3; nothing removed it
- [ ] PriorityQueue returns entries in insertion order when keys tie
- [ ] Dijkstra re-examines every vertex twice
```

> Removing S inserted A with key four. Then removing B found the shorter route through B and inserted A again with key three. Lazy deletion never deletes the older entry, so it waits in the heap until its key is the smallest. By then A was finalized with key three, and the done check discards the stale entry. Ties between entries with equal keys are broken by our comparator, by vertex number, not by insertion order.

---
@type section
# When Dijkstra fails

---
# A negative edge

```diagram Vertices S, A, B, C; one negative edge
@dir LR
@reveal all
S -> A : 2
S -> B : 5
B -> A : -4
A -> C : 1
```

- True distances: $A = 5 - 4 = 1$ via `B`, and $C = 1 + 1 = 2$ via `S, B, A`.
- No negative cycle: there is no cycle at all.

> Here is a four-vertex graph with one negative edge, from B to A of weight minus four. It has no cycles, so every distance is well defined. The best route to A goes the long way through B: five, then minus four, is one, which beats the direct edge of weight two. Then C is one more, so two. Keep those true answers in mind while we watch Dijkstra. We also trace Bellman-Ford on this graph rather than on the running example, because the running example has no negative edges; Floyd-Warshall gets its own four-vertex graph later, since a six-by-six matrix per round is too big for a slide.

---
# Dijkstra on the negative edge

```diagram
@dir TB
@reveal manual
N1[finalize S | A 2, B 5]
focus N1
--- S relaxes both edges.
N1 -> N2[finalize A = 2 | C 3]
focus N1 N2
--- A is closest, so it is declared final at 2.
N2 -> N3[finalize C = 3]
focus N2 N3
--- C is finalized from A's wrong value.
N3 -> N4[finalize B = 5 | A lowered to 1]
focus N3 N4
--- Too late: A was already used to finalize C.
N4 -> R[answer C = 3, true C = 2].rose
focus R
--- (A,1) is skipped because A is already marked done; C is never fixed.
```

> This is the Java run. Dijkstra finalizes A at two because it is closest when chosen, and relaxes A to C, giving C three. C is finalized at three. Only then is B removed, and its negative edge lowers A to one. The done flag says A is finished, so the new entry for A is discarded and C is never revisited. Bellman-Ford on the same graph returns C equals two, and a brute-force search over all simple paths agrees with Bellman-Ford. The greedy commitment was wrong because a later, heavier vertex had a negative edge.

---
# What went wrong in the proof

- The proof needed: a path's prefix is never heavier than the whole path.
- With $B \to A$ of weight $-4$, the path `S, B, A` is lighter than its prefix `S, B`.
- So the smallest key in the queue is **not** guaranteed to be final.
- Reweighting by adding a constant to every edge does not fix it: paths with more edges gain more.
- Use Bellman-Ford for negative edges, or Floyd-Warshall for all pairs.

> The failure is exactly the step marked in the proof. A tempting fix is to add four to every edge so that all weights become non-negative, but that changes which path is shortest: a path with three edges gains twelve while a path with one edge gains four. Some implementations without a done check re-process vertices whose distance drops, which repairs this small case but gives up Dijkstra's time bound. The clean answer is a different algorithm.

---
@type section
# Bellman-Ford

---
# Bellman-Ford's idea

- No greedy choice: relax **every** edge, in any fixed order, in each pass.
- Repeat for $V - 1$ passes.
- Then one more check: if any edge can still be relaxed, report a negative cycle.
- Works with negative edges. Detects negative cycles **reachable from** $s$.
- Our version stops early after a pass that changes nothing.

> Bellman-Ford gives up the clever order and uses brute persistence instead. Each pass relaxes all E edges. We will see that V minus one passes are always enough when no negative cycle is reachable, so any improvement still possible after that proves that a negative cycle exists. The early stop is a common optimization: once a pass changes nothing, every later pass would see exactly the same values and change nothing too.

---
# Bellman-Ford in pseudocode

```algorithm
function BELLMAN-FORD(G, s):
  dist[v] ← ∞, parent[v] ← none for every v; dist[s] ← 0
  for pass ← 1 to V - 1 do
    for each edge u → v with weight w do
      RELAX(u, v, w)
  for each edge u → v with weight w do
    if dist[u] + w < dist[v] then
      return "negative cycle reachable from s"
  return dist, parent
```

> The structure is two nested loops and a final check. The order of edges within a pass does not affect correctness, only how quickly the values settle. The final loop does not change anything; it only asks whether any edge could still improve its head. Remember that RELAX skips an edge whose tail still has distance infinity.

---
# Why $V - 1$ passes suffice

- Suppose no negative cycle is reachable. Take a shortest **simple** path $s = v_0 \to v_1 \to \dots \to v_k = v$.
- A simple path has at most $V - 1$ edges, so $k \le V - 1$.
- Pass 1 relaxes $v_0 \to v_1$, so `dist[v_1]` $= \delta(s, v_1)$ afterwards.
- By induction, after pass $i$, `dist[v_i]` $= \delta(s, v_i)$.
- After pass $k \le V - 1$, `dist[v]` is exact, and no edge can relax further.

> The argument follows one shortest path edge by edge. In pass one, whatever the order, the first edge of the path gets relaxed after the source's distance is zero, so the second vertex becomes exact. Once a vertex is exact it stays exact, since dist never goes below the true distance. Each pass extends the exact prefix by at least one edge. Conversely, if a negative cycle is reachable, summing the relaxation inequalities around the cycle would force its weight to be non-negative, so some edge on the cycle must still relax in the check.

---
# Bellman-Ford in Java: the passes

```java
for (int pass = 1; pass <= n - 1; pass++) {
    boolean changed = false;
    for (WeightedDigraph.Edge e : g.edges()) {
        if (relax(e, dist, parent)) {
            changed = true;
        }
    }
    // ...
    if (!changed) {
        break; // no change: every later pass would do nothing too
    }
}
```

- `dist`, `parent` and `n` are set up as in Dijkstra, with `dist[source] = 0`.
- `g.edges()` lists all edges in insertion order.

> Each pass walks the whole edge list once and relaxes every edge. The changed flag records whether any relaxation succeeded, and a pass that changes nothing ends the loop early. g.edges is the list of all edges in insertion order, which is the order the trace follows. The marked gap is the trace-recording statement of the tested file. The relax method comes next, and after it the final check that looks for a negative cycle.

---
# Relax in Java

```java
static boolean relax(WeightedDigraph.Edge e, long[] dist, int[] parent) {
    int u = e.from();
    int v = e.to();
    if (dist[u] != ShortestPaths.INF && dist[u] + e.weight() < dist[v]) {
        dist[v] = dist[u] + e.weight();
        parent[v] = u;
        return true;
    }
    return false;
}
```

- Returns whether it changed anything, which drives the early stop.
- Without the `INF` guard, `Long.MAX_VALUE + (-4)` would look like a real distance.

> This is RELAX from the pseudocode, plus the overflow guard. The guard matters more with negative weights: adding minus four to Long.MAX_VALUE does not overflow at all, it produces a huge but finite-looking number, which would then be treated as a real path to an unreachable vertex. Checking for infinity first avoids both that and genuine overflow for positive weights.

---
# Bellman-Ford in Java: the final check

```java
for (WeightedDigraph.Edge e : g.edges()) {
    if (dist[e.from()] != ShortestPaths.INF
            && dist[e.from()] + e.weight() < dist[e.to()]) {
        return Optional.empty(); // still improvable: negative cycle
    }
}
return Optional.of(new ShortestPaths(dist, parent));
```

- One more scan of every edge after the passes. It changes nothing.
- An edge that could still relax proves a negative cycle reachable from the source.
- The result is an `Optional`: empty means "no answer exists".

> The method returns an Optional of ShortestPaths: empty means a reachable negative cycle, so the caller cannot mistake meaningless distances for answers. The test is the same comparison as in relax, with the same guard against infinity, but it only asks the question and does not update anything. If no edge can improve its head, the distances and parent pointers are final and the method wraps them in a ShortestPaths object.

---
# Bellman-Ford trace

| After pass | S | A | B | C | Changed? |
|---|---|---|---|---|---|
| start | 0 | ∞ | ∞ | ∞ | |
| 1 | 0 | 1 | 5 | ∞ | yes |
| 2 | 0 | 1 | 5 | 2 | yes |
| 3 | 0 | 1 | 5 | 2 | no: stop |

- Edge order: `A -> C`, `S -> A`, `S -> B`, `B -> A`.
- The check finds nothing to relax. The answer $C = 2$ is correct.

> The edge list starts with A to C, deliberately. In pass one that edge is relaxed while A is still infinity, so it is skipped. Then S to A sets A to two, S to B sets B to five, and B to A lowers A to one. Pass two finally relaxes A to C, giving C two. Pass three changes nothing, so the loop stops early. With V equal to four, pass three was also the last one allowed. Had the list started with S to A, the first pass would have settled everything: edge order changes speed, not the answer.

---
# Detecting a negative cycle

| After pass | S | A | B | C |
|---|---|---|---|---|
| 1 | 0 | 1 | 5 | ∞ |
| 2 | 0 | 1 | 4 | 2 |
| 3 | 0 | 0 | 4 | 2 |
| check | `A -> C`: $0 + 1 < 2$ | | | |

- Add `C -> B` with weight 2: the cycle `B -> A -> C -> B` weighs $-4 + 1 + 2 = -1$.
- Values keep falling each pass. After $V - 1 = 3$ passes an edge still relaxes: report it.

> The same graph with one more edge, appended last. Pass two now also lowers B to four through C. Pass three lowers A to zero. Each trip around the cycle subtracts one, and it would never stop. After the three allowed passes the check finds that A to C could still improve C, so the method returns an empty Optional. A negative cycle that the source cannot reach has no effect on the distances from s, and Bellman-Ford does not report it; our checks include such a graph.

---
# Bellman-Ford's cost

- $V - 1$ passes of $E$ relaxations, plus one checking pass: $O(VE)$ time.
- The worst case is $\Theta(VE)$; the early stop can finish after one pass, in $\Theta(E)$ plus setup.
- Extra space: $\Theta(V)$.
- On a DAG, relaxing edges in topological order needs just one pass: $\Theta(V + E)$.

> Bellman-Ford is slower than Dijkstra, roughly by a factor of V over log V on sparse graphs, which is the price of handling negative edges. The last bullet connects to L16: in a DAG, if you relax each vertex's outgoing edges in topological order, every shortest path is relaxed in order within a single pass. CLRS presents that algorithm in Chapter 22. It works with negative edges because a DAG has no cycles at all.

---
# Quiz: how many passes?

```quiz
A graph with 6 vertices has no negative cycle. What is the largest number of Bellman-Ford passes that can ever change a distance?
- [ ] 1
- [x] 5
- [ ] 6
- [ ] 36
```

> A shortest simple path has at most V minus one edges, here five. Each pass extends the settled prefix of such a path by at least one edge, so by the end of pass five every distance is final, and a sixth pass could change nothing. Five passes can be needed: on a chain of six vertices whose edges are listed from the far end back toward the source, each pass settles exactly one more vertex.

---
@type section
# Floyd-Warshall

---
# All pairs

- Now we want $\delta(i, j)$ for **every** pair: a $V \times V$ matrix.
- One option: run a single-source algorithm from each vertex.
- Floyd-Warshall instead uses **dynamic programming** on the matrix.
- It allows negative edges, and detects negative cycles anywhere in the graph.
- Simple triple loop, good for dense graphs; L20 covers dynamic programming in general.

> Some questions need distances between all pairs: a travel-time table between all stations, or the diameter of a network. V runs of Bellman-Ford would cost V squared times E, which is expensive on dense graphs. V runs of Dijkstra are fine when weights are non-negative. Floyd-Warshall is a third route: a three-line triple loop that fills the whole matrix, handles negative edges, and needs no priority queue. It is our first real example of dynamic programming, which gets its own lecture later.

---
# Intermediate vertices

- An **intermediate** vertex of a path is any vertex except its two ends.
- Let $d_k[i][j]$ be the shortest $i \to j$ weight using intermediates only from $\{0, \dots, k-1\}$.
- $d_0$ allows no intermediates: edge weights, 0 on the diagonal, $\infty$ otherwise.
- The best path using $\{0, \dots, k\}$ either avoids $k$, or goes $i \leadsto k \leadsto j$:

$$\begin{aligned}d_{k+1}[i][j] = \min\bigl(&d_k[i][j],\\ &d_k[i][k] + d_k[k][j]\bigr)\end{aligned}$$

- $d_V$ allows every vertex, so it holds the true distances.

> This is the dynamic-programming insight: grow the set of vertices a path may pass through, one vertex at a time. When vertex k becomes allowed, a best path either does not use it, and its weight is unchanged, or uses it exactly once, and then splits at k into two pieces that use only the older vertices. Using it once is enough when there is no negative cycle, because a shortest path can be taken simple. Both pieces were computed in the previous round, so each entry costs constant time to update.

---
# Floyd-Warshall in pseudocode

```algorithm
function FLOYD-WARSHALL(G):
  d[i][j] ← ∞ for all i, j;  d[i][i] ← 0
  for each edge i → j with weight w do d[i][j] ← min(d[i][j], w)
  for k ← 0 to V - 1 do                 // allow vertex k
    for i ← 0 to V - 1 do
      for j ← 0 to V - 1 do
        if d[i][k] + d[k][j] < d[i][j] then
          d[i][j] ← d[i][k] + d[k][j]
  return d          // some d[i][i] < 0 means a negative cycle exists
```

- $k$ must be the **outermost** loop. One matrix is updated in place.

> Updating a single matrix in place is safe: during round k, row k and column k do not improve, because going through k to reach k cannot help unless there is a negative cycle. So it does not matter whether an entry is read before or after its update within the round. The min in the initialization keeps the lighter of two parallel edges. The loop order is not a detail: the next slides show what happens with k in the wrong place.

---
# Floyd-Warshall in Java

```java
for (int k = 0; k < n; k++) {
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (d[i][k] != ShortestPaths.INF && d[k][j] != ShortestPaths.INF
                    && d[i][k] + d[k][j] < d[i][j]) {
                d[i][j] = d[i][k] + d[k][j];
            }
        }
    }
}
```

- `d` starts as $d_0$: `INF` everywhere, 0 on the diagonal, then the edge weights.

> The Java is the pseudocode with the infinity guard added to both halves of the sum. Without it, adding two Long.MAX_VALUE entries overflows to a negative number, and the algorithm would invent a very short path. The body is constant work, so the three loops give V cubed updates, regardless of how many edges the graph has.

---
# The four-vertex example

```diagram One negative edge, 2 -> 1; no negative cycle
@dir LR
@reveal all
V0((0)) -> V1((1)) : 4
V0 -> V2((2)) : 1
V2 -> V1 : -2
V1 -> V3((3)) : 2
V2 -> V3 : 5
V3 -> V0 : 3
```

| $d_0$ | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| **0** | 0 | 4 | 1 | ∞ |
| **1** | ∞ | 0 | ∞ | 2 |
| **2** | ∞ | −2 | 0 | 5 |
| **3** | 3 | ∞ | ∞ | 0 |

> A small graph with vertices zero to three. It has one negative edge, from two to one. Every cycle passes through vertex zero and weighs at least four, so there is no negative cycle. Row i of the matrix lists direct edge weights out of vertex i. Row two, for example, has minus two to vertex one and five to vertex three. Rows are sources and columns are targets throughout.

---
# After allowing 0, then 1

| after $k=0$ | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| **0** | 0 | 4 | 1 | ∞ |
| **1** | ∞ | 0 | ∞ | 2 |
| **2** | ∞ | −2 | 0 | 5 |
| **3** | 3 | ==7== | ==4== | 0 |

| after $k=1$ | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| **0** | 0 | 4 | 1 | ==6== |
| **1** | ∞ | 0 | ∞ | 2 |
| **2** | ∞ | −2 | 0 | ==0== |
| **3** | 3 | 7 | 4 | 0 |

> Highlighted cells changed in that round. Allowing vertex zero helps only paths from vertex three, the one vertex with an edge into zero: three to one becomes three plus four, seven, and three to two becomes three plus one, four. Allowing vertex one then helps paths ending at three: zero to three becomes four plus two, six, and two to three becomes minus two plus two, zero, better than the direct five.

---
# After allowing 2, then 3

| after $k=2$ | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| **0** | 0 | ==−1== | 1 | ==1== |
| **1** | ∞ | 0 | ∞ | 2 |
| **2** | ∞ | −2 | 0 | 0 |
| **3** | 3 | ==2== | 4 | 0 |

| after $k=3$ | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| **0** | 0 | −1 | 1 | 1 |
| **1** | ==5== | 0 | ==6== | 2 |
| **2** | ==3== | −2 | 0 | 0 |
| **3** | 3 | 2 | 4 | 0 |

> Allowing vertex two uses the negative edge: zero to one becomes one plus minus two, minus one; zero to three becomes one plus zero, one; three to one becomes four plus minus two, two. Allowing vertex three, which leads back to zero, fills in the last gaps: one to zero is two plus three, five; one to two is two plus four, six; two to zero is zero plus three, three. The final matrix matches four separate Bellman-Ford runs, one from each source, as the checks confirm.

---
# Loop order and negative cycles

- With $k$ innermost, entry $d[i][j]$ is finished before the entries it needs are computed.
- Chain `0 -> 2 -> 3 -> 1`: $k$ outermost finds $d[0][1] = 3$; $k$ innermost leaves $\infty$.
- After the run, some $d[i][i] < 0$ exactly when the graph has a negative cycle.
- Every vertex on a simple negative cycle gets a negative diagonal entry.
- Adding `C -> B` to the earlier graph gives negative $d[i][i]$ for `A`, `B` and `C`, but 0 for `S`.

> With the wrong loop order, the algorithm decides d of zero one while d of zero three and d of three one are still infinity, and never returns to it. Our checks run both versions on that chain. On the four-vertex example the wrong order happens to give the right matrix, which is exactly why a single passing example proves little. For negative cycles, the diagonal starts at zero, and every finite entry is the weight of some walk, so a negative diagonal entry means a negative closed walk, which contains a negative cycle. Conversely, each vertex on a simple negative cycle gets a negative entry. The reverse per vertex fails: the checks include a vertex whose entry goes negative although it lies on no negative simple cycle. Use the diagonal as a yes-or-no test for the whole graph. Unlike Bellman-Ford, it sees cycles anywhere.

---
# Floyd-Warshall's cost

- Three nested loops of $V$: $\Theta(V^3)$ time, whatever the number of edges.
- One $V \times V$ matrix, updated in place: $\Theta(V^2)$ space.
- Compare $V$ runs of Dijkstra: $O(V(V+E)\log V)$, better when the graph is sparse.
- Compare $V$ runs of Bellman-Ford: $O(V^2E)$, which is $O(V^4)$ on dense graphs.

> Floyd-Warshall's running time does not depend on the edges at all, which makes it attractive for dense graphs where E is close to V squared, and for small graphs where its simplicity wins. On large sparse graphs with non-negative weights, running Dijkstra from every vertex is faster. With negative edges on sparse graphs, CLRS describes Johnson's algorithm, which combines one Bellman-Ford run with V Dijkstra runs; we do not cover it here.

---
@type section
# Choosing and using the answer

---
# Path reconstruction

```java
public List<Integer> pathTo(int v) {
    if (!hasPathTo(v)) {
        return List.of();
    }
    Deque<Integer> path = new ArrayDeque<>();
    for (int x = v; x != -1; x = parent[x]) {
        path.push(x);
    }
    return new ArrayList<>(path);
}
```

- From Dijkstra on the running graph: `pathTo(E)` returns `S, B, A, C, D, E`.
- Floyd-Warshall needs a second $V \times V$ matrix of predecessors to do the same.

> Follow parents backwards from v until reaching the source, whose parent is minus one, and push each vertex on the front, so the list comes out source first. The walk costs time proportional to the path length. An unreachable vertex returns an empty list rather than a path of one vertex. For all pairs, CLRS keeps a predecessor matrix alongside the distance matrix, updated whenever an entry improves; our Floyd-Warshall code returns distances only.

---
# Which algorithm when?

| Algorithm | Weights | Question | Time |
|---|---|---|---|
| BFS | all equal | single source | $\Theta(V+E)$ |
| Dijkstra | $\ge 0$ | single source | $O((V+E)\log V)$ |
| Bellman-Ford | any; reports reachable negative cycles | single source | $O(VE)$ |
| Floyd-Warshall | any; reports negative cycles | all pairs | $\Theta(V^3)$ |

Assumes adjacency lists for the first three and binary heap for Dijkstra.

> Read the table from the top and stop at the first row that fits. Equal weights: BFS. Non-negative weights: Dijkstra. Negative edges: Bellman-Ford, which also tells you if the question has no answer. All pairs on a small or dense graph: Floyd-Warshall. And if your graph is a DAG, the L16 topological order gives single-source shortest paths in linear time, even with negative edges.

---
# Pitfalls and edge cases

- Running Dijkstra with any negative edge: wrong answers, no error message.
- Using `Integer.MAX_VALUE` or `Long.MAX_VALUE` as infinity and adding to it.
- An **undirected** edge of negative weight is a negative cycle: `u - v - u`.
- Unreachable vertices keep $\infty$ and an empty path; the source has path `[s]`.
- Parallel edges: keep the lighter one. Zero-weight edges are fine for Dijkstra.

> The first pitfall is the dangerous one, because nothing crashes; the answers are just wrong. The second causes overflow bugs. They appear whenever an edge is scanned from a vertex whose estimate is still infinite, as A to C is in pass 1 of the Bellman-Ford trace. The third surprises people: an undirected edge is two directed edges, and walking back and forth over a negative one lowers the weight without limit. Our test suite covers unreachable vertices, a single vertex, parallel edges and zero weights for all three algorithms.

---
# Quiz: pick the algorithm

```quiz
A graph has 10,000 vertices, 40,000 edges, some edges of negative weight, and you need distances from one source. Which algorithm fits best?
- [ ] BFS
- [ ] Dijkstra
- [x] Bellman-Ford
- [ ] Floyd-Warshall
```

> BFS ignores weights, and Dijkstra is incorrect with negative edges. Floyd-Warshall would compute all hundred million pairs with a trillion inner updates when only one row is needed. Bellman-Ford handles the negative edges, reports any reachable negative cycle, and costs at most V times E relaxations, about four hundred million here, and far fewer if the early stop triggers. If the graph also happened to be a DAG, the one-pass topological method would be better still.

---
# In Java and in the visualizations

- `java.util.PriorityQueue` is heap-based (a binary heap in current OpenJDK); `add` and `remove()` are $O(\log n)$.
- It has no decrease-key; `remove(Object)` is linear, so we use lazy deletion.
- Its iterator has no guaranteed order: sort a copy to print the queue.
- Step through [Dijkstra](../../../visualizations/algorithms/dijkstra.html), [Bellman-Ford](../../../visualizations/algorithms/bellman_ford.html) and [Floyd-Warshall](../../../visualizations/algorithms/floyd_warshall.html).

> These are statements from the Java SE API documentation for PriorityQueue. The iterator detail matters when debugging: printing a PriorityQueue need not show sorted order, which is why our trace code sorts a copy before printing. The three visualizations let you run each algorithm on other graphs. Try the negative-edge example in the Dijkstra and Bellman-Ford views and compare.

---
@type section
# Wrap-up

---
# Summary

- Every algorithm today improves `dist` and `parent` only by relaxation.
- BFS: equal weights. Dijkstra: non-negative weights, greedy, heap.
- Dijkstra's proof needs non-negative weights; one negative edge can break it.
- Bellman-Ford: $V - 1$ passes over all edges; one more reveals a negative cycle.
- Floyd-Warshall: allow intermediates one by one; a negative diagonal reveals a negative cycle.
- Parent pointers or a predecessor matrix recover the actual route.

> The three algorithms trade assumptions for speed. Dijkstra is the fastest but needs non-negative weights. Bellman-Ford accepts any weights and detects trouble, at a higher cost. Floyd-Warshall answers every pair at once with a cubic triple loop. Next lecture stays with weighted graphs but changes the question: instead of the cheapest route from one vertex, the cheapest set of edges that connects all of them.

---
# Check yourself

- Change `S -> A` in the running graph to weight 2. Which rows of the Dijkstra trace change?
- Why does Bellman-Ford's answer not depend on the order of edges within a pass?
- In Floyd-Warshall, why can row $k$ and column $k$ not improve during round $k$ without a negative cycle?

> Work the first on paper with the table layout from the trace; notice which entries go stale and which do not. For the second, go back to the proof that V minus one passes suffice: where did it use the order? For the third, write out d of i k plus d of k k and ask what d of k k must be.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 22: Single-source shortest paths (relaxation, Bellman-Ford, DAG shortest paths, Dijkstra).
- CLRS, Chapter 23: All-pairs shortest paths (Floyd-Warshall, Johnson's algorithm).
- E. W. Dijkstra, "A note on two problems in connexion with graphs", *Numerische Mathematik* 1, 1959, 269–271.
- Java SE API documentation: `java.util.PriorityQueue`.
- All examples, traces and code are original to this course and were produced by the tested Java files for this lecture.

> CLRS Chapter 22 is the reference for relaxation, Bellman-Ford, shortest paths in DAGs, and Dijkstra's algorithm with its correctness proof. Chapter 23 covers Floyd-Warshall and the predecessor matrix. Dijkstra's short 1959 paper introduced the algorithm. The Java API documentation is the source for what PriorityQueue guarantees. Every trace in this deck came from running the lecture's Java code.
