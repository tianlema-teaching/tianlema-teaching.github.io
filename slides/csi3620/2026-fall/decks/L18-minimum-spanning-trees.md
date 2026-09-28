@title Lecture 18: Minimum spanning trees
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Minimum spanning trees
## Kruskal, Prim, and the cut property

---
# Where we are

- L15: graphs as adjacency lists; BFS and DFS.
- L16: topological order and cycle detection.
- L17: shortest paths from one source (Dijkstra and others).
- Today: connect every vertex as cheaply as possible.
- L14 heaps and a new structure, union-find, do the heavy lifting.

> Over the last three lectures we treated graphs as maps to explore and to route through. Today the question changes. We are no longer asking how to get from one vertex to another; we are asking which edges to build so that everything is connected at the lowest total cost. The priority queue from lecture fourteen comes back, and we add one new data structure, union-find, which answers "are these two vertices already connected?" very quickly.

---
# By the end of today you can

- Decide whether a set of edges is a spanning tree.
- State the cut property and use it to justify an edge.
- Trace Kruskal's algorithm with union-find, edge by edge.
- Trace lazy Prim with its priority queue, step by step.
- Give the running time of each and pick one for a graph.
- Show that an MST and a shortest-path tree can differ.

> These are the things you should be able to do on paper by the end of class. Each one is something you can check: given a list of edges, say yes or no; given a cut, name the edge it forces; given a graph, write the order in which each algorithm accepts edges. We will trace everything on one six-vertex graph, so by the end you will know that graph well.

---
# The problem: wire six buildings

- Six buildings, A to F, must share one network.
- Some pairs can be joined by a cable; each has a cost.
- Any building must reach any other, possibly through others.
- Goal: the cheapest set of cables that does this.
- A cycle wastes money: drop any cable on it and all stay connected.

> Picture a small campus that needs a wired network. We know which pairs of buildings can be joined directly and what each cable would cost. We do not need a cable between every pair; we only need every building to be reachable from every other. If our plan ever contains a cycle, one cable on that cycle is redundant, because the rest of the cycle still connects its ends. So the cheapest plan has no cycles at all. That shape has a name: a tree.

---
# The running graph

```diagram
@dir LR
@reveal all
A -- B : 4
A -- C : 2
B -- C : 5
B -- D : 10
C -- E : 3
C -- D : 8
D -- E : 7
D -- F : 6
E -- F : 9
```

Six vertices, nine edges, all weights distinct. Every trace today uses this graph.

> Here is the graph we will use for every trace. Vertices are the buildings, edges are possible cables, and each number is the cable's cost, called the edge's weight. Copy it now: A to B four, A to C two, B to C five, B to D ten, C to E three, C to D eight, D to E seven, D to F six, E to F nine. Every weight is different on purpose; we will see why that matters.

---
# Spanning tree

- A **tree** is a connected graph with no cycle.
- A **spanning tree** of $G$ uses all $V$ vertices of $G$ and some of its edges, and is a tree.
- Every spanning tree has exactly $V-1$ edges.
- Fewer than $V-1$ edges: not connected. More: a cycle.
- Only a **connected** graph has a spanning tree.

> Write these three conditions next to each other: connected, no cycle, and exactly V minus one edges. For a graph on V vertices, any two of them imply the third, which is why counting edges is a quick first test. Our graph has six vertices, so every spanning tree of it has exactly five edges. If a graph is not connected, no set of its edges can connect it, and the best we can build is a spanning forest: one tree per connected piece.

---
# Minimum spanning tree

- Input: a connected, undirected graph with edge weights $w(u,v)$.
- The weight of a tree $T$ is the sum of its edge weights.
- An **MST** is a spanning tree of minimum total weight.
- There may be several MSTs with the same weight.
- Weights may be negative; only their order matters.

$$w(T)=\sum_{(u,v)\in T} w(u,v)$$

> The weight of a tree is just the sum of its edge weights. A minimum spanning tree is any spanning tree whose weight is as small as possible. Notice the article: "a" minimum spanning tree, not "the", because ties can produce several. Unlike Dijkstra, nothing here breaks with negative weights: every spanning tree has the same number of edges, so adding a constant to every weight shifts every tree's total by the same amount and does not change which tree wins.

---
# Where MSTs are used

- **Network design**: cable, pipe or road layouts that connect every site.
- **Clustering**: build an MST, delete the $k-1$ heaviest edges, get $k$ groups.
- **Building block**: some approximation algorithms start from an MST.
- Common thread: connect everything, pay as little as possible.

