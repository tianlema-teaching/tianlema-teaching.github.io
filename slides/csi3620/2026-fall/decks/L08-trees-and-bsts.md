@title Lecture 8: Trees and binary search trees
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Trees and binary search trees
## Keep keys in order and search by walking down

---
# Where we are

- L03: binary search finds a key in a sorted array in $O(\log n)$ steps.
- L04-L05: sorting puts an array in that searchable order.
- L06-L07: linked nodes grow and shrink without shifting an array.
- Today: linked nodes arranged so that search still halves the work.

> Binary search is fast because each comparison throws away one side of a sorted array. The weakness is the array itself: inserting a new key in the middle shifts everything after it. Linked lists fixed shifting but lost the halving, because a list can only be walked from one end. Today we link nodes in a branching shape, so each comparison again chooses one side, and new keys attach without shifting anything.

---
# By the end of today you can

- Name the root, leaves, depth and height of any node in a drawn tree.
- Decide whether a binary tree is full, complete or perfect.
- Check the BST property and trace search, insert, min, max and successor.
- Delete a key in each of the three cases and redraw the tree.
- State why every operation costs $O(h)$ and when $h$ grows to $n-1$.

> Each outcome is something you can do with a pencil. Given a picture, you should be able to point at a node and say its depth and height. Given a key, you should be able to walk the path a search takes, and given a deletion, you should be able to draw the tree that results. The last outcome is about cost: the height of the tree decides everything, so you will learn what shapes make it small and what shapes make it large.

---
# Hierarchies are everywhere

```diagram
@dir LR
@reveal all
Root[/] -- Home[home]
Root -- Etc[etc]
Home -- Ana[ana]
Home -- Ben[ben]
Ana -- Notes[notes.txt]
Ana -- Code[Main.java]
```

- A file system: every folder sits inside exactly one parent folder.
- An org chart: every employee reports to one manager.
- An expression: `(3 + 4) * 2` is `*` applied to `3 + 4` and `2`.

> Look at the folder picture. Every item except the top one lives inside exactly one folder, and there are no loops: a folder never contains itself. An org chart has the same shape, with each person reporting to exactly one manager. Even arithmetic has it: the multiplication sits on top, and its two operands hang below it, one of them being a smaller addition. That shape, one top and a single parent for everything else, is what the word tree means in this course.

---
# The problem for today

| Structure | Search | Insert or delete |
|---|---|---|
| Sorted array | $O(\log n)$ binary search | $O(n)$ shifting |
| Linked list | $O(n)$ scan | $O(1)$ once you hold the spot |
| Binary search tree | $O(h)$ | $O(h)$ |

- Goal: a dynamic set that supports search, insert, delete and ordered queries.
- $h$ is the tree's height; the second half of today says how big it gets.

> A dynamic set is a collection of keys that changes over time: you add keys, remove keys, and ask whether a key is present. Ordered queries ask things like the smallest key or the next key after forty. Neither the sorted array nor the linked list is good at all of these at once. A binary search tree makes every one of them cost time proportional to its height. Whether that is fast depends on the height, and that is the question we answer at the end.

---
@type section
# Tree vocabulary

---
# What is a tree?

- A **tree** is a set of **nodes** joined by **edges**.
- One node, the **root**, has no parent.
- Every other node has exactly one **parent**.
- Following parents from any node always reaches the root: no cycles.
- A nonempty tree with $n$ nodes has exactly $n - 1$ edges.

> Think of an edge as a line from a parent down to a child. Every node except the root has exactly one edge coming down into it, from its parent, which is why a nonempty tree has one edge fewer than it has nodes. Following parent links upward can never loop, so it always ends at the root. The empty tree, with no nodes at all, also counts as a tree; it is the base case for every recursive method today.

---
# The running tree
## Inserted as 50, 30, 70, 20, 40, 80, 35, 45

```diagram
@dir TB
@reveal all
N50((50)) -- N30((30))
N50 -- N70((70))
N30 -- N20((20))
N30 -- N40((40))
N70 -- X70[nil].gray
N70 -- N80((80))
N40 -- N35((35))
N40 -- N45((45))
```

> Copy this tree onto paper now; every trace today starts from it. It holds eight keys, inserted in the order in the subtitle. The gray box under seventy is not a node: it only shows that seventy has no left child, so that eighty is clearly its right child. Leaves such as twenty have no children at all, and we do not draw anything under them. Later in the lecture we will build this exact tree by inserting the keys in the order listed.

