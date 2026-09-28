@title Lecture 16: Topological sort and cycle detection
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Topological sort and cycle detection
## Ordering work that depends on other work

---
# Where we are

- L15 stored graphs as adjacency lists and explored them with BFS and DFS.
- Today both searches answer a new question: in what order can we do the work?
- By the end of today you can:
  - check whether a given order respects every edge of a directed graph;
  - trace Kahn's algorithm and DFS-based topological sort by hand;
  - find and report a cycle in a directed or an undirected graph;
  - state the cost $O(V+E)$ and the assumptions behind it.

> Last lecture gave us two traversals, breadth-first and depth-first, and a way to store a graph with adjacency lists. Today we reuse both traversals almost unchanged. The new question is about order: if some tasks must happen before others, can we list all the tasks so that no rule is broken, and if we cannot, which rules are to blame? Each outcome on the slide is something you can do with a pencil by the end of class.

---
# A planning problem

- A department lists seven courses and which ones must come first.
- A student wants one semester-by-semester sequence that breaks no rule.
- With seven courses you can juggle it in your head. With seven hundred you cannot.
- Worse, someone may type a rule that makes the plan impossible.
- We want an algorithm that either produces an order or names the conflict.

> Picture a curriculum committee adding prerequisite rules one at a time. Each rule is harmless alone, but together they might say that course A needs B, B needs C, and C needs A. Nobody could ever graduate. We need a method that produces a valid sequence whenever one exists, and otherwise points at the circular chain of rules so a person can fix it. That is exactly topological sort plus cycle detection.

---
# Precedence constraints are everywhere

| Domain | Vertex | Edge `u -> v` means |
|---|---|---|
| Course plans | a course | take `u` before `v` |
| Software builds | a module | compile `u` before `v` |
| Data pipelines | a task | `v` reads the output of `u` |
| Spreadsheets | a cell | `v`'s formula uses `u` |

> A precedence constraint says one thing must happen before another. Build tools compile a library before the program that links it. A data pipeline cleans the raw file before training a model on it. A spreadsheet recomputes a cell only after the cells its formula mentions. In every row the graph is directed, and the arrow points from what must come first to what comes after. Keep that direction convention in mind; it is the one we use all lecture.

---
# The running example

```diagram Edge u -> v: take u before v
@dir LR
@reveal all
I[Intro] -> D[Data]
I -> L[Logic]
D -> A[Algo]
D -> S[Sys]
L -> A
A -> C[Cap]
S -> N[Net]
N -> C
```

- Seven made-up courses, eight rules, numbered 0 to 6 left to right:
  Intro, Data, Logic, Algo, Sys, Net, Cap.
- Neighbor lists keep edges in the order they were added.

> These course names are invented for this lecture. Intro is vertex zero, Data one, Logic two, Algo three, Sys four, Net five and Cap six. Intro leads to Data and Logic, both of which are needed for Algo; Data also leads to Sys, then Net; and the capstone, Cap, needs both Algo and Net. Every trace today uses this graph, and the order of each neighbor list matters: Data's list is Algo then Sys, and Net's list is just Cap. All states on later slides come from running the Java code on exactly this graph.

---
# Vocabulary

- A **directed acyclic graph (DAG)** is a directed graph with no directed cycle.
- A **directed cycle** is a path $v_0 \to v_1 \to \dots \to v_k = v_0$ with $k \ge 1$.
- The **in-degree** of $v$ is the number of edges entering $v$.
- A **source** has in-degree 0. A **sink** has no outgoing edges.
- In our example Intro is the only source and Cap the only sink.

> A cycle must follow the arrows, so a self-loop from a vertex to itself counts as a cycle of length one. Our course graph has no directed cycle, so it is a DAG, even though Intro, Data, Algo and Logic form a loop if you ignore the arrow directions. In-degree is just a count of arrows coming in: Algo has two, Cap has two, Intro has none. A source is a course with no prerequisites, and a sink is a course that nothing else requires.

---
# Topological order

- A **topological order** lists every vertex exactly once.
- For every edge $u \to v$, $u$ appears before $v$.
- `Intro, Data, Logic, Sys, Algo, Net, Cap` is one topological order.
- `Data, Intro, ...` is not: the edge Intro to Data points backwards.
- Checking a proposed order costs $O(V+E)$: record positions, then test every edge.

> The definition has two parts: a permutation of all vertices, and one inequality per edge. To check a proposed order, write each vertex's position in an array, then walk every edge and confirm the tail's position is smaller than the head's. One backwards edge is enough to reject the order. Our Java checker does exactly this, and the test suite uses it on every order the algorithms produce.

---
@type section
# Orders exist exactly for DAGs

---
# A cycle forbids every order

- Suppose $v_0 \to v_1 \to \dots \to v_{k-1} \to v_0$ is a cycle.
- An order would need $v_0$ before $v_1$, $v_1$ before $v_2$, and so on.
- Chaining these gives $v_0$ before $v_0$: impossible.
- So a graph with a directed cycle has **no** topological order.

