@title Lecture 9: Tree traversals
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Tree traversals
## Visit every node, in the order the job needs

---
# Where we are

- L07: a stack is last in, first out; a queue is first in, first out.
- L08: binary trees, the BST property, and operations that walk one path.
- Today: operations that must visit **every** node of a tree.
- Recursion gives three depth-first orders; a queue gives level order.

> Last time every operation followed a single path from the root, which is why it cost the height. Many jobs need more than one path: printing all keys, copying a tree, computing its size. Today we visit every node exactly once, and the interesting question is in what order. Recursion, which uses the call stack, gives three natural orders. A queue from lecture seven gives a fourth, level by level.

---
# By the end of today you can

- Write preorder, inorder and postorder recursively and trace them by hand.
- Explain why inorder lists a BST's keys in sorted order.
- Trace level order with a queue and inorder with an explicit stack.
- Pick the traversal that fits a job: evaluate, copy, measure, print.
- Rebuild a tree from preorder and inorder, and explain why preorder and postorder are not enough.

> These are all pencil skills again. You should be able to take any small tree and write down its four traversal orders without running code, and to take a piece of code and say which order it uses. The reconstruction outcome is a good test of understanding: it only works if you know exactly what each order tells you about where the root is.

---
# Why the order matters

- Print a BST's keys from smallest to largest.
- Evaluate `(3 + 4) * (5 - 2)`: operands before their operator.
- Copy a tree: each parent must exist before its children attach.
- Compute size or height: children's answers before the parent's.
- Print an org chart one management level at a time.

> Each job visits every node, but each one needs the nodes in a particular order. Printing sorted keys needs left, then the node, then right. Evaluating an expression needs both operands before the operator can be applied. Copying needs the parent first, and measuring needs the children first. Printing by levels ignores the left-right recursion entirely. By the end of today each of these has a matching traversal.

---
# The running tree again

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

> This is the binary search tree from lecture eight, built by inserting fifty, thirty, seventy, twenty, forty, eighty, thirty-five, forty-five. Gray nil marks the missing left child of seventy. Every trace today runs on this tree, and every order shown was produced by running the Java code in package l09 on it. Copy it once more; you will write four different sequences under it.

---
@type section
# Depth-first traversals

---
# What a traversal is

- A **traversal** visits every node of the tree exactly once.
- To **visit** a node is to do the job's work there: here, append its key to a list.
- **Depth-first**: finish one subtree completely before starting its sibling.
- **Breadth-first**: finish one level completely before starting the next.

> The word visit is deliberately vague: it means whatever the application needs, such as printing a key, adding it to a sum or copying it. In our code a visit appends the key to an output list, so the list records the order. Depth-first traversals dive down one branch all the way before backing up. Breadth-first traversals sweep across the tree one level at a time. We start with depth-first, which recursion gives us almost for free.

---
# One template, three places to visit

```algorithm
function TRAVERSE(x):
  if x = nil then return
  // PREORDER: visit x here
  TRAVERSE(x.left)
  // INORDER: visit x here
  TRAVERSE(x.right)
  // POSTORDER: visit x here
```

- Pre means before the children, in means between them, post means after.

> All three depth-first traversals have the same skeleton: return on nil, recurse left, recurse right. The only difference is where the single visit line goes. Before both recursive calls gives preorder, between them gives inorder, and after both gives postorder. Every one of them explores the left subtree before the right one; only the moment when the node itself is recorded changes.

---
# Preorder in Java

```java
static <T> void preorder(BinaryNode<T> x, List<T> out) {
    if (x == null) return;
    out.add(x.value);                         // visit before the children
    preorder(x.left, out);
    preorder(x.right, out);
}
```

- `BinaryNode<T>` has `value`, `left` and `right`, like L08's `Node`.
- Call it as `preorder(root, out)` with an empty list `out`.

> The node class in this package is the same idea as last lecture's, with a generic value instead of a comparable key, because traversals do not compare anything. The method adds the node to the list and then handles its left and right subtrees. The null check at the top is the base case, and it is also what makes the empty tree work: the list simply stays empty.

