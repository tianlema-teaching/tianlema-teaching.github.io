@title Lecture 13: B-trees and B+ trees
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# B-trees and B+ trees
## Wide, shallow search trees for data stored in blocks

---
# Where we are

- L10 and L11: AVL and red-black trees keep a binary search tree balanced.
- L12: hash tables give expected $O(1)$ lookups but no key order.
- Both assume every node costs about the same to reach.
- Today: data on disk or SSD, where reaching a node is the expensive part.
- Next time: priority queues and heaps.

> So far we have measured cost by counting comparisons or steps in memory. Balanced binary trees give logarithmic height, and hash tables give expected constant time, but neither keeps order and low cost when the data is too large for memory. Today we change the cost model. When a tree lives on a disk or a solid-state drive, the time is dominated by how many nodes we fetch from storage, and that leads to a very different tree shape: wide nodes and very few levels.

---
# By the end of today you can

- State the B-tree properties for minimum degree $t$.
- Bound the height of a B-tree with $n$ keys.
- Search a B-tree and count the nodes it reads.
- Trace insertion with node splitting, including a root split.
- Explain how a B+ tree differs and why it suits range queries.

> These are the observable skills for today. You should be able to check whether a drawn tree is a valid B-tree, compute an upper bound on its height, and trace a sequence of insertions by hand, splitting full nodes at the right moments. Finally you should be able to explain the difference between a B-tree and a B+ tree, and why the second is the usual choice for database indexes.

---
# Storage is read in blocks

- A disk or SSD transfers a whole **block** (a page) at a time, not one key.
- Fetching a block from storage is far slower than working in memory.
- So the cost that matters is the number of blocks touched.
- Idea: make one tree node fill one block.
- Then cost is the number of nodes read on a root-to-leaf path.

> Storage devices do not hand you a single integer. They read and write fixed-size blocks, often a few kilobytes, and the operating system and database move whole blocks into memory. Getting a block from storage is much slower than any amount of comparing inside a block that is already in memory. So a good on-disk structure minimizes the number of blocks it touches. If each tree node is exactly one block, that number is the number of nodes on the search path, which is the height plus one.

---
# Why a wide tree wins

```diagram
@dir LR
B[binary node | 1 key, 2 children] -> T[tall tree | many block reads]
W[wide node | hundreds of keys] -> S[short tree | few block reads]
```

- A balanced binary tree with $10^6$ keys has about 20 levels.
- With about 1000 children per node, depth 3 already has $1000^3 = 10^9$ nodes.
- Scanning keys inside one node is cheap: the block is already in memory.

> A binary search tree node holds one key, so a block holding it is mostly wasted space, and the path from the root is long: about log base two of n levels, roughly twenty for a million keys. A B-tree packs many keys into each node, so each node has many children. With a branching factor near a thousand, three levels below the root already fan out to a billion positions. We do more comparisons per node, but those happen in memory, while each level saved is one fewer block read.

---
# One running example

- Minimum degree $t = 2$: every node holds 1 to 3 keys.
- Insert `10, 20, 30, 40, 50, 60, 70, 80, 90, 25`, in that order.
- Mostly increasing keys: the worst input for a plain BST.
- Every tree drawn today was produced by `BTree.java` on this sequence.

```diagram
@dir LR
A[10] -> B[20] -> C[30] -> D[40] -> E[50] -> F[...] -> G[90] -> H[25]
```

> Here is the sequence we will trace. With t equal to two, nodes are small enough to draw, and each node holds one, two or three keys. The first nine keys arrive in increasing order, which would turn an ordinary binary search tree into a linked list. Watch the B-tree stay perfectly balanced instead. The last key, twenty-five, lands in the middle to show an insertion that is not at the right edge. Every state on the following slides was printed by the Java code and is asserted in the check file.

---
@type section
# The B-tree definition

---
# B-tree properties (CLRS, minimum degree t)

- Each node stores keys in increasing order: $k_0 < k_1 < \dots < k_{r-1}$.
- An internal node with $r$ keys has exactly $r + 1$ children.
- Keys separate subtrees: everything in child $i$ lies between $k_{i-1}$ and $k_i$.
- All leaves have the same depth.
- Every node except the root has at least $t - 1$ keys; every node has at most $2t - 1$.
- The root of a nonempty tree has at least 1 key. Here $t \ge 2$.

