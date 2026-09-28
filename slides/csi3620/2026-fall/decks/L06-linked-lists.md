@title Lecture 6: Linked lists
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Linked lists
## Nodes, references and careful link surgery

---
# Where we are

- L02 gave us Big-O, Big-Omega and Big-Theta for counting work.
- L03 searched arrays: linear scan, then binary search on sorted data.
- L04 and L05 sorted arrays by moving records between positions.
- Every structure so far was one contiguous array.
- Today: a sequence built from separate objects joined by references.

> So far every algorithm lived inside one array: we searched it, we sorted it, we shifted records left and right inside it. Arrays are wonderful at one thing, reaching position i in one step, and awkward at another, making room in the middle. Today we build a sequence out of small separate objects that point to each other. The cost profile flips, and seeing exactly where it flips is the point of the lecture.

---
# By the end of today you can

- Draw a singly linked list and trace each operation on paper.
- Write `addFirst`, `addLast`, `get`, `insertAfter` and `remove` in Java.
- Reverse a list in place with three references.
- Explain why a doubly linked list removes a known node in $O(1)$.
- Use a sentinel node to delete special cases from code.
- Pick array, `ArrayList` or linked list from a cost table.

> Each outcome is something you can do with a pencil or a keyboard, not just recognise. The drawing skill comes first on purpose: almost every linked-list bug is visible in a picture before it is visible in code. If you can trace an operation box by box, writing the Java becomes copying your picture into assignments.

---
# The problem: room in the middle

```diagram
@dir TB
@reveal manual
A[4, 12, 7, 19, _ | insert 25 at index 2]
focus A
--- One spare slot at the end. We want 25 at index 2.
A -> B[4, 12, 7, 7, 19 | shift 19, then 7, right]
focus A B
--- Every element from index 2 onward moves one slot right.
B -> C[4, 12, 25, 7, 19 | write 25]
focus B C
--- Only then does the new value fit. n - i elements moved.
```

> Inserting at index i in an array of n elements shifts n minus i elements, one at a time, starting from the right so nothing is overwritten. At the front that is all n of them, so front insertion is Theta of n. And if there is no spare slot at all, a plain Java array cannot grow: its length is fixed when you create it.

---
# Dynamic arrays help, but only at the end

- A **dynamic array** keeps a bigger array and a separate size count.
- When full, it allocates a larger array and copies everything over.
- Growing by a constant factor makes append **amortized** $O(1)$.
- `ArrayList` docs: adding $n$ elements takes $O(n)$ time in total.
- Insert at the front still shifts everything: $\Theta(n)$.

> ArrayList is Java's dynamic array. It hides the fixed capacity by copying into a larger array when it fills up; the API promises only that n appends take linear time in total; OpenJDK achieves this by growing the array by half its length, so the expensive copies are rare. We will prove that doubling argument next lecture. What ArrayList cannot hide is shifting: add at index zero moves every element, every time.

---
# The idea: link separate nodes

```diagram
@dir LR
H[head] -> N4[4] -> N12[12] -> N7[7] -> N19[19] -> X[null]
T[tail] -> N19
```

- A **node** stores one value and a reference to the next node.
- The list object remembers the first node (`head`) and the last (`tail`).
- `null` after the last node marks the end.

> Instead of one block of memory, each value lives in its own small object called a node, and each node remembers where the next one is. The list itself only keeps two references, to the first and last nodes, plus a count. Nodes can be anywhere in memory; the arrows, not the addresses, define the order. That freedom is what makes insertion cheap and indexing expensive.

---
# One running example

- Start empty. `addLast(12)`, `addLast(7)`, `addFirst(4)`.
- `addLast(19)`, then `insertAfter(12, 25)`: `[4, 12, 25, 7, 19]`.
- `removeFirst()`, then `remove(7)`: `[12, 25, 19]`.
- `reverse()`: `[19, 25, 12]`.
- Every singly linked trace today continues this one list.

> Keep this sequence of calls on the side of your page. Each picture in the lecture is one step of it, and every list shown is checked by the lecture's test program after each call; the in-between pointer positions follow from the code. By the end the list has grown to five values, shrunk to three, and been turned around.

