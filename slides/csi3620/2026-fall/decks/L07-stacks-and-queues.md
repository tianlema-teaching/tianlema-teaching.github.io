@title Lecture 7: Stacks and queues
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Stacks and queues
## Last in first out, first in first out

---
# Where we are

- L02: Big-O, Big-Omega, Big-Theta, and what "worst case" means.
- L04 and L05: arrays, shifting, and recursion in merge sort and quicksort.
- L06: linked lists with `head` and `tail`: $\Theta(1)$ work at the front, and at the back for appends.
- L06 promised a proof that doubling makes appends amortized $O(1)$.
- Today: two restricted lists that power parsers, recursion and search.

> Last lecture we built lists that can change anywhere. Today we deliberately give up that freedom. A stack or a queue lets you touch only the ends, and in exchange every operation is constant time, amortized for the array versions, and the order in which items come back out is guaranteed. We also owe you the doubling argument from last time; it appears in the stack section.

---
# By the end of today you can

- State the stack and queue ADTs and their LIFO and FIFO rules.
- Implement both on a resizing array and on a linked list.
- Prove that doubling gives amortized $O(1)$ push.
- Trace a circular-array queue through wrap-around and resizing.
- Trace bracket checking, postfix evaluation and the call stack.
- Choose `ArrayDeque` and the right `Deque` methods in Java.

> Each outcome is observable: you can write it, prove it, or trace it on paper. The traces matter most, because stacks and queues are simple to state and easy to get subtly wrong, especially the circular array. By the end you should be able to predict the exact contents of every slot after any sequence of operations.

---
# Two ways to wait

- Undo in an editor reverses your **most recent** change first.
- A printer serves print jobs in the order they **arrived**.
- Both hold pending work; they differ only in which item leaves next.
- **Stack**: the newest item leaves first. **Queue**: the oldest item leaves first.

> Both situations store a pile of waiting items and remove them one at a time. Undo must reverse the last action first, or the document ends up in a state that never existed. A printer that jumped to the newest job would starve whoever printed first. The whole difference between the two structures is the rule for which item comes out next.

---
# An ADT is a contract

- An **abstract data type** (ADT) names operations and their behaviour.
- It says nothing about arrays, nodes or memory.
- A **data structure** is one concrete way to meet the contract.
- Today: one stack ADT, two structures; one queue ADT, two structures.
- In Java an ADT is naturally an `interface`.

> Separating the contract from the implementation lets us swap structures without touching the code that uses them. A method that only calls push and pop does not care whether the stack is an array or a chain of nodes. The test program for this lecture runs the exact same stack checks on both implementations, which is only possible because they share an interface.

---
# One running example

- Items arrive in order: `10`, `20`, `30`.
- Remove one item, then `40` arrives.
- A stack removes `30`: it holds `10, 20, 40`, top `40`.
- A queue removes `10`: it holds `20, 30, 40`, front `20`.
- The stack and queue pictures reuse these values; the applications use their own inputs. The test program checks these states.

> Keep these four operations in view. The same inputs produce different outputs under the two rules, and that difference is the whole lecture in miniature. The stack and queue pictures reuse these values, while the applications later on use their own inputs. The test program checks every state shown, so none of them was worked out by hand.

---
@type section
# Stacks

---
# The stack ADT

```java
public interface SimpleStack<T> {
    void push(T value);   // add on top
    T pop();              // remove and return the top; throws if empty
    T peek();             // return the top without removing; throws if empty
    boolean isEmpty();
    int size();
}
```

- **LIFO**: last in, first out.
- **Underflow**: `pop` or `peek` on an empty stack; ours throws.

> A stack has one open end, called the top. Push puts an item there, pop takes the item from there, and peek looks at it without taking it. Calling pop on an empty stack is called underflow. Our implementations throw NoSuchElementException, the same exception java dot util dot Deque uses; later we will see the alternative of returning null.

---
# The running example on a stack