> These are the defining properties in the style of CLRS, with t called the minimum degree. The first three generalize a binary search tree: a node with r keys splits the key range into r plus one intervals, one per child. The fourth is the balance condition, and it is stronger than AVL or red-black balance: every leaf is at exactly the same depth. The last two say how full a node may be. The root is exempt from the minimum, because a tree with one key must still be a tree. Our code stores distinct keys and rejects duplicates.

---
# Reading one node

```diagram
@dir TB
N[20, 40] -> A[keys below 20]
N -> B[keys between 20 and 40]
N -> C[keys above 40]
```

- Two keys, three children, three key ranges.
- A search for 25 compares with 20, then 40, and follows the middle child.

> One internal node with keys twenty and forty. The left child holds everything smaller than twenty, the middle child everything between twenty and forty, and the right child everything larger than forty. Searching inside a node is a small search over its sorted keys: find the first key that is not smaller than the target. If it equals the target we are done; otherwise that position tells us which child to follow. Twenty-five is above twenty and below forty, so it goes to the middle.

---
# Minimum degree t versus order m

| $t$ | Keys per non-root node | Children per internal non-root node | Name |
|---|---|---|---|
| 2 | 1 to 3 | 2 to 4 | 2-3-4 tree |
| 3 | 2 to 5 | 3 to 6 | |
| $t$ | $t-1$ to $2t-1$ | $t$ to $2t$ | |

- Other books describe a B-tree by its **order** $m$: the maximum number of children.
- CLRS's minimum degree $t$ gives order $m = 2t$. Check which convention a text uses.

> Textbooks disagree on how to name the size of a B-tree node. CLRS uses the minimum degree t, the smallest number of children a non-root internal node may have. Many other books and papers use the order m, the largest number of children. With CLRS's definition the maximum is two t, so the order is always even; books using order m also allow odd orders, such as the two-three tree with m equal to three. When you read another source, check which convention it uses before comparing formulas. With t equal to two, nodes have two, three or four children, which is why it is called a two-three-four tree.

---
# How tall can a B-tree be?

$$h \le \log_t \frac{n+1}{2}$$

- Height $h$ counts edges from the root to a leaf, as in CLRS.
- Root: at least 1 key; if it is not a leaf, at least 2 children. Other internal nodes: at least $t$ children.
- So depth $d \ge 1$ holds at least $2t^{d-1}$ nodes, each with $\ge t - 1$ keys.
- Adding up: $n \ge 1 + (t-1)\sum_{d=1}^{h} 2t^{d-1} = 2t^h - 1$.

> Height counts edges, so a single-node tree has height zero. To bound the height, imagine the sparsest possible tree of height h. The root has one key and two children. Every other internal node has only t children, so the number of nodes multiplies by t per level: two at depth one, two t at depth two, and so on. Each non-root node has at least t minus one keys. The geometric sum collapses to two t to the h, minus one. Solving n at least two t to the h minus one for h gives the bound on the slide.

---
# The bound, for a million keys

| $t$ | Bound on $h$ for $n = 10^6$ | Nodes read by a search, at most |
|---|---|---|
| 2 | 18 | 19 |
| 50 | 3 | 4 |
| 500 | 2 | 3 |

- Compare: a balanced binary tree needs at least $\lfloor \log_2 n \rfloor = 19$ edges.

> Plug in one million keys. With t equal to two the bound is about eighteen point nine, so height at most eighteen, much like a binary tree. With t equal to fifty the logarithm base fifty is about three point three five, so the height is at most three. With t equal to five hundred, at most two. A search reads one node per level, so at most height plus one nodes. The height grows with the logarithm base t, so a larger t shrinks it quickly. The check file tests this bound after thousands of random insertions for t from two to five.

---
# Quiz: which tree is a valid B-tree?

```quiz
With t = 2, which of these is a valid B-tree?
- [ ] Root [20] with children [10] and [30, 40, 50, 60]
- [ ] Root [20, 40] with children [10] and [30]
- [x] Root [20, 40] with children [10], [30] and [50, 60]
- [ ] Root [40] with children [20] and [50], where [20] has children [10] and [30]
```