> This direction is the easy one. Follow the cycle around and apply the rule to each edge. Before is transitive, so the chain of rules ends up saying the first vertex comes before itself, which no list can do. A self-loop is the shortest example: it demands that a vertex come before itself directly.

---
# Every DAG has a source

- Pick any vertex and walk **backwards** along incoming edges.
- If the walk ever stops, it stopped at a vertex with in-degree 0.
- If it never stops, after $V$ steps some vertex repeats.
- A repeat means the walk traced a directed cycle, backwards.
- In a DAG that cannot happen, so the walk stops at a source.

> Start at Cap in our example and step back along an incoming arrow, say to Net, then Sys, then Data, then Intro, where no arrow comes in. In general, a finite graph has only V vertices, so a walk of more than V steps must revisit one. The stretch between the two visits is a cycle, which a DAG does not have. So every nonempty DAG has at least one source. The same argument, walking forward, shows every nonempty DAG has a sink.

---
# Every DAG has an order

- Induction on $V$. An empty graph has the empty order.
- Take a source $s$ of the DAG; put $s$ first.
- Deleting $s$ and its edges leaves a smaller DAG.
- Order the rest by induction; append it after $s$.
- Every edge out of $s$ points forward. Every other edge is handled by the rest.

$$\text{order exists} \iff \text{DAG}$$

> Removing a vertex cannot create a cycle, so the remainder is still a DAG and, by induction, has an order. Placing the source in front is safe because no edge enters it, and every edge leaving it now points to a later position. Together with the previous slide this proves the equivalence on screen. The proof is also an algorithm: repeatedly take a source and delete it. That is Kahn's algorithm, which we trace next.

---
# Quiz: orders and cycles

```quiz
A directed graph has edges A -> B, B -> C and A -> C. How many topological orders does it have?
- [ ] zero, because A, B, C form a cycle
- [x] exactly one: A, B, C
- [ ] two: A, B, C and A, C, B
- [ ] six, one per permutation
```

> The three edges form a triangle only if you ignore direction; following the arrows there is no way back to A, so the graph is a DAG and has at least one order. A must come first because it has in-degree zero and is the only source. Then B must come before C because of the edge B to C. That leaves exactly one order. The option A, C, B breaks the edge B to C.

---
@type section
# Kahn's algorithm

---
# Kahn's idea

- Count the in-degree of every vertex.
- Put every source in a FIFO queue.
- Repeatedly remove a vertex $u$ from the queue and output it.
- "Delete" $u$: decrement the in-degree of each neighbor of $u$.
- A neighbor whose count reaches 0 has become a source: enqueue it.

> We never actually delete anything from the graph. The in-degree array plays the role of the shrinking graph: a count tells how many prerequisites of that vertex are still waiting. When the count drops to zero, all its prerequisites have been output, so it is safe to schedule. The queue holds the vertices that are ready right now. Any order of removal from that set works; a FIFO queue is simply a convenient choice.

---
# Kahn in pseudocode

```algorithm
function KAHN(G):                     // G has vertices 0..V-1
  indeg[v] ← 0 for every v
  for each edge u → v do indeg[v] ← indeg[v] + 1
  Q ← queue of every v with indeg[v] = 0
  order ← empty list
  while Q is not empty do
    u ← DEQUEUE(Q); append u to order
    for each v in adj[u] do
      indeg[v] ← indeg[v] - 1
      if indeg[v] = 0 then ENQUEUE(Q, v)
  if len(order) < V then report "cycle"   // some vertex never freed
  return order
```

> Read it in three parts: count, seed, repeat. The count loop visits every edge once. The seed puts all current sources in the queue. The main loop outputs one vertex per round and releases the vertices whose last waiting prerequisite was just output. The final test matters: if fewer than V vertices came out, something was never released. We will see that this happens exactly when there is a cycle.

---
# Kahn in Java: count and seed

```java
int n = g.vertexCount();
int[] inDegree = new int[n];
for (int u = 0; u < n; u++) {
    for (int v : g.neighbors(u)) {
        inDegree[v]++;
    }
}
Deque<Integer> queue = new ArrayDeque<>();
for (int v = 0; v < n; v++) {
    if (inDegree[v] == 0) {
        queue.add(v);
    }
}
```

> The double loop visits each adjacency list once, so it touches every edge exactly once and increments the head's count. `ArrayDeque` serves as the queue: `add` puts a vertex at the tail and, on the next slide, `remove` takes from the head. The seed loop scans vertices in index order, which is why Intro, vertex zero, is examined first. On our graph it is also the only source.

---
# Kahn in Java: the main loop