---
# Family words

```diagram
@dir LR
@reveal all
N50[50 | root] -- N30[30 | internal]
N50 -- N70[70 | internal]
N30 -- N20[20 | leaf]
N30 -- N40[40 | internal, children not drawn]
N70 -- N80[80 | leaf]
```

- 20 and 40 are **children** of their **parent** 30, and **siblings**.
- A **leaf** has no children; an **internal node** has at least one.

> This is the top of the running tree, labeled. Forty's children, thirty-five and forty-five, are left out of this picture to save space, which is why forty is labeled internal although nothing hangs below it here. The root is the only node without a parent. Twenty and forty are siblings because they share the parent thirty. A leaf has no children, and everything else is an internal node, so seventy is internal even though it has only one child. Ancestors are the parent, the grandparent and so on up to the root; descendants are the children, grandchildren and so on down.

---
# Paths, depth and levels

- A **path** is a sequence of nodes, each the parent of the next.
- Its length is the number of **edges** on it.
- The **depth** of a node is the length of the path from the root to it.
- Depths in the running tree: 50 is 0; 30 and 70 are 1; 45 is 3.
- **Level** $d$ is the set of all nodes at depth $d$.

> The path from fifty to forty-five goes fifty, thirty, forty, forty-five: four nodes and three edges, so forty-five has depth three. The root always has depth zero. Level one holds thirty and seventy, level two holds twenty, forty and eighty, and level three holds thirty-five and forty-five. Depth measures how far down a node sits, counted from the top.

---
# Height counts edges

- The **height** of a node: edges on the longest path down to a leaf.
- The height of a tree is the height of its root.
- Running tree: height 3, along 50, 30, 40, 35.
- A single node has height 0; the empty tree has height $-1$.
- Some books count nodes instead, giving heights one larger.

> We follow CLRS and count edges. A leaf has height zero, forty has height one, thirty has height two, and the root has height three. Defining the empty tree's height as minus one makes the formula one plus the larger child height work at a leaf. Some textbooks and websites count nodes instead of edges, so a single node has height one there. When you read another source, check which convention it uses before comparing numbers.

---
# Subtrees make trees recursive

- The **subtree** rooted at a node is that node plus all its descendants.
- The subtree at 40 holds 40, 35 and 45; its height is 1.
- A tree is empty, or a root whose children are roots of smaller trees.
- Recursive methods follow this shape: handle `null`, then recurse.

> The recursive view is the one you will program with. Every child of a node is itself the root of a smaller tree, and eventually you reach empty trees, which in Java are null references. So a method that works on trees usually has two parts: what to return for null, and how to combine the answers from the children. Height and size, which you will code in the next lecture, are both one line after the null case.

---
# Quiz: height and depth

```quiz
In the running tree, what are the height of node 30 and the depth of node 80?
- [x] Height 2, depth 2
- [ ] Height 3, depth 2
- [ ] Height 2, depth 3
- [ ] Height 1, depth 2
```

> The longest downward path from thirty is thirty, forty, thirty-five, or thirty, forty, forty-five: two edges, so height two. Height three belongs to the root, not to thirty. Eighty is reached from the root by fifty, seventy, eighty: two edges, so depth two. Height looks down from a node to the leaves; depth looks up from a node to the root.

---
@type section
# Binary trees

---
# Binary trees

- A **binary tree** gives each node at most two children.
- Each child is labeled: a **left child** or a **right child**.
- A node with only a left child differs from one with only a right child.
- The left subtree and the right subtree are both binary trees.

```diagram
@dir TB
@reveal all
A((7)) -- B((3))
A -- XA[nil].gray
C((7)) -- XC[nil].gray
C -- D((3))
```

> The picture shows two different binary trees. Both have seven with a single child three, but on the left three is the left child and on the right it is the right child. In a general tree that distinction would not exist, but in a binary tree it matters, and for search trees it decides whether three is smaller or larger than seven. That is why our pictures show a gray nil when a node has only one child.

---
# Full, complete and perfect

| Term (as used here) | Rule |
|---|---|
| **Full** | Every node has 0 or 2 children |
| **Complete** | Every level is filled except possibly the last, filled left to right |
| **Perfect** | Every internal node has 2 children and all leaves share one depth |