> The third option is valid. The first has a child with four keys, but with t equal to two a node holds at most three. The second has two keys in the root but only two children; an internal node with two keys needs exactly three. The fourth puts leaves at different depths: ten and thirty are at depth two, while fifty is a leaf at depth one. The third is exactly the tree our code builds after inserting sixty, which you will see in a few minutes.

---
@type section
# Search

---
# B-tree search in pseudocode

```algorithm
function B-TREE-SEARCH(x, k):   // x is a node, k the key
  i ← 0
  while i < x.n and k > x.key[i] do
    i ← i + 1                    // first key ≥ k
  if i < x.n and k = x.key[i] then
    return (x, i)                // found in this node
  if x is a leaf then
    return not found
  READ x.child[i] from storage   // one block read
  return B-TREE-SEARCH(x.child[i], k)
```

> This is the binary-search-tree search generalized to many keys per node. Scan the node's keys to find the first one that is at least k. If it equals k, stop. Otherwise the index i is exactly the child whose range contains k, because every key before position i was smaller. The line that reads the child from storage is the expensive one, and it runs at most once per level. A linear scan inside the node is fine for small t; for large t a binary search over the keys cuts the comparisons to about log base two of t per node.

---
# Search in Java

```java
Node<K> x = root;
while (true) {
    int i = 0;
    while (i < x.keys.size() && key.compareTo(x.keys.get(i)) > 0) {
        i++;                          // first key >= the search key
    }
    if (i < x.keys.size() && key.compareTo(x.keys.get(i)) == 0) {
        return true;
    }
    if (x.isLeaf()) {
        return false;
    }
    x = x.children.get(i);            // descend between keys i-1 and i
}
```

- A node keeps `List<K> keys` and `List<Node<K>> children`; a leaf has no children.

> This is the body of contains, written as a loop instead of recursion. The inner while loop finds the index i, using compareTo, since keys are Comparable. The first if checks for a match; the second stops at a leaf. The last line descends into child i, which in a real on-disk tree would be the block read. Our nodes use ArrayLists for readability. A disk-based B-tree would instead lay out each node as a fixed-size array of keys and child block numbers that fits one block.

---
# Search trace on the final tree

```diagram
@dir TB
@reveal manual
R[40] -> A[20]
R -> B[60]
A -> L1[10]
A -> L2[25, 30]
B -> L3[50]
B -> L4[70, 80, 90]
focus R
--- Search 25: at the root, 25 < 40, so take child 0.
focus R A
--- At [20]: 25 > 20, so take child 1.
focus R A L2
--- At [25, 30]: found. Three nodes read.
```

> This is the tree after all ten insertions. A search for twenty-five reads the root, goes left because twenty-five is below forty, reads the node holding twenty, goes right, and finds twenty-five in the leaf. That is three node reads, which is the height plus one. An unsuccessful search, say for thirty-five, follows the same path and fails at the leaf, also after three reads. A search for forty stops at the root after one read. The check file asserts all three counts.

---
@type section
# Insertion

---
# Insert into a leaf, split when full

- New keys always go into a **leaf**, at their sorted position.
- A node is **full** when it has $2t - 1$ keys; it has no room.
- **Split** a full node: its median key moves up into the parent.
- The lower $t - 1$ keys and the upper $t - 1$ keys become two nodes.
- The parent gains one key and one child.

> Insertion in a B-tree never creates a new leaf at a deeper level; that is how all leaves stay at the same depth. The new key goes into the correct leaf. The only problem is a leaf that is already full. Then we split it: the middle key moves up to the parent, where it becomes the separator between the two halves. With t equal to two, a full node has three keys, the middle one moves up, and each half keeps one key. But the parent receives a key, so the parent must have room for it.

---
# Splitting a full child

```diagram
@dir TB
@reveal manual
group Full node { Y }
group After the split { P Lo Hi }
Y[30, 40, 50]
focus Y
--- A full node with t = 2 has 3 keys. The median is 40.
P[parent gains 40] -> Lo[30]
P -> Hi[50]
focus P Lo Hi
--- 40 moves up; 30 stays in the old node; 50 moves to a new sibling.
```

- The two halves stay at the same depth: no leaf moves deeper.