```diagram
@dir TB
@reveal manual
A[10 | top 10]
focus A
--- push(10)
A -> B[10, 20 | top 20] : push(20)
focus A B
--- The newest item is always on top.
B -> C[10, 20, 30 | top 30] : push(30)
focus B C
--- Three items; only 30 is reachable.
C -> D[10, 20 | top 20] : pop() returns 30
focus C D
--- The last item pushed is the first popped.
D -> E[10, 20, 40 | top 40] : push(40)
focus D E
--- peek() would now return 40.
```

> Boxes list the stack from bottom to top. Notice that ten, the first item in, is still at the bottom and will be the last to leave. If we popped everything now the order would be forty, twenty, ten: the reverse of the order among the survivors. That reversal property is exactly what undo and recursion need.

---
# An array-backed stack

```java
public void push(T value) {
    if (size == items.length) {
        resize(2 * items.length);
    }
    items[size++] = value;
}

public T pop() {
    if (size == 0) {
        throw new NoSuchElementException("stack is empty");
    }
    T value = items[--size];
    items[size] = null; // drop the reference so it can be collected
    return value;
}
```

> The bottom of the stack is index zero and the top is index size minus one, so push and pop only touch the end of the array: no shifting. Pop writes null into the vacated slot. Without that line the array would keep a reference to an object the program no longer uses, and the garbage collector could not reclaim it; this is sometimes called loitering.

---
# Growing a full stack

```diagram
@dir TB
@reveal manual
A[10, _ | capacity 2]
focus A
--- push(10). One slot free.
A -> B[10, 20 | full, copies 0] : push(20)
focus A B
--- The array is now full.
B -> C[10, 20, 30, _ | capacity 4, copies 2] : push(30)
focus B C
--- Full, so copy 2 items into a new array of 4, then write 30.
C -> D[10, 20, _, _] : pop() returns 30
focus C D
--- The slot is set to null, shown as _.
D -> E[10, 20, 40, _] : push(40)
focus D E
--- No resize: capacity is still 4.
```

> Each box shows every slot of the underlying array, with an underscore for unused slots. The third push found the array full, so resize allocated twice the capacity and copied both items: that copy is the expensive step. The next push after the pop fits without copying. The copy count is tracked in the Java class so the test can check the analysis on the next slide.

---
# Why doubling gives amortized O(1)

- Start at capacity 1 and push $n \ge 2$ items; copies happen at the powers of two below $n$: $1, 2, \dots, 2^k$.
- Total copies: $1 + 2 + \dots + 2^k = 2^{k+1} - 1 < 2n$.
- Plus $n$ ordinary writes: fewer than $3n$ element writes in total.
- **Amortized** cost per push: under 3 writes, so $O(1)$.

$$\underbrace{n}_{\text{writes}} + \underbrace{(2^{k+1}-1)}_{\text{copies}} < 3n$$

> A resize is triggered only when the size is a power of two, and it copies that many items. The geometric sum of those copies is less than twice the largest term, and the largest is below n, so the total is below two n. Amortized means averaged over a sequence of operations, in the worst case over all sequences; it is not an average over random inputs. One single push can still cost Theta of n. The test checks this bound, for example 1025 pushes make 2047 copies.

---
# Growth policy matters

- Grow by a constant $c$ instead: copies $c + 2c + 3c + \dots \approx n^2/(2c)$.
- That is $\Theta(n^2)$ total: $\Theta(n)$ amortized per push.
- Any constant factor above 1 works; the constant in the bound changes.
- Shrinking: halve when only a quarter full, never at half.
- Our `ArrayStack` never shrinks; after a big burst it keeps its capacity.

> The geometric growth is what makes the sum small. Adding a fixed number of slots makes every c-th push copy the whole stack, and the arithmetic series is quadratic. If you also shrink, choose the thresholds with a gap: shrinking at half full lets one push and one pop alternate between growing and shrinking forever, each costing Theta of n. CLRS chapter sixteen analyses this dynamic table with the quarter-full rule.

---
# A linked stack

```java
private Node<T> top; // null when empty
private int size;

@Override
public void push(T value) {
    top = new Node<>(value, top);
    size++;
}
```