- Every perfect tree is both full and complete.
- The running tree is none of the three: 70 has one child.
- Names differ: CLRS calls a perfect tree *complete*; some books say *full*.

> Read the three rules carefully, because they test different things. Full is about each node separately: no node has exactly one child. Complete is about levels: no gaps anywhere except at the right end of the bottom level. Perfect is the strictest: every level is completely filled. The running tree fails full because seventy has one child, fails complete because level two has a gap under seventy while eighty sits to its right, and so is not perfect either. Be careful with the names, because textbooks disagree. In CLRS's appendix on trees, a complete binary tree has all leaves at the same depth, which is what we call perfect, and other books use full for that same shape. The filled-left-to-right meaning of complete is the heap shape of lecture fourteen. Whenever you answer a question about these shapes, state the definition you are using.

---
# Counting nodes bounds the height

- Level $d$ holds at most $2^d$ nodes; height $h$ allows at most this many:

$$1 + 2 + 4 + \cdots + 2^h = 2^{h+1} - 1$$

- A perfect tree reaches that: height 3 holds $1 + 2 + 4 + 8 = 15$.
- So $n$ nodes need height $h \ge \lfloor \log_2 n \rfloor$; a complete tree achieves it.
- Running tree: 8 nodes do not fit in height 2 (at most 7), and its height is 3.

> The root level has one node, and each level at most doubles the one above, because every node has at most two children. Adding levels zero to h gives a geometric sum, two to the h plus one, minus one, and a perfect tree fills every level exactly. Turn that around: storing n nodes forces n to be at most two to the h plus one minus one, so h is at least log two of n plus one, minus one, and because height is a whole number that rounds to the floor of log two of n. A complete tree achieves that minimum. Our running tree does too, so it is as short as eight nodes allow, even though it is not complete.

---
# A binary tree node in Java

```java
static final class Node<K> {
    K key;
    Node<K> left, right;

    Node(K key) {
        this.key = key;
    }
}
```

- `null` in `left` or `right` means that child is missing.
- The tree object keeps only `root` and a `size` count.

> This node class lives inside our BST class. It holds a key and two references, exactly like a linked-list node with two next pointers instead of one. A missing child is simply null, so the gray nil boxes in our pictures are null references in the code. The tree object itself stores the root reference, which is null for an empty tree, and a count of keys so that size is constant time.

---
@type section
# The BST property

---
# The binary search tree property

- For every node `x` with key $k$:
- every key in the **left subtree** of `x` is less than $k$;
- every key in the **right subtree** of `x` is greater than $k$.
- Duplicates in our code: inserting an existing key is ignored.
- Keys must be comparable: our class requires `K extends Comparable<? super K>`.

> The property talks about whole subtrees, not just children. Everything below and to the left of fifty is smaller than fifty, and everything to the right is larger. Our code stores each key at most once: inserting a key already present returns false and changes nothing. Other designs keep a count per key, or send equal keys consistently to one side; CLRS allows equal keys in either subtree. Always state the duplicate rule you use.

---
# Children alone are not enough

```diagram
@dir TB
@reveal all
A((50)) -- B((30))
A -- C((70))
B -- XB[nil].gray
B -- D((60)).rose
```

- Each pair looks fine, but 60 is in the **left** subtree of 50. Search misses it.

> This is the most common mistake when checking the property by eye. Compare each child with its parent only, and this tree looks valid. But sixty is in fifty's left subtree, and every key there must be smaller than fifty. The consequence is practical: search for sixty compares with fifty, goes right to seventy, and reports not found. A correct check passes down a range of allowed keys, which our test code does.

---
# Why the property helps

- At node `x`, one comparison decides where the target can be.
- Smaller: only the left subtree can hold it. Larger: only the right.
- Each comparison moves one level down and never comes back up.
- This is binary search, with the tree deciding where the middle is.

> In binary search on an array, the middle element splits the remaining range. In a BST, the node you are standing on does the same job: everything smaller is to its left, everything larger to its right. So one comparison discards an entire subtree. The difference from the array is that the split is only as even as the tree's shape, which is why the height matters so much later.

---
@type section
# Search, insert, min, max

---
# Search in pseudocode

```algorithm
function SEARCH(x, k):          // x is a subtree root
  if x = nil then return false  // fell off: k absent
  if k = x.key then return true
  if k < x.key then
    return SEARCH(x.left, k)    // only the left can hold k
  else
    return SEARCH(x.right, k)   // only the right can hold k
```