> Here is one split, taken from our trace when sixty arrives. The full node holds thirty, forty and fifty. The median, at index t minus one counting from zero, is forty. It moves up into the parent, between the keys that were already there. The old node keeps thirty, and a new node takes fifty. If the node had children, it would also give its upper t children to the new sibling. The split keeps key order and depth, and adds exactly one key to the parent.

---
# Split on the way down (CLRS)

- Insert walks down from the root to a leaf, once.
- Before stepping into a **full** child, split it.
- So every node we enter has room for a key moving up.
- If the **root** is full, split it first: a new root holds its median.
- That root split is the only way a B-tree grows taller.

> CLRS inserts in a single downward pass, splitting proactively. Whenever the next child on the path is full, it is split before we descend, so the parent we are standing in always has space for the median. That guarantee comes from the previous step: we only stand in nodes that were not full. The root has no parent, so a full root gets a new empty root above it, and then it is split as that root's child. The tree grows at the top, never at the bottom, which is why every leaf stays at the same depth.

---
# Insertion in pseudocode

```algorithm
function B-TREE-INSERT(T, k):
  if T.root is full then          // 2t - 1 keys
    s ← new node with child T.root
    T.root ← s
    SPLIT-CHILD(s, 0)             // height grows by one
  INSERT-NONFULL(T.root, k)

function INSERT-NONFULL(x, k):    // x is not full
  while x is not a leaf do
    i ← index of the child whose range contains k
    if x.child[i] is full then
      SPLIT-CHILD(x, i)
      if k > x.key[i] then i ← i + 1   // k goes right of the new median
    x ← x.child[i]
  insert k into x in sorted position
```

> The first function handles the root. The second walks down. Look at the line after SPLIT-CHILD: the split just put a new median at position i of x, so we compare k with that median to decide whether k belongs in the left half or the new right half. Forgetting that comparison is the classic bug; the key then lands in the wrong subtree and breaks the ordering property. When the loop ends we are at a leaf that is not full, so inserting there is safe.

---
# SPLIT-CHILD in Java

```java
private void splitChild(Node<K> x, int i) {
    Node<K> y = x.children.get(i);
    Node<K> z = new Node<>();
    z.keys.addAll(y.keys.subList(t, 2 * t - 1));          // upper t-1 keys
    if (!y.isLeaf()) {
        z.children.addAll(y.children.subList(t, 2 * t));  // upper t children
        y.children.subList(t, 2 * t).clear();
    }
    K median = y.keys.get(t - 1);
    y.keys.subList(t - 1, 2 * t - 1).clear();             // y keeps t-1 keys
    x.keys.add(i, median);
    x.children.add(i + 1, z);
}
```

> Here y is the full child and z its new right sibling. Indices count from zero, so y's keys are at positions zero to two t minus two, and the median is at t minus one. The new sibling copies keys t through two t minus two, and, if y is internal, children t through two t minus one. Then y drops its upper part including the median. Look at the last two lines: the median goes into the parent at position i, and z becomes child i plus one, right after y. The subList calls copy or clear ranges of an ArrayList with half-open bounds.

---
# Insert in Java

```java
public boolean insert(K key) {
    // ...
    if (contains(key)) {
        return false;
    }
    if (root.keys.size() == 2 * t - 1) {  // full root: the tree grows by one level
        Node<K> s = new Node<>();
        s.children.add(root);
        root = s;
        splitChild(s, 0);
    }
    insertNonFull(root, key);
    size++;
    return true;
}
```

> The omitted lines reject a null key. Our tree stores each key at most once, so insert first checks whether the key is present and returns false if so; that extra search costs one more root-to-leaf pass. The full-root case matches the pseudocode: a new empty root adopts the old root as its only child, and splitting that child gives the new root exactly one key. After that, insertNonFull walks down from a root that is guaranteed not to be full.

---
# INSERT-NONFULL in Java

```java
private void insertNonFull(Node<K> x, K key) {
    while (!x.isLeaf()) {
        int i = 0;
        while (i < x.keys.size() && key.compareTo(x.keys.get(i)) > 0) {
            i++;
        }
        if (x.children.get(i).keys.size() == 2 * t - 1) {
            splitChild(x, i);             // split before descending
            if (key.compareTo(x.keys.get(i)) > 0) {
                i++;                      // the key belongs right of the new median
            }
        }
        x = x.children.get(i);
    }
    // ...
```