```java
List<Integer> order = new ArrayList<>();
// ...
while (!queue.isEmpty()) {
    int u = queue.remove();
    order.add(u);
    for (int v : g.neighbors(u)) {
        inDegree[v]--;
        if (inDegree[v] == 0) {
            queue.add(v);
        }
    }
    // ...
}
return order;
```

> This is the rest of the method kahnOutput. Each round removes the head of the queue, appends it to the output, and walks its adjacency list once. The decrement and the zero test are the whole algorithm: a neighbor joins the queue at the exact moment its last waiting prerequisite is output. The two marked gaps are the trace-recording lines of the tested file. The method returns whatever it managed to output, even if that is fewer than V vertices; the wrapper on the next slide turns that into a clear answer.

---
# Kahn in Java: an order or nothing

```java
public static Optional<List<Integer>> kahn(Digraph g) {
    List<Integer> order = kahnOutput(g, null);
    return order.size() == g.vertexCount()
            ? Optional.of(order) : Optional.empty();
}
```

- All $V$ vertices output: return the order.
- Fewer: some vertex never reached in-degree 0. Return an empty `Optional`.
- The caller must handle the cyclic case; no partial list slips through.

> The public wrapper returns an order only when every vertex came out, and an empty Optional when a cycle blocked some of them. Returning Optional instead of a possibly short list forces the caller to think about the cyclic case. The null argument switches off the tracing hook that printed the trace on the next slides; the traced run and the plain run execute the same statements otherwise.

---
# Kahn trace: the starting counts

| Vertex | Intro | Data | Logic | Algo | Sys | Net | Cap |
|---|---|---|---|---|---|---|---|
| in-degree | 0 | 1 | 1 | 2 | 1 | 1 | 2 |

- Only Intro has in-degree 0, so the queue starts as `[Intro]`.
- Algo and Cap each wait for two prerequisites.

> Check each count against the picture of the graph. Data and Logic each have one arrow in, from Intro. Algo has two, from Data and from Logic. Sys and Net have one each. Cap has two, from Algo and from Net. These are the numbers the Java code printed before its main loop began, and the queue held Intro alone.

---
# Kahn trace, steps 1 to 4

```diagram
@dir TB
@reveal manual
K1[out Intro | queue: Data, Logic]
focus K1
--- Intro leaves. Data and Logic drop to 0 and join the queue.
K1 -> K2[out Data | queue: Logic, Sys]
focus K1 K2
--- Algo drops to 1. Sys drops to 0 and joins.
K2 -> K3[out Logic | queue: Sys, Algo]
focus K2 K3
--- Algo drops to 0: both of its prerequisites are out.
K3 -> K4[out Sys | queue: Algo, Net]
focus K3 K4
--- Net drops to 0 and joins behind Algo.
```

> Each box shows the vertex just output and the queue after its neighbors were updated. Removing Intro frees both Data and Logic. Removing Data lowers Algo from two to one, not yet zero, and frees Sys. Removing Logic finally frees Algo. Removing Sys frees Net. Notice the queue is first in, first out: Algo joined before Net, so Algo will leave first.

---
# Kahn trace, steps 5 to 7

```diagram
@dir TB
@reveal manual
K5[out Algo | queue: Net]
focus K5
--- Cap drops from 2 to 1. It still waits for Net.
K5 -> K6[out Net | queue: Cap]
focus K5 K6
--- Cap drops to 0 and joins.
K6 -> K7[out Cap | queue empty]
focus K6 K7
--- Seven of seven vertices are out.
K7 -> R[Intro, Data, Logic, Sys, Algo, Net, Cap].green
focus R
--- The output is a topological order.
```

> Algo leaves and lowers Cap to one. Net leaves and lowers Cap to zero, so Cap joins the queue and leaves last. The queue is empty and the output holds all seven vertices, which the checker confirms respects all eight edges. Read the final order against the graph: every arrow points from an earlier course to a later one.

---
# Why Kahn works, and what it costs

- Invariant: `inDegree[v]` counts edges into $v$ from vertices not yet output.
- A vertex is queued only when that count is 0: all its prerequisites are out.
- So every edge $u \to v$ has $u$ output before $v$.
- Time: $\Theta(V)$ to seed plus $\Theta(E)$ for all decrements: $\Theta(V+E)$ with adjacency lists.
- Extra space: $\Theta(V)$ for the counts, the queue and the output.

> The invariant holds at the start by construction, and each decrement keeps it true because one more tail has just been output. When v enters the queue, every vertex with an edge into v is already in the output, so v lands after all of them. Each vertex enters the queue at most once and each edge is decremented once, so the total work is linear in V plus E. With an adjacency matrix, scanning neighbors costs V per vertex and the total becomes Theta of V squared.

---
@type section
# DFS-based topological sort

---
# The finish-time idea

- Run DFS from every unvisited vertex, as in L15.
- A vertex **finishes** when its whole adjacency list has been explored.
- In a DAG, for every edge $u \to v$, $v$ finishes before $u$.
- So listing vertices by **decreasing** finish time is a topological order.
- Implementation: push each vertex on the front of a list as it finishes.

