@title Lecture 11: Red-black trees
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Red-black trees
## Balance by color: one bit per node, few rotations per update

---
# Where we are

- L08: binary search trees cost $O(h)$ per operation.
- L10: AVL trees keep $h$ below about $1.44 \log_2 n$ with stored heights and rotations.
- Today: red-black trees, a looser rule that needs only one bit of color information per node.
- Next: L12 hash tables; L13 B-trees, which generalize the 2-3-4 trees hidden in today's colors.

> Last lecture we made binary search trees safe: AVL trees store a height in each node and rotate whenever two sibling subtrees differ by more than one. Today's structure reaches the same logarithmic guarantee with a different bookkeeping trick. Each node carries one extra bit of information, red or black, and a few rules about colors on paths keep the tree short. It is the design used by Java's TreeMap, and at the end we will see that it is really a B-tree in disguise, which prepares lecture thirteen.

---
# By the end of today you can

- State the five red-black properties and check them on a drawing.
- Compute black-heights and explain why $h \le 2\log_2(n+1)$.
- Insert a key and repair the tree with recoloring and rotations.
- Name the three fix-up cases and their mirror images.
- Compare red-black trees with AVL trees and relate them to 2-3-4 trees.

> These are the skills to practice with a pencil. The properties and black-height come first, because the height proof depends on them. Insertion is the main algorithm of the day: we trace it on one small example in which every case appears. Deletion gets an overview only. The last outcome connects today to the previous and the next lectures.

---
# Why another balanced tree?

- A sorted map needs ordered keys and $O(\log n)$ worst-case updates.
- Example: a scheduler keyed by time, with constant insertions and removals.
- An AVL deletion may need $\Theta(\log n)$ rotations, at many levels of the path.
- A red-black tree needs at most 2 rotations per insertion and 3 per deletion.
- The price: a taller worst case, up to $2\log_2(n+1)$.

> Picture a program that keeps pending events sorted by time: it inserts new events, removes finished ones, and asks for the earliest or the next after a given time. A hash table cannot answer next-after queries, and a plain BST can degrade. AVL trees work, but an AVL deletion may need theta of log n rotations, at many levels of the path. Red-black trees bound the structural changes per update by a small constant, and they recolor instead. They pay with a looser shape: up to twice the minimum height rather than about one point four four times.

---
@type section
# The rules

---
# NIL leaves and the sentinel

```html
<svg viewBox="0 0 254 221" style="width:100%;max-height:40cqh;font-family:var(--sans)" role="img">
<g>
<line x1="60.2" y1="100.0" x2="26.7" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="127.2" y1="34.0" x2="60.2" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="60.2" y1="100.0" x2="93.7" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="194.1" y1="100.0" x2="160.7" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="127.2" y1="34.0" x2="194.1" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="194.1" y1="100.0" x2="227.6" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<rect x="17.7" y="159.0" width="18" height="14" fill="#262626" stroke="var(--ink)" stroke-width="1" rx="2"/><text x="26.7" y="170.0" text-anchor="middle" font-size="9" fill="#fff">NIL</text>
<circle cx="60.2" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="60.2" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<rect x="84.7" y="159.0" width="18" height="14" fill="#262626" stroke="var(--ink)" stroke-width="1" rx="2"/><text x="93.7" y="170.0" text-anchor="middle" font-size="9" fill="#fff">NIL</text>
<circle cx="127.2" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="127.2" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<rect x="151.7" y="159.0" width="18" height="14" fill="#262626" stroke="var(--ink)" stroke-width="1" rx="2"/><text x="160.7" y="170.0" text-anchor="middle" font-size="9" fill="#fff">NIL</text>
<circle cx="194.1" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="194.1" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<rect x="218.6" y="159.0" width="18" height="14" fill="#262626" stroke="var(--ink)" stroke-width="1" rx="2"/><text x="227.6" y="170.0" text-anchor="middle" font-size="9" fill="#fff">NIL</text>
<text x="127.2" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">three keys, four NIL leaves</text>
</g>
</svg>
```

- CLRS treats every missing child as a leaf called NIL, colored black.
- Keys live only in the **internal** nodes; NILs hold no key.
- Our Java code uses one shared black sentinel object `nil` for all of them.
- The root's parent is `nil` too, so no `null` checks are needed.

> The red-black rules talk about leaves, and the word leaf means something special here. Every place where a BST would have a null pointer, imagine a small black leaf called NIL. The tree on the slide stores three keys in three internal nodes and has four NIL leaves. In code, we do not allocate a separate object for each NIL. We keep one sentinel node, colored black, and every missing child and the root's parent point to it. Reading nil dot color then always works, which removes many special cases.

---
# The five red-black properties

