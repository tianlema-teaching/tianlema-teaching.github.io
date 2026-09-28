@title Lecture 10: Balanced trees and AVL trees
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Balanced trees and AVL trees
## Keep the height logarithmic, whatever the input order

---
# Where we are

- L08 built binary search trees: search, insert and delete follow one path.
- L09 walked trees in order; an inorder walk of a BST lists keys sorted.
- Today: stop a BST from growing tall, whatever order the keys arrive in.
- Next time: red-black trees, the balanced tree behind `java.util.TreeMap`.

> Two lectures ago we built binary search trees and saw that every operation walks one path from the root. Last time we walked whole trees, and the inorder walk of a search tree gives the keys in sorted order. Keep that inorder fact in mind, because every repair we make today must leave it unchanged. Today's goal is a guarantee: no matter what order the keys arrive in, the tree stays short. Next lecture shows a second design with the same goal.

---
# By the end of today you can

- Rotate a subtree left or right and show the inorder order is unchanged.
- Decide whether a tree is AVL from its balance factors.
- Name the LL, RR, LR and RL cases and apply the matching rotations.
- Trace AVL insertion and deletion on a small tree.
- Explain why an AVL tree with $n$ nodes has height $O(\log n)$.

> These are things you should be able to do with a pencil by the end of the class. Each one gets a worked example on our running tree. The last outcome is the reason the whole structure exists: it turns a promise about shape into a promise about time. If you can trace the four cases on paper and justify the height bound in a few sentences, you have the core of this lecture.

---
# Tree height, as used today

- **Height** of a node: edges on the longest downward path to a leaf.
- A single node has height 0. The empty tree has height $-1$.
- The height of a tree is the height of its root.
- Textbooks differ: some count nodes instead. We count edges, as CLRS does.

> Before any bounds, fix the convention, because books disagree and formulas shift by one when they do. We count edges. A lone node has height zero, and an empty tree gets height minus one, which makes the arithmetic work: a node's height is one plus the larger of its children's heights, even when a child is missing. If you read another text that says a single node has height one, it counts nodes; every result shifts by one but nothing essential changes.

---
# BST operations cost the height

- Search, insert and delete each walk one root-to-leaf path.
- Work per level is constant, so each costs $O(h)$ for height $h$.
- A tree of $n$ nodes has $h \ge \lfloor \log_2 n \rfloor$: at best logarithmic.
- At worst $h = n - 1$: the tree is a single path.

> Recall why the height matters. Every BST operation compares at one node, then moves to one child, so the number of levels visited bounds the work. The best a binary tree can do is about log base two of n levels, because each level can at most double the number of nodes. The worst is a path, where every node has one child and the height is n minus one. Which one we get depends entirely on the insertion order.

---
# Sorted input builds a path

```html
<svg viewBox="0 0 290 353" style="width:100%;max-height:34cqh;font-family:var(--sans)" role="img">
<g>
<line x1="37.0" y1="34.0" x2="91.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="145.0" y1="166.0" x2="199.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="232.0" x2="253.0" y2="298.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<circle cx="91.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<circle cx="145.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<circle cx="199.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<circle cx="253.0" cy="298.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="253.0" y="304.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="145.0" y="345.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 10, 20, 30, 40, 50 in order</text>
</g>
</svg>
```

- Each new key is larger than all before, so it always goes right.
- `PlainBst` with keys 1 to 1023 inserted in order: height 1022.
- An AVL tree with the same insertions: height 9.

> Sorted input is not rare: think of timestamps, student IDs issued in order, or a file that is already sorted. Each new key is bigger than everything in the tree, so it becomes the right child of the previous one, and the tree degenerates into a linked list. Searching it is linear search with extra pointers. The two numbers on the slide come from running our Java code: a plain BST reaches height one thousand twenty-two, while the AVL tree we build today stays at height nine for the same input.

---
# What "balanced" means

- A family of trees is **balanced** if every tree in it has $h = O(\log n)$.
- The guarantee must hold for every insertion and deletion order.
- Perfect shape is too strict: keeping it would force large rebuilds.
- Balanced trees allow some slack and repair it with local changes.

> Balanced is a promise about a whole family of trees, not about one lucky tree. The height must stay within a constant times log n for every sequence of operations. We cannot insist on a perfect shape, because one insertion could force us to move most of the nodes. Instead each design allows a controlled amount of imbalance, and when an update breaks the rule, it repairs the damage near the path it just walked, at a cost of a few pointer changes per level.

---
# A map of balanced search trees

| Family | What it keeps | Guarantee |
|---|---|---|
| AVL tree (today) | Subtree heights differ by at most 1 | Worst-case $O(\log n)$ |
| Red-black tree (L11) | Color rules on paths | Worst-case $O(\log n)$ |
| B-tree (L13) | Many keys per node, leaves at one depth | Worst-case $O(\log n)$ |
| Treap | Heap order on random priorities | Expected $O(\log n)$ |
| Splay tree | Nothing; accessed node moves to the root | Amortized $O(\log n)$ |

> This course covers the first three. AVL and red-black trees are binary and store a little extra information per node: a height or a color. B-trees keep many keys in one node and suit disks, which we see in lecture thirteen. Two other designs are worth knowing by name. A treap gives each key a random priority and gets logarithmic height in expectation, over its own random choices. A splay tree stores no balance data at all and gets logarithmic cost amortized over a sequence of operations, not per operation. Read the guarantee column carefully: worst case, expected and amortized are different promises.

---
@type section
# Rotations

---
# A rotation changes shape, not order