> DFS gives us two moments for each vertex: when it is first discovered and when it is finished. The claim on screen is about finish times. If every arrow's head finishes before its tail, then reversing the finish order puts every tail before its head, which is the definition of a topological order. We prove the claim in a moment. The trick in code is cheap: instead of sorting by finish time, push each vertex onto the front of a deque the instant it finishes.

---
# DFS order in pseudocode

```algorithm
function DFS-ORDER(G):
  visited[v] ← false for every v
  L ← empty list
  for s ← 0 to V - 1 do
    if not visited[s] then VISIT(s)
  return L                         // reverse finish order

function VISIT(u):
  visited[u] ← true
  for each v in adj[u] do
    if not visited[v] then VISIT(v)
  insert u at the front of L       // u finishes now
```

> This is plain depth-first search with one extra line at the end of VISIT. The outer loop matters: it starts a new search from every vertex that is still unvisited, so vertices unreachable from vertex zero are still ordered. The pseudocode assumes the input is a DAG. On a graph with a cycle it still returns a list, but that list is meaningless, so check for cycles first.

---
# DFS order in Java

```java
private void visit(int u) {
    visited[u] = true;
    // ...
    for (int v : g.neighbors(u)) {
        if (!visited[v]) {
            visit(v);
        }
    }
    finished.push(u); // push adds at the front: latest finisher first
    // ...
}
```

- The constructor calls `visit(s)` for every `s` from 0 to $V-1$ that is still unvisited.

> The fields visited, finished and g live in a small DepthFirstOrder object, which keeps the recursive method's signature short. `finished` is an `ArrayDeque`, and `push` inserts at the front, so after the search the deque reads from the last finisher to the first. The bullet describes the outer loop in the constructor, the same loop as in the pseudocode. The two marked gaps are the trace lines that printed the next two slides.

---
# DFS trace: the first branch

```diagram
@dir TB
@reveal manual
D1[enter Intro, Data, Algo, Cap]
focus D1
--- DFS dives Intro, Data, Algo, Cap. Cap has no neighbors.
D1 -> D2[finish Cap, then Algo | list: Algo, Cap]
focus D1 D2
--- Cap finishes first, then Algo. Each goes to the front.
D2 -> D3[enter Sys, Net | from Data]
focus D2 D3
--- Back at Data, the next neighbor is Sys, then Net.
D3 -> D4[finish Net | list: Net, Algo, Cap]
focus D3 D4
--- Net's only neighbor, Cap, is already visited.
```

> Follow adjacency lists in their stored order. From Intro the first neighbor is Data; from Data it is Algo; from Algo it is Cap, which has no outgoing edges and finishes immediately. Algo then has nothing left and finishes. Data's second neighbor is Sys, which leads to Net. Net's only neighbor Cap is already visited, so Net finishes. Each finisher is pushed onto the front, so the list reads Net, Algo, Cap.

---
# DFS trace: unwinding

```diagram
@dir TB
@reveal manual
E1[finish Sys, then Data | list: Data, Sys, Net, Algo, Cap]
focus E1
--- Sys and Data finish as the recursion unwinds.
E1 -> E2[enter Logic | from Intro]
focus E1 E2
--- Intro's second neighbor is Logic. Its neighbor Algo is visited.
E2 -> E3[finish Logic, then Intro]
focus E2 E3
--- Logic finishes, then Intro. The outer loop finds nothing unvisited.
E3 -> R[Intro, Logic, Data, Sys, Net, Algo, Cap].green
focus R
--- Reverse finish order: a topological order.
```

> Sys finishes, then Data, each pushed to the front. Back at Intro, the next neighbor is Logic, whose only neighbor Algo is done, so Logic finishes at once and lands in front of Data. Intro finishes last and goes to the very front. The outer loop then checks vertices one through six and finds them all visited. The checker confirms this order respects all eight edges.

---
# Why reverse finish order works

- Take any edge $u \to v$ when DFS scans it from $u$. Then $u$ is gray: started, not finished.
- If $v$ is white (unvisited), $v$ becomes a descendant and finishes before $u$.
- If $v$ is black (finished), it finished before $u$ already.
- If $v$ were gray, $v$ would be on the current path to $u$: a cycle. A DAG has none.
- In every possible case $v$ finishes first, so $u$ comes first in the reversed list.

> The colors here are the ones we formalize in the next section: white is unvisited, gray is on the recursion stack, black is finished. When DFS looks along the edge from u to v, u is still gray. The three cases for v cover everything. The gray case is exactly a cycle, which a DAG cannot have, and the other two cases both make v finish before u. Reversing the finish order therefore places u before v for every edge.

---
# Orders are not unique