- **Property 1.** Every node is either red or black.
- **Property 2.** The root is black.
- **Property 3.** Every leaf (NIL) is black.
- **Property 4.** If a node is red, then both its children are black.
- **Property 5.** For each node, all simple paths from the node down to descendant leaves contain the same number of black nodes.

> These are the properties exactly as CLRS numbers them, and we will refer to them by number. One and three are almost free: a boolean color field and a black sentinel satisfy them automatically. Two is a convention that simplifies the proofs. The real work is done by four and five. Property four says no two reds in a row on any path. Property five says every path from a node down to a leaf sees the same count of black nodes. Together they force every path to be within a factor of two of every other.

---
# Black-height

- $\text{bh}(x)$: black nodes on any path from $x$ down to a leaf, **not counting** $x$ itself.
- Property 5 makes this well defined: every path gives the same count.
- NIL counts as black, so $\text{bh}(\text{NIL}) = 0$; a node whose children are both NIL has $\text{bh} = 1$.
- The black-height of the tree is $\text{bh}(\text{root})$.

```html
<svg viewBox="-25 0 394 287" style="width:100%;max-height:34cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="145.0" y1="166.0" x2="199.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="34.0" x2="253.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="100.0" x2="307.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="91.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="91.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="145.0" cy="166.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="199.0" cy="232.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="199.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">40</text>
<circle cx="253.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="253.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">50</text>
<circle cx="307.0" cy="166.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="307.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="172.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">bh(20) = 2: every path meets two black nodes below 20</text>
</g>
</svg>
```

> Black-height is the number that property five talks about. Start at x, walk down any path to a NIL, and count black nodes, including the NIL but not x itself. In the tree shown, every path from the root meets exactly two: the path to the NIL under ten meets ten and the NIL, and the path through fifty, thirty and forty meets thirty and the NIL, because fifty and forty are red. Our checker computes this count for every node and fails if two paths disagree.

---
# Two quick consequences

- Along any path, reds cannot touch, so at least half the nodes below the root are black.
- So the longest root-to-leaf path is at most twice the shortest.
- The shortest path can be all black; the longest alternates black and red.
- A node with one real child must have it red, with NIL children.

> From property four, every red node is followed by a black one, and the last node on a path is a black NIL. So at least half of the nodes below the root on any path are black. The shortest possible path is all black, and property five says every path has the same black count, so the longest path is at most twice as long as the shortest. The last bullet is a useful check when you draw trees: if a node has one real child, that child must be red and must have only NIL children, otherwise the black counts on the two sides differ.

---
# Height is at most $2\log_2(n+1)$

- **Claim 1**: the subtree rooted at $x$ has at least $2^{\text{bh}(x)} - 1$ internal nodes.
- Induction: each child has black-height at least $\text{bh}(x) - 1$, so the subtree has at least $2 \cdot (2^{\text{bh}(x)-1} - 1) + 1 = 2^{\text{bh}(x)} - 1$ nodes.
- **Claim 2**: $\text{bh}(\text{root}) \ge h/2$, by the half-black argument.

- Together: $n \ge 2^{h/2} - 1$. Solve for $h$:

$$h \le 2\log_2(n+1)$$

> This is the proof sketch from CLRS in two claims. For the first, a NIL has black-height zero and zero internal nodes, which matches two to the zero minus one. For an internal node x, each child has black-height bh of x if the child is red, or one less if it is black, so at least bh of x minus one. Apply the claim to both children and add one for x. For the second claim, a longest path from the root has h nodes below the root plus a NIL, and at least half of them are black. Combine the two and take logarithms. Our randomized checks assert both claims and the final bound after every insertion.

---
```quiz
In a red-black tree, a node $x$ has a real (non-NIL) left child $y$ and a NIL right child. Which statement is true?
- [ ] $y$ may be red or black.
- [ ] $y$ must be black.
- [x] $y$ must be red, and $x$ must be black.
- [ ] Such a node cannot exist.
```

> Count black nodes on the two paths down from x. The right path meets only the NIL, so its count is one. On the left, if y were black, the path through y would meet y and then at least one NIL: two or more, which breaks property five. So y is red. Then property four forbids x from being red, so x is black. And y's own children must be NILs, because a black child under y would add another black to that side. Such nodes do exist: in our final tree, thirty is exactly this shape, mirrored, with a NIL on the left and red forty on the right.

---
@type section
# Insertion

---
# Rotations, now with parent pointers

```java
private void leftRotate(Node x) {
    Node y = x.right;
    x.right = y.left;                        // y's left subtree moves under x
    if (y.left != nil) y.left.parent = x;
    y.parent = x.parent;                     // y takes x's place
    if (x.parent == nil) root = y;
    else if (x == x.parent.left) x.parent.left = y;
    else x.parent.right = y;
    y.left = x;                              // x goes below y
    x.parent = y;
}
```