> The loop finds the child index exactly as search does, then checks whether that child is full. If it is, it splits the child and compares the key with the median that just arrived at position i. The descent line then moves to a child that is guaranteed not to be full. The omitted tail runs once the loop reaches a leaf: it finds the sorted position with the same scan and inserts the key there with a list add.

---
@type section
# Insertion trace, t = 2

---
# Filling the root: 10, 20, 30

```diagram
@dir LR
@reveal manual
S1[10]
focus S1
--- Insert 10: the root is a leaf with one key.
S1 -> S2[10, 20]
focus S1 S2
--- Insert 20: still one leaf.
S2 -> S3[10, 20, 30]
focus S2 S3
--- Insert 30: the root now holds 2t - 1 = 3 keys. It is full.
```

> The first three keys go into the single root node, which is also a leaf. Each key lands in its sorted position. After thirty the root holds three keys, the maximum for t equal to two. Nothing has split yet, and the height is zero. The next insertion will find a full root before it even starts walking down.

---
# Insert 40: the root splits

```diagram
@dir TB
@reveal manual
group Full root { F }
group After the split and 40 { R A B }
F[10, 20, 30]
focus F
--- The root is full, so split it before inserting 40.
R[20] -> A[10]
R -> B[30, 40]
focus R A B
--- New root [20]. 40 > 20, so it goes into the leaf [30], giving [30, 40].
```

> Insert sees a full root. It creates a new root, makes the old root its child, and splits: the median twenty moves up, ten stays left, thirty goes to a new right sibling. Now the tree has height one. Insertion continues from the new root: forty is greater than twenty, so it descends to the right leaf, which has room, and forty joins thirty. Every leaf is at depth one.

---
# Insert 50, then 60: a leaf splits

```diagram
@dir TB
@reveal manual
group After 50 { R A B }
group After 60 { R2 C1 C2 C3 }
R[20] -> A[10]
R -> B[30, 40, 50]
focus R A B
--- Insert 50: it joins the right leaf, which is now full.
R2[20, 40] -> C1[10]
R2 -> C2[30]
R2 -> C3[50, 60]
focus R2 C1 C2 C3
--- 60 heads for [30, 40, 50], which is full: 40 moves up, then 60 joins [50].
```

> After fifty the right leaf holds three keys and is full. When sixty arrives, the root has room, but the child sixty would enter is full, so we split that child before descending: forty moves up into the root, thirty stays, and fifty moves to a new leaf. Sixty is greater than the new median forty, so it goes right, into the leaf with fifty. The root now has two keys and three children. This is the tree from the quiz.

---
# Insert 70, then 80

```diagram
@dir TB
@reveal manual
R[20, 40, 60] -> A[10]
R -> B[30]
R -> C[50]
R -> D[70, 80]
focus R C D
--- 70 made [50, 60, 70]. Then 80 split it: 60 moved up, 80 joined [70].
focus R
--- The root now holds 3 keys: it is full.
```

> Seventy simply joins the rightmost leaf, which becomes fifty, sixty, seventy, full again. Eighty then finds that leaf full on its way down, so the leaf splits: sixty moves up into the root, fifty stays, and seventy moves to a new leaf that eighty then joins. The tree is still only one level deep, but the root now holds three keys: twenty, forty and sixty. Any further insertion will begin with a root split.

---
# Insert 90: the tree grows taller

```diagram
@dir TB
@reveal manual
R[40] -> A[20]
R -> B[60]
focus R A B
--- The full root [20, 40, 60] splits first: a new root [40].
A -> L1[10]
A -> L2[30]
B -> L3[50]
B -> L4[70, 80, 90]
focus R B L4
--- 90 > 40 and 90 > 60: it goes to [70, 80], which had room.
```

> The root is full, so insertion starts with a root split: forty becomes the only key of a brand new root, and the nodes with twenty and with sixty become its children, each keeping two of the old root's four children. The height is now two. Then ninety walks down: right at forty, right at sixty, into the leaf with seventy and eighty, which is not full. Notice that proactive splitting grew the tree even though the target leaf had room; an algorithm that splits only after a node overflows would not have split here.

---
# Insert 25: the final tree

```diagram
@dir TB
@reveal manual
R[40] -> A[20]
R -> B[60]
A -> L1[10]
A -> L2[25, 30]
B -> L3[50]
B -> L4[70, 80, 90]
focus R A L2
--- 25 < 40, then 25 > 20: it joins [30] as [25, 30]. No splits.
```