| Method | Order produced on the course graph |
|---|---|
| Kahn, FIFO queue | Intro, Data, Logic, Sys, Algo, Net, Cap |
| DFS, reverse finish | Intro, Logic, Data, Sys, Net, Algo, Cap |

- Both are valid. A brute-force count finds 9 valid orders of this graph.
- Unique exactly when consecutive vertices of a topological order are joined by edges (the DAG has a Hamiltonian path).
- Changing neighbor order or queue discipline can change the answer, not its validity.

> Two correct algorithms gave two different answers, and both are right. Our test program tries all five thousand and forty permutations of the seven courses and finds nine that respect every rule. Intro must be first and Cap last, but Logic can sit in several places, and Sys can come before or after Algo. When grading your own trace, do not compare against one expected list; check the definition, edge by edge. The order is forced only when each adjacent pair in it is joined by an edge.

---
# Kahn or DFS?

| Question | Kahn | DFS-based |
|---|---|---|
| Time, adjacency lists | $\Theta(V+E)$ | $\Theta(V+E)$ |
| Cycle signal | fewer than $V$ output | needs the color test |
| Recursion | none | depth up to $V$ |
| Natural extras | list vertices "level by level" | reuses one DFS for other tasks |

> Both run in linear time with adjacency lists. Kahn's algorithm is iterative and detects cycles for free, which makes it a common choice for build and task schedulers. The DFS version needs the three-color test to be safe on cyclic input, and its recursion can be as deep as the longest path. In Java, a very deep recursion can throw StackOverflowError on the default thread stack; an explicit stack avoids that. The DFS version is attractive when you already run a DFS for another purpose.

---
@type section
# Cycle detection in directed graphs

---
# A rule that breaks the plan

```diagram The course graph plus one mistaken rule, Net -> Data
@dir LR
@reveal all
I[Intro] -> D[Data]
I -> L[Logic]
D -> A[Algo]
D -> S[Sys]
L -> A
A -> C[Cap]
S -> N[Net]
N -> C
N -> D
```

- Someone adds "Net before Data". Now Data, Sys, Net need each other.
- No order exists. We want the algorithm to say which vertices form the cycle.

> The new edge from Net back to Data is appended after Net's existing edge to Cap, so Net's adjacency list is now Cap, then Data. With it, Data requires Net, which requires Sys, which requires Data. Answering "no order exists" is not enough for the committee; they need the offending chain to know which rule to remove. Both detectors we study find this cycle, and the DFS one reports it.

---
# Three colors

- **White**: not yet discovered.
- **Gray**: discovered, still on the recursion stack.
- **Black**: finished; everything reachable from it has been explored.
- At any moment the gray vertices form one path: the current DFS path.
- Edge $u \to v$ with $v$ gray is a **back edge**: it points to an ancestor.

> Plain DFS uses a single visited flag, which merges gray and black into one state. For cycle detection that is not enough, because an edge to a black vertex is harmless while an edge to a gray vertex closes a loop. Gray vertices are exactly the calls that have started and not returned, so they line up as a path from the current root to the vertex we are exploring. A back edge jumps from the end of that path to a vertex earlier on it.

---
# A back edge means a cycle, and conversely

- Back edge $u \to v$: $v$ is gray, so the path $v \leadsto u$ exists. Add $u \to v$: a cycle.
- Conversely, suppose a cycle exists. Let $v$ be its first vertex DFS turns gray.
- At that moment the rest of the cycle is white and reachable from $v$.
- So its predecessor $u$ on the cycle finishes inside $v$'s call.
- When $u$ scans $u \to v$, $v$ is still gray: a back edge.

> The first direction is immediate from the gray path. The second direction uses the white-path idea from CLRS Chapter 20: when v turns gray, every other cycle vertex is still white and reachable from v along the cycle, so each becomes a descendant of v before v finishes. In particular the cycle vertex just before v, call it u, is explored inside v's call, and its edge back to v finds v gray. So the test finds a cycle whenever one exists.

---
# Three-color DFS in pseudocode

```algorithm
function FIND-CYCLE(G):
  color[v] ← WHITE; parent[v] ← none, for every v
  for s ← 0 to V - 1 do
    if color[s] = WHITE and DFS(s) found a cycle then return it
  return "no cycle"

function DFS(u):
  color[u] ← GRAY
  for each v in adj[u] do
    if color[v] = GRAY then return cycle from v to u   // back edge
    if color[v] = WHITE then
      parent[v] ← u; DFS(v); stop if it found a cycle
  color[u] ← BLACK
```

> Compare this with the DFS order recipe: the only changes are the two-level color test and the parent array. A black neighbor is skipped silently, because everything reachable from it was already explored without finding a way back. The outer loop again starts from every white vertex. The parent array records who discovered each vertex, which is what lets us print the cycle rather than just say one exists.

---
# Three-color DFS in Java