- Same rotation as in L10: inorder order preserved, $O(1)$ time.
- Colors are not touched; the fix-up code sets them explicitly.

> This is the left rotation from our RedBlackTree class, following CLRS. It is the same move as last lecture, but each node now also stores its parent, so every pointer change has a partner: when y's left subtree moves under x, its parent pointer must change too. The method also fixes the pointer from x's old parent, or the root field if x was the root. Forgetting either of those is the classic bug. The right rotation is the mirror image.

---
# Insert red, then repair

- Insert as in a plain BST; the new node $z$ gets two NIL children.
- Color $z$ **red**: every black count stays the same, so property 5 holds.
- A black $z$ would add one black to some paths only, breaking property 5.
- A red $z$ can break only property 2 ($z$ is the root) or property 4 (red parent).

> Why red? Property five is the hard one to restore, because it is a statement about all paths at once. A new red node adds no black to any path, so property five survives for free. What can go wrong instead is local. If the tree was empty, the red node is the root, breaking property two, which one recoloring fixes. If z's parent is red, two reds touch, breaking property four. The fix-up procedure repairs that one local violation, moving it upward if necessary.

---
# Insertion in Java

```java body of insert(key), after the null check
// ...
while (x != nil) {                       // ordinary BST descent
    y = x;
    int c = key.compareTo(x.key);
    if (c == 0) return false;            // duplicate: no change
    x = c < 0 ? x.left : x.right;
}
Node z = new Node(key, RED);             // new nodes start red
z.parent = y;
z.left = z.right = nil;
if (y == nil) root = z;
else if (key.compareTo(y.key) < 0) y.left = z;
else y.right = z;
size++;
insertFixup(z);
```

> This is the body of insert after the null check; the omitted lines start y at the sentinel and x at the root. The loop is the BST descent from lecture eight, except that it stops at the sentinel instead of null, and it remembers the last real node y, which becomes the parent. A duplicate returns false, so this class is a set. The new node starts red, gets the sentinel as both children, and is attached on the correct side. Everything interesting happens in the last line.

---
# The fix-up cases (parent is a left child)

| Case | Situation | Action |
|---|---|---|
| 1 | uncle is red | recolor parent and uncle black, grandparent red; move $z$ up two levels |
| 2 | uncle is black, $z$ is a right child ("triangle") | $z$ ← parent; left-rotate at $z$; now case 3 |
| 3 | uncle is black, $z$ is a left child ("line") | recolor parent black, grandparent red; right-rotate at grandparent |

- When the parent is a right child, swap left and right everywhere: the mirror cases.

> The loop runs while z's parent is red. Then the grandparent exists and is black, because the red parent cannot be the root. Everything depends on the uncle, the parent's sibling. If the uncle is red, we push the grandparent's black down to both of its children. No rotation is needed, but the grandparent is now red and might clash with its own parent, so z moves up two levels and the loop repeats. If the uncle is black, rotations finish the job. A triangle, where z and its parent bend in opposite directions, is first rotated into a line; a line is fixed by one rotation at the grandparent and two recolorings, after which the loop ends.

---
# Fix-up in pseudocode

```algorithm
function INSERT-FIXUP(T, z):
  while z.p.color = RED do
    g ← z.p.p                              // grandparent, black
    if z.p = g.left then
      u ← g.right                          // uncle
      if u.color = RED then                // case 1
        z.p.color ← BLACK; u.color ← BLACK; g.color ← RED; z ← g
      else
        if z = z.p.right then              // case 2: triangle
          z ← z.p; LEFT-ROTATE(T, z)
        z.p.color ← BLACK; g.color ← RED   // case 3: line
        RIGHT-ROTATE(T, g)
    else (same with left and right exchanged)
  T.root.color ← BLACK                     // property 2
```

> Read this with the table beside it. After case two, z has moved down to the old parent's position and is again a red child of a red parent, now in a line, so case three applies immediately. In case three, the grandparent g is still the same node as before case two, which is why we saved it in a variable at the top. The final line repairs property two, both for the empty-tree case and for case one pushing red up to the root.

---
# Fix-up in Java: case 1

```java
while (z.parent.color == RED) {  // red parent: property 4 broken
    Node g = z.parent.parent;    // exists: red parent is not root
    if (z.parent == g.left) {
        Node uncle = g.right;
        if (uncle.color == RED) {  // case 1: recolor, move up two levels
            events.add("case 1 at " + z.key);
            z.parent.color = BLACK;
            uncle.color = BLACK;
            g.color = RED;
            z = g;
        } else {
            // ...
```