```html
<svg viewBox="0 0 620 221" style="width:100%;max-height:42cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="100.0" x2="37.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="91.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="253.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<path d="M37.0 145.0 L62.0 193.0 L12.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">A</text>
<circle cx="91.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">x</text>
<path d="M145.0 145.0 L170.0 193.0 L120.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">B</text>
<circle cx="199.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">y</text>
<path d="M253.0 79.0 L278.0 127.0 L228.0 127.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="253.0" y="114.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">C</text>
<text x="145.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">before: y on top</text>
</g>
<g data-step="1"><path d="M292.0 100.0 H326.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M326.0 93.0 L336.0 100.0 L326.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="421.0" y1="34.0" x2="367.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="100.0" x2="475.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="421.0" y1="34.0" x2="529.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="100.0" x2="583.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<path d="M367.0 79.0 L392.0 127.0 L342.0 127.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="367.0" y="114.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">A</text>
<circle cx="421.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="421.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">x</text>
<path d="M475.0 145.0 L500.0 193.0 L450.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="475.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">B</text>
<circle cx="529.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="529.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">y</text>
<path d="M583.0 145.0 L608.0 193.0 L558.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="583.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">C</text>
<text x="475.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">after rotating right at y</text>
</g>
</svg>
```

- $A$, $B$, $C$ are whole subtrees, possibly empty.
- Inorder before and after: $A,\ x,\ B,\ y,\ C$.
- Subtree $B$ moves from $x$'s right to $y$'s left.

> A rotation is the one tool every balanced binary tree uses. Look at the left picture: x is y's left child, and x has subtrees A and B. A right rotation at y lifts x to the top and makes y its right child. The only subtree that changes parent is B. It sat between x and y in sorted order before, and it still sits between them after, because it becomes y's left subtree. Read both trees in order and you get A, x, B, y, C both times, so the BST property holds.

---
# Left rotation is the mirror

```html
<svg viewBox="0 0 620 221" style="width:100%;max-height:42cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="34.0" x2="199.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="100.0" x2="253.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<path d="M37.0 79.0 L62.0 127.0 L12.0 127.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="114.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">A</text>
<circle cx="91.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">x</text>
<path d="M145.0 145.0 L170.0 193.0 L120.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">B</text>
<circle cx="199.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">y</text>
<path d="M253.0 145.0 L278.0 193.0 L228.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="253.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">C</text>
<text x="145.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">before: x on top</text>
</g>
<g data-step="1"><path d="M292.0 100.0 H326.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M326.0 93.0 L336.0 100.0 L326.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="421.0" y1="100.0" x2="367.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="34.0" x2="421.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="421.0" y1="100.0" x2="475.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="34.0" x2="583.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<path d="M367.0 145.0 L392.0 193.0 L342.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="367.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">A</text>
<circle cx="421.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="421.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">x</text>
<path d="M475.0 145.0 L500.0 193.0 L450.0 193.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="475.0" y="180.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">B</text>
<circle cx="529.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="529.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">y</text>
<path d="M583.0 79.0 L608.0 127.0 L558.0 127.0 Z" fill="var(--mist)" stroke="var(--ink-2)" stroke-width="2"/><text x="583.0" y="114.0" text-anchor="middle" font-size="17" font-style="italic" fill="var(--ink)">C</text>
<text x="475.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">after rotating left at x</text>
</g>
</svg>
```

- A left rotation at $x$ lifts its right child $y$.
- Subtree $B$ moves from $y$'s left to $x$'s right.
- Left then right at the same spot undoes the change.

> The left rotation is the same move seen in a mirror. Now y is the right child, and the rotation lifts it above x. Again exactly one subtree changes parent: B, which holds the keys between x and y. Notice that the right-hand picture here is the left-hand picture of the previous slide. A left rotation and a right rotation are inverses, which is a useful sanity check when you trace by hand.

---
# Rotation in Java

```java
/** Rotate y's left child x up; returns x, the new subtree root. */
private static <K> Node<K> rotateRight(Node<K> y) {
    Node<K> x = y.left;
    y.left = x.right;        // x's right subtree moves under y
    x.right = y;
    update(y);
    update(x);
    return x;
}
```

- Three pointer changes and two height updates: $O(1)$ time.
- Update `y` before `x`: `y` is now `x`'s child.

> This is the right rotation from our Java file, AvlTree dot java. The method returns the new root of the subtree, and the caller must store it where y used to hang; forgetting that is the most common bug. Only x and y change height, because A, B and C keep their contents. The order of the two update calls matters: y is below x now, so y's height must be correct before we compute x's. The left rotation is the mirror image with left and right swapped.

---
# Rotations: what changes, what stays

- Stays: the inorder key sequence, so the BST property.
- Stays: the contents of subtrees $A$, $B$, $C$.
- Changes: the parent of $B$, the roles of $x$ and $y$.
- Changes: within the rotated subtree, only the heights of $x$ and $y$.

> Keep this checklist when you rotate. The key order never changes, and the three hanging subtrees are carried along whole. What changes is which of the two nodes is on top, and, within the rotated subtree, only the heights of those two nodes. Ancestors above it can change height too, which is why the repair keeps recomputing heights on the way up. In the right rotation, subtree A moves one level up and subtree C moves one level down. That is exactly the lever we need: if the left side is too tall, a right rotation lifts it.

---
```quiz
A right rotation at node $y$ is applied to a valid BST. Which statement is always true afterwards?
- [ ] The height of the tree decreases by one.
- [x] An inorder walk lists the same keys in the same order.
- [ ] The root of the whole tree changes.
- [ ] Every node keeps its parent.
```

> The answer is the inorder walk. A rotation keeps the sorted order by construction, which is why it is safe to use inside any BST. The height may go down, stay the same, or even go up, depending on which side was taller, so the first option is false. The whole tree's root changes only when y was the root. And at least x, y and the middle subtree B get new parents.

---
@type section
# AVL trees

---
# The AVL property

