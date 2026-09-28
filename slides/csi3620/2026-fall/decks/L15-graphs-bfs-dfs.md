@title Lecture 15: Graphs, BFS and DFS
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Graphs, BFS and DFS
## Representations and the two basic searches

---
# Where we are

- L08 to L11: trees, a special kind of graph with no cycles.
- L09: tree traversals visited every node in a fixed pattern.
- L14: heaps; they return in L17 for shortest paths.
- Today: general graphs, how to store them, and how to search them.
- Next: L16 topological sort and cycle detection, built on DFS.

> Every tree we have drawn this semester was already a graph: nodes joined by edges, with one path between any two nodes. Today we drop the restrictions. A graph can have cycles, many paths between two vertices, pieces that are not connected at all, and edges with a direction. The traversal ideas from lecture nine carry over, with one new problem: because of cycles, a search can come back to a vertex it has already seen, so it must remember where it has been. The next four lectures all build on the two searches we meet today.

---
# By the end of today you can

- Use graph vocabulary precisely: degree, path, cycle, component, DAG.
- Store a graph as a matrix, adjacency lists or an edge list, and state the costs.
- Trace BFS with its queue, and read shortest paths from parent pointers.
- Trace DFS, recursive and iterative, with discovery and finish times.
- Find connected components with repeated searches.
- Explain the common BFS and DFS bugs and how to avoid them.

> These outcomes are things you should be able to do with a pencil and with Java. The tracing outcomes matter most. If you can keep the queue or the stack, the visited marks and the distance array straight on paper for a small graph, the code is a direct transcription. We will also see that a search's answer depends on the order of the neighbor lists, which is why we fix that order for the whole lecture.

---
# The problem: fewest hops

- A subway map: stations joined by direct lines.
- Question: the fewest stops from station A to station B?
- Question: can every station reach every other one?
- A course catalog: which courses must come before which?
- One model answers all of these: a **graph**.

> Here are questions that do not fit any structure we have so far. On a subway map, what is the route with the fewest stops? Is the network in one piece, or are some stations cut off? In a course catalog, can prerequisites be satisfied in some order, or is there a circular requirement? Each question is about things and connections between them. Lists, trees and hash tables do not capture arbitrary connections. A graph does, and breadth-first and depth-first search answer the first two questions today; the third is lecture sixteen.

---
@type section
# Vocabulary

---
# Graphs, edges and weights

- A **graph** $G = (V, E)$: a set of **vertices** $V$ and a set of **edges** $E$.
- **Undirected** edge $\{u, v\}$: a two-way link, written `u - v`.
- **Directed** edge $(u, v)$: a one-way link from `u` to `v`, written `u -> v`.
- **Weighted** graph: each edge carries a number $w(u, v)$, such as a length.
- We write $V$ and $E$ for the counts $|V|$ and $|E|$ inside $O(\cdot)$.

> A graph is just two sets: vertices, sometimes called nodes, and edges, each joining two vertices. In an undirected graph an edge has no direction; friendship is the usual example. In a directed graph, each edge points from one vertex to another, like a one-way street or a web link. A weighted graph attaches a number to each edge, such as a distance or a cost; today's searches ignore weights, and lecture seventeen uses them. When we write big-O bounds we follow CLRS and write V and E for the numbers of vertices and edges.

---
# Degree

- **Degree** of `v` (undirected): the number of edges touching `v`.
- **Out-degree**: edges leaving `v`. **In-degree**: edges entering `v`.
- In an undirected graph, the degrees sum to $2E$.
- Each edge adds 1 to the degree of each of its two ends.
- In a directed graph, in-degrees sum to $E$, and so do out-degrees.

> Degree counts connections. In an undirected graph, the degree of a vertex is how many edges touch it. In a directed graph we split it: out-degree counts edges leaving and in-degree counts edges arriving. The sum of all degrees in an undirected graph is exactly twice the number of edges, because every edge is counted once at each end; this is sometimes called the handshake lemma, and it is the reason BFS on adjacency lists costs O of V plus E. A self-loop, an edge from a vertex to itself, is the one exception to watch: by the usual convention it adds two to that vertex's degree.

---
# Paths and cycles

- A **path**: a sequence of vertices, each joined to the next by an edge.
- Its **length** is the number of edges, not vertices.
- A **simple path** repeats no vertex.
- A **cycle**: a path of length at least 1 that returns to its start.
- In a simple undirected graph, a cycle needs at least 3 distinct vertices.

> A path walks from vertex to vertex along edges. Its length counts edges, so a single vertex is a path of length zero. A simple path never visits a vertex twice. A cycle is a path that ends where it started; in a directed graph, a single edge from a vertex to itself is a cycle of length one, and two opposite edges form a cycle of length two. In a simple undirected graph we do not count walking along one edge and straight back, so a cycle needs at least three distinct vertices. Textbooks differ on some of these details, so when a problem statement matters, check its definitions.