> The wiring problem is the classic use: anything where you must connect every site and each link has a cost. The clustering use is less obvious. Treat edge weight as distance between data points. The heaviest MST edges bridge points that are far apart, so removing the k minus one heaviest ones splits the tree into k groups of nearby points; this is called single-linkage clustering. MSTs also appear inside approximation algorithms for harder problems, which you will meet in later courses.

---
@type section
# Why greedy choices are safe

---
# Cuts and crossing edges

- A **cut** $(S, V-S)$ splits the vertices into two nonempty sides.
- An edge **crosses** the cut if its ends are on different sides.
- A **light edge** is a crossing edge of minimum weight.
- Every spanning tree uses at least one crossing edge of every cut.

> A cut is nothing more than a way to color every vertex one of two colors, with at least one vertex of each. An edge crosses the cut when its two ends get different colors. Every spanning tree must use at least one crossing edge, otherwise the two sides would not be connected to each other. Among the crossing edges, the lightest one is called a light edge; with distinct weights there is exactly one.

---
# A cut in the running graph

```diagram S = {A, B, C}. Light edge: C-E 3
@dir LR
@reveal all
group S { A B C }
group Rest { D E F }
C -- E : 3
C -- D : 8
B -- D : 10
```

> In this example the left side is A, B and C, and the right side is D, E and F. Only three edges cross: C to E three, C to D eight, and B to D ten. The picture shows only those crossing edges; the edges inside each side are left out. The lightest crossing edge, C to E, is the light edge of this cut, and the next slide says it must be in the minimum spanning tree.

---
# The cut property

- Take any cut, and let $e$ be a light edge crossing it.
- Then some MST contains $e$.
- With distinct weights, **every** MST contains $e$.
- Stronger form used by the algorithms: if a set $A$ of chosen edges lies in some MST and no edge of $A$ crosses the cut, then $A\cup\{e\}$ also lies in some MST.

> This is the one theorem both algorithms rest on. It says: look at any cut you like; the cheapest edge across it is always a safe choice. The stronger form is what the algorithms actually use: if the edges we already picked are part of some minimum spanning tree and none of them crosses our cut, then adding the light edge keeps us inside some minimum spanning tree. So a greedy algorithm can keep adding light edges and never has to take one back.

---
# Proof of the cut property