- Adelson-Velsky and Landis introduced the idea in 1962.
- **AVL property**: at every node, the two subtree heights differ by at most 1.
- **Balance factor** $\text{bf}(v) = h(\text{left}) - h(\text{right})$.
- AVL means $\text{bf}(v) \in \{-1, 0, +1\}$ for every node $v$.
- Some books use right minus left; only the signs flip.

> The AVL tree is usually credited as the first balanced search tree design, and it is named after its two inventors. The rule is local: look at any node, compute the heights of its left and right subtrees, and they may differ by at most one. The balance factor packages that difference into a number. We subtract right from left, so a positive balance factor means the left side is taller. Remember that an empty subtree has height minus one: a leaf has balance factor zero, and a node with only a left leaf has balance factor plus one.

---
# Check the balance factors

```html
<svg viewBox="0 0 468 221" style="width:100%;max-height:38cqh;font-family:var(--sans)" role="img">
<g>
<line x1="145.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="37.0" y1="100.0" x2="91.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="145.0" y1="34.0" x2="199.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="61.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="91.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="115.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="145.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="169.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="199.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="223.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="118.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">AVL: every factor in {-1, 0, +1}</text>
</g>
<g><path d="M238.0 100.0 H272.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M272.0 93.0 L282.0 100.0 L272.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="367.0" y1="100.0" x2="313.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="421.0" y1="34.0" x2="367.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="313.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="313.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="337.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="367.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="367.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="391.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="421.0" cy="34.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="421.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="445.0" y="17.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<text x="367.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">not AVL: 70 has +2</text>
</g>
</svg>
```

- Small numbers are balance factors: left height minus right height.
- Left tree: 10 has $-1$ because its only child is on the right.
- Right tree: at 70 the left height is 1 and the right height is $-1$.

> Practice reading the numbers. In the left tree, ten has an empty left subtree of height minus one and a right leaf of height zero, so its factor is minus one. Fifty has a left subtree of height one and a right subtree of height zero, so plus one. Every factor is in range, so the tree is AVL. In the right tree the node seventy has a left subtree of height one and nothing on the right, so its factor is one minus minus one, which is two. One bad node is enough to break the property.

---
# Store the height in each node

```java
static final class Node<K> {
    K key;
    Node<K> left, right;
    int height;              // stored height of the subtree rooted here
    // ...
}
```

```java
static int height(Node<?> n) {
    return n == null ? -1 : n.height;
}
private static void update(Node<?> n) {
    n.height = 1 + Math.max(height(n.left), height(n.right));
}
```

> Computing a height from scratch visits the whole subtree, which would ruin the logarithmic cost. So each node stores its own height, and we refresh it whenever its children change. The helper height turns a null child into minus one, so update works for leaves and one-child nodes without special cases. A balance factor is then one subtraction of two stored numbers. The price is one int per node and the discipline to call update after every change below a node.

---
# The four ways to go out of balance

| Case | Unbalanced node $z$ | New key went into | Fix |
|---|---|---|---|
| LL | $\text{bf}(z) = +2$ | left child's left subtree | right rotation at $z$ |
| RR | $\text{bf}(z) = -2$ | right child's right subtree | left rotation at $z$ |
| LR | $\text{bf}(z) = +2$ | left child's right subtree | left at child, then right at $z$ |
| RL | $\text{bf}(z) = -2$ | right child's left subtree | right at child, then left at $z$ |

> After an insertion, walk up from the new node and stop at the first node whose factor is plus or minus two; call it z. Two letters name the path from z toward the new key. LL and RR are straight lines, and one rotation at z fixes them. LR and RL are zigzags: a single rotation at z would just move the zigzag to the other side. So we first rotate the child to turn the zigzag into a straight line, and then rotate at z. A double rotation is simply two single rotations.

---
# Rebalancing in pseudocode

```algorithm
function REBALANCE(z):             // z's subtrees are AVL
  update height of z
  if bf(z) > 1 then                // left side too tall
    if bf(z.left) < 0 then         // LR: straighten first
      z.left ← ROTATE-LEFT(z.left)
    return ROTATE-RIGHT(z)         // LL, or LR after straightening
  if bf(z) < -1 then               // right side too tall
    if bf(z.right) > 0 then        // RL: straighten first
      z.right ← ROTATE-RIGHT(z.right)
    return ROTATE-LEFT(z)          // RR, or RL after straightening
  return z                         // already balanced
```

> Read it top down. The function returns the root of the repaired subtree, so the caller can reattach it. When the left side is too tall, we look at the left child's factor. A negative factor means the left child leans right, the zigzag case, so we rotate the child first. Then one right rotation at z. The right-heavy half is the mirror. Notice the tests use strictly less than zero and strictly greater than zero. When the child is exactly balanced, which can happen during deletion, a single rotation is the correct fix.

---
# Rebalancing in Java

```java inside rebalance(node): the left-heavy half
update(node);
int bf = balance(node);
if (bf > 1) {                            // left side too tall
    if (balance(node.left) < 0) {        // LR: first rotate the child
        events.add("LR at " + node.key);
        node.left = rotateLeft(node.left);
        snapshot();
    } else {
        events.add("LL at " + node.key);
    }
    return rotateRight(node);
}
// ...
return node;                             // already balanced
```

> This is the left-heavy half of the Java method; the omitted part is its mirror. The lines with events and snapshot only record what happened: every tree state on the trace slides that follow was printed by this code, and the checks file asserts those same states. Take them out and the logic is exactly the pseudocode. Look at where the result of rotateLeft is stored: back into node dot left, so the tree stays connected.

---
# Insertion: BST insert, then repair upward

```java
private Node<K> insert(Node<K> node, K key) {
    if (node == null) {
        size++;
        return new Node<>(key);
    }
    int c = key.compareTo(node.key);
    if (c < 0) node.left = insert(node.left, key);
    else if (c > 0) node.right = insert(node.right, key);
    else return node;                        // duplicate: no change
    return rebalance(node);
}
```