---
# Preorder, step by step

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
--- Visit 50 on arrival. Output: 50
focus N50 N30
--- Go left; visit 30. Output: 50 30
focus N50 N30 N20
--- Visit 20, a leaf. Output: 50 30 20
focus N50 N30 N20 N40
--- 30's right child: visit 40. Output: 50 30 20 40
focus N50 N30 N20 N40 N35
--- Visit 35. Output: 50 30 20 40 35
focus N50 N30 N20 N40 N35 N45
--- Visit 45. The left subtree of 50 is done. Output: ... 35 45
focus N50 N30 N20 N40 N35 N45 N70
--- Visit 70; its left child is missing. Output: ... 45 70
focus N50 N30 N20 N40 N35 N45 N70 N80
--- Visit 80. Preorder: 50 30 20 40 35 45 70 80
```

> Each node is recorded the moment the traversal first reaches it. So the root comes first, then everything in its left subtree, then everything in its right subtree, and the same pattern repeats inside each subtree. Notice that the left subtree of fifty, thirty through forty-five, forms one unbroken block of the output. That block structure is what reconstruction will use later.

---
# Inorder and postorder in Java

```java
static <T> void inorder(BinaryNode<T> x, List<T> out) {
    if (x == null) return;
    inorder(x.left, out);
    out.add(x.value);                         // visit between the children
    inorder(x.right, out);
}

static <T> void postorder(BinaryNode<T> x, List<T> out) {
    if (x == null) return;
    postorder(x.left, out);
    postorder(x.right, out);
    out.add(x.value);                         // visit after the children
}
```

- Only the position of the visit line differs from preorder.

> Compare both with preorder: the same three lines, with the visit moved. In inorder the visit sits between the two recursive calls, so a node waits until everything on its left has been recorded, and the first key recorded is the leftmost node, the one you reach by going left until you cannot. In postorder the visit comes last, so a node is recorded only after everything below it: the root is always the final entry. That is the order for any job where a node needs its children's results first, such as computing heights, evaluating expressions, or freeing memory.

---
# Inorder, step by step

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
focus N20
--- Go left past 50 and 30; 20 has no left child. Output: 20
focus N20 N30
--- Back at 30, its left side done. Output: 20 30
focus N20 N30 N35
--- Right to 40, left to 35. Output: 20 30 35
focus N20 N30 N35 N40
--- Back at 40. Output: 20 30 35 40
focus N20 N30 N35 N40 N45
--- Then 45. Output: 20 30 35 40 45
focus N20 N30 N35 N40 N45 N50
--- The left subtree of 50 is done: visit 50. Output: ... 45 50
focus N20 N30 N35 N40 N45 N50 N70
--- 70's left child is missing: visit 70. Output: ... 50 70
focus N20 N30 N35 N40 N45 N50 N70 N80
--- Visit 80. Inorder: 20 30 35 40 45 50 70 80
```

> The traversal passes through fifty and thirty on the way down but does not record them yet; they wait for their left subtrees. Twenty is recorded first because it is the leftmost node. Watch fifty: it is recorded sixth, exactly when its entire left subtree has been output. The result is the keys in increasing order, which is no accident, as the next section proves.

---
# Postorder, step by step

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
focus N20
--- 20 is a leaf: finished at once. Output: 20
focus N20 N35
--- Right to 40, left to leaf 35. Output: 20 35
focus N20 N35 N45
--- Leaf 45. Output: 20 35 45
focus N20 N35 N45 N40
--- Both children of 40 are done. Output: 20 35 45 40
focus N20 N35 N45 N40 N30
--- Both subtrees of 30 are done. Output: ... 40 30
focus N20 N35 N45 N40 N30 N80
--- Down the right side: leaf 80. Output: ... 30 80
focus N20 N35 N45 N40 N30 N80 N70
--- 70's only child is done. Output: ... 80 70
focus N20 N35 N45 N40 N30 N80 N70 N50
--- The root comes last. Postorder: 20 35 45 40 30 80 70 50
```

> Every node appears after all of its descendants. The three leaves on the left come first, then forty, which needed both of them, then thirty. The right side follows the same rule: eighty before seventy. Fifty is last because it depends on everything. Notice that postorder is not simply preorder reversed; reversing preorder gives eighty, seventy, forty-five first, which is a different order.

---
# The three orders side by side

| Order | Running tree | First entry | Last entry |
|---|---|---|---|
| Preorder | `50 30 20 40 35 45 70 80` | Root | A leaf |
| Inorder | `20 30 35 40 45 50 70 80` | Leftmost node | Rightmost node |
| Postorder | `20 35 45 40 30 80 70 50` | A leaf | Root |

- Every order visits left subtrees before right subtrees.

> Read the columns to learn what each order reveals. Preorder always starts with the root, and postorder always ends with it. Inorder starts at the leftmost node and ends at the rightmost, which in a BST are the minimum and the maximum. These facts are what let us recover a tree from its traversals later. The last preorder entry and the first postorder entry are always leaves: a node with a child cannot be the last one preorder records, or the first one postorder records.

---
# The walk around the tree

```html
<svg viewBox="0 0 400 230" style="width:100%;max-width:560px" role="img" aria-label="A path walking around a three-node tree, numbered at each visit">
  <path d="M 169 32 L 77 131 A 38 38 0 1 0 142.9 131 Q 200 70 257.1 131 A 38 38 0 1 0 322.9 131 L 231 32" fill="none" stroke="var(--accent)" stroke-width="2" stroke-dasharray="6 5"/>
  <g stroke="var(--ink)" stroke-width="1.5" fill="none"><line x1="200" y1="50" x2="110" y2="150"/><line x1="200" y1="50" x2="290" y2="150"/></g>
  <g fill="var(--paper)" stroke="var(--ink)" stroke-width="1.5"><circle cx="200" cy="50" r="22"/><circle cx="110" cy="150" r="22"/><circle cx="290" cy="150" r="22"/></g>
  <g fill="var(--ink)" font-size="16" text-anchor="middle" dominant-baseline="central"><text x="200" y="50">40</text><text x="110" y="150">35</text><text x="290" y="150">45</text></g>
  <g fill="var(--accent)" font-size="14" font-weight="600" text-anchor="middle" dominant-baseline="central">
    <text x="160" y="56" data-step="1">1</text><text x="62" y="152" data-step="2">2</text><text x="110" y="205" data-step="3">3</text>
    <text x="146" y="166" data-step="4">4</text><text x="200" y="112" data-step="5">5</text><text x="254" y="166" data-step="6">6</text>
    <text x="290" y="205" data-step="7">7</text><text x="338" y="152" data-step="8">8</text><text x="240" y="56" data-step="9">9</text>
  </g>