> This is the top of the loop in insertFixup, for a parent that is a left child. The grandparent always exists here. The root can be red only when z itself is the root, and then z's parent is the black sentinel and the loop stops. So a red parent is never the root, and the grandparent exists. The events line only records which case ran, so that the trace slides could be printed by the code itself; remove it and the lines match the pseudocode one for one. After case one, z is the grandparent, and the loop test looks at its parent next.

---
# Fix-up in Java: cases 2 and 3

```java
} else {
    if (z == z.parent.right) {   // case 2: triangle, rotate into a line
        events.add("case 2 at " + z.key);
        z = z.parent;
        leftRotate(z);
        snapshot();
    }
    events.add("case 3 at " + z.key);  // case 3: line
    z.parent.color = BLACK;
    g.color = RED;
    rightRotate(g);
}
```

- The mirror branch that follows swaps every `left` and `right`.

> This is the black-uncle branch. In case two, z first moves up to its parent, then the rotation at the new z pushes it back down, so z is again the lower of two reds, now in a straight line. The snapshot line records the middle state for our trace. Case three uses g, which was saved before case two, so it still names the grandparent. After case three, z's parent is black and the loop ends. The else branch for a right-child parent is the same code with every left and right exchanged.

---
@type section
# Tracing insertion

---
# The running example

- Insert, in this order: `10, 60, 20, 30, 50, 40`.
- Six keys, and case 1, case 2 and case 3 each happen once on each side.
- Every tree was printed by `RedBlackTree.shape()` and asserted in `Checks.java`.
- NIL leaves are not drawn. A green ring marks the key just inserted.

> We use one sequence for the whole trace: ten, sixty, twenty, thirty, fifty, forty. A short search over sequences picked it because it exercises every fix-up case and every mirror case, with at most six nodes. The code printed each state, including the states in the middle of a fix-up, and the checks file asserts all of them. Dark nodes are black, red nodes are red, and NIL leaves are left out of the pictures to save space.

---
# Insert 10, then 60

```html
<svg viewBox="-9 0 522 155" style="width:100%;max-height:40cqh;font-family:var(--sans)" role="img">
<g>
<circle cx="37.0" cy="34.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="37.0" cy="34.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="37.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<text x="64.0" y="147.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 10: a red root</text>
</g>
<g data-step="1"><path d="M130.0 67.0 H164.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M164.0 60.0 L174.0 67.0 L164.0 74.0 Z" fill="var(--ink-2)"/>
<circle cx="205.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="205.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<text x="232.0" y="147.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">property 2: recolor the root</text>
</g>
<g data-step="2"><path d="M298.0 67.0 H332.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M332.0 60.0 L342.0 67.0 L332.0 74.0 Z" fill="var(--ink-2)"/>
<line x1="373.0" y1="34.0" x2="427.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="373.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="373.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="427.0" cy="100.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="427.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="427.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="400.0" y="147.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 60: parent is black, done</text>
</g>
</svg>
```

- The first key is inserted red and then recolored black.
- 60 goes right of 10; its parent is black, so nothing is violated.

> Ten goes into an empty tree, so it is a red root. The fix-up loop does not run, because the root's parent is the sentinel, which is black. The last line of the fix-up recolors the root black. Sixty is larger, so it becomes ten's right child, red. Its parent is black, so property four holds and there is nothing to repair. Both paths from ten see one black node, the NIL, below ten.

---
# Insert 20: mirror cases 2 and 3

```html
<svg viewBox="-52 0 729 221" style="width:100%;max-height:44cqh;font-family:var(--sans)" role="img">
<g>
<line x1="145.0" y1="100.0" x2="91.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="37.0" y1="34.0" x2="145.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="37.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="91.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="91.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="91.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="145.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="145.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="91.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">20 under 60: red-red, uncle NIL is black</text>
</g>
<g data-step="1"><path d="M184.0 100.0 H218.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M218.0 93.0 L228.0 100.0 L218.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="259.0" y1="34.0" x2="313.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="313.0" y1="100.0" x2="367.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="259.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="259.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="313.0" cy="100.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="313.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="313.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="367.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="367.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="313.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">case 2 (mirror): right-rotate at 60</text>
</g>
<g data-step="2"><path d="M406.0 100.0 H440.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M440.0 93.0 L450.0 100.0 L440.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="535.0" y1="34.0" x2="481.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="535.0" y1="34.0" x2="589.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="481.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="481.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="535.0" cy="34.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="535.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="535.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="589.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="589.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="535.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">case 3 (mirror): recolor, left-rotate at 10</text>
</g>
</svg>
```

- The parent 60 is a right child and 20 is its left child: a triangle.
- Rotating at 60 makes a line 10, 20, 60 going right.
- Rotating at 10 and recoloring lifts 20 to the black root.