- Down: an ordinary BST insertion. Up: `rebalance` at every node on the path.
- Duplicates are ignored: this tree stores a set of distinct keys.

> The recursion does two jobs. On the way down it is the BST insertion from lecture eight: compare, go left or right, and create a leaf at the empty spot. On the way back up, every node on the path calls rebalance, which refreshes its height and rotates if needed. Each call returns the possibly new subtree root, and the parent stores it. A duplicate key returns early, so nothing on the path changes. Our tree is a set; a map would update the value instead.

---
@type section
# Tracing insertion

---
# The running example

- Insert, in this order: `70, 50, 10, 20, 40, 30, 60`.
- Seven keys, and each of LL, RR, LR, RL happens exactly once.
- Every tree shown was printed by `AvlTree.shape()` and asserted in `Checks.java`.
- Small numbers beside nodes are balance factors; rose marks $\pm 2$.

> We use one key sequence for the rest of the lecture: seventy, fifty, ten, twenty, forty, thirty, sixty. It was chosen by a small search over short sequences so that all four cases occur once each. Nothing on the next slides was drawn by hand. The Java code printed every state, and the checks file fails if any of them changes. A green ring marks the key just inserted. Rose nodes have factor plus or minus two; the lowest one is repaired.

---
# Insert 70, 50, 10: the LL case

```html
<svg viewBox="0 0 572 221" style="width:100%;max-height:38cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="61.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="91.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="115.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<text x="64.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">after 70, 50</text>
</g>
<g data-step="1"><path d="M130.0 100.0 H164.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M164.0 93.0 L174.0 100.0 L164.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="259.0" y1="100.0" x2="205.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="313.0" y1="34.0" x2="259.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="205.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="205.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="205.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="229.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="259.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="259.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="283.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="313.0" cy="34.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="313.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="337.0" y="17.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<text x="259.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 10: bf(70) = +2</text>
</g>
<g data-step="2"><path d="M352.0 100.0 H386.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M386.0 93.0 L396.0 100.0 L386.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="481.0" y1="34.0" x2="427.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="481.0" y1="34.0" x2="535.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="427.0" cy="100.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="427.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="427.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="451.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="481.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="481.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="505.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="535.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="535.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="559.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="481.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">right rotation at 70</text>
</g>
</svg>
```

- 70 then 50: the left side is one taller, still AVL.
- 10 goes left of 50: path from 70 is left, left.
- One right rotation at 70 lifts 50 to the root.

> Seventy becomes the root and fifty its left child; seventy now has factor plus one, which is allowed. Ten is smaller than both, so it goes to the left of fifty. Walking back up, fifty is fine, but seventy now has a left subtree of height one and an empty right subtree: factor plus two. The new key went left, then left again, so this is the LL case. A single right rotation at seventy makes fifty the root with ten and seventy as children. The height drops back to one.

---
# Insert 20, then 40: the RR case

```html
<svg viewBox="0 0 896 287" style="width:100%;max-height:58cqh;font-family:var(--sans)" role="img">
<g>
<line x1="145.0" y1="34.0" x2="37.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="37.0" y1="100.0" x2="91.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="145.0" y1="34.0" x2="199.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="61.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="91.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="91.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="115.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="145.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="169.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="199.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="223.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="118.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 20</text>
</g>
<g data-step="1"><path d="M238.0 133.0 H272.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M272.0 126.0 L282.0 133.0 L272.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="475.0" y1="34.0" x2="313.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="313.0" y1="100.0" x2="367.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="367.0" y1="166.0" x2="421.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="475.0" y1="34.0" x2="529.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="313.0" cy="100.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="313.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="337.0" y="83.0" font-size="13" font-weight="700" fill="var(--rose-ink)">-2</text>
<circle cx="367.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="367.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="391.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="421.0" cy="232.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="421.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="421.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="445.0" y="215.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="475.0" cy="34.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="475.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="499.0" y="17.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<circle cx="529.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="529.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="553.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="421.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 40: bf(10) = -2</text>
</g>
<g data-step="2"><path d="M568.0 133.0 H602.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M602.0 126.0 L612.0 133.0 L602.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="697.0" y1="100.0" x2="643.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="805.0" y1="34.0" x2="697.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="697.0" y1="100.0" x2="751.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="805.0" y1="34.0" x2="859.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="643.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="643.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="667.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="697.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="697.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="721.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="751.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="751.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="751.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="775.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="805.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="805.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="829.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="859.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="859.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="883.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="751.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">left rotation at 10</text>
</g>
</svg>
```

- 20 lands right of 10; factors change but none leaves the range.
- 40 goes right of 20: path from 10 is right, right.
- The lowest unbalanced node is 10, not the root.

> Twenty is bigger than ten and smaller than fifty, so it becomes ten's right child. Ten gets factor minus one and fifty gets plus one: no repair. Then forty follows the same route and lands right of twenty. Walking up, twenty is fine, but ten now has an empty left side and a right side of height one: factor minus two. This is RR, fixed by a left rotation at ten. Notice the repair happens deep in the tree. For a moment fifty shows plus two as well, but it is never rotated: fixing ten restores that subtree's height, so when the repair walk reaches fifty its factor is back to plus one.

---
# Insert 30: the LR case