- Push and pop work at the head: this is `addFirst` and `removeFirst` from L06.
- Every operation is worst-case $\Theta(1)$: no resizing ever.
- Each item pays for a node object and one reference.

> The linked stack is the singly linked list from last lecture with only its head end in use. Pop reads top dot value, moves top to top dot next, and decrements size, after the usual empty check. Compared with the array version, every operation is constant time in the worst case, not just amortized, but memory per item is higher and the nodes may be scattered in memory.

---
# Amortized or worst case?

```quiz
An `ArrayStack` starts at capacity 1 and doubling is its only growth. Which statement about $n$ pushes is true?
- [ ] Every single push costs $\Theta(1)$ in the worst case
- [x] One push can cost $\Theta(n)$, but all $n$ pushes cost $O(n)$ in total
- [ ] The total is $\Theta(n \log n)$ because there are $\log n$ resizes
- [ ] The total is $\Theta(n^2)$ because each resize copies the whole array
```

> The push that triggers a resize copies every item, so a single push can be linear. But resizes are rare, and their copies form a geometric series that sums to less than two n. There are about log n resizes, yet they do not each cost n: they cost one, two, four, and so on. So the total is linear and the amortized cost per push is constant.

---
@type section
# Stack applications

---
# Balanced brackets

```algorithm
function BALANCED(text):
  S ← empty stack
  for each character c in text do
    if c is an opener ( [ { then push c onto S
    else if c is a closer ) ] } then
      if S is empty or pop(S) does not match c then return false
  return S is empty            // leftovers were never closed
```

- The most recent unmatched opener must close first: that is LIFO.
- Other characters are ignored.

> Compilers and editors do this every time you type. Each opener waits on the stack until its closer arrives, and the closer must match the opener that was pushed most recently, because brackets nest. Two things can go wrong: a closer that finds the wrong opener or no opener at all, and openers still waiting when the text ends.

---
# Balanced brackets in Java

```java
public static boolean isBalanced(String text) {
    Deque<Character> open = new ArrayDeque<>();
    for (char c : text.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            open.push(c);
        } else if (c == ')' || c == ']' || c == '}') {
            if (open.isEmpty() || open.pop() != opener(c)) {
                return false; // nothing to match, or the wrong kind
            }
        }
        Trace.record(() -> c + " " + open); // character, then stack top first
    }
    return open.isEmpty(); // leftovers were never closed
}
```

> We use the library's ArrayDeque as the stack; the next section explains why it beats the old Stack class. The isEmpty test comes first and short-circuits, so pop is never called on an empty deque. The helper opener maps each closer to the opener it requires. The comparison unboxes the popped Character to a char, so it compares values, not objects. The Trace line only records the stack for the next slides. It takes a lambda, so outside a capture it builds nothing and costs constant time.

---
# Tracing {a[(b)c]}

```diagram
@dir TB
@reveal manual
A[after { and a | stack: {]
focus A
--- Push {. Letters are skipped.
A -> B[after the opener ( | stack: ( [ {, top first]
focus A B
--- Push [ then (. Then b is skipped.
B -> C[after ) | stack: [ {] : pop ( matches )
focus B C
--- The closer matches the most recent opener.
C -> D[after c and ] | stack: {] : pop [ matches ]
focus C D
--- Skip c, then ] matches [.
D -> E[after } | stack empty | balanced] : pop { matches }
focus D E
--- Every opener was matched. Return true.
```

> Stacks are listed top first, the way ArrayDeque prints them. The deepest bracket, the parenthesis, is opened last and closed first. At the end nothing is waiting, so the text is balanced. Every state here was recorded by the Trace line inside this loop, run on this string, and the test program checks the recorded states against this slide.

---
# Two ways to fail

```diagram
@dir TB
@reveal manual
F0[text {[(])} | stack after three openers: ( [ {]
focus F0
--- Crossed brackets: three openers pushed.
F0 -> F1[closer ] pops ( | mismatch, return false]
focus F0 F1
--- The most recent opener is (, not [.
G0[text (() | stack after two openers: ( (]
focus G0
--- Unclosed: two openers, one closer.
G0 -> G1[closer ) pops ( | stack: ( at the end, return false]
focus G0 G1
--- A leftover opener was never closed.
```