</svg>
```

- Walk around the tree, keeping it on your left. Each node is passed three times.
- Left side: preorder. Underneath: inorder. Right side: postorder.

> Imagine tracing around the outside of the tree with your finger, starting at the top left of the root and hugging every node and edge. You pass each node three times: on its left as you arrive from above, underneath it as you come back from its left subtree, and on its right as you leave. Number the passes one to nine on this small subtree. The left-side passes, one, two and six, give forty, thirty-five, forty-five, which is preorder. The passes underneath, three, five and seven, give thirty-five, forty, forty-five, which is inorder. The right-side passes, four, eight and nine, give thirty-five, forty-five, forty, which is postorder. This walk is often called an Euler tour. The eulerTour method in package l09 records the three passes, and our checks confirm that its output on this subtree matches the nine numbers.

---
# Quiz: postorder

```quiz
In the postorder of the running tree, which key is recorded fourth?
- [ ] 35
- [x] 40
- [ ] 30
- [ ] 45
```

> Postorder records the leaves on the far left first: twenty, then thirty-five and forty-five, which are the children of forty. Forty has now had both subtrees finished, so it is recorded fourth. Thirty comes fifth because it had to wait for forty. If you answered forty-five, you probably stopped counting after the leaves; if you answered thirty, you may have recorded thirty before its right subtree was finished.

---
@type section
# Inorder and BSTs

---
# Inorder of a BST is sorted

- Claim: inorder lists a BST's keys in increasing order.
- Empty tree: nothing listed, which is sorted.
- Otherwise: inorder lists the left subtree, then the root, then the right subtree.
- By induction each part is sorted, and left keys < root < right keys.
- So the whole list is sorted.

> This is a proof by strong induction on the size of the tree. Assume inorder sorts every smaller BST. For a tree with root key k, inorder first lists the left subtree, sorted by assumption, and all smaller than k by the BST property. Then it lists k. Then the right subtree, sorted by assumption, and all larger than k. Concatenating three sorted pieces where each piece is below the next gives a sorted list.

---
# What that gives us

- The L08 method `keys()` is an inorder walk.
- Inorder also checks a tree: a binary tree is a BST (distinct keys) iff its inorder is strictly increasing.
- Inserting $n$ keys, then walking inorder, is a sort: the inserts cost up to $O(nh)$.
- `TreeMap` iterates its keys in ascending order for the same reason.

> The sorted-order fact has a converse, which gives a simple validity test: if the inorder sequence of a binary tree with distinct keys is strictly increasing, the tree satisfies the BST property. That test is a useful alternative to the range-passing check. Building a BST and walking it is a correct sort, but its cost depends on the height: with sorted input the inserts alone are quadratic. The ascending iteration of TreeMap from last lecture follows from the same property.

---
@type section
# Breadth-first: level order

---

# Level order with a queue

- **Level order**: level 0, then level 1, and so on; each level left to right.
- A FIFO queue serves nodes in the order they were discovered.

```algorithm
function LEVEL-ORDER(root):
  if root = nil then return
  Q ← empty queue; enqueue root
  while Q is not empty do
    x ← dequeue Q            // oldest discovered node
    visit x
    if x.left ≠ nil then enqueue x.left
    if x.right ≠ nil then enqueue x.right