> Twenty is bigger than ten and smaller than sixty, so it becomes sixty's left child. Sixty is red, so we have a red-red violation. Sixty is a right child, so we use the mirror cases, and the uncle, ten's left child, is a NIL, which is black. Twenty and sixty bend in opposite directions: a triangle. Mirror case two rotates right at sixty, making twenty the parent of sixty. Now it is a straight line, and mirror case three colors twenty black and ten red, then rotates left at ten. The loop ends because z, now sixty, has the black parent twenty.

---
# Insert 30: mirror case 1, recolor only

```html
<svg viewBox="-14 0 802 221" style="width:100%;max-height:44cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="34.0" x2="199.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="91.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="91.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="145.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="145.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="199.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="199.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="118.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">30 under 60: red-red, uncle 10 is red</text>
</g>
<g data-step="1"><path d="M238.0 100.0 H272.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M272.0 93.0 L282.0 100.0 L272.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="367.0" y1="34.0" x2="313.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="475.0" y1="100.0" x2="421.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="367.0" y1="34.0" x2="475.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="313.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="313.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="367.0" cy="34.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="367.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="421.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="421.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="421.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="475.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="475.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="394.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">case 1 (mirror): recolor, z moves to 20</text>
</g>
<g data-step="2"><path d="M514.0 100.0 H548.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M548.0 93.0 L558.0 100.0 L548.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="643.0" y1="34.0" x2="589.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="751.0" y1="100.0" x2="697.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="643.0" y1="34.0" x2="751.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="589.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="589.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="643.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="643.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="697.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="697.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="697.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="751.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="751.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="670.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">root recolored black</text>
</g>
</svg>
```

- Uncle 10 is red: parent and uncle turn black, grandparent 20 turns red.
- $z$ moves up to 20, whose parent is the black sentinel: the loop stops.
- The final line recolors the root; the black-height grows from 1 to 2.

> Thirty lands as sixty's left child, under a red parent again. This time the uncle, ten, is red. So we recolor: sixty and ten become black and twenty becomes red. No rotation. Then z jumps to twenty. Its parent is the sentinel, which is black, so the loop ends, leaving a red root for a moment, which the last line of the fix-up repairs. Once the tree is nonempty, this is the only way an insertion raises the black-height: case one pushes red all the way to the root, and the root is recolored black.

---
# Insert 50: cases 2 and 3

```html
<svg viewBox="0 0 950 287" style="width:100%;max-height:44cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="145.0" y1="166.0" x2="199.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="34.0" x2="253.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="91.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="91.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="145.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="199.0" cy="232.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="199.0" cy="232.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="199.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">50</text>
<circle cx="253.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="253.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="145.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">50 under 30: red-red, uncle NIL</text>
</g>
<g data-step="1"><path d="M292.0 133.0 H326.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M326.0 126.0 L336.0 133.0 L326.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="421.0" y1="34.0" x2="367.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="166.0" x2="475.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="583.0" y1="100.0" x2="529.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="421.0" y1="34.0" x2="583.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="367.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="367.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="421.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="421.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="475.0" cy="232.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="475.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="529.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="529.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="529.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">50</text>
<circle cx="583.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="583.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="475.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">case 2: left-rotate at 30</text>
</g>
<g data-step="2"><path d="M622.0 133.0 H656.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M656.0 126.0 L666.0 133.0 L656.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="751.0" y1="34.0" x2="697.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="859.0" y1="100.0" x2="805.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="751.0" y1="34.0" x2="859.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="859.0" y1="100.0" x2="913.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="697.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="697.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="751.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="751.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="805.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="805.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="859.0" cy="100.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="859.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="859.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">50</text>
<circle cx="913.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="913.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="805.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">case 3: recolor, right-rotate at 60</text>
</g>
</svg>
```

- Parent 30 is a left child and 50 is its right child: a triangle.
- Rotate left at 30, then right at the grandparent 60.
- 50 ends black with red children 30 and 60.

> Fifty goes right at twenty, left at sixty and right at thirty. Thirty is red, it is the left child of sixty, and the uncle, sixty's right child, is a NIL. Thirty and fifty bend in opposite directions, so this is case two, now the non-mirrored version. The left rotation at thirty makes fifty the parent of thirty, a straight line to the left. Case three then colors fifty black and sixty red, and rotates right at sixty. The subtree under twenty is back to black-height one on every path.

---
# Insert 40: case 1, stops halfway up