> In the crossed example the counts of each bracket kind are all correct, which is why counting alone cannot solve this problem; order matters, and the stack remembers order. In the unclosed example every closer found a match, but one opener was still waiting when the text ended. The third failure, a closer with an empty stack, happens for a string like a lone closing parenthesis. The test program checks both traces shown here.

---
# Postfix expressions

- **Postfix** writes each operator after its two operands: `3 4 +` means $3 + 4$.
- `6 2 3 + * 4 -` means $6 \times (2 + 3) - 4 = 26$.
- No parentheses or precedence rules are needed.
- Rule: push numbers; an operator pops two, applies, pushes the result.
- The first pop is the **right** operand.

> In ordinary infix notation you need precedence rules and parentheses to know what to do first. Postfix puts operators exactly where their operands are complete, so a left-to-right scan with a stack is enough. Pop order matters for minus and divide: the operand pushed last is on top, and it is the right-hand operand.

---
# Postfix in Java

```java
for (String token : expression.trim().split("\\s+")) {
    if (isOperator(token)) {
        if (operands.size() < 2) {
            throw new IllegalArgumentException("missing operand for " + token);
        }
        int right = operands.pop(); // pushed last, so popped first
        int left = operands.pop();
        operands.push(apply(token, left, right));
    } else {
        operands.push(Integer.parseInt(token));
    }
    Trace.record(() -> token + " " + operands); // token, then stack top first
}
```

> The operands variable is an ArrayDeque of integers created just above this loop. Two malformed cases are rejected: an operator arriving with fewer than two operands, and, after the loop, a stack that does not hold exactly one value. A token that is neither an operator nor an integer makes parseInt throw NumberFormatException, and dividing by zero throws ArithmeticException. Results outside the int range wrap around silently: 2147483647 1 + gives -2147483648. The Trace line records the stack after each token for the next slide.

---
# Evaluating 6 2 3 + * 4 -

```diagram
@dir TB
@reveal manual
A[after 6 2 3 | stack: 3 2 6, top first]
focus A
--- Push three numbers.
A -> B[stack: 5 6] : + pops 3 then 2, pushes 2 + 3
focus A B
--- right = 3, left = 2.
B -> C[stack: 30] : * pops 5 then 6, pushes 6 × 5
focus B C
--- right = 5, left = 6.
C -> D[after 4 | stack: 4 30]
focus C D
--- Push 4.
D -> E[stack: 26 | result 26] : - pops 4 then 30, pushes 30 - 4
focus D E
--- One value remains: the answer.
```

> Follow the rule mechanically: numbers go on the stack, operators combine the top two. At the minus sign, four is popped first, so it is the right operand, and the result is thirty minus four, not four minus thirty. One value remains at the end, so the expression was well formed. These states were recorded by the Trace line in the loop, and the test program checks them against this slide.

---
# Pop order in postfix

```quiz
What does the postfix expression `8 2 - 3 *` evaluate to?
- [x] 18
- [ ] -18
- [ ] 2
- [ ] 22
```

> Push eight and two. The minus pops two first, so right is two and left is eight: eight minus two is six. Push three; the times pops three and six and pushes eighteen. Minus eighteen is what you get if you swap the pop order and compute two minus eight. The test program checks this expression.

---
# The call stack

```java
public static int sumTo(int n, List<String> log) {
    log.add("call sumTo(" + n + ")");
    int result = (n == 0) ? 0 : n + sumTo(n - 1, log);
    log.add("return " + result + " from sumTo(" + n + ")");
    return result;
}
```

- Each call gets a **frame**: its parameters, local variables and return point.
- Frames live on the **call stack**: a call pushes, a return pops.
- The newest call always finishes first: LIFO again.