```

- Each node is enqueued once and dequeued once.

> Level order is what you get reading the picture like a page of text: the root, then the second row, then the third. Recursion is the wrong tool, because it always finishes a whole subtree before its sibling. A queue from lecture seven fits exactly: when we visit a node, its children join the back of the queue, behind everything already discovered, so each level is served before the one below it. The loop invariant: the queue holds the discovered but unvisited nodes, in level order, with the oldest at the front. Enqueuing left before right keeps each level in left-to-right order, the nil checks keep missing children out of the queue, and the first line handles the empty tree.

---
# Level order in Java

```java
static <T> List<T> levelOrder(BinaryNode<T> root, List<String> log) {
    List<T> out = new ArrayList<>();
    if (root == null) return out;
    Deque<BinaryNode<T>> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
        BinaryNode<T> x = queue.remove();     // take from the front
        out.add(x.value);
        if (x.left != null) queue.add(x.left);    // children join the back
        if (x.right != null) queue.add(x.right);
        if (log != null) log.add(x.value + " | " + frontToBack(queue));
    }
    return out;
}
```

> ArrayDeque serves as the queue: add appends at the back and remove takes from the front. The Java documentation notes that ArrayDeque does not accept null elements, which is one more reason to test children for null before adding them. The log parameter is only for tracing: when it is not null, the method records the queue after each visit, and the next slide shows that record.

---
# Level order, step by step

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
--- Remove 50; add 30, 70. Queue: 30 70
focus N50 N30
--- Remove 30; add 20, 40. Queue: 70 20 40
focus N50 N30 N70
--- Remove 70; add 80. Queue: 20 40 80
focus N50 N30 N70 N20
--- Remove 20, a leaf. Queue: 40 80
focus N50 N30 N70 N20 N40
--- Remove 40; add 35, 45. Queue: 80 35 45
focus N50 N30 N70 N20 N40 N80
--- Remove 80. Queue: 35 45
focus N50 N30 N70 N20 N40 N80 N35
--- Remove 35. Queue: 45
focus N50 N30 N70 N20 N40 N80 N35 N45
--- Remove 45. Queue empty. Level order: 50 30 70 20 40 80 35 45
```

> Each caption is copied from the log our code produced, with the queue listed from front to back. Notice the moment after seventy is removed: the queue holds twenty, forty and eighty, which is exactly level two. Later, after forty, it holds eighty from level two followed by thirty-five and forty-five from level three. The queue never holds more than three nodes on this tree, which is the width of its widest level.

---
# Printing by levels

```java
// ...
while (!queue.isEmpty()) {
    List<T> level = new ArrayList<>();
    for (int k = queue.size(); k > 0; k--) {  // exactly one level
        BinaryNode<T> x = queue.remove();
        level.add(x.value);
        if (x.left != null) queue.add(x.left);
        if (x.right != null) queue.add(x.right);
    }
    levels.add(level);
}
```

- Running tree: `[[50], [30, 70], [20, 40, 80], [35, 45]]`.

> To print one line per level we need to know where each level ends. At the top of each outer iteration the queue holds exactly one complete level, so we record its size and remove exactly that many nodes. Their children, added during the inner loop, form the next level. The outer loop runs once per level, so it runs height plus one times: four for the running tree.

---
# Quiz: the queue

```quiz
Level order on the running tree has just removed 40 and added its children. What does the queue hold, front to back?
- [ ] 35 45
- [x] 80 35 45
- [ ] 45 35 80
- [ ] 70 80 35 45
```

> Before forty was removed, the queue held forty and eighty: seventy had added eighty at the back. Removing forty leaves eighty at the front, and forty's children thirty-five and forty-five join behind it, left child first. So the queue is eighty, thirty-five, forty-five. Seventy is not there because it was removed two steps earlier. This matches the log line our code produced.

---
@type section
# Inorder with an explicit stack

---
# Why avoid recursion?

- Each recursive call uses a frame on the Java call stack.
- Depth-first recursion goes as deep as the tree: up to $h + 1$ frames on nodes.
- A chain of many nodes can throw `StackOverflowError`.
- An explicit stack in the heap does the same job under our control.
- It also shows exactly what the recursion was remembering.

> The call stack is a limited resource. Our checks build a chain of a thousand nodes, which recursion handles, but a very deep degenerate tree can exhaust the default stack and throw StackOverflowError. The limit depends on the JVM settings, so do not rely on any particular depth. Replacing the call stack with an explicit ArrayDeque stack removes that limit and makes the hidden state visible: the stack holds the ancestors that still wait to be visited.

---
# Iterative inorder in pseudocode

```algorithm
function INORDER-ITERATIVE(root):
  S ← empty stack; x ← root
  while x ≠ nil or S is not empty do
    while x ≠ nil do        // walk left, saving the path
      push x on S; x ← x.left
    x ← pop S               // leftmost unvisited node
    visit x
    x ← x.right             // then its right subtree
```