- Start with `SEARCH(root, k)`.

> Read the three outcomes at each node: equal means found, smaller means go left, larger means go right. The first line handles falling off the bottom of the tree, which is how an unsuccessful search ends. Every call moves one level deeper, so the number of calls is at most the height plus one, plus the final call on nil.

---
# Search for 45, step by step

```diagram
@dir TB
@reveal manual
N50((50)) -- N30((30))
N50 -- N70((70))
N30 -- N20((20))
N30 -- N40((40))
N70 -- X70[nil].gray
N70 -- N80((80))
N40 -- N35((35))
N40 -- N45((45))
focus N50
--- 45 < 50: only the left subtree can hold 45.
focus N50 N30
--- 45 > 30: go right.
focus N50 N30 N40
--- 45 > 40: go right.
focus N50 N30 N40 N45
--- 45 = 45: found after 4 comparisons, at depth 3.
```

> Follow the highlighted path. At fifty, forty-five is smaller, so the whole right side, seventy and eighty, is never looked at. At thirty it is larger, so twenty is skipped. At forty it is larger again, so thirty-five is skipped. The fourth comparison finds it. The code's search path method returns exactly these four keys: fifty, thirty, forty, forty-five.

---
# Recursive search in Java

```java
public boolean contains(K key) {
    Objects.requireNonNull(key, "null key");
    return contains(root, key);
}

private boolean contains(Node<K> x, K key) {
    if (x == null) return false;              // fell off the tree: absent
    int c = key.compareTo(x.key);
    if (c == 0) return true;
    if (c < 0) return contains(x.left, key);  // smaller keys live left
    return contains(x.right, key);            // larger keys live right
}
```

> The public method rejects a null key, then starts the recursion at the root; the private helper takes any subtree. Notice that we call compareTo once and keep the result in c, then test it three ways. compareTo returns a negative number, zero or a positive number, not necessarily minus one or one, so compare c with zero, never with minus one.

---
# Iterative search in Java

```java
public List<K> searchPath(K key) {
    Objects.requireNonNull(key, "null key");
    List<K> path = new ArrayList<>();
    Node<K> x = root;
    while (x != null) {
        path.add(x.key);
        int c = key.compareTo(x.key);
        if (c == 0) break;
        x = (c < 0) ? x.left : x.right;
    }
    return path;
}
```

- Same walk as the recursion, but $O(1)$ extra space besides the path list.

> The recursive search is tail recursive: each call returns the answer of the next call and does nothing afterward. Java does not optimize that away, so the recursion keeps up to h plus one frames on the stack. The loop version replaces the call with an assignment to x. This variant also records every key it compares, which is how we checked the search traces, including the miss for forty-two on the insert slide. The search found the key exactly when the last key in the path equals it.

---
# Insert: search, then attach a leaf

```java
private Node<K> insert(Node<K> x, K key) {
    if (x == null) {                          // empty spot: new leaf
        size++;
        return new Node<>(key);
    }
    int c = key.compareTo(x.key);
    if (c < 0) x.left = insert(x.left, key);
    else if (c > 0) x.right = insert(x.right, key);
    // c == 0: duplicate, ignore
    return x;
}
```

- Called as `root = insert(root, key)`; each call returns its subtree's root.

> Insert walks the same path as an unsuccessful search, and when it falls off the tree it creates a new leaf there. The trick is the return value. Each call returns the root of the subtree it was given, and the caller stores it back into its left or right field. Nothing changes except at the very bottom, where null is replaced by the new node. If the key is found, the duplicate is ignored and the tree is returned unchanged.

---
# A miss for 42, then insert 42

```diagram
@dir TB
@reveal manual
N30((30)) -- N20((20))
N30 -- N40((40))
N40 -- N35((35))
N40 -- N45((45))
focus N30 N40 N45
--- 42 < 50, 42 > 30, 42 > 40, 42 < 45; 45 has no left child, so 42 is absent.
N45 -- N42((42)).green
N45 -- X45[nil].gray
focus N45 N42
--- Insert 42 as the left child of 45. The tree's height grows from 3 to 4.
```

> This picture shows only the subtree under thirty, since fifty, seventy and eighty take no part. An unsuccessful search walks until it needs a child that does not exist. Forty-two follows the same turns as forty-five, then compares with forty-five, is smaller, and finds the left child empty. That empty spot is exactly where forty-two would have to be if it were present, and insert attaches the new leaf there. A new key always becomes a leaf; insert never moves existing nodes. The tree got one level taller, from height three to four, because the new leaf is deeper than any old one.