---
# Connected, components, DAGs

- An undirected graph is **connected** if every pair has a path between them.
- A **connected component** is a maximal connected piece.
- A **DAG** is a directed acyclic graph: directed, with no directed cycle.
- A **tree** is a connected undirected graph with no cycle.
- Directed graphs have a stronger notion, strongly connected components.

> Connected means one piece: from any vertex you can reach any other. When a graph is not connected, it splits into connected components, each one as large as possible, and every vertex belongs to exactly one component. A directed acyclic graph, or DAG, is a directed graph with no directed cycle; prerequisite charts should be DAGs, because a cycle would mean no course could be taken first. Trees from earlier lectures are connected undirected graphs with no cycles. For directed graphs, connectivity is subtler, and strongly connected components are a topic beyond this lecture.

---
# Sparse and dense

- A **simple** graph has no self-loops and no repeated edges.
- Simple undirected: $E \le V(V-1)/2$, one edge per pair.
- Simple directed: $E \le V(V-1)$, one edge per ordered pair.
- **Dense**: $E$ close to $V^2$. **Sparse**: $E$ much smaller, often $O(V)$.
- Road maps and social links are usually sparse.

> How many edges can a graph have? In a simple undirected graph each pair of distinct vertices has at most one edge, and there are V times V minus one over two pairs. So E is at most on the order of V squared. A graph near that bound is dense; one with far fewer edges is sparse. Most graphs from the real world are sparse: an intersection meets a handful of roads, not every other intersection in the country. That fact drives the choice of representation on the next slides, because some representations pay for every possible edge, not just the real ones.

---
# Modeling with graphs

| Situation | Vertices | Edges | Kind |
|---|---|---|---|
| Road map | Intersections | Road segments | Weighted by length; directed where one-way |
| Prerequisites | Courses | "Take `u` before `v`" | Directed; should be a DAG |
| Social network | People | Friendships, or follows | Undirected, or directed for follows |
| Web | Pages | Hyperlinks | Directed |

> Modeling is the first step of every graph problem: decide what the vertices are, what the edges are, and whether direction and weights matter. A road network needs weights for lengths and direction for one-way streets. A prerequisite chart is directed, and a cycle in it would be a mistake in the catalog. Friendship is mutual, so undirected, but following someone on a social platform is not, so directed. Web links are directed. The same search algorithms then apply to all of them.

---
# The running example

```diagram
@dir LR
V0((0)) -- V1((1))
V0 -- V2((2))
V1 -- V3((3))
V2 -- V3
V2 -- V4((4))
V3 -- V5((5))
V4 -- V5
V6((6)) -- V7((7))
```

- Undirected, 8 vertices, 8 edges: two connected components.
- Neighbor lists in increasing order: `2: [0, 3, 4]`, `3: [1, 2, 5]`.

> This graph is our running example for the whole lecture. Vertices zero through five form one component, containing the cycle zero, one, three, two, back to zero, and the cycle two, three, five, four, back to two. Vertices six and seven form a second component with a single edge. We add the edges in the order zero-one, zero-two, one-three, two-three, two-four, three-five, four-five, six-seven, so every neighbor list is in increasing order. Every trace today was produced by running the lecture's Java code on exactly this graph.

---
# Quick check: how many edges?

```quiz
A simple undirected graph has 6 vertices. What is the largest number of edges it can have?
- [ ] 12
- [x] 15
- [ ] 30
- [ ] 36
```

> The answer is fifteen. Each edge joins one unordered pair of distinct vertices, and there are six times five over two, which is fifteen, such pairs. Thirty is the count of ordered pairs, which is the bound for a simple directed graph, where u to v and v to u are different edges. Thirty-six is six squared: it counts all ordered pairs, including the six self-loops from a vertex to itself. Twelve has no special meaning here.

---
@type section
# Representations

---
# Adjacency matrix

- A $V \times V$ table: `m[u][v]` is true if edge `u - v` exists.
- Undirected: the matrix is symmetric, `m[u][v] == m[v][u]`.
- Weighted: store the weight instead of true.
- Edge check in $\Theta(1)$ time.
- Space $\Theta(V^2)$, even when there are few edges.

> The adjacency matrix is the most direct representation: a two-dimensional boolean array with a row and a column per vertex. Asking whether an edge exists is a single array access. The cost is space: V squared entries whether or not the edges exist. For our eight-vertex example that is sixty-four entries to store eight edges. For a road network with a million intersections it would be a trillion entries, almost all false. Listing the neighbors of one vertex also costs a full row, Theta of V, no matter how few neighbors it has.

---
# Adjacency lists

- An array indexed by vertex; entry `u` is a list of `u`'s neighbors.
- Undirected: edge `u - v` appears twice, in `u`'s list and in `v`'s.
- Directed: `u -> v` appears once, in `u`'s list.
- Space $\Theta(V + E)$.
- Iterating the neighbors of `u` costs $\Theta(\deg(u))$.