> The inner loop runs down the left spine of the current subtree and pushes every node on the way, because each of them must wait for its left side. When the loop falls off the bottom, the top of the stack is the leftmost unvisited node, so we pop and visit it, then repeat the whole process on its right subtree. The outer loop ends when there is no current subtree and nobody is waiting.

---
# Iterative inorder in Java

```java
static <T> List<T> inorderIterative(BinaryNode<T> root, List<String> log) {
    List<T> out = new ArrayList<>();
    Deque<BinaryNode<T>> stack = new ArrayDeque<>();
    BinaryNode<T> x = root;
    while (x != null || !stack.isEmpty()) {
        while (x != null) {                   // walk left, saving the path
            stack.push(x);
            x = x.left;
        }
        x = stack.pop();                      // leftmost unvisited node
        out.add(x.value);
        x = x.right;                          // then its right subtree
    }
    return out;
}
```

> ArrayDeque serves as the stack this time: push and pop work at the same end. The Java documentation recommends Deque implementations over the older Stack class. The tested version has one more line after the visit, used only for tracing: it records the stack right after each visit, listed from bottom to top, which is what the next slide shows.

---
# Iterative inorder, step by step

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
focus N50 N30 N20
--- Push 50, 30, 20. Pop and visit 20. Stack: 50 30
focus N50 N30
--- 20 has no right child. Pop and visit 30. Stack: 50
focus N50 N40 N35
--- Right to 40: push 40, 35. Pop and visit 35. Stack: 50 40
focus N50 N40
--- Pop and visit 40, then go right to 45. Stack: 50
focus N50 N45
--- Push 45, pop and visit it. Stack: 50
focus N50
--- Nothing to the right: pop and visit 50. Stack empty
focus N70
--- Right to 70: push, pop, visit. Stack empty
focus N80
--- Push, pop and visit 80. Output: 20 30 35 40 45 50 70 80
```

> Highlighted nodes are the ones on the stack plus the node just visited. The stack reaches its largest size, three, at the start, holding fifty, thirty and twenty, the whole left spine. From then on it holds only ancestors still waiting for their left side to finish. Each node is pushed once and popped once, and the output matches the recursive inorder exactly.

---
# Why the stack version works

- The stack holds exactly the ancestors whose left subtree is not finished.
- They are the ancestors the recursion has not yet visited; the recursion also keeps frames for visited ancestors such as 30, so the explicit stack is never larger.
- Each node is pushed once and popped once: $\Theta(n)$ time.
- The stack never holds more than one root-to-node path: $O(h)$ space.

> The recursive inorder, when it is deep inside a left subtree, has a pending call for each ancestor that is waiting to be visited once that left subtree finishes. The explicit stack holds exactly those ancestors, in the same order. The recursion keeps more: while it works inside forty's subtree it still has a frame for thirty, already visited and waiting only for its right call to return, and the explicit stack has dropped thirty. Since the stack only ever holds nodes along one path from the root, its size is at most h plus one. The version we traced never needed more than three.

---
@type section
# Applications

---
# Expression trees

```diagram
@dir TB
@reveal all
M((*)) -- P((+))
M -- S((−))
P -- A((3))
P -- B((4))
S -- C((5))
S -- D((2))
```

- The tree for `(3 + 4) * (5 - 2)`: operands at leaves, operators inside.

> An expression tree stores a formula without any parentheses. Each operator node applies to the values of its two subtrees, so the multiplication at the root multiplies whatever the plus subtree and the minus subtree produce. The structure encodes precedence and grouping. Parsing text into such a tree is a topic for a compilers course; today we assume the tree is given and look at what the traversals do with it. Our code also assumes the tree is nonempty and every operator node has exactly two children; evaluate and infix throw an IllegalArgumentException when that precondition fails.

---
# Three notations, three traversals

| Traversal | Output on the expression tree | Name |
|---|---|---|
| Preorder | `* + 3 4 - 5 2` | Prefix (Polish) notation |
| Inorder, parenthesized | `((3 + 4) * (5 - 2))` | Infix notation |
| Postorder | `3 4 + 5 2 - *` | Postfix (reverse Polish) notation |

- Prefix and postfix need no parentheses when every operator has two operands.

> Run the three depth-first traversals on the expression tree and you get the three standard ways of writing a formula. Prefix puts each operator before its operands, postfix puts it after, and infix puts it between. Prefix and postfix are unambiguous without any parentheses, as long as each operator takes a fixed number of operands. Infix is the one humans prefer, and it is the one that needs help, as the next slide shows.

---
# Infix needs parentheses

```java
static String infix(BinaryNode<String> x) {
    if (isLeaf(x)) return x.value;
    return "(" + infix(x.left) + " " + x.value + " " + infix(x.right) + ")";
}
```

- A bare inorder gives `3 + 4 * 5 - 2`, which reads as a different formula.
- Wrapping every operator's subtree in parentheses restores the grouping.

> A plain inorder walk outputs three plus four times five minus two, and the usual precedence rules read that as three plus twenty minus two. By coincidence that is also twenty-one, but it is a different formula: change the last two to a six, and the tree gives minus seven while the bare string gives seventeen. Our infix method puts parentheses around every operator's subtree, which is safe though sometimes more than needed. Dropping only the unnecessary parentheses requires knowing operator precedence and associativity.

---
# Evaluate with postorder

```java
static int evaluate(BinaryNode<String> x) {
    if (isLeaf(x)) return Integer.parseInt(x.value);   // an operand
    int a = evaluate(x.left);                 // left operand first,
    int b = evaluate(x.right);                // then the right one,
    return switch (x.value) {                 // then the operator
        case "+" -> a + b;
        case "-" -> a - b;
        case "*" -> a * b;
        case "/" -> a / b;
        default -> throw new IllegalArgumentException(
                "unknown operator " + x.value);
    };
}
```

> Evaluation is postorder because an operator can be applied only after both operands are known. The two recursive calls compute the operands, and the switch applies the operator last. The order of a and b matters for subtraction and division: the left subtree is the first operand. Division here is Java integer division. An unknown operator raises an exception rather than returning a wrong number, and isLeaf enforces the precondition: it rejects an empty tree or an operator with only one child.

---
# Evaluation, step by step

```diagram
@dir TB
@reveal manual
M((*)) -- P((+))
M -- S((−))
P -- A((3))
P -- B((4))
S -- C((5))
S -- D((2))
focus P A B
--- Postorder reaches 3, 4, then +: 3 + 4 = 7.
focus S C D
--- Then 5, 2, then −: 5 − 2 = 3.
focus M P S
--- Finally the root: 7 * 3 = 21.
```

> The evaluation visits nodes in postorder: three, four, plus, five, two, minus, times. Each operator combines the two values just computed below it. Our code returns twenty-one. The postfix string lists exactly this sequence, which is why a stack machine can evaluate postfix text: push each number, and on each operator pop two values and push the result. That is the stack algorithm from lecture seven.

---
# Copy with preorder

```java
static <T> BinaryNode<T> copy(BinaryNode<T> x) {
    if (x == null) return null;
    BinaryNode<T> c = new BinaryNode<>(x.value);  // parent first,
    c.left = copy(x.left);                    // then its children
    c.right = copy(x.right);
    return c;
}
```

- A new node is created before its subtrees are copied into it.

> Copying creates each new node when the traversal first arrives at the original, which is preorder, and then fills in its children with recursive copies. The result shares no nodes with the original, a deep copy: our checks change a value in the copy and confirm the original is untouched. A shallow copy, which only copies the root reference, would make both names point to the same nodes.

---
# Size and height with postorder

```java
static <T> int size(BinaryNode<T> x) {
    if (x == null) return 0;
    return size(x.left) + size(x.right) + 1;
}