```html
<svg viewBox="0 0 1112 287" style="width:100%;max-height:58cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="100.0" x2="37.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="34.0" x2="91.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="166.0" x2="145.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="100.0" x2="199.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="34.0" x2="307.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="61.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="91.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="115.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="145.0" cy="232.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="145.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="169.0" y="215.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="199.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="223.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="253.0" cy="34.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="253.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="277.0" y="17.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<circle cx="307.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="307.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="331.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="172.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 30: bf(50) = +2</text>
</g>
<g data-step="1"><path d="M346.0 133.0 H380.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M380.0 126.0 L390.0 133.0 L380.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="475.0" y1="166.0" x2="421.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="583.0" y1="100.0" x2="475.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="475.0" y1="166.0" x2="529.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="34.0" x2="583.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="34.0" x2="691.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="421.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="421.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="445.0" y="215.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="475.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="475.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="499.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="529.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="529.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="553.0" y="215.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="583.0" cy="100.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="583.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="607.0" y="83.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<circle cx="637.0" cy="34.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="637.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="661.0" y="17.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<circle cx="691.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="691.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="715.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="556.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">left rotation at 20</text>
</g>
<g data-step="2"><path d="M730.0 133.0 H764.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M764.0 126.0 L774.0 133.0 L764.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="859.0" y1="100.0" x2="805.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="967.0" y1="34.0" x2="859.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="859.0" y1="100.0" x2="913.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="967.0" y1="34.0" x2="1021.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="1021.0" y1="100.0" x2="1075.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="805.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="805.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="829.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="859.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="859.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="883.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="913.0" cy="166.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="913.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="913.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="937.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="967.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="967.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="991.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="1021.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1021.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="1045.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="1075.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1075.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="1099.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="940.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">right rotation at 50</text>
</g>
</svg>
```

- 30 goes left of 50, then right of 20: a zigzag.
- Left rotation at 20 straightens it into a left-left line.
- Right rotation at 50 finishes: 40 becomes the root.

> Thirty goes left at fifty, right at twenty and left at forty. The first unbalanced node on the way up is fifty, with factor plus two. The path from fifty toward thirty goes left, then right: the LR case. The middle picture shows the tree after the first rotation, at twenty. It is still unbalanced, but now it is a straight left-left shape, and fifty's factor is still two. The right rotation at fifty finishes the job. Forty ends up on top with twenty and fifty below it, and the height is back to two.

---
# Insert 60: the RL case

```html
<svg viewBox="0 0 1274 287" style="width:100%;max-height:58cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="100.0" x2="37.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="91.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="253.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="361.0" y1="166.0" x2="307.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="253.0" y1="100.0" x2="361.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="61.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="91.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="115.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="145.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="169.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="199.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="223.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="253.0" cy="100.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="253.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="277.0" y="83.0" font-size="13" font-weight="700" fill="var(--rose-ink)">-2</text>
<circle cx="307.0" cy="232.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="307.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="307.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">60</text>
<text x="331.0" y="215.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="361.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="361.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="385.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<text x="199.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">insert 60: bf(50) = -2</text>
</g>
<g data-step="1"><path d="M400.0 133.0 H434.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M434.0 126.0 L444.0 133.0 L434.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="529.0" y1="100.0" x2="475.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="34.0" x2="529.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="100.0" x2="583.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="34.0" x2="691.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="691.0" y1="100.0" x2="745.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="745.0" y1="166.0" x2="799.0" y2="232.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="475.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="475.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="499.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="529.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="529.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="553.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="583.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="583.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="607.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="637.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="637.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="661.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="691.0" cy="100.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="691.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="715.0" y="83.0" font-size="13" font-weight="700" fill="var(--rose-ink)">-2</text>
<circle cx="745.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="745.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">60</text>
<text x="769.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="799.0" cy="232.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="799.0" y="238.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="823.0" y="215.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="637.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">right rotation at 70</text>
</g>
<g data-step="2"><path d="M838.0 133.0 H872.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M872.0 126.0 L882.0 133.0 L872.0 140.0 Z" fill="var(--ink-2)"/>
<line x1="967.0" y1="100.0" x2="913.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="1075.0" y1="34.0" x2="967.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="967.0" y1="100.0" x2="1021.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="1183.0" y1="100.0" x2="1129.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="1075.0" y1="34.0" x2="1183.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="1183.0" y1="100.0" x2="1237.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="913.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="913.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="937.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="967.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="967.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="991.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="1021.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1021.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="1045.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="1075.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1075.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="1099.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="1129.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1129.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="1153.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="1183.0" cy="100.0" r="26" fill="none" stroke="var(--green-ink)" stroke-width="3"/><circle cx="1183.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1183.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">60</text>
<text x="1207.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="1237.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="1237.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="1261.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="1075.0" y="279.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">left rotation at 50</text>
</g>
</svg>
```

- 60 goes right of 50, then left of 70: the mirror zigzag.
- Right rotation at 70, then left rotation at 50.
- Result: a perfect tree of height 2 holding all seven keys.

> Sixty is bigger than forty and fifty and smaller than seventy, so it lands as seventy's left child. Fifty now has an empty left side and a right subtree of height one: factor minus two. The path from fifty goes right, then left: the RL case. First a right rotation at seventy turns the zigzag into a straight right-right line, shown in the middle. Then a left rotation at fifty lifts sixty. The final tree is perfectly balanced with height two, and an inorder walk gives ten through seventy.

---
# The trace in one table

| Insert | Lowest unbalanced node | Case | Rotations | Height after |
|---|---|---|---|---|
| 70, 50 | none | none | 0 | 1 |
| 10 | 70 | LL | 1 | 1 |
| 20 | none | none | 0 | 2 |
| 40 | 10 | RR | 1 | 2 |
| 30 | 50 | LR | 2 | 2 |
| 60 | 50 | RL | 2 | 2 |

> Read the table as a summary of the four trace slides. Only four of seven insertions needed any repair, and each needed exactly one fix, single or double. Also notice the height column: seven keys never pushed the height past two, which is the minimum possible for seven nodes. A plain BST with the same insertions reaches height five, as PlainBst confirms; draw it to see the long zigzag.

---
# Why one fix is enough after an insertion

- Let $z$ be the lowest unbalanced node, and let $h$ be its height before the insertion.
- The insertion raised $z$'s taller side, so $z$'s height became $h+1$.
- The single or double rotation brings that subtree back to height $h$.
- Every ancestor above sees the same height as before: nothing else to fix.
- So an insertion needs at most one single or double rotation.