> Adjacency lists store only the edges that exist. Each vertex has a list of its neighbors, so the total length of all lists is twice E for an undirected graph, by the handshake lemma, or E for a directed graph. The array itself adds V. That makes space Theta of V plus E, which is ideal for sparse graphs. Iterating over a vertex's neighbors, the core step of every search, costs only its degree. The price is edge checks: to ask whether u and v are adjacent, we scan u's list.

---
# Edge list

- A plain list of pairs `(u, v)`, or triples with a weight.
- Space $\Theta(E)$.
- Edge check or neighbors of `u`: scan everything, $\Theta(E)$.
- Natural input format: files often list one edge per line.
- Kruskal's algorithm (L18) sorts an edge list by weight.

> An edge list is the simplest format of all: every edge once, in no particular structure. It is how graphs are usually written in files, and our Java example builds the graph from exactly such an array of pairs. It is poor for searching, since finding a vertex's neighbors means scanning every edge. But some algorithms only need to see every edge once in some order. Kruskal's minimum spanning tree algorithm, in lecture eighteen, sorts the edges by weight and processes them in that order, so an edge list is the natural fit.

---
# Representation costs

| | Matrix | Adjacency lists | Edge list |
|---|---|---|---|
| Space | $\Theta(V^2)$ | $\Theta(V + E)$ | $\Theta(E)$ |
| Is `u - v` an edge? | $\Theta(1)$ | $O(\deg(u))$ | $O(E)$ |
| Iterate neighbors of `u` | $\Theta(V)$ | $\Theta(\deg(u))$ | $\Theta(E)$ |
| Iterate all edges | $\Theta(V^2)$ | $\Theta(V + E)$ | $\Theta(E)$ |
| Add an edge | $\Theta(1)$ | $O(1)$ amortized | $O(1)$ amortized |

Default choice: adjacency lists for sparse graphs; a matrix when dense or edge checks dominate.

> Read the table by operation. Searches iterate over neighbors, so adjacency lists win for them: the whole search touches each list once. The matrix wins edge checks and is simple for dense graphs. Adding an edge to a list is amortized constant time when the list is a dynamic array and we do not check for duplicates; checking would cost the degree. Most graph algorithms in this course assume adjacency lists, and their O of V plus E bounds depend on that assumption.

---
# A graph class in Java

```java
public final class Graph {
    private final List<List<Integer>> adj;
    private final boolean directed;
    private int edges;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        this.directed = directed;
        adj = new ArrayList<>(n);
        for (int v = 0; v < n; v++) adj.add(new ArrayList<>());
    }
```

- Vertices are the integers `0` to `n - 1`.

> Our graph class stores adjacency lists as a list of lists of integers, with vertices numbered zero to n minus one. Numbering vertices lets the searches use plain int arrays for marks, distances and parents. If your vertices are names, like station names, keep a HashMap from name to number on the side. The constructor creates one empty list per vertex. The directed flag decides whether addEdge stores one or two entries.

---
# Adding and reading edges

```java
public void addEdge(int u, int v) {
    check(u);
    check(v);
    adj.get(u).add(v);
    if (!directed && u != v) adj.get(v).add(u);
    edges++;
}

public boolean hasEdge(int u, int v) {
    check(u);
    check(v);
    return adj.get(u).contains(v);
}
```

> addEdge validates both vertices, then appends v to u's list, and for an undirected graph also u to v's list. A self-loop is stored once, so it does not appear twice in its own list; that means our degree method counts a self-loop once, not twice as the textbook convention does. We do not check for duplicate edges, which keeps addEdge constant amortized time; if the input might contain duplicates, it is the caller's job. hasEdge shows the weakness of lists: contains scans u's list, which costs the degree of u. The neighbors method, not shown, returns an unmodifiable copy of the list so callers cannot corrupt the graph.

---
@type section
# Breadth-first search

---
# BFS: explore in waves

- Start at a **source** `s`. Visit everything 1 edge away, then 2, then 3.
- A FIFO **queue** (L07) holds discovered vertices waiting to be expanded.
- A **discovered** mark stops a vertex from entering the queue twice.
- `dist[v]`: number of edges on the path BFS found to `v`.
- `parent[v]`: the vertex from which `v` was discovered.

> Breadth-first search explores like a ripple in a pond. First the source, then all its neighbors, then all their new neighbors, and so on, one distance level at a time. The first-in first-out queue from lecture seven enforces that order: vertices found earlier are expanded earlier. Because graphs have cycles, BFS must remember which vertices it has already discovered, or it would loop forever. It also records two arrays: dist, how many edges from the source, and parent, which vertex discovered it. Those two arrays are the output.

---
# BFS in pseudocode

```algorithm
function BFS(G, s):
  for each vertex v do dist[v] ← -1; parent[v] ← -1
  dist[s] ← 0; mark s discovered    // mark when enqueued
  Q ← queue containing s
  while Q is not empty do
    u ← DEQUEUE(Q)
    for each neighbor v of u do
      if v is not discovered then
        mark v discovered
        dist[v] ← dist[u] + 1; parent[v] ← u
        ENQUEUE(Q, v)
```