- Let $T$ be an MST. If $e=(u,v)\in T$, done.
- Otherwise add $e$ to $T$: this makes exactly one cycle.
- That cycle goes from $u$'s side to $v$'s side and back, so it crosses the cut again on some edge $f\ne e$.
- $T' = T + e - f$ is still a spanning tree, and $w(e)\le w(f)$.
- So $w(T')\le w(T)$: $T'$ is an MST that contains $e$.

> This is an exchange argument, a proof pattern we will reuse all of next lecture. Start from any optimal tree. If it already has our edge, we are done. If not, adding our edge closes a cycle. The cycle starts on one side, crosses to the other on e, and must come back, so it crosses the cut a second time on some other edge f. Swap f out and e in. The result still has V minus one edges and is still connected, so it is a spanning tree, and it is no heavier because e was the lightest crossing edge. For the stronger form, start from an MST T that contains the chosen set A, and use a cut that no edge of A crosses; then f is not in A, so the new tree still contains all of A, plus e.

---
# The cycle property

- Take any cycle, and let $f$ be its unique heaviest edge.
- Then no MST contains $f$.
- Proof: suppose MST $T$ contains $f$. Remove $f$: $T$ splits into two parts, a cut.
- The rest of the cycle joins those parts, so some other cycle edge $g$ crosses that cut, with $w(g)<w(f)$.
- $T - f + g$ is a lighter spanning tree: contradiction.
- Example: cycle A-B-C has heaviest edge B-C 5, so B-C is in no MST.

> The cycle property is the mirror image of the cut property. The cut property tells us which edges we may add; the cycle property tells us which edges we may throw away. The proof is the same kind of swap. Removing the heaviest cycle edge from a tree leaves two parts, and the remainder of the cycle must jump between them somewhere, on an edge that is strictly lighter. In our graph the triangle A, B, C has weights four, two and five, so B to C can never be in a minimum spanning tree. Watch for exactly that rejection when we trace Kruskal.

---
# Distinct weights give one MST

- Suppose $T_1\ne T_2$ are both MSTs and all weights are distinct.
- Let $e$ be the lightest edge in exactly one of them, say in $T_1$.
- $T_2+e$ has a cycle; it holds an edge $f\notin T_1$, so $f\in T_2$ only.
- $e$ is the lightest edge in exactly one tree, so $w(e)<w(f)$.
- $T_2+e-f$ is lighter than $T_2$: contradiction. The MST is unique.

> Here is why every weight in our graph is different. Suppose two different minimum spanning trees existed. Look at the edges that belong to one tree but not the other, and pick the lightest of them; call it e, and say it lies in the first tree. Adding e to the second tree makes a cycle, and that cycle cannot lie entirely inside the first tree, because the first tree has no cycles. So some cycle edge f is in the second tree only. By our choice of e, f is heavier. Swapping gives a lighter tree than an optimal one, which is impossible.

---
# Ties allow several MSTs

```diagram
@dir LR
@reveal all
P -- Q : 1
Q -- R : 1
P -- R : 1
```

- Every two of the three edges form a spanning tree of weight 2.
- So this triangle has three MSTs; our checks count exactly 3.
- All MSTs of a graph share the same total weight.
- Kruskal and Prim may return different trees; totals agree.

> When weights tie, uniqueness fails. On this triangle with three edges of weight one, any two edges form a spanning tree, and all three trees weigh two. The brute-force check in our Java code enumerates every spanning tree and finds exactly three of minimum weight. What never changes is the minimum total. That is why our random tests compare total weights, and compare edge sets only on graphs with distinct weights.

---
# One generic greedy method

```algorithm
function GENERIC-MST(G):
  A ← empty set                  // invariant: A ⊆ some MST
  while A has fewer than V - 1 edges do
    pick a cut that no edge of A crosses
    add a light edge of that cut to A   // safe by the cut property
  return A
```

- Kruskal and Prim are this loop with two different ways to choose the cut.

> Both algorithms we study are this one loop. The invariant is that the edges chosen so far belong to some minimum spanning tree. The cut property says that adding a light edge of any cut that avoids our chosen edges keeps the invariant. After V minus one additions we hold a spanning tree inside a minimum spanning tree, so it is one. Kruskal's cut separates one growing tree from the rest; Prim's cut separates the single tree it grows from all other vertices.

---
# Quiz: which edge is forced?

```quiz
In the running graph, which edge must belong to every MST?
- [ ] C-D 8
- [x] A-C 2
- [ ] B-D 10
- [ ] E-F 9
```

> A to C is the lightest edge in the whole graph. Take the cut that puts A alone on one side: the crossing edges are A to B four and A to C two, so A to C is the light edge, and with distinct weights every minimum spanning tree contains it. In fact the globally lightest edge is always forced, because it is the light edge of any cut it crosses. C to D eight is the heaviest edge on the cycle C, D, E, since C to E is three and D to E is seven, so the cycle property excludes it. B to D ten is the heaviest edge on the cycle B, D, C, whose other edges are eight and five. E to F nine is the heaviest on the cycle D, E, F, whose other edges are seven and six.

---
@type section
# Kruskal's algorithm

---
# Kruskal in one idea

- Start with $V$ one-vertex trees (a forest).
- Look at edges from lightest to heaviest.
- If an edge joins two different trees, accept it: the trees merge.
- If both ends are already in one tree, reject it: it would close a cycle.
- Stop after $V-1$ accepted edges.

> Kruskal never cares where an edge is. It sorts all edges by weight and walks down the list. The only question it asks about each edge is whether its two ends are already connected by edges accepted so far. If not, the edge is the light edge of the cut between its endpoint's tree and everything else, so the cut property says it is safe. If they are connected, the edge would close a cycle and is the heaviest edge on it, so the cycle property says to drop it.

---
# Kruskal in pseudocode

```algorithm
function KRUSKAL(G):
  T ← empty list
  make a one-vertex set for every vertex    // union-find
  for each edge (u, v) in order of increasing weight do
    if len(T) = V - 1 then stop               // tree complete
    if FIND(u) ≠ FIND(v) then                 // different trees
      append (u, v) to T
      UNION(u, v)
  return T
```

> Read the loop body as one yes-or-no test. FIND returns a name for the tree that currently holds a vertex. If u and v have different names, the edge joins two trees: we keep it and merge the two sets with UNION. Otherwise we skip it. The early stop is optional for correctness, since every later edge would be rejected anyway, but it saves work. On a disconnected graph the loop simply runs out of edges and returns a spanning forest.

---
# Kruskal trace

```diagram
@dir LR
@reveal manual
A ; B ; C ; D ; E ; F
focus A B C D E F
--- Six one-vertex trees. No edges yet.
A -- C : 2
focus A C
--- A-C 2 joins two trees. Accept.
C -- E : 3
focus C E
--- C-E 3 joins tree {A, C} and {E}. Accept.
A -- B : 4
focus A B
--- A-B 4 joins {A, C, E} and {B}. Accept.
focus B C
--- B-C 5: B and C are already in one tree. Reject. Three trees so far: {A, B, C, E}, {D} and {F}.
D -- F : 6
focus D F
--- D-F 6 joins {D} and {F}. Accept.
D -- E : 7
focus D E
--- D-E 7 joins the two remaining trees. Accept. Five edges: done, total 2+3+4+6+7 = 22.
```

- Sorted: A-C 2, C-E 3, A-B 4, B-C 5, D-F 6, D-E 7, then C-D 8, E-F 9, B-D 10, never examined.

> Every accept and reject here is copied from the trace log our Kruskal method printed. Follow the forest grow. A to C merges two single vertices, C to E attaches E, and A to B attaches B, so A, B, C and E form one tree. The next edge, B to C with weight five, has both ends in that tree already, so it would close the cycle A, B, C and is rejected; that is the cycle property, since B to C is the heaviest edge on that triangle. D to F joins two single vertices, and D to E connects that pair to the big tree. Now we have five edges on six vertices, so the loop stops; the last three edges would all have been rejected. The minimum spanning tree weighs twenty-two.

---
# Why Kruskal is correct

- Invariant: accepted edges lie inside some MST.
- An accepted edge $(u,v)$ joins tree $T_u$ to a different tree.
- Cut: $T_u$'s vertices versus all the rest. No accepted edge crosses it.
- Every lighter edge was already seen, and it lies inside one tree, so none crosses this cut: $(u,v)$ is a light edge.
- Cut property: accepting it keeps the invariant.

> The proof is the generic method with a specific cut. When Kruskal accepts u to v, put the tree holding u on one side and everything else on the other. No accepted edge crosses this cut, because accepted edges live inside trees. Any lighter edge crossing it would have been examined earlier and would have joined two trees then, so its ends would now sit in the same tree, contradicting that it crosses. So u to v is the lightest crossing edge, and the cut property applies.

---
@type section
# Union-find

---
# What Kruskal needs

- A collection of **disjoint sets** over the vertices.
- `find(x)`: the name (representative) of the set holding `x`.
- `union(a, b)`: merge the sets holding `a` and `b`.
- Test "same tree?" as `find(u) == find(v)`.
- Checking with BFS each time would cost $O(V)$ per edge.

> Kruskal's only question is whether two vertices are in the same tree. We could answer it by running a BFS inside the accepted edges each time, but that is linear per edge and quadratic overall on sparse graphs. Union-find, also called a disjoint-set structure, keeps the sets directly. Each set has one representative vertex that serves as its name, and two vertices are connected exactly when they share a representative.

---
# Sets as a forest of parent links

- Each vertex stores a `parent`. A root is its own parent.
- The root of a vertex's tree is its set's representative.
- `find` follows parent links up to the root.
- `union` makes one root the parent of the other.
- These trees are inside the data structure; they are not the MST.

```diagram
@dir BT
@reveal all
C -> A
E -> A
B -> A
F -> D
```

> Here is the state of our union-find right before Kruskal's last merge, as our Java checks assert it: C, E and B point to A, and F points to D. Two roots, so two sets. Careful: these arrows are bookkeeping, not graph edges. E points to A even though the graph has no edge between E and A. The union-find trees only record who shares a set.

---
# Parent array during the Kruskal run

| After | A | B | C | D | E | F | Sets |
|---|---|---|---|---|---|---|---|
| start | A | B | C | D | E | F | 6 |
| union A,C | A | B | A | D | E | F | 5 |
| union C,E | A | B | A | D | A | F | 4 |
| union A,B | A | A | A | D | A | F | 3 |
| B,C refused | A | A | A | D | A | F | 3 |
| union D,F | A | A | A | D | A | D | 2 |
| union D,E | D | A | A | D | A | D | 1 |

> Each row is the parent array after one call; our Java checks assert every row, all six entries. The union of C and E finds root A for C, so E goes under A. When B and C are tested, both finds return A, the union refuses, and nothing changes. In the last union, the roots are D and A, both of rank one, so the tie goes to D and A moves under D. The ranks come next.

---
# Union by rank

- Each root keeps a **rank**: an upper bound on its tree's height.
- Union puts the lower-rank root under the higher-rank root.
- Equal ranks: pick one as the new root and add 1 to its rank.
- A root of rank $r$ has at least $2^r$ nodes, so rank is at most $\log_2 n$.
- So `find` costs $O(\log n)$ even without path compression.

> If we always hung one root under the other at random, a bad sequence of unions could build a chain, and find would cost linear time. Union by rank prevents that. A tree's rank only grows when two trees of equal rank merge, which at least doubles the node count behind that rank. So rank r needs at least two to the r nodes, and with n nodes no rank exceeds log base two of n. Union by size, which hangs the smaller tree under the larger, gives the same logarithmic bound.

---
# Path compression

- During `find(x)`, point every node on the path straight at the root.
- The first `find` pays for the walk; later ones are short.
- It changes only parents, never which set a node is in.
- Ranks are not updated, so a rank becomes only an upper bound on height.

```diagram
@dir LR
@reveal manual
N3[3] -> N2[2] -> N0[0]
focus N3 N2 N0
--- Before: 3 points to 2, and 2 points to root 0.
N3 -> N0 : after find(3)
focus N3 N0
--- After find(3), 3 points straight at 0.
```

> This example comes from our checks: after three unions, vertex three hangs under two, which hangs under root zero. Calling find on three walks up to zero and then rewrites three's parent to zero, so the next find on three takes one step. The set membership is unchanged; only the shape of the bookkeeping tree gets flatter. That is why we can compress freely without breaking anything.

---
# UnionFind in Java

```java
public int find(int x) {
    if (parent[x] != x) parent[x] = find(parent[x]);
    return parent[x];
}

public boolean union(int a, int b) {
    int ra = find(a), rb = find(b);
    if (ra == rb) return false;
    if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
    parent[rb] = ra;                     // lower rank goes under
    if (rank[ra] == rank[rb]) rank[ra]++;
    sets--;
    return true;
}
```

> Look at the one line of find: the recursive call returns the root, and the assignment stores it as the parent on the way back, which is path compression. In union, the swap makes ra the root of higher rank, then rb goes under it. The method returns false when both elements already share a root, and Kruskal uses that return value directly as its accept-or-reject test. Recursion depth is at most the rank bound, logarithmic, so the stack is not a concern here.

---
# Union-find costs

- Union by rank alone: `find` is $O(\log n)$, and $\Theta(\log n)$ in the worst case.
- Union by rank plus path compression: $O(\alpha(n))$ **amortized** per operation.
- CLRS form: $m$ operations, $n$ of them MAKE-SET, take $O(m\,\alpha(n))$ time.
- $\alpha$ is the inverse Ackermann function; $\alpha(n)\le 4$ for any practical $n$.
- Space: two arrays, $\Theta(n)$.

> With union by rank alone, the rank bound makes every find logarithmic, and a chain of equal-rank unions really does build a path that long, so the worst case is Theta of log n. Adding path compression gives the famous result: m operations, n of which create the singleton sets, take order m times alpha of n time in total. Alpha is the inverse of a function that grows explosively, so alpha itself barely moves; for any input you could store on a real computer it is at most four. Two precise points. The bound is amortized: it holds for the whole sequence, while one unlucky find can still walk a logarithmic path. And it is an upper bound, stated with Big-O; we will treat it as a constant in Kruskal's analysis.

---
# Kruskal in Java

```java
List<Edge> sorted = new ArrayList<>(g.edges());
sorted.sort(null);                   // natural order: by weight
UnionFind sets = new UnionFind(g.vertexCount());
List<Edge> tree = new ArrayList<>();
for (Edge e : sorted) {
    if (tree.size() == g.vertexCount() - 1) break;   // tree complete
    if (sets.union(e.u(), e.v())) {
        tree.add(e);
        log.add("accept " + e);
    } else {
        log.add("reject " + e + " (cycle)");
    }
}
return tree;
```

> Edge is a record that implements Comparable by weight, breaking ties by endpoints so runs are repeatable, which is why sort with null uses the natural order. The whole algorithm is the for loop: union both finds and merges, and its boolean answer decides accept or reject. The log list is how we produced the trace on the earlier slides. Everything else is the setup of a sorted copy of the edge list and an empty result.

---
# Kruskal's running time

- Sort the edges: $O(E\log E)$ comparisons.
- Up to $E$ unions, each with two finds: $O(E\,\alpha(V))$.
- $E\le V^2$, so $\log E\le 2\log V$: $O(E\log E)=O(E\log V)$.
- Total: $O(E\log V)$, dominated by sorting.
- Extra space: $\Theta(V+E)$ for the sorted copy and the sets.

$$O(E\log E + E\,\alpha(V)) = O(E\log V)$$

> Sorting dominates. The union-find part is almost linear in E. The rewrite from log E to log V uses a simple fact: a simple graph has fewer than V squared edges, so log E is at most two log V, and constant factors vanish inside Big-O. If the edges already arrive sorted, or the weights are small integers that radix sort from lecture five can sort in linear time, the union-find part becomes the bottleneck and Kruskal is nearly linear.

---
# Quiz: Kruskal's decision

```quiz
Kruskal has accepted A-C, C-E and A-B. The next edge is B-C 5. What happens?
- [ ] Accept: it is the lightest remaining edge
- [x] Reject: find(B) equals find(C)
- [ ] Accept, then remove A-B to break the cycle
- [ ] Stop: the tree is complete
```

> B and C are already in one set, because A to C put C with A and A to B put B with A. So both finds return the same root and the edge is rejected. Being the lightest remaining edge is not enough; it must also join two different trees. Kruskal never removes an accepted edge, which is the "never undo" part of being greedy. And the tree is not complete, because only three of the five needed edges are in.

---
@type section
# Prim's algorithm

---
# Prim in one idea

- Grow **one** tree, starting from any vertex $s$.
- Keep a min-priority queue of edges leaving the tree.
- Repeatedly take the lightest such edge and add its new vertex.
- **Lazy** version: old edges stay in the queue; skip them when both ends are in the tree.
- Stop after $V-1$ edges.

> Prim looks more like Dijkstra than like Kruskal. There is always exactly one tree, and at each step it adds the cheapest edge that reaches a new vertex. The priority queue holds candidate edges. When a vertex joins the tree, some edges in the queue stop crossing, because now both of their ends are inside. The lazy version does not hunt for those edges to delete them; it leaves them in the queue and throws each away when it reaches the front.

---
# Lazy Prim in pseudocode

```algorithm
function LAZY-PRIM(G, s):
  T ← empty list; mark s; push every edge of s   // min-queue by weight
  while queue not empty and len(T) < V - 1 do
    (u, v) ← pop the lightest edge
    if u and v are both marked then continue    // stale edge
    x ← the unmarked end of (u, v)
    append (u, v) to T; mark x
    push every edge (x, y) with y unmarked
  return T
```

> The loop pops the cheapest candidate. If both ends are already marked, the edge no longer crosses the cut between the tree and the rest, so we skip it. Otherwise exactly one end is new: we record the edge, mark the new vertex, and push its edges to unmarked neighbours. Pushing only edges to unmarked vertices is a small saving; stale edges can still appear because a neighbour may be marked after its edge was pushed.

---
# Prim trace, start at A

| Step | Pop | Action | Queue after, lightest first |
|---|---|---|---|
| start | | mark A | A-C 2, A-B 4 |
| 1 | A-C 2 | add, reach C | C-E 3, A-B 4, B-C 5, C-D 8 |
| 2 | C-E 3 | add, reach E | A-B 4, B-C 5, D-E 7, C-D 8, E-F 9 |
| 3 | A-B 4 | add, reach B | B-C 5, D-E 7, C-D 8, E-F 9, B-D 10 |
| 4 | B-C 5 | skip: both ends in tree | D-E 7, C-D 8, E-F 9, B-D 10 |
| 5 | D-E 7 | add, reach D | D-F 6, C-D 8, E-F 9, B-D 10 |
| 6 | D-F 6 | add, reach F | C-D 8, E-F 9, B-D 10 |

- Five edges: A-C, C-E, A-B, D-E, D-F. Total $22$, the same tree as Kruskal.

> This table is the trace printed by our Java code, with the queue shown lightest first. A to C wins first and brings in C, whose edges join the queue. C to E brings in E, and A to B brings in B. Now B to C sits in the queue with both ends in the tree: it is stale, and step four skips it. D to E brings in D, and D's edge to F enters the queue as the new lightest edge, even though it is lighter than the edge we just popped; it only became a candidate now. D to F brings in F, and three stale edges are never popped. The distinct weights guarantee the same edge set as Kruskal's; only the order differs.

---
# The tree Prim grew

```diagram
@dir LR
@reveal manual
A.blue
focus A
--- The tree starts as A alone.
A -- C : 2
focus A C
--- Step 1: the cut {A} versus the rest. Light edge A-C 2.
C -- E : 3
focus C E
--- Step 2: cut {A, C}. Light edge C-E 3.
A -- B : 4
focus A B
--- Step 3: cut {A, C, E}. Light edge A-B 4.
E -- D : 7
focus E D
--- Step 5: cut {A, B, C, E}. Light edge D-E 7.
D -- F : 6
focus D F
--- Step 6: cut is F alone. Light edge D-F 6.
```

> Every addition is a light edge of the cut between the current tree and the other vertices, which is exactly what the cut property needs. Read the cuts in the captions: first A alone, then A and C, and so on. Step four is missing from the picture because it only discarded a stale edge. The final picture matches the tree Kruskal built, though the two algorithms added the edges in different orders.

---
# Lazy Prim in Java

```java
visit(g, start, inTree, pq);
log.add("start " + Graph.name(start) + "; queue " + sorted(pq));
while (!pq.isEmpty() && tree.size() < g.vertexCount() - 1) {
    Edge e = pq.poll();              // lightest edge in the queue
    if (inTree[e.u()] && inTree[e.v()]) {
        log.add("skip " + e + " (both ends in tree)");
        continue;                    // stale: no longer crossing
    }
    int next = inTree[e.u()] ? e.v() : e.u();
    tree.add(e);
    log.add("add " + e + ", reach " + Graph.name(next));
    visit(g, next, inTree, pq);
    log.add("queue " + sorted(pq));
}
```

> The queue is a java.util.PriorityQueue of Edge, ordered by the same compareTo that Kruskal sorts with. The stale test is one line: both ends already in the tree. The next vertex is whichever end is not yet in the tree. The log lines are there only to produce the trace tables; remove them and the loop body is five statements.

---
# Visiting a vertex

```java
private static void visit(Graph g, int v, boolean[] inTree,
        PriorityQueue<Edge> pq) {
    inTree[v] = true;
    for (Edge e : g.adjacent(v)) {
        if (!inTree[e.other(v)]) pq.add(e);
    }
}
```

- Each edge is pushed at most once: by whichever end joins the tree first.
- `inTree` is the "marked" flag from the pseudocode.

> Visit marks the vertex and pushes every edge that leads to a vertex outside the tree. An edge is pushed from its first endpoint to join the tree; by the time its other endpoint is visited, the first endpoint is marked, so the edge is not pushed again. That bounds the queue size by E. The method reads adjacency lists, which is why Prim wants that representation, while Kruskal only needs the edge list.

---
# Why Prim is correct, and its cost

- Each added edge is a light edge of the cut (tree, rest): cut property.
- Stale edges do not cross that cut, so skipping them is safe.
- Lazy Prim: at most $E$ pushes and pops, each $O(\log E)$ with a binary heap.
- Total $O(E\log E)=O(E\log V)$ time; $O(E)$ extra space for the queue.
- Eager Prim keeps one entry per vertex with decrease-key: also $O(E\log V)$ with a binary heap, queue size $O(V)$.

> Correctness is the cut property with the cut separating the current tree from everything else; no chosen edge crosses it, because all chosen edges are inside the tree. The popped edge is the lightest crossing edge once stale ones are discarded, since every crossing edge was pushed when its tree end joined. For cost, a binary heap does push and pop in logarithmic time, and there are at most E of each. The eager version, like the Dijkstra you saw, keeps only the best edge per outside vertex; with a Fibonacci heap it reaches O of E plus V log V, a known result.

---
@type section
# Choosing and comparing

---
# Kruskal versus Prim

| | Kruskal | Prim |
|---|---|---|
| Grows | a forest that merges | one tree |
| Needs | an edge list | adjacency lists |
| Extra structure | sort plus union-find | binary-heap priority queue (lazy or eager) |
| Time | $O(E\log V)$ | $O(E\log V)$ |
| Disconnected input | spanning forest | tree of the start's component |
| Tends to suit | sparse graphs, pre-sorted edges | dense graphs (eager version) |

> Both are O of E log V on connected graphs, so the choice is practical. Kruskal is simple when edges come as a list, and it is excellent when they are already sorted. Prim fits naturally when the graph is stored as adjacency lists; the Prim column covers both the lazy version we traced and the eager one. On dense graphs, where E is close to V squared, the eager Prim with an adjacency matrix and a plain array instead of a heap runs in V squared time, which beats E log V. On a disconnected graph the two differ: our Kruskal returns a spanning forest, while Prim covers only the start's component.

---
# An MST is not a shortest-path tree

- Shortest-path tree from A (Dijkstra, L17): A-B, A-C, C-D, C-E, E-F.
- Its weight is $4+2+8+3+9=26$; the MST weighs $22$.
- In the MST, A to D costs $2+3+7=12$; the shortest path is A-C-D, $10$.
- MST: cheapest total wiring. SPT: cheapest route from one source.
- Our checks compute both trees on the running graph and confirm they differ.

```diagram
@dir LR
@reveal all
A -- B : 4
A -- C : 2
C -- D : 8
C -- E : 3
E -- F : 9
```

> This is the shortest-path tree from A, computed by the Dijkstra method in our Java package. It keeps C to D eight because it gives D a distance of ten, while the MST route from A to D goes through E and costs twelve. It keeps E to F nine because F is at distance fourteen that way, versus sixteen through D. The MST does not care about distances from any particular vertex; it minimizes the sum of all edge weights. So when someone asks for the cheapest network, use an MST; when they ask for the fastest route from a hub, use shortest paths.

---
# Common mistakes and edge cases

- **Disconnected graph**: there is no spanning tree; expect a forest.
- **Directed graph**: MST is defined for undirected graphs; the directed version needs different algorithms.
- **Ties**: several MSTs; compare totals, not edge sets.
- **Parallel edges and self-loops**: the lighter parallel edge wins; a self-loop is always rejected.
- **One vertex**: the MST is empty, with $V-1=0$ edges.
- Kruskal: forgetting the cycle test. Prim: forgetting stale edges.

> Each of these has a check in our Java file. A four-vertex graph in two pieces gives Kruskal two edges and Prim one. A two-vertex graph with parallel edges of weights five and two plus a self-loop gives total two for both algorithms. The equal-weight triangle has three minimum trees. The most common coding mistake in Kruskal is adding the lightest remaining edge without asking union-find; in lazy Prim it is forgetting that an edge popped from the queue may no longer cross the cut.

---
# In the Java library

- There is no MST class in `java.util`; you build it from parts.
- `PriorityQueue` is based on a priority heap (a binary heap in current OpenJDK); its implementation note gives $O(\log n)$ `offer` and `poll`.
- `List.sort`: guaranteed stable; `sort(null)` uses natural order.
- A record such as `Edge` gets `equals` and `hashCode` for free, so edge sets compare by value.

> The Java standard library gives us the ingredients but not the algorithm. The PriorityQueue documentation says it is based on a priority heap, and its implementation note gives logarithmic time for offer and poll, which is exactly what Prim's bound assumes; in current OpenJDK that heap is a binary heap stored in an array. List sort is guaranteed stable by its documentation. Because Edge is a record, two edges with the same fields are equal, which is how our tests compare the Kruskal and Prim trees as sets.

---
# Watch them run

- [Kruskal visualization](../../../visualizations/algorithms/kruskal.html): edges in sorted order, cycle tests shown.
- [Prim visualization](../../../visualizations/algorithms/prim.html): one tree growing; eager version, a priority queue of vertices keyed by their lightest crossing edge.
- While watching, name the cut that justifies each accepted edge.

> These two visualizations animate the algorithms on their own graphs. The Prim animation is the eager version: instead of our lazy queue of edges with stale entries, it keeps one key per outside vertex and lowers it when a lighter crossing edge appears. Open them side by side after class. For each accepted edge, say out loud which cut makes it a light edge: for Kruskal, the tree of one endpoint against everything else; for Prim, the current tree against the rest. If you can do that for every step, you understand why both algorithms are correct.

---
# Summary

- A spanning tree connects all $V$ vertices with $V-1$ edges and no cycle.
- Cut property: a light crossing edge is safe. Cycle property: a unique heaviest cycle edge is never needed.
- Kruskal: sorted edges plus union-find, $O(E\log V)$.
- Prim: one tree plus a priority queue, $O(E\log V)$ with a binary heap.
- Both gave total 22 on our graph; the shortest-path tree from A weighed 26.
- Next, L19: when does a greedy choice work in general?

> Everything today follows from two exchange arguments. The cut property lets us add edges greedily; the cycle property lets us discard edges greedily. Kruskal and Prim are two schedules for applying the cut property, and union-find and the heap are what make each schedule fast. Next time we step back and ask when making the locally best choice works in general, and when it fails.

---
# Check yourself

- Add 100 to every edge weight. Does the MST change? Why?
- Give a graph where Kruskal and Prim return different trees of the same weight.
- Why does lazy Prim's queue hold up to $E$ edges, while eager Prim's holds at most $V$ vertices?

> Try each question without the slides. For the first, count how many edges every spanning tree has. For the second, you will need a tie. For the third, think about what each queue stores: lazy Prim stores edges and never deletes stale ones, while eager Prim stores one entry per outside vertex and lowers its key.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 19 (disjoint sets) and Chapter 21 (minimum spanning trees).
- J. B. Kruskal, "On the shortest spanning subtree of a graph and the traveling salesman problem", *Proceedings of the American Mathematical Society* 7(1), 1956, 48–50.
- R. C. Prim, "Shortest connection networks and some generalizations", *Bell System Technical Journal* 36(6), 1957, 1389–1401.
- Java SE API documentation: `java.util.PriorityQueue`, `java.util.List`.

> The definitions, the cut property and the analysis follow CLRS chapters nineteen and twenty-one; the proofs on these slides are written in our own words. Kruskal's and Prim's original papers are listed for history. Library facts come from the Java SE API documentation. The running graph, the traces and all numbers were produced by the Java code for this lecture.