```html
<svg viewBox="0 0 742 287" style="width:100%;max-height:44cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="145.0" y1="166.0" x2="199.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="34.0" x2="253.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="100.0" x2="307.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="91.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="91.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="145.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="199.0" cy="232.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="199.0" cy="232.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="199.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">40</text>
<circle cx="253.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="253.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">50</text>
<circle cx="307.0" cy="166.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="307.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="172.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">40 under 30: red-red, uncle 60 is red</text>
</g>
<g data-step="1"><path d="M346.0 133.0 H380.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M380.0 126.0 L390.0 133.0 L380.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="475.0" y1="34.0" x2="421.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="100.0" x2="529.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="166.0" x2="583.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="475.0" y1="34.0" x2="637.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="100.0" x2="691.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="421.0" cy="100.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="421.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">10</text>
<circle cx="475.0" cy="34.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="475.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">20</text>
<circle cx="529.0" cy="166.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="529.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">30</text>
<circle cx="583.0" cy="232.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="583.0" cy="232.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="583.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">40</text>
<circle cx="637.0" cy="100.0" r="21" fill="#c62828" stroke="#8e0000" stroke-width="2"/><text x="637.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">50</text>
<circle cx="691.0" cy="166.0" r="21" fill="#262626" stroke="var(--ink)" stroke-width="2"/><text x="691.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="#ffffff">60</text>
<text x="556.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">case 1: recolor, z moves to 50, whose parent is black</text>
</g>
</svg>
```

- Uncle 60 is red: recolor 30 and 60 black and 50 red.
- $z$ moves up to 50. Its parent 20 is black, so the loop stops.
- Six keys, height 3, black-height 2.

> Forty lands as thirty's right child under a red parent. The uncle sixty is red, so case one recolors: thirty and sixty turn black and fifty turns red. Then z moves up two levels to fifty. Fifty's parent is twenty, which is black, so there is no new violation and the loop stops without reaching the root. This is the final tree from the black-height slide. Its height is three, while any AVL tree with six keys has height two: red-black trees allow a little more slack.

---
# The trace in one table

| Insert | Cases | Rotations | The loop stops because |
|---|---|---|---|
| 10 | none | 0 | parent is the sentinel; root recolored |
| 60 | none | 0 | parent 10 is black |
| 20 | mirror 2, mirror 3 | 2 | case 3 made the parent black |
| 30 | mirror 1 | 0 | $z$ reached the root; root recolored |
| 50 | 2, 3 | 2 | case 3 made the parent black |
| 40 | 1 | 0 | $z$ = 50 has black parent 20 |

> Across six insertions we used four rotations in total, never more than two in one insertion. Case one did its work by recoloring, and once it climbed to the root. Also notice that cases two and three always come together or case three alone; after case three the loop ends. That observation is the heart of the cost analysis on the next slide.

---
# Why the fix-up works, and what it costs

- Invariant: the only violation is property 4 at $z$ and its parent, or property 2.
- Case 1 keeps every black count: a black moves from $g$ down to both children.
- Cases 2 and 3 rotate and recolor without changing any path's black count.
- Case 1 moves $z$ up two levels: at most $O(\log n)$ iterations, $O(1)$ each.
- Case 3 ends the loop, so an insertion performs at most **2 rotations**.
- Space: $\Theta(n)$ in total; each of our nodes also stores a parent pointer.

> The fix-up loop keeps one invariant: the tree satisfies every property except possibly one red-red pair at z, or a red root. Each case preserves property five; you can check it by counting blacks on each path in the before and after pictures of our trace. Case one either finishes or moves the problem two levels up, so it runs at most about the height many times, and the height is logarithmic. Case two always leads to case three, and after case three z's parent is black, so the loop ends. Total: logarithmic time and at most two rotations. Our randomized checks assert the two-rotation limit on every insertion. Space is theta of n: one node per key, and in our implementation each node holds a key, two child pointers, a parent pointer and the color. The one color bit is conceptual; in Java the color is a boolean field, which is not stored as a single bit.

---
```quiz
During insertion, $z$ and its parent are both red, the parent is a left child, and $z$'s uncle is red. What does the fix-up do?
- [ ] Right-rotate at the grandparent.
- [ ] Left-rotate at the parent, then right-rotate at the grandparent.
- [x] Recolor parent and uncle black, grandparent red, and continue from the grandparent.
- [ ] Recolor $z$ black and stop.
```

> A red uncle means case one: recoloring alone. The grandparent was black, and giving its black to both children keeps every path's black count the same. The grandparent is now red, and its own parent might be red, so the loop continues from the grandparent. The rotation options are for a black uncle. Recoloring z black would add a black node to some paths only and break property five.

---
@type section
# Deletion, comparisons, and 2-3-4 trees

---
# Deletion: an overview

- Delete as in a BST; track the node $y$ that is removed or moved.
- If $y$ was red, every property still holds.
- If $y$ was black, paths through its spot lost one black.
- Give the node $x$ that took its place an **extra black** ("doubly black").
- A fix-up pushes the extra black up or absorbs it: $O(\log n)$ time, at most **3 rotations**.