> Read the loop body: take the vertex at the front of the queue, look at each neighbor, and for each one not yet discovered, mark it, record its distance and parent, and put it at the back of the queue. Minus one in dist means not reached, which is useful when the graph is disconnected. The comment on the third line is important: we mark a vertex at the moment it enters the queue, not when it leaves. We will come back to why at the end of the lecture.

---
# BFS from 0, step by step

```diagram
@dir LR
@reveal manual
V0((0)) -- V1((1))
V0 -- V2((2))
V1 -- V3((3))
V2 -- V3
V2 -- V4((4))
V3 -- V5((5))
V4 -- V5
focus V0
--- Start: 0 is discovered, dist 0. Queue [0].
focus V0 V1 V2
--- Dequeue 0. Discover 1 and 2 at dist 1. Queue [1, 2].
focus V1 V3
--- Dequeue 1. 0 is already marked; discover 3 at dist 2. Queue [2, 3].
focus V2 V3 V4
--- Dequeue 2. 0 and 3 are marked; discover 4 at dist 2. Queue [3, 4].
focus V3 V5
--- Dequeue 3. Discover 5 at dist 3. Queue [4, 5].
focus V4 V5
--- Dequeue 4, then 5. Nothing new. Queue empty: done.
```

> Follow the queue in the captions. Zero goes first and discovers one and two. One discovers three. When two is expanded, three is already marked, because one discovered it a moment ago, so two only discovers four. Three discovers five. When four is expanded, five is already marked. The vertices come out of the queue in order zero, one, two, three, four, five, which is also the order of their distances. Vertices six and seven are never reached, because no path leads there from zero.

---
# The BFS trace as a table

| Step | Dequeue | Newly discovered | Queue after | `dist` of new |
|---|---|---|---|---|
| 0 | | `0` | `[0]` | 0 |
| 1 | `0` | `1, 2` | `[1, 2]` | 1, 1 |
| 2 | `1` | `3` | `[2, 3]` | 2 |
| 3 | `2` | `4` | `[3, 4]` | 2 |
| 4 | `3` | `5` | `[4, 5]` | 3 |
| 5 | `4` | none | `[5]` | |
| 6 | `5` | none | `[]` | |

Final `dist = [0, 1, 1, 2, 2, 3, -1, -1]`, `parent = [-1, 0, 0, 1, 2, 3, -1, -1]`.

> This is the same run as a table, printed by a trace of the Java code. At every step, the queue holds vertices in nondecreasing distance, and at most two distinct distances appear in it at once. That is exactly the property the correctness proof uses. The final arrays show minus one for six and seven: unreachable from zero. Try covering the right-hand columns and reproducing them from the adjacency lists.

---
# BFS in Java

```java
discovered[s] = true;             // mark when enqueued
dist[s] = 0;
queue.add(s);
while (!queue.isEmpty()) {
    int u = queue.remove();
    order.add(u);
    for (int v : g.neighbors(u)) {
        if (!discovered[v]) {
            discovered[v] = true;
            dist[v] = dist[u] + 1;
            parent[v] = u;
            queue.add(v);
        }
    }
}
```

> This is the heart of the Bfs constructor; before it, dist and parent are filled with minus one. The queue is an ArrayDeque used through the Queue interface: add puts at the back, remove takes from the front. Its API documentation says it is likely to be faster than LinkedList when used as a queue; LinkedList also works. The order list records the dequeue order, which is handy for testing and tracing. Notice that the three assignments to discovered, dist and parent all happen together, at the moment v enters the queue.

---
# Why BFS finds shortest paths

- Claim: for every reachable `v`, `dist[v]` is the fewest edges from `s`.
- The queue holds distances in nondecreasing order, at most $d$ and $d+1$.
- So vertices leave the queue in order of distance.
- `v` is discovered from the first dequeued neighbor, one of smallest distance.
- A shortest path's last edge comes from a vertex at distance one less.

> Here is the sketch; CLRS chapter twenty gives the full proof. Suppose the true shortest distance to v is k. Then some neighbor u of v has true distance k minus one. By induction, that u gets the right distance and leaves the queue before any vertex of distance k or more. So v is discovered no later than when u is expanded, with dist equal to k. It cannot get a smaller value, since every discovered path is a real path. This works only because every edge counts as one; with weights, fewest edges is not shortest length, and we need Dijkstra's algorithm.

---
# Reading a path from parent pointers

```java
public List<Integer> pathTo(int v) {
    if (dist[v] == -1) return List.of();
    List<Integer> path = new ArrayList<>();
    for (int x = v; x != -1; x = parent[x]) path.add(x);
    Collections.reverse(path);
    return path;
}
```