> The Java runtime keeps a stack of frames for every thread. When sumTo calls itself, the caller's frame waits, suspended in the middle of the addition, while a new frame is pushed for the callee. This method writes a line to a log when each call starts and ends, so we can see the stack discipline in the output on the next slide.

---
# sumTo(3) on the call stack

```diagram
@dir TB
@reveal manual
A[frames: sumTo 3]
focus A
--- call sumTo(3)
A -> B[frames: sumTo 3, sumTo 2, sumTo 1, sumTo 0] : three more calls
focus A B
--- Each call pushes a frame before any returns.
B -> C[frames: sumTo 3, sumTo 2, sumTo 1] : sumTo(0) returns 0
focus B C
--- The base case returns first.
C -> D[frames: sumTo 3] : returns 1, then 3
focus C D
--- Each waiting caller finishes its addition and pops.
D -> E[no frames | result 6] : sumTo(3) returns 6
focus D E
--- Calls returned in reverse order of starting.
```

> The logged order was call three, two, one, zero, then return zero, one, three, six: exactly last in, first out. Recursion depth equals the peak number of frames, so recursion that goes too deep exhausts the stack and Java throws StackOverflowError. That is also why quicksort's worst-case stack depth from lecture four mattered.

---
@type section
# Queues

---
# The queue ADT

```java
public interface SimpleQueue<T> {
    void enqueue(T value); // add at the back
    T dequeue();           // remove and return the front; throws if empty
    T peek();              // return the front, keep it; throws if empty
    boolean isEmpty();
    int size();
}
```

- **FIFO**: first in, first out.
- Items join at the **back** and leave from the **front**.
- `java.util.Queue`: `add`, `remove`, `element` throw like ours; `offer`, `poll`, `peek` return `false` or `null` instead.

> A queue has two ends with different jobs. Enqueue adds at the back, dequeue removes from the front, and peek looks at the front. Java's Queue interface has two sets of names that differ only in how they report failure: add, remove and element throw, like ours, while offer, poll and peek return false or null instead. Our running example: after ten, twenty, thirty and one dequeue, ten is gone and twenty is at the front.

---
# A linked queue

```diagram
@dir LR
H[head: front] -> A[20] -> B[30] -> C[40] -> X[null]
T[tail: back] -> C
```

- Dequeue at `head`, enqueue at `tail`: both $\Theta(1)$ worst case.
- The other way round, removing at `tail` would need the predecessor: $\Theta(n)$.
- Enqueue is exactly `addLast` from L06.

> The choice of which end is the front is forced by the singly linked list. Removing at the head is constant time, but removing at the tail needs the node before the tail, which a singly linked list can only find by walking. Adding at the tail is constant time thanks to the tail reference. The picture shows the running example after its last step.

---
# Linked dequeue

```java
@Override
public T dequeue() {
    if (head == null) {
        throw new NoSuchElementException("queue is empty");
    }
    T value = head.value;
    head = head.next;
    if (head == null) {
        tail = null; // the queue became empty
    }
    size--;
    return value;
}
```

> This is removeFirst from last lecture. The line most often forgotten is the one that clears tail when the last item leaves. Without it, tail still refers to the removed node, and the next enqueue would link the new node after a node no longer in the queue; head would stay null and the new item would be lost. The test program enqueues again after emptying to catch exactly this bug.

---
# Why not a plain array?

- Front at index 0: every dequeue shifts the rest left, $\Theta(n)$.
- Or keep a `front` index that moves right: dequeue is $\Theta(1)$.
- But the used part drifts right and the freed slots are wasted.
- Fix: let indices **wrap around** to 0 at the end of the array.

> An array queue that shifts on every dequeue is correct but linear per operation. Moving a front index instead makes dequeue cheap, but after many operations the items sit at the far right of the array with empty slots on the left that nobody reuses. The circular array reuses them by treating the array as a ring.

---
# The circular array

- Fields: `slots`, `front` (index of the oldest item) and `size`.
- The item $k$ places behind the front sits at $(\text{front} + k) \bmod \text{capacity}$.
- The next enqueue writes at $(\text{front} + \text{size}) \bmod \text{capacity}$.
- A dequeue advances $\text{front} \leftarrow (\text{front} + 1) \bmod \text{capacity}$.
- Empty means `size == 0`; full means `size == capacity`.