```java
private void dfs(int u) {
    color[u] = Color.GRAY;
    for (int v : g.neighbors(u)) {
        if (color[v] == Color.GRAY) {
            cycle = cycleFrom(u, v);
        } else if (color[v] == Color.WHITE) {
            parent[v] = u;
            dfs(v);
        }
        if (!cycle.isEmpty()) {
            return;
        }
    }
    color[u] = Color.BLACK;
}
```

> Color is an enum with WHITE, GRAY and BLACK, and cycle is a field that starts as an empty list. Look at the early return: once any call records a back edge, every call on the stack returns immediately, leaving its vertex gray. That is fine because the search is over. A self-loop from u to u is caught on the first test, since u is gray when it scans its own edge.

---
# Reporting the cycle with parent pointers

```java
private List<Integer> cycleFrom(int u, int v) {
    Deque<Integer> path = new ArrayDeque<>();
    for (int x = u; x != v; x = parent[x]) {
        path.push(x);
    }
    path.push(v);
    return new ArrayList<>(path);
}
```

- The back edge is $u \to v$. Parents lead from $u$ up to the ancestor $v$.
- Pushing on the front reverses the walk, so the list follows edge direction.

> parent of x is the vertex whose call discovered x, so following parents from u climbs the gray path toward the root. We stop when we reach v, the target of the back edge, which must be on that path. Each vertex is pushed onto the front of the deque, so the finished list reads v first and u last. The cycle is that list plus the back edge from u to v. The walk costs at most the length of the cycle.

---
# Three-color trace on the broken graph

```diagram
@dir TB
@reveal manual
C1[gray: Intro, Data, Algo, Cap]
focus C1
--- DFS dives from Intro to Cap. All four are gray.
C1 -> C2[black: Cap, Algo | gray: Intro, Data]
focus C1 C2
--- Cap and Algo finish without finding a gray neighbor.
C2 -> C3[gray: Intro, Data, Sys, Net]
focus C2 C3
--- Data's next neighbor is Sys, then Net.
C3 -> C4[Net -> Data: Data is gray].rose
focus C3 C4
--- Net skips black Cap, then finds Data gray: a back edge.
C4 -> R[cycle: Data -> Sys -> Net -> Data].rose
focus R
--- Parents: Net came from Sys, Sys from Data. Stop at Data.
```

> This is the Java output on the graph with the extra rule. The first dive reaches Cap, which has no neighbors, so Cap turns black, and Algo follows. Back at Data the search tries Sys and then Net, all gray now, forming the path Intro, Data, Sys, Net. Net's list is Cap, which is black and ignored, then Data, which is gray. That back edge triggers the report: parent of Net is Sys, parent of Sys is Data, and the walk stops. The reported cycle is Data, Sys, Net.

---
# Kahn detects cycles too

```diagram
@dir TB
@reveal manual
Q0[start | queue: Intro]
focus Q0
--- The extra edge raises Data's in-degree to 2.
Q0 -> Q1[out Intro | queue: Logic]
focus Q0 Q1
--- Data drops only to 1: Net has not been output.
Q1 -> Q2[out Logic | queue empty]
focus Q1 Q2
--- Algo drops to 1. Nothing else reaches 0.
Q2 -> R[2 of 7 output: cycle].rose
focus R
--- Fewer than V vertices came out.
```

> On a DAG, Kahn outputs everything. Here Data waits for Net, Net waits for Sys, and Sys waits for Data, so none of the three ever reaches zero, and Algo and Cap are stuck behind them. The run outputs Intro and Logic and stops. Conversely, if the queue empties early, the vertices left over each have a remaining incoming edge from another leftover vertex, so walking backwards among them must repeat, which is a cycle. Kahn tells you that a cycle exists but does not name it.

---
# Quiz: which edge reveals a cycle?

```quiz
During a directed DFS, vertex u scans the edge u -> v. Which color of v proves the graph has a cycle?
- [ ] white
- [x] gray
- [ ] black
- [ ] any color other than white
```

> Only gray does. A gray vertex is on the current recursion path, so there is already a path from v down to u, and the edge from u back to v closes the loop. A white vertex is simply explored next. A black vertex is finished: in the course graph, when Net scans its edge to Cap, Cap is black, and there is no cycle through that edge. Treating black like gray is the classic false alarm, and we will see it again among the pitfalls.

---
@type section
# Undirected graphs

---
# Cycles in undirected graphs

- In an undirected graph each edge `u - v` is stored twice: `v` in `u`'s list and `u` in `v`'s list.
- DFS from `u` sees the edge back to its own parent. That is not a cycle.
- Rule: a visited neighbor **other than the parent** means a cycle.
- Parallel edges `u - w`: the child `w` skips both copies, but `u`'s scan of the second copy reports the 2-cycle.
- A self-loop is caught too. `new Graph(n)` rejects both; the checks test them on a multigraph.