- `pathTo(5)` follows 5, 3, 1, 0 and reverses it: `[0, 1, 3, 5]`.
- `0, 2, 4, 5` is also shortest; neighbor order decided which one BFS kept.

> The parent pointers form a tree rooted at the source, the BFS tree. To recover a shortest path to v, walk parent pointers from v back to the source, then reverse the list. For vertex five: its parent is three, three's parent is one, one's parent is zero, and zero's parent is minus one, so we stop. Note that a shortest path is often not unique. Zero, two, four, five also has three edges. BFS kept the path through one because one came before two in zero's neighbor list. The length is guaranteed; which shortest path you get is not.

---
# BFS costs

- Each vertex is enqueued and dequeued at most once: $O(V)$.
- Each adjacency list is scanned once, when its vertex is dequeued.
- Total list length is $2E$ (undirected) or $E$ (directed): $O(E)$.
- Adjacency lists: $O(V + E)$ time.
- Matrix: $O(V^2)$; $\Theta(V^2)$ when every vertex is reached.
- Extra space: $\Theta(V)$ for the marks, `dist`, `parent` and the queue.

> The analysis is aggregate counting, not per-step worst cases. The discovered mark guarantees that each vertex enters the queue at most once, so queue operations total O of V. Each vertex's list is scanned exactly once, when it leaves the queue, so the inner loop runs a total of the sum of the degrees, which is two E. Initializing the arrays costs V. With a matrix, scanning a vertex's neighbors costs a full row, V, for each vertex that is dequeued, so the total is O of V squared, and Theta of V squared when the search reaches every vertex. That is why adjacency lists are the default.

---
# Quick check: the BFS queue

```quiz
BFS from 0 on our graph, marking vertices when they are enqueued. What is the queue right after vertex 2 is dequeued and expanded?
- [x] `[3, 4]`
- [ ] `[3, 3, 4]`
- [ ] `[4, 3]`
- [ ] `[3, 4, 5]`
```

> The answer is three, four. When two is expanded, three is already marked, because one discovered it a step earlier, so two adds only four at the back of the queue. The second option is what you get if you mark vertices only when they are dequeued: three has not left the queue yet, so two enqueues it a second time. We ran that variant too, and it produces exactly three, three, four at this step. The third option would need four to jump ahead of three, which a FIFO queue never allows. The last adds five, which only three discovers, one step later.

---
@type section
# Depth-first search

---
# DFS: go deep, then back up

- From `u`, go to an undiscovered neighbor and continue from there.
- When `u` has no undiscovered neighbors left, **finish** `u` and back up.
- Recursion keeps the path back: the call stack is the DFS path.
- Like a preorder traversal (L09), plus visited marks for cycles.
- Run it from every undiscovered vertex to cover the whole graph.

> Depth-first search explores like a person in a maze who always takes an unexplored corridor and only backs up at a dead end. From the current vertex, pick the first neighbor not yet discovered and move there immediately, before looking at the other neighbors. When a vertex has no undiscovered neighbors left, it is finished, and we return to the vertex we came from. Recursion does the bookkeeping for free. The CLRS version runs this from every vertex in turn, so it visits every vertex, even in a disconnected graph.

---
# DFS with timestamps, in pseudocode

```algorithm
function DFS(G):
  time ← 0
  for each vertex s in increasing order do
    if s is undiscovered then VISIT(s)

function VISIT(u):
  time ← time + 1; d[u] ← time      // discovered
  for each neighbor v of u do
    if v is undiscovered then
      parent[v] ← u; VISIT(v)
  time ← time + 1; f[u] ← time      // finished
```

> A single clock ticks on two kinds of events. When DFS first reaches a vertex, the clock ticks and the time is recorded as its discovery time, d. When all its neighbors have been handled, the clock ticks again and we record its finish time, f. With V vertices there are two V events, so all times fall between one and two V. These timestamps look like bookkeeping, but they carry real information: lecture sixteen uses finish times to produce a topological order.

---
# DFS from 0, step by step

```diagram
@dir LR
@reveal manual
V0((0)) -- V1((1))
V0 -- V2((2))
V1 -- V3((3))
V2 -- V3
V2 -- V4((4))
V3 -- V5((5))
V4 -- V5
focus V0
--- Discover 0 at time 1. Its first neighbor is 1.
focus V0 V1
--- Discover 1 at time 2. Its list is [0, 3]; 0 is marked, so go to 3.
focus V1 V3
--- Discover 3 at time 3. Its list is [1, 2, 5]; 1 is marked, so go to 2.
focus V3 V2
--- Discover 2 at time 4. 0 and 3 are marked; go to 4.
focus V2 V4
--- Discover 4 at time 5. 2 is marked; go to 5.
focus V4 V5
--- Discover 5 at time 6. 3 and 4 are marked: finish 5 at 7, then back up.
```