---
@type section
# Nodes and references

---
# A node in Java

```java
private static class Node<T> {
    T value;
    Node<T> next;

    Node(T value, Node<T> next) {
        this.value = value;
        this.next = next;
    }
}
```

- Nested inside `SinglyLinkedList<T>`; `static` because it needs no outer object.
- `Node<T>` is generic: the same code holds `Integer`, `String`, anything.

> Look at the field next: its type is Node of T, the class being defined. A node refers to another node of the same kind, which is why these are called self-referential structures. The class is private because callers should never touch nodes directly, and static because a node does not need a hidden reference to the list that contains it.

---
# A variable holds a reference, not a node

```diagram
@dir LR
@reveal manual
a[a] -> N12[node 12]
focus a N12
--- Node<Integer> a = new Node<>(12, null);
b[b] -> N12
focus a b N12
--- Node<Integer> b = a; two names, one node.
```

- Assigning a reference copies the arrow, never the node.
- `null` means "refers to no node"; reading `.next` through a `null` reference throws `NullPointerException`.
- Changing `b.next` is visible through `a` too.

> This is the single most important mental model for the whole lecture. A Java variable of a class type holds an arrow to an object. Copying the variable copies the arrow, so a and b now name the same node, and a change made through one is seen through the other. Link surgery means redirecting arrows; nodes themselves never move.

---
# The list object and its invariants

```java
private Node<T> head; // first node, or null when the list is empty
private Node<T> tail; // last node, or null when the list is empty
private int size;     // number of nodes reachable from head
```

- Empty list: `head == null`, `tail == null`, `size == 0`.
- Nonempty: `tail` is the last node reached from `head`, and `tail.next == null`.
- `size` equals the number of nodes you reach by following `next` from `head`.
- Every public method must leave all three facts true.

> An invariant is a fact that holds before and after every public operation. These three are the contract between the fields. Most bugs we will see today break exactly one of them: a stale tail, a size off by one, or a head that points into nothing. When you write a method, check the invariant for the empty list, a one-node list, and a longer list.

---
@type section
# Adding and finding

---
# Add at the front

```algorithm
function ADD-FIRST(L, x):
  L.head ← new node(x, next = L.head)   // new node points at old first
  if L.tail = null then L.tail ← L.head  // list was empty
  L.size ← L.size + 1
```

```java
public void addFirst(T value) {
    head = new Node<>(value, head);
    if (tail == null) {
        tail = head;
    }
    size++;
}
```

> The new node is built already pointing at the old first node, then head moves to it. Order matters: we read the old head before we overwrite it, and Java evaluates the constructor argument first, so one line does both safely. The tail check covers the empty list, where the new node is also the last one. No loop, so this is Theta of one.

---
# Add at the back with a tail

```java
public void addLast(T value) {
    Node<T> node = new Node<>(value, null);
    if (tail == null) {
        head = node;
    } else {
        tail.next = node;
    }
    tail = node;
    size++;
}
```

- With `tail`, appending is $\Theta(1)$; without it, a walk of $\Theta(n)$.
- The empty case sets `head` too: the new node is both first and last.

> The tail reference is what makes this constant time: we jump straight to the last node and hang the new one after it. Without a tail we would walk the whole list to find the end. The branch is the classic special case: in an empty list there is no last node to link from, so the new node becomes the head instead.

---
# Building the list, step by step

```diagram
@dir TB
@reveal manual
E[empty | head = tail = null, size 0]
focus E
--- Start with no nodes at all.
E -> A[12 → null | head = tail = 12, size 1] : addLast(12)
focus E A
--- Empty case: head and tail both point to the new node.
A -> B[12 → 7 → null | tail = 7, size 2] : addLast(7)
focus A B
--- Old tail 12 links to 7; tail moves.
B -> C[4 → 12 → 7 → null | head = 4, size 3] : addFirst(4)
focus B C
--- 4 points at old head 12; head moves.
C -> D[4 → 12 → 7 → 19 → null | tail = 19, size 4] : addLast(19)
focus C D
--- Tail makes this one step, not a walk.
```