> The modulo operator turns a straight array into a ring: after the last index comes index zero. Keeping a size field makes empty and full easy to tell apart. A design that keeps only front and back indices sees front equal to back in both cases and must waste a slot or keep a flag. With front and size, the invariant is simply that the items occupy size consecutive slots on the ring starting at front.

---
# Circular enqueue and dequeue

```java
public void enqueue(T value) {
    // ...
    int back = (front + size) % slots.length;
    slots[back] = value;
    size++;
}

public T dequeue() {
    // ...
    T value = slots[front];
    slots[front] = null;
    front = (front + 1) % slots.length;
    size--;
    return value;
}
```

> The omitted lines in enqueue reject null, because this class uses null to mark an unused slot and poll returns null for an empty queue, and then call grow when the array is full; the omitted line in dequeue is the empty check, which throws. Both methods do constant work apart from grow. Java's percent operator gives the remainder, which is the right wrap-around here because front and size are never negative. Dequeue nulls the slot for the same reason the array stack does: so the queue does not hold on to objects it no longer contains.

---
# Wrap-around, step by step

```diagram
@dir TB
@reveal manual
A[10, 20, 30, _ | front 0, size 3]
focus A
--- Capacity 4. Enqueue 10, 20, 30 into slots 0, 1, 2.
A -> B[_, 20, 30, _ | front 1, size 2] : dequeue() returns 10
focus A B
--- Slot 0 is freed; front advances.
B -> C[_, _, 30, _ | front 2, size 1] : dequeue() returns 20
focus B C
--- Two free slots now sit on the left.
C -> D[_, _, 30, 40 | front 2, size 2] : enqueue(40)
focus C D
--- back = (2 + 1) mod 4 = 3.
D -> E[50, _, 30, 40 | front 2, size 3] : enqueue(50)
focus D E
--- back = (2 + 2) mod 4 = 0: the index wraps around.
E -> F[50, 60, 30, 40 | front 2, size 4, full] : enqueue(60)
focus E F
--- Queue order is 30, 40, 50, 60, not slot order.
```

> Each box lists slots zero through three. Fifty lands in slot zero, before thirty in slot order but after it in queue order. That gap between slot order and queue order is the one thing to keep straight with circular arrays. The queue is now full; the next enqueue must grow the array.

---
# Growing a circular queue

```java
private void grow() {
    T[] bigger = newArray(2 * slots.length);
    for (int i = 0; i < size; i++) {
        bigger[i] = slots[(front + i) % slots.length];
    }
    slots = bigger;
    front = 0;
}
```

- `enqueue(70)` on the full queue gives `[30, 40, 50, 60, 70, _, _, _]`, front 0.
- Copy in **queue order**, not slot order: `Arrays.copyOf` would be wrong.
- Doubling makes enqueue amortized $O(1)$, by the stack argument.

> A plain array copy would keep fifty and sixty at slots zero and one, and with a larger capacity the wrap-around no longer lines up: the queue order breaks. So grow walks the old ring from front and writes items to the new array in queue order, starting at slot zero. The same doubling argument as for the stack bounds the total copying.

---
# Where does the next item go?

```quiz
A circular queue has capacity 8, `front == 6` and `size == 3`. Which slot does the next `enqueue` write?
- [ ] 9
- [x] 1
- [ ] 0
- [ ] 6
```

> The three items occupy slots six, seven and zero. The next write goes to front plus size, nine, modulo eight, which is slot one. Nine is not a valid index in an array of eight, and zero is already occupied by the third item. The test program builds exactly this state and checks it.

---
@type section
# Deques and a preview of BFS

---
# The deque

- A **deque** (double-ended queue, said "deck") adds and removes at both ends.
- A stack uses one end; a queue adds at one end and removes at the other.
- A circular array supports both ends: moving front back is $(\text{front} - 1 + \text{capacity}) \bmod \text{capacity}$.
- A doubly linked list with a sentinel also gives $\Theta(1)$ at both ends.
- Java: the `Deque` interface; `ArrayDeque` and `LinkedList` implement it.