---
# Building the running tree

| Insert | Compared with | Attached as |
|---|---|---|
| 50 | (empty tree) | root |
| 30, 70 | 50 | left, right child of 50 |
| 20, 40 | 50, 30 | left, right child of 30 |
| 80 | 50, 70 | right child of 70 |
| 35, 45 | 50, 30, 40 | left, right child of 40 |

- Same keys, different order, different tree: order decides shape.

> Our running tree came from these eight inserts. Each row lists the keys the new key was compared with, which searchPath returns before each insert; our checks confirm every row. Fifty became the root only because it came first. If thirty had come first, thirty would be the root and the whole shape would differ, although the tree would hold the same keys in the same sorted order. Keep this in mind for the cost section.

---
# Minimum and maximum

```java
public K min() {
    if (root == null) throw new IllegalStateException("empty tree");
    Node<K> x = root;
    while (x.left != null) x = x.left;        // keep going left
    return x.key;
}
```

- Minimum: keep going left. Running tree: 50, 30, 20. Answer 20.
- Maximum: keep going right. Running tree: 50, 70, 80. Answer 80.
- The minimum need not be a leaf: it has no left child, but may have a right one.

> Every key smaller than the root is on the left, so the smallest key is as far left as you can go. Stop at the first node without a left child; its right subtree, if any, holds only larger keys. The maximum is the mirror image. Both methods throw on an empty tree, because there is no key to return, and the empty case is one of the edge cases the checks test.

---
# Successor: the next key in sorted order

- The **successor** of $k$ is the smallest key greater than $k$.
- Case 1, `x` has a right subtree: the minimum of that subtree.
- Successor of 30: go right to 40, then left to 35. Answer 35.
- Case 2, no right subtree: the lowest ancestor whose left subtree holds `x`.
- Successor of 45: climb past 40 and 30 to 50. Answer 50.

> Sorted order of the running tree is twenty, thirty, thirty-five, forty, forty-five, fifty, seventy, eighty. The successor of thirty is thirty-five: the right subtree of thirty holds everything between thirty and fifty, and its smallest key is the answer. Forty-five has no right subtree, so nothing just above it is below it. Climb up until you arrive at an ancestor from its left side; that ancestor, fifty, is the next larger key. The largest key, eighty, has no successor.

---
# Successor in Java, from the root

```java
public K successor(K key) {
    Objects.requireNonNull(key, "null key");
    Node<K> x = root, best = null;
    while (x != null) {
        if (key.compareTo(x.key) < 0) {
            best = x;                         // x is larger: a candidate
            x = x.left;                       // look for a smaller one
        } else {
            x = x.right;                      // x too small: go right
        }
    }
    return best == null ? null : best.key;
}
```

> Our nodes have no parent pointers, so we cannot climb. Instead we walk down from the root and remember the last node where we turned left, because that node is larger than the key. For forty-five, we turn left only at fifty, so fifty is the answer: that is case two. For thirty, we turn left at fifty, pass thirty going right, then turn left at forty and at thirty-five; the last left turn, thirty-five, is the answer. That is case one. It returns null for the largest key, and it even works for a key that is not in the tree.

---
@type section
# Delete

---
# Delete has three cases

- Find the node `z` with the key, as in search.
- Case 1, `z` is a leaf: remove it.
- Case 2, `z` has one child: the child takes `z`'s place.
- Case 3, `z` has two children: its **in-order successor** takes its place.
- Each case below starts from a fresh copy of the running tree.

> Deleting a leaf is easy, and deleting a node with one child is almost as easy: its parent adopts the child. The hard case is a node with two children, because its parent has only one slot for two orphaned subtrees. The standard fix replaces the node by the next larger key, its successor, which fits exactly between the two subtrees. We trace each case on the original running tree so you can compare the results.

---
# Case 1: delete leaf 20

```diagram
@dir TB
@reveal manual
N50((50)) -- N30((30))
N50 -- N70((70))
N30 -- X30[nil].gray
N30 -- N40((40))
N70 -- X70[nil].gray
N70 -- N80((80))
N40 -- N35((35))
N40 -- N45((45))
focus N30 X30
--- 20 was the left child of 30. Now 30's left child is nil.
```