> Read each box as the chain you would reach from head, followed by the fields. Watch the tail after addFirst: it stays at seven, because adding at the front never changes the last node. Every list here is checked by the lecture's test program after each call.

---
# Reaching index i

```java
public T get(int index) {
    Objects.checkIndex(index, size);
    Node<T> current = head;
    for (int i = 0; i < index; i++) {
        current = current.next;
    }
    return current.value;
}
```

- `get(2)` on `[4, 12, 25, 7, 19]` follows two links and returns `25`.
- Cost $\Theta(i+1)$: worst case $\Theta(n)$ for the last index.
- `Objects.checkIndex` throws `IndexOutOfBoundsException` for bad `i`.

> There is no arithmetic shortcut to position i: the only way to reach a node is to follow arrows from the head. So get of i costs i link steps plus constant work, linear in the worst case, compared with one step in an array. Check the index first: without it, an index past the end crashes with a less helpful NullPointerException, and a negative index silently returns the first value.

---
# Searching by value

```java
public int indexOf(T value) {
    int i = 0;
    for (Node<T> current = head; current != null; current = current.next) {
        if (Objects.equals(current.value, value)) {
            return i;
        }
        i++;
    }
    return -1;
}
```

- This `for` header is the standard list walk: start, stop at `null`, advance.
- `Objects.equals` handles `null` values without crashing.
- `indexOf(7)` on `[4, 12, 25, 7, 19]` returns `3`; `indexOf(99)` returns `-1`.

> This is linear search from lecture three, walking links instead of indices: Theta of one in the best case, Theta of n when the value is missing. Memorise the loop header; it appears in nearly every list method. Objects dot equals is used instead of value dot equals because our list allows null elements, and calling a method on null would throw.

---
# Insert after a node

```algorithm
function INSERT-AFTER(L, target, x):
  cur ← first node with value target   // walk from L.head
  if cur = null then return false
  node ← new node(x)
  node.next ← cur.next                 // 1. new node grabs the rest
  cur.next ← node                      // 2. then cur points to it
  if cur = L.tail then L.tail ← node
  L.size ← L.size + 1; return true
```

- Once `cur` is known, the splice itself is $\Theta(1)$: two link writes.
- The search to find `cur` is what costs $O(n)$.

> Separate the two costs in your head. Finding the target is a linear search. The splice, once you stand on the right node, is two assignments no matter how long the list is. That is the advantage over arrays: no shifting. The order of the two writes is not a style choice, as the next slides show.

---
# Insert after, in Java

```java
public boolean insertAfter(T target, T value) {
    Node<T> current = head;
    while (current != null && !Objects.equals(current.value, target)) {
        current = current.next;
    }
    if (current == null) {
        return false;
    }
    current.next = new Node<>(value, current.next);
    if (current == tail) {
        tail = current.next;
    }
    size++;
    return true;
}
```

> The while condition tests null first, so the loop stops safely at the end of the list; Java's and operator short-circuits and never reads a missing node. The splice line builds the new node with next set to current dot next before overwriting current dot next, which is steps one and two of the pseudocode in one line. If we inserted after the tail, the new node becomes the tail.

---
# Insert 25 after 12, step by step

```diagram
@dir TB
@reveal manual
S0[4 → 12 → 7 → 19 → null | current walks 4, then 12]
focus S0
--- The loop stops at the node holding 12.
S0 -> S1[new node 25, its next = 7 | list itself unchanged]
focus S0 S1
--- Step 1: the new node points at 12's old successor.
S1 -> S2[4 → 12 → 25 → 7 → 19 → null | size 5]
focus S1 S2
--- Step 2: 12.next now points at 25. Tail stays 19.
```

> Between step one and step two the list reached from head is unchanged, and the new node already holds a reference to the rest of the list. That is why this order is safe. After step two, reading from head gives four, twelve, twenty-five, seven, nineteen, exactly what the program printed.

---
# Which order is safe?

```quiz
To insert `node` after `cur`, a student writes `cur.next = node;` first and then `node.next = cur.next;`. What happens?
- [ ] The insertion works; the order of the two lines does not matter
- [x] `node.next` ends up pointing at `node` itself, and the rest of the list is lost
- [ ] A `NullPointerException` is thrown on the second line
- [ ] The new node is inserted before `cur` instead of after
```