- 10 keys, 7 nodes, height 2. In-order: `10 20 25 30 40 50 60 70 80 90`.

> Twenty-five goes left at the root and right at twenty, into the leaf with thirty. Neither node on the path was full, so nothing splits. Check every property on this final tree: keys are sorted within each node, each internal node has one more child than keys, the separators bound their subtrees, every node has between one and three keys, and all four leaves are at depth two. The in-order traversal visits the keys in sorted order.

---
# In-order traversal

```java
private void inOrder(Node<K> x, List<K> out) {
    for (int i = 0; i < x.keys.size(); i++) {
        if (!x.isLeaf()) {
            inOrder(x.children.get(i), out);   // subtree left of key i
        }
        out.add(x.keys.get(i));
    }
    if (!x.isLeaf()) {
        inOrder(x.children.get(x.keys.size()), out);   // rightmost subtree
    }
}
```

- Visits every node once: $\Theta(n)$ time for $n$ keys.

> This generalizes the in-order traversal from L09. For each key, first visit the subtree to its left, then emit the key. After the last key, visit the rightmost subtree. Because the keys separate the subtrees, this produces all keys in increasing order. The check file compares this output with a java dot util dot TreeSet after thousands of random insertions, which tests ordering and completeness at once.

---
# Quiz: one more insertion

```quiz
Insert 100 into the final tree (root [40]; [20] and [60]; leaves [10], [25, 30], [50], [70, 80, 90]). What happens?
- [ ] 100 joins [70, 80, 90], making four keys
- [ ] The root splits and the height becomes 3
- [x] [70, 80, 90] splits: 80 moves up into [60], and 100 joins [90]
- [ ] [60] splits and 60 moves up into the root
```

> The third option is right. The root holds one key and the node with sixty holds one key, so neither is full. The next child on the path, seventy, eighty, ninety, is full, so it splits before we enter it: eighty moves up, making the parent sixty, eighty; seventy stays; ninety moves to a new leaf. One hundred is greater than eighty, so it joins ninety. A leaf can never hold four keys, and the height stays two because the root was not full. The check file asserts this exact result.

---
@type section
# Why it works and what it costs

---
# Why insertion keeps every property

- Split: $2t - 1$ keys become $t - 1$, $1$ moved up, and $t - 1$: both halves legal.
- The parent was not full, so it can take the median.
- Split keeps key order: the median separates the two halves.
- Leaves never move deeper; a root split lifts all leaves by one level together.
- The key lands in a non-full leaf, which then has at most $2t - 1$ keys.

> Walk through each property. Size: a full node has two t minus one keys, and splitting leaves t minus one on each side, exactly the minimum, with one key moving up. The parent has room because we never enter a full node. Order: the median is larger than every key in the left half and smaller than every key in the right half, so it is a correct separator. Depth: a split creates a sibling at the same depth, and a root split adds one level above every leaf at once. The validator in the check file tests all of these after random insertions.

---
# Costs

| Operation | Node reads or writes | CPU time |
|---|---|---|
| Search | $O(h) = O(\log_t n)$ | $O(t \log_t n)$ |
| Insert | $O(\log_t n)$ | $O(t \log_t n)$ |
| In-order traversal | $\Theta(\text{nodes})$ | $\Theta(n)$ |

- These are worst-case bounds. Space: $\Theta(n)$.
- With binary search inside each node, search CPU time drops to $O(\log n)$.

> Every operation touches one node per level, and there are at most log base t of n levels, so at most that many block reads. Inside each node we may scan up to two t minus one keys, and a split copies about t keys, which gives order t log base t of n CPU time. For large t, binary search inside the node reduces the search part to log t per level, and log t times log base t of n is log n. Because every non-root node fills at least t minus one of its two t minus one key slots, close to half for large t, space is linear in n. These bounds are worst case, not averages.

---
# Deletion, in outline

- A key in an internal node is replaced by its predecessor or successor, or its two children merge.
- A node must not drop below $t - 1$ keys.
- **Borrow**: take a key through the parent from a sibling with $t$ or more keys.
- **Merge**: join two minimum siblings with the separator pulled down from the parent.
- If the root loses its last key, its only child becomes the root: height shrinks.
- Our Java code implements search and insertion, not deletion.