> In an undirected DFS every non-tree edge leads to an ancestor, so any visited neighbor besides the parent closes a cycle, and no colors beyond visited are needed. The parent exception looks as if it could hide the cycle formed by two parallel edges, but it does not. The child w skips both copies of its edge to u, since both lead to its parent. When w's call returns, u scans its second copy, finds w visited and not u's own parent, and reports the 2-cycle. A self-loop is caught when its vertex scans itself. A graph made with new Graph rejects both in addEdge; the checks build a small multigraph to confirm these cases.

---
# Undirected DFS in Java

```java
private static boolean dfs(Graph g, int u, int parent, boolean[] visited) {
    visited[u] = true;
    for (int v : g.neighbors(u)) {
        if (!visited[v]) {
            if (dfs(g, v, u, visited)) {
                return true;
            }
        } else if (v != parent) {
            return true;
        }
    }
    return false;
}
```

- Example: edges `0-1`, `1-2`, `3-4`, `2-0`. DFS goes 0, 1, 2; at 2 the neighbor 0 is visited and not the parent.

> The parent is passed down as an argument, with minus one for a root. On the small example, DFS starts at zero, visits one, where it skips its parent zero, then visits two, where it skips its parent one and then meets zero, visited and not the parent. That is the triangle zero, one, two. The public method wraps this in an outer loop over all vertices, so the component three-four is also searched when no cycle has been found yet.

---
# Preview of L18: union-find

- Keep a forest where each tree is one connected component found so far.
- For each edge `u - v`: find the roots of `u` and `v`.
- Different roots: link one root under the other. The components merge.
- Same root: `u` and `v` were already connected, so this edge closes a cycle.
- Needs only the edge list. L18 uses it in Kruskal's algorithm.

```diagram
@dir LR
@reveal manual
U1[0-1: roots 0, 1 | link]
U1 -> U2[1-2: roots 1, 2 | link]
U2 -> U3[3-4: roots 3, 4 | link]
U3 -> U4[2-0: roots 2, 2 | cycle].rose
focus U4
--- Edges in order. The fourth finds both ends under root 2.
```

> Union-find, also called disjoint sets, is the subject of CLRS Chapter 19 and the engine of Kruskal's algorithm in Lecture 18. The idea is independent of DFS: process edges one at a time, and keep track of which vertices are already connected. The trace is the Java output on the same four edges. The first three edges each join two different trees. For the edge two-zero, zero's root chain leads to one and then to two, which is also two's root, so the edge is redundant and closes a cycle.

---
# Union-find in Java, bare version

```java
for (int[] e : g.edges()) {
    int a = find(root, e[0]);
    int b = find(root, e[1]);
    if (a == b) {
        return true;
    }
    root[a] = b;
}
return false;
```

```java
private static int find(int[] root, int v) {
    while (root[v] != v) {
        v = root[v];
    }
    return v;
}
```

> Every vertex starts as its own root. find follows root pointers to the top of its tree. This bare version can build long chains, so one find may take up to Theta of V steps. L18 adds union by rank and path compression, which make the total cost almost linear in the number of edges. For today, the point is the test on the fourth line: an edge whose ends already share a root is a cycle edge.

---
@type section
# Costs and pitfalls

---
# Costs at a glance

| Task | Time | Extra space |
|---|---|---|
| Check a proposed order | $O(V+E)$ | $\Theta(V)$ |
| Kahn's algorithm | $\Theta(V+E)$ | $\Theta(V)$ |
| DFS order | $\Theta(V+E)$ | $\Theta(V)$ |
| Three-color DFS | $O(V+E)$; $\Theta(V+E)$ worst case (a DAG) | $\Theta(V)$ |
| Undirected DFS cycle test | $O(V+E)$ | $\Theta(V)$ |
| Bare union-find test | $O(V + VE)$ | $\Theta(V)$ |

Adjacency lists assumed; DFS recursion depth is up to $V$. A matrix makes neighbor scans $\Theta(V^2)$.

> Kahn's algorithm and the DFS order always scan every vertex and every edge, so they are Theta of V plus E on every input. The three-color DFS may stop at the first back edge, so in general it is O of V plus E; its worst case is a DAG, where no back edge exists and it scans everything, which is Theta of V plus E. The order checker may reject early, so it is O of V plus E, and Theta of V plus E for a valid order. The DFS rows' extra space includes the recursion stack, which can be V calls deep. The undirected test stops even sooner: on a simple graph it scans at most 2V minus 1 list entries before it stops, since every entry before the reporting one is a tree edge or the edge back to the parent. The union-find row is only an upper bound for the bare version; L18 improves it.

---
# Pitfall: the undirected rule on a directed graph

```diagram A DAG: A -> B -> C and A -> C
@dir LR
@reveal all
A -> B -> C
A -> C
```

- DFS from A reaches C through B. Then A scans `A -> C`: C is visited but black.
- The rule "visited means cycle" reports a cycle here. There is none.
- Directed graphs need the gray test. Our test suite confirms the false alarm.