> After the first line, cur dot next already refers to the new node, so the second line copies that arrow into node dot next: the node points to itself. Nothing refers to seven and nineteen any more, so they are lost, and a later walk loops forever on the new node. Always let the new node grab the rest of the list before anything is redirected.

---
@type section
# Removing nodes

---
# Remove the first node

```java
public T removeFirst() {
    if (head == null) {
        throw new NoSuchElementException("list is empty");
    }
    T value = head.value;
    head = head.next;
    if (head == null) {
        tail = null;
    }
    size--;
    return value;
}
```

- Save the value, then move `head` one link forward: $\Theta(1)$.
- Removing the only node must also clear `tail`.

> Empty list first: there is nothing to remove, and we signal that with an exception, which is what java dot util dot LinkedList does too. Then read the value before moving head, or it is gone. The old first node is now unreachable, and Java's garbage collector reclaims it. The last check is easy to forget: removing the only node must leave tail null, not pointing at a node that is no longer in the list.

---
# Removing by value needs the predecessor

```diagram
@dir LR
P[25] -> V[7] -> Q[19]
P ..> Q : after: 25.next = 19
```

- A singly linked node knows its successor, not its predecessor.
- To unlink `7` we must change `25.next`, so we stop one node early.
- Walk with `prev`, testing `prev.next.value`.
- The head has no predecessor: a separate case.

> To cut a node out, you redirect the arrow that points into it. In a singly linked list that arrow belongs to the previous node, and nodes do not know who points at them. So the loop keeps a reference to the node before the candidate and looks one step ahead. The first node is different, because the arrow into it is the head field itself.

---
# Remove by value, in Java

```java
if (Objects.equals(head.value, value)) {
    removeFirst();
    return true;
}
Node<T> prev = head;
while (prev.next != null && !Objects.equals(prev.next.value, value)) {
    prev = prev.next;
}
if (prev.next == null) {
    return false;
}
if (prev.next == tail) {
    tail = prev;
}
prev.next = prev.next.next;
```

> This is the body of remove, after an empty-list check that returns false. Three special cases are visible: the value is at the head, the value is missing, and the value is at the tail. Only the first occurrence is removed when there are duplicates. After the splice the method decrements size and returns true. The search is O of n; the unlink itself is two constant-time steps.

---
# Two removals, step by step

```diagram
@dir TB
@reveal manual
S0[4 → 12 → 25 → 7 → 19 → null | size 5]
focus S0
--- Start from the list after insertAfter.
S0 -> S1[12 → 25 → 7 → 19 → null | head = 12, size 4] : removeFirst() returns 4
focus S0 S1
--- Head moves one link. Node 4 is unreachable.
S1 -> S2[prev stops at 25 | prev.next holds 7]
focus S1 S2
--- remove(7): look one node ahead from prev.
S2 -> S3[12 → 25 → 19 → null | tail = 19, size 3]
focus S2 S3
--- 25.next now points at 19. Node 7 is unreachable.
```

> The walk for remove seven starts with prev at twelve, sees twenty-five ahead, moves on, and stops with prev at twenty-five because the node ahead holds seven. Seven was not the tail, so tail stays nineteen. Three values remain, and the program printed exactly this list.

---
# Common bugs

- **Losing the rest:** overwriting a `next` before saving what it pointed to.
- **Missing `null` checks:** reading `current.next` when `current` is `null`.
- **Stale `tail`:** removing or appending at the end without moving `tail`.
- **Wrong `size`:** a branch that returns before `size++` or `size--`.
- **Head case:** forgetting that the first node has no predecessor.

> Each bug on this list breaks one of the three invariants from earlier. A good habit is to trace every method on four lists before trusting it: empty, one node, the target at the front, and the target at the back. The stale tail is the sneakiest because the list prints correctly from head; the bug appears only at the next addLast.

---
# Edge cases to test

| Case | What must hold afterwards |
|---|---|
| Empty list | `removeFirst` throws; `remove` returns `false` |
| One node, removed | `head`, `tail` both `null`; `size` 0 |
| Remove the tail | `tail` moves to the predecessor |
| Duplicates | only the first match is removed |
| `null` element | found and removed with `Objects.equals` |
| Missing value | list and `size` unchanged |