static <T> int height(BinaryNode<T> x) {
    if (x == null) return -1;
    return 1 + Math.max(height(x.left), height(x.right));
}
```

- Running tree: size 8, height 3 (edges; the empty tree is $-1$).
- Freeing nodes by hand, as in C, is postorder too: children before the parent.

> Both methods need their children's answers before they can compute their own, so both are postorder even though no explicit visit line appears. The size of a tree is the two subtree sizes plus one for the root. The height is one more than the taller child, with the empty tree at minus one so that a leaf gets height zero, the edge-counting convention from last lecture. Freeing a tree follows the same pattern. In C, or in C++ without smart pointers, a program must release every node's memory itself, and if it frees a node before its children it can no longer follow the links to them, so they leak. Java needs none of this: once no reference leads to the nodes, the garbage collector may reclaim them, so root equals null suffices.

---
# Which traversal for which job?

| Job | Traversal | Reason |
|---|---|---|
| Sorted keys of a BST | Inorder | Left keys, node, right keys |
| Copy a tree; prefix output | Preorder | Parent before children |
| Evaluate; size; height; free | Postorder | Children's results first |
| Print by levels; shortest depth to a leaf | Level order | One level at a time |

> The pattern behind the table: ask when the node's own work can happen relative to its children. If the node must exist or be announced first, use preorder. If it depends on its children, use postorder. If it belongs between its left and right sides, as in sorted output, use inorder. If the job is about distance from the root, use level order: for example the first leaf level order reaches is a leaf of minimum depth.

---
@type section
# Rebuilding a tree

---
# Preorder plus inorder fix the tree

- Assume distinct keys, and both sequences from the same tree.
- Preorder's first key is the root.
- In inorder, keys before the root form the left subtree; keys after it form the right.
- The left subtree's size tells where its block ends in preorder.
- Recurse on both halves.

> Each traversal alone loses information, but together they pin the tree down. Preorder tells you which key is the root. Inorder tells you which keys go left and which go right of it. Knowing how many keys go left, you can cut the preorder sequence into the left block and the right block, because preorder lists the whole left subtree before the right one. Distinct keys matter: with repeated keys you could not tell which copy is the root.

---
# Rebuilding the running tree

| Subproblem | Root: first in preorder | Inorder to its left | Inorder to its right |
|---|---|---|---|
| Whole tree | 50 | `20 30 35 40 45` | `70 80` |
| Left of 50 | 30 | `20` | `35 40 45` |
| Right of 30 | 40 | `35` | `45` |
| Right of 50 | 70 | (empty) | `80` |

- Preorder `50 30 20 40 35 45 70 80`, inorder `20 30 35 40 45 50 70 80`.

> Start with the whole sequences. Fifty comes first in preorder, so it is the root, and in inorder it has five keys to its left and two to its right. The next five preorder keys, thirty through forty-five, are the left subtree, whose root is thirty, and so on. The table lists the splits in the order our code made them, leaving out the single-key leaves. Seventy has nothing to its left in inorder, which is how we learn that eighty is its right child.

---
# Rebuild in Java

```java
private static <T> BinaryNode<T> build(List<T> pre, int p,
        Map<T, Integer> where, int lo, int hi) {
    if (lo > hi) return null;         // empty range: no subtree
    T rootValue = pre.get(p);         // preorder lists the root first
    int m = where.get(rootValue);     // inorder splits at the root
    if (m < lo || m > hi)
        throw new IllegalArgumentException("sequences are not from one tree");
    int leftSize = m - lo;
    BinaryNode<T> x = new BinaryNode<>(rootValue);
    x.left = build(pre, p + 1, where, lo, m - 1);
    x.right = build(pre, p + 1 + leftSize, where, m + 1, hi);
    return x;
}
```

- `where` maps each key to its inorder index, built once in a `HashMap`.

> The method builds the subtree whose inorder occupies positions lo to hi and whose preorder starts at p. The map lookup finds the root's inorder position in constant expected time, so the whole rebuild takes expected linear time, assuming the hash map behaves as lecture twelve describes; searching the inorder list instead would cost quadratic time in the worst case. The public method first checks that the lengths match, that each list has distinct keys, and that both hold the same keys. The range check inside the helper catches the remaining bad input: if the root falls outside the current inorder range, the two sequences cannot come from one tree, and the method says so with an IllegalArgumentException instead of failing with an index error deeper in the recursion.

---
# Preorder plus postorder is not enough

```diagram
@dir TB
@reveal all
A((1)) -- B((2))
A -- XA[nil].gray
C((1)) -- XC[nil].gray
C -- D((2))
```

- Both trees: preorder `1 2`, postorder `2 1`.
- Inorder tells them apart: `2 1` versus `1 2`.
- For a general binary tree, preorder and postorder cannot say whether a lone child is left or right.

> Here are two different binary trees. On the left, two is the left child of one; on the right, it is the right child. Preorder gives one then two for both, and postorder gives two then one for both, so no algorithm can recover the tree from those two sequences. Our checks confirm the four sequences. The ambiguity comes only from nodes with exactly one child; for full binary trees with distinct keys, where every node has zero or two children, preorder and postorder together do determine the tree.

---
# Quiz: rebuild

```quiz
A tree with distinct keys has preorder 10 5 3 7 12 and inorder 3 5 7 10 12. What is its postorder?
- [ ] 3 5 7 12 10
- [x] 3 7 5 12 10
- [ ] 12 7 3 5 10
- [ ] 3 7 12 5 10
```

> The root is ten, the first key in preorder. In inorder, three, five and seven lie to its left and twelve to its right. The left block of preorder is five, three, seven, so five is the root of the left subtree, with three on its left and seven on its right. Postorder lists the left subtree in postorder, three, seven, five, then the right subtree, twelve, then the root ten. Our rebuild code produces this tree and this postorder.

---
@type section
# Costs

---
# Time: every traversal is $\Theta(n)$

- Each node is visited exactly once: $n$ visits.
- A tree with $n$ nodes has $n + 1$ null child links; each is checked once.
- Constant work per node and per null link.
- So each traversal takes $\Theta(n)$ time, whatever the shape.
- This is a lower bound too: any traversal must touch all $n$ nodes.

> Count the work in the recursive versions: one call per node and one call per null link. A binary tree with n nodes has two n child fields, of which n minus one hold edges, so n plus one are null. That makes two n plus one calls, each doing constant work besides its recursive calls. For the iterative versions, each node enters and leaves the stack or queue once. Both counts are linear, and no traversal can do better than visiting every node, so Theta of n is tight.

---
# Space: depth for DFS, width for BFS

| Shape, $n$ nodes | DFS stack: $O(h)$ | BFS queue: $O(w)$ |
|---|---|---|
| Running tree, $n = 8$ | $h = 3$ | $w = 3$ |
| Perfect tree | $h = \lfloor \log_2 n \rfloor$ | $w = (n+1)/2$ |
| Chain | $h = n - 1$ | $w = 1$ |

- $w$ is the maximum number of nodes on one level.

> Depth-first traversals keep one path's worth of pending nodes, whether in recursion frames or an explicit stack, so their extra space is proportional to the height. Level order keeps part of at most two adjacent levels in the queue, which is proportional to the maximum level width. Neither always wins. A perfect tree is short but wide: half its nodes are leaves on the bottom level, so the queue grows to about n over two. A chain is the opposite: one node per level but a deep stack.

---
# Common mistakes and edge cases

- Missing the `null` check: the first leaf's child throws `NullPointerException`.
- Visiting in the wrong place: one line moved changes the whole order.
- Using a stack where level order needs a queue: that gives a depth-first order.
- Rebuilding with repeated keys: the root's inorder position is ambiguous.
- Empty tree: return an empty result, or reject it with a clear exception.

> Most traversal bugs are small. Forgetting the base case crashes at the first missing child. Moving the visit line one position silently changes preorder into inorder. Swapping the queue for a stack in the level-order code does not crash either, but visits the tree depth-first instead. Rebuilding from sequences with duplicate keys can produce a wrong tree, so our code rejects them. Our checks run every traversal, every tree operation, the rebuild and every expression method on the empty tree and on a single node; evaluate and infix reject the empty tree with an IllegalArgumentException by design.

---
# From trees to graphs

- A graph (L15) is nodes and edges without the one-parent rule.
- Depth-first search generalizes preorder: explore one branch fully, then back up.
- Breadth-first search generalizes level order: a queue, distance by distance.
- Graphs can have cycles, so the search must mark nodes it has already seen.
- Preview: [BFS](../../../visualizations/algorithms/bfs.html), [DFS](../../../visualizations/algorithms/dfs.html), [BFS and DFS compared](../../../visualizations/algorithms/bfs_dfs_comparison.html).

> A tree is a special graph: connected, with no cycles, and a chosen root. General graphs can have several paths between two nodes and can loop back on themselves, so a traversal that only follows edges could visit a node twice or run forever. The fix is a visited mark on each node. With that one addition, the depth-first and breadth-first patterns from today become the graph searches of lecture fifteen. The linked visualizations run them on graphs.

---
@type section
# Wrap-up

---
# Summary

- Preorder visits a node before its subtrees, inorder between, postorder after.
- Inorder of a BST gives its keys in sorted order.
- Level order uses a queue; iterative inorder uses a stack of pending ancestors.
- Pick the order by when the node's work can happen relative to its children.
- All take $\Theta(n)$ time; DFS uses $O(h)$ space and BFS $O(w)$.
- Preorder plus inorder (distinct keys) determine a tree; preorder plus postorder may not.

> The three depth-first orders come from one template with the visit in three places, and the Euler tour shows all three at once. The choice among them follows from the job. Level order is the one traversal that needs a queue rather than recursion. Time is always linear; space depends on the shape, through height or width. Next lecture we return to search trees and learn how AVL trees keep the height logarithmic.

---
# Check yourself

- Write all four traversal orders for the L08 tree after deleting 30.
- Which traversal would you use to print a directory tree with each folder above its contents, and why?
- Give a tree with 7 nodes where the level-order queue is larger than the recursion depth, and one where it is smaller.

> For the first question, recall that the tree after deleting thirty has thirty-five in its place with forty keeping only its right child forty-five; draw it before traversing. For the second, think about which order announces a node before anything below it. For the third, compare a perfect tree with a chain, and count both the stack and the queue as the traversals run.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 12, Binary search trees (inorder tree walk).
- CLRS, Chapter 10, Elementary data structures (stacks, queues, rooted trees).
- CLRS, Chapter 20, Elementary graph algorithms (BFS and DFS on graphs).
- Java SE API documentation: `java.util.ArrayDeque`, `java.util.Deque`.
- Code on these slides: package `l09`, tested by `l09.Checks`.

> The traversal definitions and the inorder sorting argument follow CLRS Chapter 12; stacks, queues and the representation of binary trees follow Chapter 10; the graph searches previewed at the end are in Chapter 20. Library facts about ArrayDeque come from the Java SE documentation. Every Java excerpt is copied from the tested package, and every traced order was produced by running that code on the running tree.