> Search reaches twenty through fifty and thirty. Twenty has no children, so the code returns its right child, which is null, and thirty's left field becomes null. The drawing is the result the code produced: thirty now has only a right child, so a gray nil marks the empty left side. Nothing else in the tree moved, and the BST property still holds, because removing a key cannot make any comparison false.

---
# Case 2: delete 70, which has one child

```diagram
@dir TB
@reveal manual
N50((50)) -- N30((30))
N50 -- N80((80)).green
N30 -- N20((20))
N30 -- N40((40))
N40 -- N35((35))
N40 -- N45((45))
focus N50 N80
--- 80, the only child of 70, is now the right child of 50.
```

> Seventy had only a right child. The code sees that seventy's left is null and returns seventy's right subtree, and fifty stores it as its new right child. Eighty was greater than fifty before, because it sat in fifty's right subtree, so it is a valid right child now. Had eighty had its own children, they would have moved up with it unchanged.

---
# Case 3: delete 30, find its successor

```diagram
@dir TB
@reveal manual
N50((50)) -- N30((30))
N50 -- N70((70))
N30 -- N20((20))
N30 -- N40((40))
N70 -- X70[nil].gray
N70 -- N80((80))
N40 -- N35((35))
N40 -- N45((45))
focus N50 N30
--- The search finds 30. It has two children, 20 and 40.
focus N30 N40 N35
--- Successor: go right to 40, then left to 35, which has no left child.
```

> Thirty has two children, so neither can simply take its place. Its successor is the minimum of its right subtree: go right once to forty, then left as far as possible, to thirty-five. The successor never has a left child, because we stopped exactly when there was none, so removing it from its old spot is a case one or case two delete. Here thirty-five is a leaf.

---
# Case 3: 35 takes 30's place

```diagram
@dir TB
@reveal manual
N50((50)) -- N35((35)).green
N50 -- N70((70))
N35 -- N20((20))
N35 -- N40((40))
N70 -- X70[nil].gray
N70 -- N80((80))
N40 -- X40[nil].gray
N40 -- N45((45))
focus N35 N20 N40 X40
--- 35 left 40's subtree and now has 30's old children, 20 and 40.
```

> The code unhooks thirty-five from under forty, which leaves forty with only its right child forty-five. Then thirty-five adopts thirty's two subtrees and is returned to fifty as its new left child. Check the property: twenty is smaller than thirty-five, and forty and forty-five are larger. The drawn tree is exactly the shape our delete method produced from the running tree.

---
# Why the successor fits

- Let `s` be the successor of `z`: the minimum of `z`'s right subtree.
- Every key in `z`'s left subtree is less than `z.key`, hence less than `s.key`.
- Every other key in `z`'s right subtree is greater than `s.key`.
- `s` has no left child, so unhooking it is an easy case.
- The in-order predecessor (maximum on the left) works just as well.

> The argument is a chain of inequalities. Left keys are smaller than z, and z is smaller than s, so left keys are smaller than s. The remaining right keys are larger than s because s was the minimum there. So s can sit exactly where z was. Using the predecessor, the largest key in the left subtree, is the mirror image and is equally correct; our code uses the successor, as CLRS does.

---
# Delete in Java

```java
private Node<K> delete(Node<K> x, K key) {
    if (x == null) return null;               // key absent
    int c = key.compareTo(x.key);
    if (c < 0) { x.left = delete(x.left, key); return x; }
    if (c > 0) { x.right = delete(x.right, key); return x; }
    size--;
    if (x.left == null) return x.right;       // leaf or right child only
    if (x.right == null) return x.left;       // left child only
    Node<K> s = minNode(x.right);             // in-order successor
    s.right = deleteMin(x.right);             // unhook s from its spot
    s.left = x.left;
    return s;                                 // s takes x's place
}
```

> The first five lines are search, using the same return-the-subtree pattern as insert. Once found, two lines handle cases one and two together: if the left child is missing, return the right one, which is null for a leaf. The last four lines are case three. deleteMin removes the successor from the right subtree and returns what remains, which becomes the successor's right subtree. The order matters: compute the new right subtree before overwriting anything.

---
# Two helpers for case 3

```java
private Node<K> minNode(Node<K> x) {
    while (x.left != null) x = x.left;
    return x;
}

private Node<K> deleteMin(Node<K> x) {
    if (x.left == null) return x.right;       // x is the minimum
    x.left = deleteMin(x.left);
    return x;
}
```