> This is the most common bug in student code. A single visited flag treats any visited neighbor as a loop; adding the undirected parent exception would not help, because C is not A's parent. But in a directed graph, reaching a vertex twice by different routes is normal: A to B to C and A directly to C. Viewed as undirected, this is a triangle, and the undirected rule is right to say so. Viewed as directed, it is a DAG with exactly one topological order. Our Pitfalls class implements the wrong rule, and the checks confirm it reports a false cycle on this graph while the three-color test does not.

---
# Pitfall: forgetting disconnected parts

- A single `dfs(0)` explores only what vertex 0 can reach.
- Example: `P` isolated, and a cycle `Q -> R -> Q`. Searching from `P` alone finds nothing.
- Every method today needs the outer loop over all vertices.
- Kahn is immune: it counts every vertex's in-degree at the start.
- Other edge cases: the empty graph has the empty order; a self-loop is a cycle.

> Graphs from real data are often in pieces: a course with no prerequisites and no dependents, or a separate cluster of tasks. A search launched from one vertex silently ignores the rest, and a topological order built that way misses vertices entirely. The checks include a three-vertex graph where the only cycle is unreachable from vertex zero: the one-start search says no cycle, while the full outer loop finds Q and R. Kahn is safe by design because it seeds from every source, in every component.

---
# Quiz: the parallel-edge case

```quiz
An undirected multigraph has two separate edges between u and w, and nothing else. The undirected DFS from the earlier slide starts at u. Which scan reports the cycle?
- [ ] none: both copies lead to a parent, so no cycle is reported
- [ ] w, when it scans its second copy of the edge to u
- [x] u, after w's call returns, when it scans its second edge to w
- [ ] u, when it first scans its edge to w
```

> Run the code in your head. u scans its first edge to w; w is unvisited, so this is a tree edge and the search calls into w with parent u. w's list holds u twice, and both times u is the parent, so w skips both and returns false. Back in u, the second edge to w finds w visited, and w is not u's parent, since u is the root. That scan reports the cycle of length two. An instrumented copy of the method printed exactly this sequence, and the checks confirm the result on the multigraph.

---
# In Java and in the visualizations

- `java.util` has no graph type: we built one on `ArrayList` and `ArrayDeque`.
- `ArrayDeque.add` and `remove` act as a FIFO queue; `push` inserts at the front.
- `ArrayDeque` does not permit `null` elements; our vertex numbers never are.
- Step through Kahn's algorithm: [Topological sort visualization](../../../visualizations/algorithms/topological_sort.html).
- Review discovery and finish order: [DFS visualization](../../../visualizations/algorithms/dfs.html).

> The Java SE API documentation for ArrayDeque describes add and remove as queue operations at the tail and head, and push as insertion at the front, which is all our code relies on. Nothing in the standard library does topological sorting for you, so being able to write one in about twenty lines is useful. The two visualizations let you replay the algorithms on other graphs; try predicting each step before you advance it.

---
@type section
# Wrap-up

---
# Summary

- A topological order puts the tail of every edge before its head.
- One exists if and only if the directed graph is acyclic.
- Kahn: repeatedly output a source; fewer than $V$ outputs means a cycle.
- DFS: reverse finish order is a topological order of a DAG.
- Directed cycles: a back edge to a gray vertex; parents recover the cycle.
- Undirected cycles: a visited non-parent neighbor, or union-find.

> Everything today costs linear time in V plus E with adjacency lists. Kahn and DFS gave two different valid orders of the same courses, and both detectors found the same broken rule. The next lecture adds weights to edges and asks a different question: not in what order, but by what cheapest route. DFS and BFS will reappear inside those algorithms as well.

---
# Check yourself

- Add the edge Logic -> Sys to the course graph. Rerun Kahn's algorithm by hand. What changes?
- Why can Kahn's algorithm use a stack instead of a queue and still be correct?
- Give a directed graph where the three-color DFS finds a cycle only because of the outer loop.

> Try the first question on paper using the in-degree table; then check that your new order still respects all nine edges. For the second, look back at the invariant: it never mentioned which ready vertex leaves first. For the third, think about the unreachable cycle from the pitfalls slide and make it your own.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 20: Elementary graph algorithms (depth-first search, topological sort).
- CLRS, Chapter 19: Data structures for disjoint sets (union-find).
- A. B. Kahn, "Topological sorting of large networks", *Communications of the ACM* 5(11), 1962, 558–562.
- Java SE API documentation: `java.util.ArrayDeque`, `java.util.Optional`.
- All examples, traces and code are original to this course and were produced by the tested Java files for this lecture.

> CLRS Chapter 20 covers depth-first search, the white-path idea, and topological sorting by finish times. Kahn's 1962 paper is the origin of the source-removal method named after him. The Java API documentation is the authority for what ArrayDeque and Optional guarantee. Every trace in this deck came from running the lecture's Java code on the course graph.