> Here is the argument. Before the insertion, z was balanced with height h. The new key made its taller side one level taller, which is why z's factor hit two and its height went to h plus one. Checking the cases shows the rotation produces a subtree of height exactly h again. We saw it in the trace: after each fix, the subtree had the same height as before the insertion. Since the ancestors above z only depend on that height, they are as balanced as they were before. Our randomized checks assert this: at most one repair per insertion.

---
```quiz
Insert the keys 30, then 10, then 20 into an empty AVL tree. Which case occurs, and at which node?
- [ ] LL at 30
- [ ] RR at 10
- [x] LR at 30
- [ ] RL at 10
```

> After thirty and ten, ten is thirty's left child. Twenty is smaller than thirty and bigger than ten, so it becomes ten's right child. Thirty now has factor plus two, and the path from thirty to the new key goes left, then right. That is the LR case at thirty: a left rotation at ten, then a right rotation at thirty, leaving twenty at the root with ten and thirty as children.

---
@type section
# Deletion

---
# Deletion: BST delete, then repair the whole path

- Zero or one child: splice the node out.
- Two children: copy in the successor key, then delete the successor.
- Walk back up and call `rebalance` at every ancestor on the path.
- If the unbalanced node's child has factor 0, a single rotation fixes it.
- Unlike insertion, a fix can lower the subtree height and unbalance an ancestor.

> Deletion starts as in lecture eight. A node with at most one child is replaced by that child. A node with two children takes the key of its successor, the smallest key in its right subtree, and then we delete the successor, which has no left child. Then, just like insertion, we walk up the path and rebalance. Two differences matter. First, the taller child can be exactly balanced, a case insertion never produces, and a single rotation is the right fix there. Second, a rotation may make the subtree one shorter, so an ancestor may now be out of balance, and the repair can continue upward.

---
# Deletion in Java

```java body of delete(node, key)
if (node == null) return null;           // key not found
int c = key.compareTo(node.key);
if (c < 0) node.left = delete(node.left, key);
else if (c > 0) node.right = delete(node.right, key);
else if (node.left == null || node.right == null) {
    size--;                              // zero or one child: splice out
    return node.left != null ? node.left : node.right;
} else {
    Node<K> s = node.right;              // two children: successor
    while (s.left != null) s = s.left;
    node.key = s.key;                    // copy successor key up
    node.right = delete(node.right, s.key);
}
return rebalance(node);                  // every ancestor on the path
```

> The structure mirrors insertion: recurse down, then rebalance on the way back. The splice case returns the one child, which is already a valid AVL subtree, so it needs no repair itself. In the two-child case we copy the successor's key and recursively delete that key from the right subtree; the successor has no left child, so that inner call ends in the splice case. The last line runs at every node on the path, which is what lets repairs cascade toward the root.

---
# Delete 60: two children, use the successor

```html
<svg viewBox="0 0 790 221" style="width:100%;max-height:48cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="100.0" x2="37.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="91.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="307.0" y1="100.0" x2="253.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="307.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="307.0" y1="100.0" x2="361.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="61.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="91.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="115.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="145.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="169.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="199.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="223.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="253.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="253.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="277.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="307.0" cy="100.0" r="26" fill="none" stroke="var(--amber-ink)" stroke-width="3" stroke-dasharray="6 3"/><circle cx="307.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="307.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">60</text>
<text x="331.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="361.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="361.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="385.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="199.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">delete 60</text>
</g>
<g data-step="1"><path d="M400.0 100.0 H434.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M434.0 93.0 L444.0 100.0 L434.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="529.0" y1="100.0" x2="475.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="34.0" x2="529.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="100.0" x2="583.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="745.0" y1="100.0" x2="691.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="637.0" y1="34.0" x2="745.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="475.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="475.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="499.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="529.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="529.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="553.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="583.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="583.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="607.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="637.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="637.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="661.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="691.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="691.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">50</text>
<text x="715.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="745.0" cy="100.0" r="26" fill="none" stroke="var(--amber-ink)" stroke-width="3" stroke-dasharray="6 3"/><circle cx="745.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="745.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="769.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<text x="610.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">70 moves up, no rotation</text>
</g>
</svg>
```

- Dashed ring: first the key to delete, then the node that received 70.
- 60 has two children, so its successor 70 takes its place.
- 70 was a leaf, so removing it from the right subtree is a splice.
- Factors on the path stay in range: no rotation.

> We continue with the tree built by the insertions. On deletion slides the dashed ring marks the key being deleted, and afterwards the node where the successor landed; seventy was moved there, not inserted. Sixty has two children. Its successor is the smallest key in its right subtree, which is seventy. We copy seventy into sixty's node and delete the old seventy leaf. On the way up, the node now holding seventy has factor plus one and the root has zero. Nothing is out of range, so no rotation is needed.

---
# Delete 50, then 70: a rotation at the root