> We do not trace deletion today; our Java class does not implement it, and a trace without tested code would break our rule that every state comes from running code. The idea is worth knowing. Removing a red node changes no black count. Removing a black node leaves some paths one black short, so CLRS pretends the node that moved into its place carries one extra black. The fix-up then has four cases, again depending on a sibling's colors. It either moves the extra black up the tree or gets rid of it with at most three rotations. CLRS chapter thirteen has the full procedure.

---
# Red-black trees versus AVL trees

| | AVL tree | Red-black tree |
|---|---|---|
| Extra data per node | a height | one color bit, conceptually |
| Worst-case height | about $1.44 \log_2 n$ | $2\log_2(n+1)$ |
| Rotations per insertion | at most 2 | at most 2 |
| Rotations per deletion | $O(\log n)$ | at most 3 |
| Search, insert, delete | $O(\log n)$ worst case | $O(\log n)$ worst case |

> Both designs give the same asymptotic guarantees, so the choice is about constants and workload. AVL trees are more rigidly balanced, so in the worst case a lookup visits fewer nodes, which tends to help read-heavy workloads. Red-black trees change structure less on updates, especially deletions, which tends to help update-heavy workloads. These are general tendencies, not measurements; which one is faster in a given program depends on the data, the hardware and the implementation, and you would have to measure.

---
# A red-black tree is a 2-3-4 tree

```html
<svg viewBox="0 0 280 184" style="width:100%;max-height:32cqh;font-family:var(--sans)" role="img">
<rect x="10.0" y="110.0" width="56" height="40" rx="6" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<text x="38.0" y="136.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<rect x="92.0" y="110.0" width="96" height="40" rx="6" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<text x="120.0" y="136.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="160.0" y="136.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<line x1="140.0" y1="116.0" x2="140.0" y2="144.0" stroke="var(--blue-ink)" stroke-width="1"/>
<rect x="214.0" y="110.0" width="56" height="40" rx="6" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<text x="242.0" y="136.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">60</text>
<line x1="100.0" y1="60.0" x2="38.0" y2="110.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="140.0" y1="60.0" x2="140.0" y2="110.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="180.0" y1="60.0" x2="242.0" y2="110.0" stroke="var(--ink-2)" stroke-width="2"/>
<rect x="92.0" y="20.0" width="96" height="40" rx="6" fill="var(--blue)" stroke="var(--blue-ink)" stroke-width="2"/>
<text x="120.0" y="46.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="160.0" y="46.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<line x1="140.0" y1="26.0" x2="140.0" y2="54.0" stroke="var(--blue-ink)" stroke-width="1"/>
<text x="140.0" y="176.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">our final tree read as a 2-3-4 tree</text>
</svg>
```

- Merge each black node with its red children into one node.
- Each merged node holds 1, 2 or 3 keys: a 2-node, 3-node or 4-node.
- Property 5 puts every merged leaf at the same depth: the black-height.

> Take the final tree of our trace and merge every red node into its black parent. Twenty absorbs fifty, thirty absorbs forty, and ten and sixty stay alone. The result is a tree whose nodes hold one, two or three keys, and all of its leaves are at the same depth, because property five counts exactly the black nodes, which are the merged nodes. That is a 2-3-4 tree, a small case of the B-trees of lecture thirteen. Case one of insertion corresponds to splitting a full 4-node, and the reading was computed by our asTwoThreeFour method.

---
```quiz
Which statement about red-black trees and AVL trees is correct?
- [ ] Red-black trees have a smaller worst-case height than AVL trees.
- [ ] AVL insertion can need $\Theta(\log n)$ rotations.
- [x] A red-black deletion needs at most 3 rotations; an AVL deletion may need more.
- [ ] Only AVL trees guarantee $O(\log n)$ worst-case search.
```

> The correct statement is about deletion: CLRS shows red-black deletion uses at most three rotations, while an AVL deletion can rotate at many ancestors, as we saw last lecture. The first option is backwards: AVL trees are the more rigidly balanced ones, about one point four four log n against two log n. AVL insertion needs at most one single or double rotation. Both structures guarantee logarithmic worst-case search.

---
# Red-black trees in the Java library

- `TreeMap`: "A Red-Black tree based NavigableMap implementation" (Java SE API).
- The API guarantees $\log(n)$ time for `containsKey`, `get`, `put` and `remove`.
- `TreeSet` is a `NavigableSet` implementation based on a `TreeMap`.
- Both iterate in sorted order and answer ordered queries: `TreeMap.floorKey`, `TreeSet.ceiling`.
- With natural ordering, a `null` key throws `NullPointerException`.