> This table is the checklist the lecture's test program runs. It is worth writing tests like these before the method, because each row names a situation that the typical middle-of-the-list picture never shows. The null row is a design choice: our list accepts null values, as java dot util dot LinkedList does, so searching must not call a method on a stored value.

---
@type section
# Reversing in place

---
# Reverse with three references

```algorithm
function REVERSE(L):
  L.tail ← L.head                    // old first becomes last
  prev ← null; cur ← L.head
  while cur ≠ null do
    next ← cur.next                  // save the rest of the list
    cur.next ← prev                  // flip one link
    prev ← cur; cur ← next           // advance both
  L.head ← prev
```

- **In place:** no new nodes, only links flipped.
- `next` exists so that flipping `cur.next` does not lose the rest.

> The idea is to walk the list once and turn every arrow around as you pass it. Flipping current dot next destroys our only way forward, so we save it in next first. Prev is the head of the part already reversed, current is the head of the part not yet touched. When current falls off the end, prev is the new head.

---
# Reverse, in Java

```java
public void reverse() {
    tail = head;
    Node<T> prev = null;
    Node<T> current = head;
    while (current != null) {
        Node<T> next = current.next; // save the rest of the list
        current.next = prev;         // flip one link
        prev = current;              // advance both references
        current = next;
    }
    head = prev;
}
```

> Line for line this is the pseudocode. Check it on the edge cases: an empty list skips the loop and leaves head and tail null; a one-node list flips its single link from null to null. The tail line runs first because the old head becomes the new tail, and after the loop no variable still refers to it directly.

---
# Reverse [12, 25, 19], step by step

```diagram
@dir TB
@reveal manual
R0[reversed: null | rest: 12 → 25 → 19 → null]
focus R0
--- Start: prev = null, current = 12.
R0 -> R1[reversed: 12 → null | rest: 25 → 19 → null] : 12.next = null
focus R0 R1
--- Iteration 1: save 25, flip 12, advance.
R1 -> R2[reversed: 25 → 12 → null | rest: 19 → null] : 25.next = 12
focus R1 R2
--- Iteration 2: save 19, flip 25, advance.
R2 -> R3[reversed: 19 → 25 → 12 → null | rest: null] : 19.next = 25
focus R2 R3
--- Iteration 3: current is null. head = 19, tail = 12.
```

> Each box shows what you reach from prev on the left and from current on the right. Every iteration moves exactly one node from the right chain to the front of the left chain. After three iterations the right chain is empty and the program prints nineteen, twenty-five, twelve.

---
# Why reversal works

- Invariant: `prev` heads the processed nodes, already reversed.
- `current` heads the unprocessed nodes, still in original order.
- Each iteration moves one node across; no node is lost or copied.
- The loop ends when `current == null`: every node processed.
- Time $\Theta(n)$; extra space $\Theta(1)$: three references.

> Before the loop, the reversed part is empty and the rest is the whole list, so the invariant holds. One iteration takes the first unprocessed node and puts it in front of the reversed part, which keeps both halves correct. When nothing remains unprocessed, the reversed part is the whole list. Constant extra space is the point: a version that copies into a new list would use linear space.

---
# Trace it yourself

```quiz
During `reverse()` on `[12, 25, 19]`, what does node 25's `next` refer to right after the second iteration?
- [ ] 19
- [x] 12
- [ ] `null`
- [ ] 25 itself
```

> In the second iteration current is twenty-five and prev is twelve, so the flip line sets twenty-five dot next to twelve. Nineteen was saved in next just before, so it is not lost; it becomes current for the third iteration. Null was twelve's new next from the first iteration, not twenty-five's.

---
@type section
# Doubly linked lists and sentinels

---
# Two links per node

```diagram
@dir LR
@reveal all
A[12] <-> B[25] <-> C[19]
```

- A **doubly linked** node has `next` and `prev` references.
- Given a node, both neighbours are one step away.
- Removing a known node becomes $O(1)$: no predecessor search.
- Cost: one more reference per node, and more links to keep consistent.