> Watch how DFS dives. From zero it goes to one, from one to three, and from three, whose first unmarked neighbor is two, it goes to two, even though two is also a neighbor of zero. From two it goes to four and from four to five. At five, both neighbors are already discovered, so five finishes at time seven. Then the recursion unwinds: four finishes at eight, two at nine, three at ten, one at eleven and zero at twelve. The path zero, one, three, two, four, five is the whole DFS tree for this component.

---
# Discovery and finish times

| Vertex | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|---|
| `d` | 1 | 2 | 4 | 3 | 5 | 6 | 13 | 14 |
| `f` | 12 | 11 | 9 | 10 | 8 | 7 | 16 | 15 |
| `parent` | -1 | 0 | 3 | 1 | 2 | 4 | -1 | 6 |

- Discovery order: `0, 1, 3, 2, 4, 5`, then a new root `6`, then `7`.
- Any two intervals are either nested (one vertex is a descendant of the other) or disjoint.

> These values were printed by the Java code. After zero's component is done at time twelve, the outer loop finds six undiscovered, starts a new tree there at thirteen, discovers seven at fourteen, and finishes both. Look at the intervals: zero's interval, one to twelve, contains every other interval in its component, and six's interval, thirteen to sixteen, is disjoint from all of them. CLRS calls this the parenthesis structure: for any two vertices, their intervals are either nested, when one is a descendant of the other, or disjoint. Our tests check this for every pair of vertices on hundreds of random directed and undirected graphs, including that the intervals are nested exactly when one vertex is a descendant of the other in the DFS forest.

---
# Recursive DFS in Java

```java
for (int s = 0; s < n; s++) {
    if (d[s] == 0) visit(s);      // d == 0 means undiscovered
}
// ...
private void visit(int u) {
    d[u] = ++time;                    // discovered
    order.add(u);
    for (int v : g.neighbors(u)) {
        if (d[v] == 0) {
            parent[v] = u;
            visit(v);
        }
    }
    f[u] = ++time;                    // finished: all neighbors explored
}
```

> The top loop is the outer driver in the Dfs constructor: it starts a new search tree from each vertex that is still undiscovered, in increasing order. The discovery time doubles as the visited mark, because times start at one, so zero means not yet discovered. The prefix increment ticks the clock and records the new time in one step. The recursive call happens inside the neighbor loop, which is what makes the search go deep before it goes wide.