> When you need a sorted map in Java, this is it. The API documentation for TreeMap states that it is based on a red-black tree and guarantees logarithmic time for the four basic operations. TreeSet is built on a TreeMap. Beyond get and put, they answer ordered questions a hash map cannot: the smallest key, the largest key at or below a value, the smallest key at or above one. With natural ordering, the key must be comparable, so a null key throws a NullPointerException. In current OpenJDK, HashMap also turns very long buckets into red-black trees; that is an implementation detail, not an API promise.

---
# A validator for all five properties

```java
private int check(Node x, K lo, K hi, int[] count) {
    if (x == nil) return 1;                  // property 3: the NIL leaf is black
    count[0]++;
    boolean inRange = (lo == null || x.key.compareTo(lo) > 0)
            && (hi == null || x.key.compareTo(hi) < 0);
    if (!inRange) throw new AssertionError("order broken at " + x.key);
    // ...
    if (x.color == RED && (x.left.color == RED || x.right.color == RED))
        throw new AssertionError("property 4: red " + x.key + " has a red child");
    int bl = check(x.left, lo, x.key, count);
    int br = check(x.right, x.key, hi, count);
    if (bl != br)
        throw new AssertionError("property 5: black counts differ below " + x.key);
    return bl + (x.color == BLACK ? 1 : 0);
}
```

> This recursive checker returns the number of black nodes on every path from x down to a NIL, counting both x and the NIL. It checks the BST order with an allowed interval, property four at every red node, and property five by comparing the two sides. The omitted lines check that each real child's parent link points back to x. The public method checks the root color and that the root's parent is the sentinel. Checks dot java runs it after every insertion in 300 random sequences, compares the keys with TreeSet, and deliberately breaks a tree three ways to make sure the checker notices.

---
# Common mistakes and edge cases

- Coloring the new node black: breaks property 5 on some paths.
- Forgetting the last line: the root can be left red after case 1.
- Rotations that skip parent pointers or forget to update `root`.
- In case 2, rotating without first moving $z$ up to its parent.
- Mirror cases: swap **every** left and right, including the uncle.
- Edge cases: empty tree ($\text{bh} = 0$), one node, duplicates, `null` keys.

> These are the bugs that the validator catches. The first two break properties five and two directly. Rotation bugs usually show up as broken parent links or as a lost root, which is why our checker tests parent pointers. In case two, CLRS first sets z to its parent and then rotates at z; rotating at the parent without moving z leaves z pointing at the wrong node for case three. When you write the mirror branch, change every left to right, including the choice of uncle. Finally, test an empty tree, a single node, duplicate inserts, and null keys, which our code rejects.

---
@type section
# Wrap-up

---
# Summary

- Five properties; the key ones: no red-red, and equal black counts on paths.
- Black-height gives $n \ge 2^{h/2} - 1$, so $h \le 2\log_2(n+1)$.
- Insert red, then fix up: recolor on a red uncle, rotate on a black uncle.
- Insertion: $O(\log n)$ time, at most 2 rotations; deletion: at most 3.
- A red-black tree is a 2-3-4 tree drawn with binary nodes.

> In one breath: red-black trees trade AVL's height rule for color rules on paths, which still force logarithmic height, with a factor of two instead of about one point four four. Insertion adds a red node and repairs a single red-red clash, by recoloring upward or by at most two rotations. Deletion has a similar repair with at most three rotations. And the colors are just a binary encoding of a 2-3-4 tree, which we will generalize to B-trees.

---
# Check yourself

- Insert `1, 2, 3, 4, 5, 6, 7, 8` into an empty red-black tree. Which cases occur?
- Why can a red-black tree with black-height 3 not have fewer than 7 keys?
- Draw the 2-3-4 tree for a red-black tree that you built yourself, and check its leaf depths.

> Try these on paper before you look at any code. For the first, insert the keys one at a time, and at every red-red clash say whether the uncle is red or black, and whether z, its parent and its grandparent form a line or a triangle; that tells you the case. Keep a count of rotations per insertion and check that it never exceeds two. For the second, use the first claim of the height proof: a subtree whose root has black-height b has at least two to the b, minus one, internal nodes, and ask what that gives for b equal to three. For the third, merge each red node into its black parent, so every black node with its red children becomes one node of the 2-3-4 tree. Then confirm that every leaf of the result sits at the same depth, and explain which red-black property guarantees it.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 13 (red-black trees), Chapter 12 (binary search trees), Chapter 18 (B-trees).
- Java SE API documentation: `java.util.TreeMap`, `java.util.TreeSet`.

> The properties, their numbering, the sentinel, and the insertion cases follow CLRS chapter thirteen; the 2-3-4 connection leads to chapter eighteen. The library facts and the one quoted sentence come from the Java SE API documentation for TreeMap. Every tree state on these slides was printed by the lecture's tested Java code and is asserted in its checks.