> A deque is the general case, and stacks and queues are ways of using it. Adding capacity before the modulo keeps the index nonnegative, because Java's remainder of a negative number is negative. ArrayDeque's API documentation promises a resizable array, and in current OpenJDK it is circular; LinkedList's documentation promises a doubly linked list, and OpenJDK uses first and last references rather than a sentinel.

---
# BFS uses a queue

```java
seen[start] = true;
queue.offer(start);
while (!queue.isEmpty()) {
    int v = queue.poll();
    visited.add(v);
    for (int w : adjacency.get(v)) {
        if (!seen[w]) {
            seen[w] = true; // mark when enqueued, not when visited
            queue.offer(w);
        }
    }
    Trace.record(() -> "visit " + v + ", queue " + queue); // front first
}
```

- **Breadth-first search** visits vertices in order of distance from `start`.
- Full treatment in L15; today, just watch the queue. [BFS visualization](../../../visualizations/algorithms/bfs.html)

> A graph is a set of vertices joined by edges, and adjacency dot get of v lists v's neighbours. Breadth-first search explores the start, then all its neighbours, then theirs, and the FIFO rule is what produces that layer-by-layer order. Marking vertices when they enter the queue, rather than when they leave, keeps any vertex from being queued twice.

---
# BFS from 0, step by step

```diagram
@dir TB
@reveal manual
A[edges 0-1, 0-2, 1-3, 2-3, 2-4 | queue: 0]
focus A
--- Start: 0 is marked and queued.
A -> B[visit 0 | add 1, 2 | queue: 1, 2]
focus A B
--- Both neighbours of 0 are new.
B -> C[visit 1 | add 3 | queue: 2, 3]
focus B C
--- 0 is already marked, so only 3 joins.
C -> D[visit 2 | add 4 | queue: 3, 4]
focus C D
--- 3 is already marked, so only 4 joins.
D -> E[visit 3, then 4 | queue empty | order 0, 1, 2, 3, 4]
focus D E
--- Distance 0, then 1, then 2. Try other starts in the BFS visualization on the previous slide.
```

> Queues are listed front first. Vertices one and two are one edge from the start, three and four are two edges away, and the queue releases them in exactly that order. Replace the queue with a stack and the order changes to one that runs deep before wide; lecture fifteen treats depth-first search properly. The queue contents after each visit were recorded by the Trace line in the loop and are checked by the test program.

---
@type section
# The Java library

---
# Prefer ArrayDeque to Stack

- `java.util.Stack` is a legacy class that extends `Vector`.
- Its API docs recommend the `Deque` interface in preference to it.
- `Stack` allows access by index, so the LIFO rule is not enforced.
- `ArrayDeque`'s docs: likely faster than `Stack` as a stack, and than `LinkedList` as a queue.
- `ArrayDeque` rejects `null` elements; most operations are amortized $O(1)$.

> Stack dates from the first Java release and inherits every Vector method, including get of i and insertion anywhere, so it is not really a stack. Its own documentation points you to Deque instead. Iteration order also differs: a Stack prints bottom first, an ArrayDeque used as a stack prints top first, which the test program checks.

---
# Two ways to report failure

| Operation | Throws an exception | Returns a special value |
|---|---|---|
| Add at back (`Queue`) | `add(e)` | `offer(e)` returns `false` |
| Remove front (`Queue`) | `remove()` | `poll()` returns `null` |
| Look at front (`Queue`) | `element()` | `peek()` returns `null` |
| Stack (`Deque`) | `push(e)`, `pop()`, `getFirst()` | `offerFirst(e)` returns `false`; `pollFirst()`, `peekFirst()` return `null` |

- On an empty deque, `pop` and `remove` throw `NoSuchElementException`.
- `add` and `offer` differ only on a full, capacity-bounded queue.