---
# Iterative DFS with a stack

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(s);
while (!stack.isEmpty()) {
    int u = stack.pop();
    if (visited[u]) continue;     // a stale copy: already visited
    visited[u] = true;
    order.add(u);
    for (int v : g.neighbors(u)) {
        if (!visited[v]) stack.push(v);
    }
}
```

- Mark when **popped**; a vertex may sit on the stack more than once.

> Replacing recursion with an explicit stack avoids deep call stacks. The structure mirrors BFS with a stack instead of a queue, but with one important change: a vertex is marked when it is popped, not when it is pushed. A vertex can be pushed several times by different neighbors; the later copies are stale and skipped by the continue. Marking on push would not give a depth-first order, because a vertex pushed early could not be pushed again by a deeper vertex that should reach it first. How big can the stack get? Each edge causes at most one push, because once the first of its two endpoints is visited, the other end can no longer push it back. So there are at most E plus one pushes, and the stack holds O of V plus E entries.

---
# Two DFS orders on the same graph

| Pop | Action | Stack after, top first |
|---|---|---|
| `0` | visit; push 1, 2 | `[2, 1]` |
| `2` | visit; push 3, 4 | `[4, 3, 1]` |
| `4` | visit; push 5 | `[5, 3, 1]` |
| `5` | visit; push 3 | `[3, 3, 1]` |
| `3` | visit; push 1 | `[1, 3, 1]` |
| `1` | visit | `[3, 1]` |
| `3`, `1` | stale copies: skip | `[]` |

- Iterative order: `0, 2, 4, 5, 3, 1`. Recursive order: `0, 1, 3, 2, 4, 5`.

> Here is the stack trace from the code. Pushing neighbors in list order means the last neighbor pushed is popped first, so the iterative version explores two before one, the opposite of the recursive version. Both are valid depth-first searches; they just break ties differently. If you need the same order as the recursive version, push each list in reverse, which the lecture code also provides, and our tests confirm that it matches the recursive order on hundreds of random graphs. Never assume two DFS implementations visit vertices in the same order.

---
# Edge classification, a preview

```diagram
@dir LR
V0((0)) ==> V1((1))
V1 ==> V3((3))
V3 ==> V2((2))
V2 ==> V4((4))
V4 ==> V5((5))
V2 ..> V0
V5 ..> V3
```

- **Tree edges** (thick): the edge DFS used to discover a vertex.
- **Back edges** (dotted): to an ancestor still being explored, other than through the tree edge just used (in an undirected graph, skip the parent).
- Undirected DFS has only these two kinds; directed adds forward and cross.

> DFS sorts every edge into a kind. The thick edges are tree edges: the ones along which DFS discovered a new vertex. In our example the tree happens to be a single path. The dotted edges are back edges: when DFS was at two, it saw zero, an ancestor that had been discovered but not finished; similarly five saw three. Our graph is undirected; the arrowheads only show the direction in which DFS used each edge. In an undirected graph every edge is a tree edge or a back edge, which our tests check on random graphs, but be careful: the tree edge from a vertex back to its parent is seen again from the child's side, and it is not a back edge. Directed graphs also have forward and cross edges. The key fact for lecture sixteen: a directed graph has a cycle exactly when DFS finds a back edge, and the same holds for an undirected graph once the edge to the parent is excluded.

---
# DFS costs

- Each vertex is discovered once and each list scanned once.
- Adjacency lists: $O(V + E)$ time. Matrix: $\Theta(V^2)$, since DFS visits every vertex.
- Extra space $\Theta(V)$ for marks, times and parents.
- Recursion depth can reach $V$: on a long path, the stack holds every vertex.
- Iterative stack version: at most $E + 1$ pushes, so $O(V + E)$ entries.

> The time analysis is the same aggregate argument as for BFS. Each vertex is discovered once, and its neighbor list is scanned once during its visit, for a total of V plus the sum of the degrees. With a matrix, each visit scans a whole row, giving V squared. Space is where the versions differ. The recursive version keeps one call frame per vertex on the current path, and on a graph that is one long path that is every vertex. The iterative version keeps its stack on the heap instead of the call stack, which can hold many more entries.

---
# Quick check: DFS order

```quiz
Recursive DFS from 0 on our graph, with neighbor lists in increasing order. In what order are vertices 0 to 5 discovered?
- [ ] `0, 1, 2, 3, 4, 5`
- [x] `0, 1, 3, 2, 4, 5`
- [ ] `0, 2, 4, 5, 3, 1`
- [ ] `0, 1, 3, 5, 4, 2`
```

> The answer is zero, one, three, two, four, five. The first option is the BFS order. The third is the iterative stack version that pushes lists in order. The fourth looks depth-first, but at vertex three the list is one, two, five, and two comes before five, so DFS goes to two first. When you trace DFS, always take the first undiscovered neighbor in the list, and go there before looking at the rest.

---
@type section
# Using the searches

---
# Connected components

```java
for (int s = 0; s < n; s++) {
    if (comp[s] != -1) continue;
    Queue<Integer> queue = new ArrayDeque<>();
    comp[s] = next;
    queue.add(s);
    while (!queue.isEmpty()) {
        int u = queue.remove();
        for (int v : g.neighbors(u)) {
            if (comp[v] == -1) {
                comp[v] = next;
                queue.add(v);
// ...
    next++;
}
```

- Our graph: `comp = [0, 0, 0, 0, 0, 0, 1, 1]`, two components.

> To find connected components of an undirected graph, run a search from each vertex that has no label yet, and give everything it reaches the same label. Here the component array doubles as the visited mark: minus one means unreached. On our graph the first search from zero labels zero through five with component zero; the loop skips them; the search from six labels six and seven with component one. The total cost is still O of V plus E, because every vertex and every list is processed once across all the searches. DFS works just as well as BFS here.

---
# BFS or DFS?

| | BFS | DFS |
|---|---|---|
| Frontier | Queue (FIFO) | Recursion or stack (LIFO) |
| Order | By distance from the source | Deep first, then back up |
| Gives | Fewest-edge paths, `dist` | `d`/`f` times, back edges |
| Used for | Unweighted shortest paths, levels | Cycles, topological sort (L16) |
| Time, adjacency lists | $O(V + E)$ | $O(V + E)$ |

Compare them side by side in the [BFS and DFS comparison](../../../visualizations/algorithms/bfs_dfs_comparison.html).

> Both searches visit everything reachable in linear time; what differs is the order and what that order tells you. BFS order is by distance, so it answers fewest-hops questions. DFS order follows paths deep, and its timestamps and back edges reveal structure: cycles, and in lecture sixteen, a valid order for a DAG. For just finding what is reachable, or connected components, either works. The comparison visualization runs both on the same graph so you can see the frontier grow as a wave for BFS and as a single path for DFS.

---
# Visualizations

- [BFS visualization](../../../visualizations/algorithms/bfs.html): the queue and the distance levels.
- [DFS visualization](../../../visualizations/algorithms/dfs.html): the recursion path and backtracking.
- [BFS and DFS comparison](../../../visualizations/algorithms/bfs_dfs_comparison.html): the same graph, both orders.
- Before each step, predict the next vertex and the frontier.

> After class, rebuild our eight-vertex graph in the visualizations if they let you choose the graph, or use their examples otherwise. Either way, predict before you press step: for BFS, which vertex leaves the queue next and what joins it; for DFS, which neighbor the search dives into, and when it backs up. Then compare with the tables in these slides. Tracing by hand first and checking second is the fastest way to make these algorithms automatic.

---
# Pitfall: when to mark in BFS

- Mark on **enqueue** (standard): each vertex enters the queue once.
- Mark on **dequeue**: a vertex can be enqueued by several neighbors.
- On our graph: 6 enqueues versus 8 (3 and 5 go in twice).
- The queue can grow to $O(E)$ entries instead of $O(V)$.
- If `dist` is also set at every enqueue, a later copy can overwrite it with a larger value.

> This is the most common BFS bug. If you only mark a vertex when it leaves the queue, then between its first enqueue and its dequeue, other neighbors do not know it is already waiting, and they enqueue it again. On our graph, three is enqueued by both one and two, and five by both three and four, for eight enqueues instead of six; the lecture code counts this. The result can still be correct if duplicates are skipped when dequeued, but the queue can grow to the number of edges, and if distances or parents are written at each enqueue, a later copy can record a longer path. Marking on enqueue avoids all of it.

---
# Pitfalls: disconnected graphs and deep recursion

- One BFS or DFS reaches only the source's component.
- Unreached vertices keep `dist = -1`: test for it before using a path.
- To cover everything, loop over all vertices, as DFS and components do.
- Recursive DFS on a long path can throw `StackOverflowError`.
- Fix: the iterative stack version; our tests run it on a 200,000-vertex path.

> Two more traps. First, a single search is not a whole-graph search: from zero, BFS never sees six and seven. Code that assumes every vertex has a distance will crash or print nonsense for unreachable vertices, so check for minus one. Second, recursion depth. Each recursive call uses a frame on the thread's call stack, whose size is limited and depends on JVM settings. A graph that is a long path can make recursive DFS nest once per vertex and overflow. The iterative version stores its stack as an ordinary object on the heap, so it handles the long path that our tests use.

---
# More edge cases

- A graph with no edges, or a single vertex: searches still terminate.
- Self-loops and repeated edges: the marks make them harmless.
- Directed graphs: search follows out-edges only; reachability is one-way.
- Invalid vertex numbers: our `Graph` throws `IndexOutOfBoundsException`.
- Results depend on neighbor order; tests should check properties, not one order.

> A few smaller cases to test. A graph with no edges gives every vertex its own component. Self-loops and duplicate edges cause no harm to BFS or DFS, because the mark stops the second visit, but they do change degree counts. In a directed graph, a search from u finds the vertices reachable from u, which is not the same as the vertices that can reach u. And because the order of neighbor lists changes which paths and which orders you get, write tests that check properties, like distances or that every path uses real edges, as our checks do.

---
@type section
# Wrap-up

---
# Summary

| Idea | Takeaway |
|---|---|
| Graph | $G = (V, E)$; directed or undirected; maybe weighted |
| Representations | Matrix $\Theta(V^2)$; lists $\Theta(V + E)$; edge list $\Theta(E)$ |
| BFS | Queue; mark on enqueue; fewest-edge `dist` and `parent` |
| DFS | Recursion or stack; `d`/`f` times; tree and back edges |
| Both | $O(V + E)$ with adjacency lists; $O(V^2)$ with a matrix ($\Theta(V^2)$ for full-graph DFS) |
| Components | Repeat the search from every unmarked vertex |

> Graphs model connections, and adjacency lists store sparse graphs in space proportional to their size. BFS explores in distance order with a queue and gives shortest paths when every edge counts as one. DFS dives with recursion or a stack and records discovery and finish times, and its back edges point to cycles. Both run in linear time with adjacency lists. Next time we use DFS finish times to order the vertices of a DAG, and back edges to detect cycles.

---
# Check yourself

- Add edge `1 - 2` to our graph. Which BFS distances from 0 change, and which parents?
- Why does the recursive DFS finish 5 before it finishes 2?
- A graph has $10^6$ vertices and $3 \times 10^6$ edges. Which representation, and why?

> Try these before looking back. For the first, redo the BFS table and notice whether a new edge between two vertices at the same distance can change any distance. For the second, think about the call stack at the moment five is discovered. For the third, compute the space of a matrix and of adjacency lists, and think about which operations the searches actually use.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 20: Elementary graph algorithms (representations, BFS, DFS, edge classification).
- Java SE API documentation: `java.util.ArrayDeque`, `java.util.Queue`, `java.util.Deque`, `java.util.List`.
- Visualizations: [BFS](../../../visualizations/algorithms/bfs.html), [DFS](../../../visualizations/algorithms/dfs.html), [BFS and DFS comparison](../../../visualizations/algorithms/bfs_dfs_comparison.html).
- Code: the `l15` Java package for this lecture (`Graph`, `Bfs`, `Dfs`, `Components`, `Pitfalls`).

> The definitions, the BFS shortest-path argument, DFS timestamps, the parenthesis structure and edge classification follow CLRS chapter twenty, with vertices numbered from zero. Library facts come from the Java SE API documentation. The running example, the traces and the code are original to this lecture, and every trace was printed by running that code.