> Deletion is the mirror image of insertion. Insertion prevents overfull nodes by splitting; deletion prevents underfull nodes by borrowing or merging. CLRS again works top-down: before descending into a child with only t minus one keys, it either rotates a key in from a richer sibling through the parent, or merges the child with a sibling and the separating key. A key in an internal node is replaced by its predecessor or successor from a leaf. When a merge empties the root, the tree loses a level at the top, just as insertion adds one there. We do not trace deletion today, because our code does not implement it.

---
@type section
# B+ trees

---
# B+ trees: records live in the leaves

```diagram
@dir TB
R[30, 60] -> L1[10, 20]
R -> L2[30, 40, 50]
R -> L3[60, 70, 80]
```

- Internal nodes hold only **routing keys** (often copies), used to choose a child.
- Every key, with its record or a pointer to it, is in a leaf.
- Leaves are linked in key order: `[10, 20] → [30, 40, 50] → [60, 70, 80]`.

> A B+ tree is a variant with a division of labor. The leaves hold all the keys together with their records, or pointers to the records. The internal nodes hold only routing keys, which may repeat keys found in the leaves. This small drawing is an illustration, not output from our code. It uses the convention that a key equal to a routing key goes right, so thirty and sixty appear both in the root and in the leaves. Each leaf also stores a link to the next leaf, shown on the last line, which a plain B-tree does not have.

---
# Why the leaf links matter

- Range query: all keys from 25 to 65.
- Search once for 25: reach the leaf where 25 would be.
- Then walk the leaf links, reporting keys, until a key exceeds 65.
- Reads: one root-to-leaf path, then one read for each further leaf the scan visits.
- A plain B-tree range scan must return to ancestor nodes between leaves: it keeps the path in memory or rereads it.

> Many queries ask for a range, not one key: all orders from the past seven days, all names starting with a given prefix. In a B+ tree, one root-to-leaf search finds the starting point, and then the scan moves sideways along the linked leaves, each leaf read giving many results. In the drawing, the scan for twenty-five to sixty-five goes down through the root to the leaf starting at ten, then reads the next two leaves, and stops at seventy: four node reads. In a plain B-tree, keys are also stored in internal nodes, so an in-order scan must return to an ancestor between two leaves. It either keeps the whole path in memory or reads those ancestors again.

---
# Why databases and file systems use B+ trees

- Internal nodes hold only keys and child pointers, so more fit per block.
- A higher fan-out means an even shorter tree.
- Range scans and sorted output follow the leaf links.
- Many relational database indexes and several file systems use B+ trees or close variants.

> The design choices line up with the block cost model. Records can be large, and keeping them out of the internal nodes lets each internal block hold many more routing keys, which raises the branching factor and lowers the height. Sequential leaf scans suit ordered queries. For these reasons B+ trees and close relatives are the standard index structure in many relational databases and appear in several file systems. Details differ from system to system, so treat any specific product's design as something to look up in its own documentation.

---
# B-tree versus B+ tree

| Question | B-tree | B+ tree |
|---|---|---|
| Where are the records? | In every node | Only in leaves |
| Internal node keys | Real keys, stored once | Routing keys, often copies |
| Search ends | As soon as the key is found | Always at a leaf |
| Range scan | In-order walk up and down | Follow linked leaves |
| Fan-out for a given block size | Lower | Higher |

> Read the table row by row. In a B-tree each key appears exactly once, possibly in an internal node, so a search can stop early at the root. In a B+ tree every search goes all the way to a leaf, but all leaves are at the same depth, so the cost is predictable and the path is short. The higher fan-out comes from internal nodes that store only keys and pointers. The leaf links make range scans cheap. That combination is why B+ trees dominate on disk.

---
# A 2-3-4 tree corresponds to a red-black tree

```diagram
@dir LR
N[2-3-4 node with 2 keys | 20, 40] -> B[black 40 | red child 20]
N -> C[or black 20 | red child 40]
```

- Each 2-3-4 node becomes one black node plus 0, 1 or 2 red children.
- All leaves at the same depth becomes: equal black-height on every path.
- A 2-3-4 split corresponds to a color flip in the red-black tree.