- Deleting 40 instead: its successor 45 is its right child, and `deleteMin` returns `null`.

> minNode is the same loop as min, on a subtree. deleteMin removes the smallest node of a subtree: when there is no left child, the node itself is the minimum, and its right subtree replaces it. A special case worth tracing on your own: delete forty from the running tree. Its successor forty-five is its own right child, deleteMin returns null, and forty-five ends up with thirty-five on its left and nothing on its right.

---
# Quiz: delete the root

```quiz
Our code deletes 50 from the running tree. Which key becomes the root?
- [ ] 45
- [x] 70
- [ ] 40
- [ ] 80
```

> Fifty has two children, so case three applies. Its successor is the minimum of its right subtree: go right to seventy, then try to go left, but seventy has no left child. So seventy is the successor and becomes the root, keeping thirty's subtree on its left and eighty on its right. Forty-five is the predecessor, the largest key on the left, which a predecessor-based delete would use instead. Our code produced seventy.

---
# Common mistakes and edge cases

- Forgetting `x.left = ...`: the recursive call's result is thrown away.
- Deleting a key that is absent: the walk reaches `null`, nothing changes.
- The empty tree: `min` and `max` have no answer; ours throw.
- Deleting the root: works through the same code, which returns a new root.
- `null` keys: every public method throws `NullPointerException`.

> Most BST bugs come from the return-the-subtree pattern. If you write delete of x dot left without storing the result, the change is lost. An absent key walks to null and returns without touching the size, which our checks confirm. Deleting the root needs no special case because the public method assigns the returned node to root. Finally, a null key cannot be compared, so every public method that takes a key rejects it up front with a NullPointerException whose message says so, which the checks test on an empty and a full tree.

---
@type section
# Cost and shape

---
# Every operation follows one path

- Search, insert, min, max, successor: one walk from the root downward.
- Delete: one walk to `z`, then one walk to its successor below it.
- Constant work per node visited.
- So each operation costs $O(h)$ time on a tree of height $h$.
- Extra space: $O(1)$ for loops, $O(h)$ for the recursive versions.

> Look back at every trace today: each one went down a single path and never came back up. Delete looks like two walks, but the walk to the successor continues below z, so together they still follow one downward path. A path has at most h plus one nodes, so the time is proportional to the height. Recursive methods also keep one stack frame per level, which costs space proportional to the height.

---
# How big is $h$?

$$\lfloor \log_2 n \rfloor \;\le\; h \;\le\; n - 1$$

| $n$ | Shortest possible $h$ | Tallest possible $h$ |
|---|---|---|
| 8 | 3 | 7 |
| 1 000 | 9 | 999 |
| 1 000 000 | 19 | 999 999 |

> The lower bound is the counting argument from earlier. The upper bound comes from a tree in which every node has only one child, so the n nodes form a single path with n minus one edges. For a million keys the gap is enormous: nineteen steps versus nearly a million. Big O of h is therefore a precise statement but not yet a useful one, until we know which end of this range the tree is at.

---
# Sorted insertion builds a chain

```diagram
@dir LR
@reveal all
C20((20)) -- C30((30)) : right
C30 -- C35((35)) : right
C35 -- C40((40)) : right
C40 -- C45((45)) : right
C45 -- C50((50)) : right
C50 -- C70((70)) : right
C70 -- C80((80)) : right
```

- Inserting `20, 30, 35, 40, 45, 50, 70, 80` in sorted order.
- Every new key is the largest so far, so it becomes a right child.
- Height 7 = $n - 1$: search for 80 compares all 8 keys.

> Insert the same eight keys in increasing order and the tree degenerates into a linked list leaning to the right. Each new key is larger than everything present, so it walks right past every node and attaches at the bottom. Our code confirms a height of seven, and a search for eighty compares with all eight keys. The same thing happens with keys in decreasing order, leaning left. Sorted input is common in practice, for example keys that are timestamps or increasing ID numbers.

---
# Average case, stated carefully

- Worst case over insertion orders: $\Theta(n)$ height, so $\Theta(n)$ per operation.
- Best case: $\Theta(\log n)$ height, as in the running tree.
- If $n$ distinct keys arrive in a uniformly random order, the expected height is $O(\log n)$.
- That assumption fails for sorted, nearly sorted or adversarial input.
- Deletions over a long time can also skew the shape.