```html
<svg viewBox="0 0 850 221" style="width:100%;max-height:48cqh;font-family:var(--sans)" role="img">
<g>
<line x1="91.0" y1="100.0" x2="37.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="91.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="91.0" y1="100.0" x2="145.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="199.0" y1="34.0" x2="253.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="37.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="37.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="61.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="91.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="91.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="115.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="145.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="145.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="169.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="199.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="199.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="223.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<circle cx="253.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="253.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">70</text>
<text x="277.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<text x="145.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">after deleting 50</text>
</g>
<g data-step="1"><path d="M292.0 100.0 H326.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M326.0 93.0 L336.0 100.0 L326.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="421.0" y1="100.0" x2="367.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="529.0" y1="34.0" x2="421.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="421.0" y1="100.0" x2="475.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="367.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="367.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="391.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="421.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="421.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="445.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="475.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="475.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="499.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="529.0" cy="34.0" r="21" fill="var(--rose)" stroke="var(--rose-ink)" stroke-width="2"/><text x="529.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="553.0" y="17.0" font-size="13" font-weight="700" fill="var(--rose-ink)">+2</text>
<text x="448.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">remove 70: bf(40) = +2</text>
</g>
<g data-step="2"><path d="M568.0 100.0 H602.0" stroke="var(--ink-2)" stroke-width="2.5"/><path d="M602.0 93.0 L612.0 100.0 L602.0 107.0 Z" fill="var(--ink-2)"/>
<line x1="697.0" y1="34.0" x2="643.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="805.0" y1="100.0" x2="751.0" y2="166.0" stroke="var(--ink-2)" stroke-width="2"/>
<line x1="697.0" y1="34.0" x2="805.0" y2="100.0" stroke="var(--ink-2)" stroke-width="2"/>
<circle cx="643.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="643.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">10</text>
<text x="667.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="697.0" cy="34.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="697.0" y="40.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">20</text>
<text x="721.0" y="17.0" font-size="13" font-weight="400" fill="var(--ink-2)">-1</text>
<circle cx="751.0" cy="166.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="751.0" y="172.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">30</text>
<text x="775.0" y="149.0" font-size="13" font-weight="400" fill="var(--ink-2)">0</text>
<circle cx="805.0" cy="100.0" r="21" fill="var(--paper)" stroke="var(--ink-2)" stroke-width="2"/><text x="805.0" y="106.0" text-anchor="middle" font-size="17" font-weight="600" fill="var(--ink)">40</text>
<text x="829.0" y="83.0" font-size="13" font-weight="400" fill="var(--ink-2)">+1</text>
<text x="724.0" y="213.0" text-anchor="middle" font-size="15" fill="var(--ink-2)">right rotation at 40</text>
</g>
</svg>
```

- Deleting 50 leaves 70 as a leaf; the root has factor $+1$.
- Deleting 70 empties the right side: the root reaches $+2$.
- The left child 20 has factor 0, so a single right rotation fixes it.

> Delete fifty: it is a leaf, and every factor stays in range. Now delete seventy. The root's right side becomes empty while its left subtree has height one, so the root's factor is plus two. Look at the left child twenty: both its children are leaves, so its factor is zero. Insertion can never produce that situation, but deletion can. The code tests for a strictly negative factor before doing a double rotation, so it does a single right rotation at forty. Twenty becomes the root, and forty keeps thirty as its left child. The height stays two.

---
# One deletion, several rotations

- After a rotation, the fixed subtree may be one level shorter than before.
- Its parent then sees a shorter child and may become unbalanced too.
- In the worst case repairs continue up to the root: $O(\log n)$ rotations.
- `Checks.java` has a 12-node tree where deleting one key rotates at two ancestors.
- Each rotation is $O(1)$, so deletion still costs $O(\log n)$.

> Our small trace needed only one rotation, but that is not a guarantee. An insertion fix restores the old height; a deletion fix may leave the subtree one shorter, and the parent then compares a shorter child with its other child. The smallest example needs twelve nodes, which is too big to draw clearly here, so it lives in the checks file: deleting eleven from that tree triggers an LR fix at twelve and then an LL fix at eight. The number of rotations is bounded by the path length, so the total work stays logarithmic.

---
@type section
# Height and cost

---
# The sparsest AVL trees

- $N(h)$: the fewest nodes an AVL tree of height $h$ can have.
- Take a root, one child subtree of height $h-1$, the other as short as allowed: $h-2$.
- Recurrence, for $h \ge 2$: $N(h) = N(h-1) + N(h-2) + 1$.
- Base cases: $N(0) = 1,\ N(1) = 2$.
- Heights 0 to 6 give $N(h)$ = 1, 2, 4, 7, 12, 20, 33.

> To bound the height, turn the question around: how few nodes can an AVL tree of height h have? A tree that tall needs a root and one child subtree of height h minus one. The AVL rule lets the other child be one shorter, height h minus two, and to use as few nodes as possible both subtrees should themselves be as sparse as possible. That gives the recurrence. The table values come from our AvlBounds class. Notice twelve at height four: that is why the cascading deletion example needed twelve nodes.

---
# From Fibonacci to $1.44 \log_2 n$

- The recurrence matches Fibonacci: $N(h) = F(h+3) - 1$, with $F(1) = F(2) = 1$.
- Fibonacci numbers grow like powers of $\varphi = (1+\sqrt5)/2 \approx 1.618$: $F(k) \ge \varphi^{k-2}$.
- So an AVL tree with $n$ nodes and height $h$ has $n + 1 \ge \varphi^{h+1}$.

$$h \le \log_\varphi(n+1) - 1$$

- Since $\log_\varphi x \approx 1.44 \log_2 x$: $h \lesssim 1.44 \log_2(n+1) - 1$.

- Asymptotically, an AVL tree is at most about 44% taller than the best possible tree.

> Add one to both sides of the recurrence and it becomes the Fibonacci recurrence, which gives N of h equals F of h plus three, minus one; our checks confirm this up to height sixty. A simple induction shows F of k is at least phi to the k minus two, where phi is the golden ratio. Put those together, take logarithms, and the height is at most log base phi of n plus one, minus one. Since one over log base two of phi is about one point four four, the height is roughly one point four four times log base two of n. The best any binary tree can do is log base two of n, so the price of AVL's loose rule is a constant factor.

---
# Costs of AVL operations

| Operation | Time, worst case | Rotations |
|---|---|---|
| search | $O(\log n)$ | none |
| insert | $\Theta(\log n)$ | at most one single or double |
| delete | $\Theta(\log n)$ | $O(\log n)$ |
| build from $n$ keys by insertion | $O(n \log n)$ | at most $n$ fixes |