> This links today to L11. Take any node of a two-three-four tree, that is, a B-tree with t equal to two. A node with one key becomes a single black node. A node with two keys becomes a black node with one red child, either way round. A node with three keys becomes a black node with two red children. Red nodes are never adjacent, and the rule that every leaf is at the same depth becomes the rule that every path has the same number of black nodes. That is why red-black trees are balanced. Java's TreeMap is a red-black tree; the Java library has no B-tree class.

---
@type section
# Wrap-up

---
# Common mistakes and edge cases

- Mixing up minimum degree $t$ with order $m$ when using formulas.
- Allowing a non-root node below $t - 1$ keys, or above $2t - 1$.
- Using the wrong median index: it is $t - 1$ when counting from 0.
- After a split, descending without comparing the key with the new median.
- Forgetting that the root may hold a single key.
- Duplicates: decide whether to reject them (our code) or allow them.

> These are the errors to look for in a trace or in code. The first two come from convention clashes and from forgetting the root's exemption. The median off-by-one produces halves of unequal size, which the validator catches as an underfull node. The missing comparison after a split sends keys into the wrong subtree, which the validator catches as a key outside its separator range. The empty tree and the one-key tree are the edge cases every test should include, and the check file does.

---
# How the Java code is tested

- `BTreeValidator` checks every property: key counts, order, separator ranges, children counts, equal leaf depth, size.
- The running-example states are asserted exactly after every insertion.
- Random insertions for $t = 2$ to $5$ are compared with `java.util.TreeSet`.
- Sorted input of 2000 keys stays within $h \le \log_t \frac{n+1}{2}$.
- Hand-broken trees confirm that the validator rejects them.

> A validator is the best test for a balanced tree, because the properties are exactly what the algorithm promises. The check file runs the validator after insertions with several minimum degrees and seeds, compares search results and in-order output with the library's TreeSet, and checks the height bound. It also breaks a valid tree on purpose, once by reordering keys and once by emptying a leaf, and confirms that the validator notices. A validator that never fails might be checking nothing.

---
# Summary

- Storage reads blocks, so count the nodes read on a path.
- B-tree: sorted wide nodes, $t - 1$ to $2t - 1$ keys, all leaves at one depth.
- Height at most $\log_t \frac{n+1}{2}$: search and insert read $O(\log_t n)$ nodes.
- Insertion splits full nodes on the way down; only a root split adds height.
- B+ tree: records in linked leaves, routing keys above: fast range scans.

> The key idea is a cost model: when reaching a node is expensive, make nodes wide so the tree is shallow. The B-tree properties keep every non-root node filled to at least t minus one of two t minus one slots, close to half for large t, and every leaf at the same depth, which gives a height logarithmic with base t. Insertion keeps those properties by splitting full nodes before entering them. The B+ tree moves all records to linked leaves, trading early exits for higher fan-out and cheap range scans. Next time we leave search trees for priority queues and heaps.

---
# Check yourself

- Insert `1, 2, 3, 4, 5, 6` into an empty B-tree with $t = 2$. Draw each split.
- What is the largest possible height of a B-tree with $t = 3$ and $n = 100$ keys?
- Why is a range scan simpler and more sequential in a B+ tree than in a B-tree?

> Try these on paper. The first is a smaller version of our trace, so you can compare your drawings with the pattern you saw. For the second, plug the numbers into the height bound and remember that the height must be a whole number. For the third, compare the order in which each scan visits nodes: sideways along the leaf links in a B+ tree, back up through ancestors between leaves in a B-tree, and what each costs when a node is a block. If you can do all three, you understand today's material.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 18 (B-trees) and Chapter 13 (red-black trees).
- R. Bayer and E. McCreight, "Organization and maintenance of large ordered indices", *Acta Informatica* 1 (1972) 173–189: the original B-tree paper.
- Java SE API documentation: `java.util.TreeMap`, `java.util.TreeSet`, `java.util.List`.

> The definition with minimum degree t, the height bound, and the top-down insertion with proactive splitting follow CLRS Chapter 18. B-trees were introduced by Bayer and McCreight. The link between two-three-four trees and red-black trees connects to Chapter 13. The B+ tree description is the standard one; the small B+ tree drawing is an illustration made for this lecture. All B-tree states on the slides were produced by the lecture's Java code.