> A plain BST has no guarantee. Its expected height is logarithmic only under an assumption about the input: every insertion order equally likely. This is a known theorem about randomly built trees. Real inputs often break that assumption, and a long mix of inserts and deletes changes the analysis too. So the honest summary is O of h per operation, with h anywhere from about log n to n minus one depending on history.

---
# Next: keep the tree balanced

- The fix is to restructure the tree during insert and delete.
- A **rotation** changes the shape but keeps the BST property.
- AVL trees (L10) keep sibling subtree heights within 1 of each other.
- Red-black trees (L11) use node colors to bound the height.
- Both guarantee $h = O(\log n)$, so every operation is $O(\log n)$ in the worst case.

> A balanced search tree does a little extra work on each insert and delete to stop chains from forming. The key tool is the rotation, a local rearrangement of a few links that keeps keys in order. The next two lectures develop two classic designs. The payoff is a worst-case guarantee instead of a hope about the input order.

---
# `TreeMap` and `TreeSet`

- `TreeMap<K, V>`: an ordered map; the API docs call it Red-Black tree based.
- Guaranteed $\log n$ time for `containsKey`, `get`, `put` and `remove`.
- `TreeSet<E>`: an ordered set built on a `TreeMap`.
- Iteration visits keys in ascending order.
- Natural ordering: `put(null, ...)` throws `NullPointerException`.

> Java's standard library already has a balanced binary search tree. The Java SE documentation describes TreeMap as a Red-Black tree based implementation and promises log n time for the basic operations, which is the guarantee our plain BST lacks. TreeSet is documented as based on a TreeMap. One behavior differs from our class: putting an existing key replaces its value and returns the old one. Like our class, it rejects a null key with NullPointerException when the keys use their natural ordering.

---
# Ordered queries on the running keys

```java
static List<Integer> queries(TreeMap<Integer, String> m) {
    return List.of(
            m.firstKey(),                     // 20: smallest key
            m.lastKey(),                      // 80: largest key
            m.floorKey(42),                   // 40: largest key <= 42
            m.ceilingKey(42),                 // 45: smallest key >= 42
            m.higherKey(45));                 // 50: successor of 45
}
```

- `HashMap` (L12) finds keys fast but cannot answer these by order.

> Here the map holds our eight running keys. Each method is one of today's operations: firstKey is min, lastKey is max, and higherKey is exactly our successor. floorKey and ceilingKey ask for the nearest key on either side of a value that may be missing, which is the unsuccessful search for forty-two, remembering the last turns. Our checks confirm each commented answer. A hash map, in lecture twelve, is faster on average for plain lookup, but it keeps no order.

---
@type section
# Wrap-up

---
# Summary

- A nonempty tree: one root, one parent per other node, $n - 1$ edges.
- Depth counts edges from the root; height counts edges down to a leaf.
- BST property: left subtree smaller, right subtree larger, at every node.
- Search, insert, min, max, successor and delete each walk one path: $O(h)$.
- $h$ ranges from $\lfloor \log_2 n \rfloor$ to $n - 1$; sorted input gives the worst.

> If you remember one sentence, remember that every BST operation costs the height of the tree, and the height depends on the order of insertions. The vocabulary lets you talk about that shape precisely. The three delete cases are the only intricate code, and the successor makes the hard case work. Next lecture we visit every node of a tree in several systematic orders, and one of them prints a BST in sorted order.

---
# Check yourself

- Insert `40, 20, 60, 10, 30, 50, 70` into an empty BST. Is the result perfect? What is its height?
- Why does the successor of a node with two children never have a left child?
- Give an insertion order of the running tree's keys that produces height 7 but is not sorted.

> Work these on paper before looking anything up. For the first, draw the tree after each insertion and then check the three definitions. For the second, recall how we found the successor and when we stopped walking. For the third, think about what each new key must be relative to the keys already in the tree for the chain to keep growing; there are many correct answers.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 12, Binary search trees.
- CLRS, Appendix B, for tree terminology and the "complete" naming.
- Java SE API documentation: `java.util.TreeMap`, `java.util.TreeSet`.
- Code on these slides: package `l08`, tested by `l08.Checks`.

> The definitions and the three delete cases follow CLRS Chapter 12, and the vocabulary follows its appendix on trees, except where these slides say they choose a different name. Library behavior is from the Java SE API documentation. Every Java excerpt on the slides is copied from the tested package, and every traced tree was produced by running that code on the running example.