> In the singly linked version, removing a node you already hold still meant walking from the head to find its predecessor, which is linear time. With a prev link the predecessor is right there. That matters whenever code keeps references to nodes, for example a cache that moves an entry when it is used. The price is memory and the discipline of updating two arrows per link change.

---
# A sentinel node

```diagram
@dir LR
@reveal all
S[sentinel].gray <-> A[12] <-> B[25] <-> C[19]
C ..> S : next wraps around
S ..> C : prev wraps around
```

- A **sentinel** (dummy) node holds no value and is never removed.
- `sentinel.next` is the first node; `sentinel.prev` is the last.
- Empty list: the sentinel's `next` and `prev` both point to itself.
- While a node is in the list, its `prev` and `next` are never `null`; at the ends they refer to the sentinel.

> CLRS also presents a version with a dummy node called nil. The sentinel closes the list into a circle, so the first node's prev is the sentinel and the last node's next is the sentinel. Because there is always a node on each side, code that links or unlinks never has to ask whether it is at the front, at the back, or in an empty list.

---
# The doubly linked node

```java
public static final class Node<T> {
    private final T value;
    private Node<T> prev;
    private Node<T> next;
    private DoublyLinkedList<T> owner;
    // ...
}

private final Node<T> sentinel = new Node<>(null, null);
```

- `addFirst` and `addLast` return the node as a **handle** for later removal.
- `owner` lets `unlink` reject a node from another list.

> The node class is public so callers can hold handles, but its fields are private, so only the list changes links. The owner field is a safety check of my own choosing, not part of the textbook structure: without it, passing a node from a different list would unlink it from that list while decrementing this list's size, leaving both size counts wrong with no error. The list constructor, not shown, sets the sentinel's prev and next to itself.

---
# Linking with no special cases

```java
private Node<T> linkAfter(Node<T> before, T value) {
    Node<T> node = new Node<>(value, this);
    Node<T> after = before.next;
    node.prev = before;
    node.next = after;
    before.next = node;
    after.prev = node;
    size++;
    return node;
}
```

- `addFirst(x)` is `linkAfter(sentinel, x)`.
- `addLast(x)` is `linkAfter(sentinel.prev, x)`.

> Four link writes, no if statement. The new node points at both neighbours first, then both neighbours point back at it. In an empty list, before and after are both the sentinel, and the same four lines still produce the right circle. Compare this with addFirst and addLast in the singly linked list, which each needed a branch for the empty case.

---
# Unlinking a known node in O(1)

```java
public T unlink(Node<T> node) {
    if (node.owner != this) {
        throw new IllegalArgumentException("node is not in this list");
    }
    node.prev.next = node.next;
    node.next.prev = node.prev;
    node.prev = null;
    node.next = null;
    node.owner = null;
    size--;
    return node.value;
}
```

> The two lines after the check are the whole removal: the neighbour before now skips forward over the node, and the neighbour after skips backward over it. No loop, so the cost is Theta of one. Clearing owner makes a second unlink of the same handle throw IllegalArgumentException instead of silently decrementing size again; clearing prev and next stops the removed node from keeping its old neighbours reachable.

---
# Unlink 25, step by step

```diagram
@dir TB
@reveal manual
U0[sentinel ⇄ 12 ⇄ 25 ⇄ 19 ⇄ sentinel | handle to 25]
focus U0
--- addLast returned a handle to node 25.
U0 -> U1[12.next = 19 | backward still 19 → 25 → 12]
focus U0 U1
--- node.prev.next = node.next
U1 -> U2[sentinel ⇄ 12 ⇄ 19 ⇄ sentinel | size 2]
focus U1 U2
--- node.next.prev = node.prev. Both directions agree.
```

> Between the two lines the list is half updated: reading forward skips twenty-five, but reading backward from nineteen still reaches it. After the second line both directions agree. The test program checks this by printing forward and backward: twelve, nineteen, and nineteen, twelve.

---
# What the sentinel bought

| Situation | Singly, head and tail | Doubly with sentinel |
|---|---|---|
| Insert into empty list | special branch | same four writes |
| Remove the first node | special branch | same two writes |
| Remove the last node | walk, then move `tail` | same two writes |
| Remove a held node | walk to predecessor | $\Theta(1)$ |
| Extra memory | none | one sentinel, one `prev` per node |