- Space: $\Theta(n)$ nodes plus one `int` height each; recursion depth $O(\log n)$.

> Every operation walks one path, whose length is at most about one point four four log n, and does constant work per level. Search can finish early, at the root in the best case, so we state its worst case as big O of log n. Our recursive insert and delete always walk the path down and back up, so they take theta log n. Space is linear: one node per key, and the height field adds one integer to each. The recursion uses stack space proportional to the height, which is logarithmic. These are worst-case bounds, not averages: that is the whole point of balancing.

---
# A checker that trusts nothing

```java
private int check(Node<K> n, K lo, K hi, int[] count) {
    if (n == null) return -1;
    count[0]++;
    boolean inRange = (lo == null || n.key.compareTo(lo) > 0)
            && (hi == null || n.key.compareTo(hi) < 0);
    if (!inRange) throw new AssertionError("order broken at " + n.key);
    int hl = check(n.left, lo, n.key, count);
    int hr = check(n.right, n.key, hi, count);
    if (n.height != 1 + Math.max(hl, hr))
        throw new AssertionError("stale height at " + n.key);
    if (Math.abs(hl - hr) > 1)
        throw new AssertionError("unbalanced at " + n.key);
    return n.height;
}
```

- Checks.java runs 300 random sequences of inserts and deletes against `TreeSet`.

> A checker recomputes everything from scratch instead of trusting stored fields. It passes down an open interval of allowed keys, which catches order violations anywhere, not only between a parent and child. It recomputes each height and compares with the stored one, which catches a forgotten update. And it tests the AVL rule at every node. The random tests call this checker after every single operation and compare the keys with java dot util dot TreeSet, so a bug shows up at the first operation that causes it.

---
# Common mistakes and edge cases

- Forgetting to store the returned subtree root: the rotation is lost.
- Updating heights top-down after a rotation: update the lower node first.
- Mixing conventions: leaf height 0 and empty height $-1$, or the node-count version.
- Using `<= 0` instead of `< 0` for the double-rotation test breaks deletion.
- Edge cases: empty tree, one node, duplicate keys, `null` keys, absent keys.

> These are the bugs the checker catches most often. Forgetting to assign the result of a rotation leaves the parent pointing at the old top node. Updating heights in the wrong order computes the upper node from a stale lower one. Mixing height conventions makes factors wrong wherever a child is empty. The test for a double rotation must be strictly negative: if the child is balanced, a double rotation can leave a node with factor two. Our checks catch that mutation: even the running example's deletion of seventy fails. Finally, test the boring inputs: an empty tree has height minus one, a duplicate insert must not grow the size, and our code rejects null keys with a NullPointerException.

---
# AVL trees and the Java library

- `java.util` has no AVL tree class.
- `TreeMap` and `TreeSet` are balanced search trees, but red-black, not AVL.
- Both give sorted iteration and $O(\log n)$ lookups; that is next lecture.
- Our `AvlTree` is a teaching implementation: a set of distinct keys.

> You will not find an AVL class in the Java standard library. When you need a sorted map or set in Java, you use TreeMap or TreeSet, and the API documentation says TreeMap is based on a red-black tree, which is the topic of the next lecture. The ideas you learned today carry straight over: rotations, a local rule, and repairs along one path. Our AvlTree stores only keys and ignores duplicates; turning it into a map means adding a value field and updating the value when a key is already present.

---
```quiz
What is the fewest number of nodes an AVL tree of height 4 can have (height counts edges)?
- [ ] 5
- [ ] 8
- [x] 12
- [ ] 16
```

> Use the recurrence: N of zero is one and N of one is two, so N of two is four, N of three is seven, and N of four is seven plus four plus one, which is twelve. Five nodes can form a path of height four, but a path is far from AVL. Eight is too few to reach height four under the AVL rule, and sixteen is more than needed. Twelve is the right answer, and it matches the cascading deletion example in the checks file.

---
@type section
# Wrap-up

---
# Summary

- BST cost is the height; sorted input makes a plain BST a path.
- A rotation changes shape in $O(1)$ and keeps the inorder order.
- AVL: every balance factor is $-1$, $0$ or $+1$; heights are stored.
- Insertion: at most one single or double rotation; deletion may need $O(\log n)$.
- Height below about $1.44 \log_2(n+1)$, so every operation is $O(\log n)$ worst case.

> Put the lecture in five sentences. The height controls the cost of a search tree. Rotations are the safe local move that changes height without changing order. The AVL rule is a local condition on heights, cheap to check because each node stores its height. After an insertion one fix suffices; after a deletion the fixes can climb to the root, but each is constant time. And the Fibonacci argument turns the local rule into a global logarithmic height bound.

---
# Check yourself

- Draw the AVL tree after inserting `1, 2, 3, 4, 5, 6, 7` in order. Which cases occur?
- Why can a single rotation not fix the LR case?
- Build a tree where deleting a leaf needs a single rotation at a node whose child has factor 0.

> Try these without notes. For the first, trace each insertion and name the case at each fix; the result should be a perfect tree. For the second, draw the LR shape, apply a right rotation at z, and see what shape you get. For the third, our deletion of seventy is one answer; try to find a different one of your own.

---
# Sources

- G. M. Adelson-Velsky and E. M. Landis (1962): the original AVL tree.
- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS): Chapter 12 (binary search trees), Chapter 13 (rotations; AVL trees appear as an end-of-chapter problem).
- Java SE API documentation: `java.util.TreeMap`, `java.util.TreeSet`.

> The AVL tree comes from the 1962 paper by Adelson-Velsky and Landis. Our conventions, height in edges and the rotation pictures, follow CLRS, which treats binary search trees in chapter twelve and rotations in chapter thirteen, with AVL trees as a problem at the end of that chapter. All tree states on these slides were produced by the lecture's tested Java code.