> The Queue and Deque interfaces give each operation in two styles. Use the throwing style when emptiness is a bug in the caller, and the special-value style when an empty queue is a normal situation, such as a loop that polls until null. Note that Deque's peek is the same as peekFirst, so it returns null rather than throwing; the throwing way to look at the top is getFirst. The Queue documentation says null should not be inserted into a queue, even where it is allowed, because poll uses null to mean the queue has no elements.

---
# Using ArrayDeque

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(10);
stack.push(20);
checkEquals(20, stack.pop(), "ArrayDeque pop");
```

```java
Queue<Integer> queue = new ArrayDeque<>();
check(queue.offer(10), "offer succeeds");
check(queue.add(20), "add succeeds");
checkEquals(10, queue.poll(), "poll front");
```

- Declare the variable by the interface you mean: `Deque` for a stack, `Queue` for a queue.

> These lines are taken from the lecture's test program, which is why they are wrapped in check calls. Declaring the variable as Queue restricts the code to queue operations, so a stray push cannot sneak in. The same ArrayDeque object serves both roles; the interface type is what documents your intent.

---
@type section
# Wrap-up

---
# Costs

| Structure | push, enqueue | pop, peek, dequeue | Extra space |
|---|---|---|---|
| Array stack | amortized $\Theta(1)$; worst $\Theta(n)$ | $\Theta(1)$ | capacity − $n$ unused |
| Circular queue | amortized $\Theta(1)$; worst $\Theta(n)$ | $\Theta(1)$ | capacity − $n$ unused |
| Linked stack or queue | $\Theta(1)$ | $\Theta(1)$ | one node per item |
| ArrayDeque | amortized $O(1)$ | amortized $O(1)$ | resizable array |

- Our arrays double and never shrink, so capacity tracks the **peak** size.

> Every operation is constant time, but the array versions are constant only amortized, because a resizing push or enqueue copies everything. The linked versions are constant in the worst case and pay with a node object per item. Once an array has doubled at least once and seen only pushes, it is more than half full; after pops without shrinking it can be nearly empty, which is why CLRS also shrinks the table when it is one-quarter full. The ArrayDeque row follows its API documentation, which says most operations run in amortized constant time.

---
# Summary

- Stack: LIFO; push, pop and peek at the top.
- Queue: FIFO; enqueue at the back, dequeue at the front.
- Doubling arrays give amortized $O(1)$ by a geometric sum.
- Circular arrays reuse slots with $(\text{front} + k) \bmod \text{capacity}$.
- Brackets, postfix and recursion use stacks; BFS uses a queue.
- In Java, use `ArrayDeque` through `Deque` or `Queue`.

> Two rules, two implementations each, and one analysis technique. The applications show why the rules matter: nesting and recursion need the most recent item first, while breadth-first search needs the oldest. Next lecture starts trees, where a queue and a stack will reappear when we traverse them in lecture nine.

---
# Check yourself

- Trace a circular queue of capacity 3: enqueue 1, 2, 3, dequeue twice, enqueue 4, 5, 6. List every slot.
- Why can shrinking at half full cost $\Theta(n)$ per operation?
- Evaluate `4 2 5 * + 3 -` by hand, writing the stack after every token.

> Do these on paper before checking with code. The first tests the wrap-around and the grow step, since the queue fills again after 5 and then 6 forces a grow. The second asks for a sequence of operations that alternates between growing and shrinking. For the third, remember that the first value popped is the right operand.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 10, Elementary data structures (stacks, queues).
- CLRS, Chapter 16, Amortized analysis (dynamic tables).
- CLRS, Chapter 20, Elementary graph algorithms (breadth-first search).
- Java SE API documentation: `java.util.Deque`, `Queue`, `ArrayDeque`, `Stack`.
- All examples and code are original to this lecture and tested.

> Stacks and queues follow CLRS chapter ten, and the doubling argument is the dynamic-table analysis of chapter sixteen, shown here with the aggregate (summation) argument; the chapter also presents the accounting and potential methods. Breadth-first search is previewed from chapter twenty and treated fully later. Library facts come from the Java SE API documentation.