> Each special case in the singly linked code exists because a neighbour might be missing. The sentinel guarantees a neighbour on both sides, so the special cases disappear. You pay for it with one extra node per list and one extra reference per node. A sentinel also works with singly linked lists: a dummy head node removes the head case from remove.

---
# Circular lists, briefly

- In a **circular** list the last node's `next` refers back to the first.
- Our sentinel list is circular: walking `next` returns to the sentinel.
- A singly circular list can keep only `tail`: `tail.next` is the head.
- Uses: taking turns in round-robin order, repeating playlists.
- Traversal must stop at the start node, never at `null`.

> A circle has no natural end, so the familiar test current not equal to null would loop forever. Loops over a circular list stop when they get back to where they started, which is what DoublyLinkedList's iterator does: it stops when it gets back to the sentinel. Keeping only a tail reference is a neat trick: one reference gives constant-time access to both the last and the first node.

---
@type section
# Iterators and the Java library

---
# An iterator for our list

```java
public Iterator<T> iterator() {
    return new Iterator<>() {
        private Node<T> current = head;
        // ...
        public boolean hasNext() {
            return current != null;
        }
        public T next() {
            // ...
            T value = current.value;
            current = current.next;
            return value;
        }
    };
}
```

> An iterator remembers a position between calls. Here the position is one node reference, so each call to next is constant time and a full pass is linear. The lines marked with dots are omitted to fit the slide: the real next throws NoSuchElementException when called past the end, and both methods carry Override annotations. Because the list implements Iterable, callers can use the for-each loop.

---
# Walking with an iterator

```java
int total = 0;
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    total += it.next();
}
```

```java
for (int value : list) { // the compiler calls iterator() for us
    total += value;
}
```

- Both loops visit each node once: $\Theta(n)$ in total.
- Never loop `for i` with `get(i)` on a linked list: see the next slides.

> The two loops do exactly the same work; the for-each loop is just compiler shorthand for the explicit one. On our final list they both add nineteen, twenty-five and twelve to get fifty-six, which the test checks. The warning in the last bullet is about a cost trap that has caught many real programs.

---
# java.util.LinkedList

- API docs: "Doubly-linked list implementation of the List and Deque interfaces."
- Supports `addFirst`, `addLast`, `removeFirst`, `removeLast` in $O(1)$.
- `get(i)` walks from whichever end is closer: $\Theta(\min(i+1, n-i))$, $O(n)$.
- It permits `null` elements, like our list.
- Its iterators are fail-fast: changing the list mid-loop is detected, best effort.

> The Java library list is doubly linked, so the end operations are constant time. Indexing is still a walk; starting from the nearer end halves the worst case but keeps it linear. Fail-fast means that if you modify the list other than through the iterator while iterating, the iterator throws ConcurrentModificationException on a best-effort basis; do not rely on it for correctness.

---
# Arrays are often faster anyway

- Array elements sit next to each other in memory.
- Processors load memory in blocks and keep recent blocks in a **cache**.
- Scanning an `int[]` uses every loaded block fully.
- Linked nodes can be scattered, so each hop may wait on memory.
- Each node also carries object overhead and a reference or two.

> Big-O counts steps, not how long each step takes. On real hardware, reading the next array element is usually cheap because it was loaded together with its neighbours, while following a reference can land anywhere. So even when both scans are Theta of n, the array version is usually faster in practice. An ArrayList of Integer stores references to boxed objects, so it gains less, though its reference array is still compact. Measure before assuming; linked lists win when you insert and remove at held positions far more than you scan.

---
@type section
# Wrap-up

---
# Costs: reading and adding

| Operation | Array | `ArrayList` | Singly (head, tail) | Doubly (sentinel) |
|---|---|---|---|---|
| `get(i)` | $\Theta(1)$ | $\Theta(1)$ | $\Theta(i+1)$ | $\Theta(\min(i+1, n-i))$ |
| Add at front | $\Theta(n)$ shift | $\Theta(n)$ | $\Theta(1)$ | $\Theta(1)$ |
| Add at back | $\Theta(1)$ if room | amortized $\Theta(1)$ | $\Theta(1)$ | $\Theta(1)$ |
| Insert after a held node | $\Theta(n)$ shift | $\Theta(n)$ | $\Theta(1)$ | $\Theta(1)$ |
| Search by value | $O(n)$ | $O(n)$ | $O(n)$ | $O(n)$ |

> Rows are worst case unless marked amortized. The doubly linked get assumes a walk from the nearer end, as java dot util dot LinkedList does; from one end only it is Theta of i plus one, like the singly column. A plain array cannot grow, so its add at back needs a spare slot. For arrays, insert after a position shifts the elements behind it, which is Theta of n at the front. Search by value is linear for all four when the data is unsorted; a sorted array alone allows binary search.

---
# Costs: removing and memory

| Operation | Array | `ArrayList` | Singly (head, tail) | Doubly (sentinel) |
|---|---|---|---|---|
| Remove first | $\Theta(n)$ shift | $\Theta(n)$ | $\Theta(1)$ | $\Theta(1)$ |
| Remove last | $\Theta(1)$ | $\Theta(1)$ | $\Theta(n)$ walk | $\Theta(1)$ |
| Remove a held node | $\Theta(n)$ shift | $\Theta(n)$ | $\Theta(n)$ walk | $\Theta(1)$ |
| Memory beyond values | spare slots | spare slots | node + 1 reference | node + 2 references |

> Removing the last node of a singly linked list is linear even with a tail reference, because the new tail is the predecessor and nobody points back to it. That single row is the best argument for the prev link. Memory overhead for arrays is unused capacity; for linked lists it is per element, one object header and one or two references for every value stored.

---
# A costly loop

```quiz
`list` is a `java.util.LinkedList` of $n$ integers. What is the total cost of `for (int i = 0; i < n; i++) sum += list.get(i);`?
- [ ] $\Theta(n)$, because each `get` is constant time
- [ ] $\Theta(n \log n)$, because `get` starts from the nearer end
- [x] $\Theta(n^2)$, because each `get(i)` walks about $\min(i+1, n-i)$ links
- [ ] $\Theta(1)$ per element after the first call
```

> Each call walks from the nearer end, so about min of i plus one and n minus i links. Summed over all i that is about n squared over four, which is still Theta of n squared. Starting from the nearer end changes the constant, not the growth rate. An iterator or for-each loop does the same job in Theta of n.

---
# Summary

- A linked list trades $\Theta(1)$ indexing for cheap splicing.
- Link surgery: save what you need before you overwrite a reference.
- `head`, `tail` and `size` form an invariant every method must keep.
- Doubly linked plus a sentinel: $O(1)$ removal of a held node and no special cases.
- Iterate with an iterator; use `ArrayList` unless splicing dominates.

> If you remember one rule from today, make it the second bullet: most linked-list bugs overwrite a reference before saving what it pointed to. Next lecture builds stacks and queues, and you will see both implementations again: a linked list that pushes and pops at its head, and a resizing array whose doubling argument we promised earlier.

---
# Check yourself

- Why does removing the last node of a singly linked list cost $\Theta(n)$, even with `tail`?
- Draw `insertAfter(19, 30)` on `[4, 12, 25, 7, 19]`. Which list field besides `size` changes?
- Rewrite `remove(value)` for a singly linked list with a dummy head node. Which cases vanish?

> Try each question on paper before looking back. The first one is about who points to whom. For the second, trace the tail check in insertAfter. The third is the sentinel idea applied to a singly linked list: once every real node has a predecessor, the head special case disappears.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 10, Elementary data structures (linked lists, sentinels).
- Java SE API documentation: `java.util.LinkedList`, `java.util.ArrayList`, `java.util.Iterator`, `java.util.Objects`.
- All examples and code are original to this lecture and tested.

> The sentinel design follows the doubly linked list with a sentinel in CLRS chapter ten. Library facts, such as LinkedList being doubly linked and ArrayList appends being amortized constant time, come from the Java SE API documentation. The running example and all Java code were written for this lecture and are checked by an accompanying test program.
