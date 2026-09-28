@title Lecture 1: What are data structures and algorithms?
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# What are data structures and algorithms?
## Representations, operations and the cost of a choice

---
# Where this course starts

- This is the first lecture: the vocabulary for everything after it.
- Next: measuring cost precisely with $O$, $\Omega$ and $\Theta$.
- Then searching, sorting, and one data structure after another.
- Every later lecture asks the same two questions: is it correct, and what does it cost?

> This review deck collects the ideas the rest of the course is built on. Nothing here is advanced, but every word we define today gets used in every later lecture. The next lecture turns today's informal idea of cost into precise notation. After that come searching and sorting, and then a long run of data structures: lists, stacks, trees, hash tables, heaps and graphs. Each of them gets judged by the same two questions.

---
# By the end of today you can

- Define a data structure as a representation plus operations.
- Separate an abstract data type (what) from a data structure (how).
- Check a procedure against the five properties of an algorithm.
- Explain why we count steps as a function of input size, not seconds.
- Predict the cost of indexing and inserting in an array.
- Read the Java the course uses: interfaces, inheritance, generics, recursion.

> These are things you can do, not topics you have heard of. By the end you should be able to take a short Java interface and say which parts are the abstract data type and which parts belong to an implementation. You should be able to look at a loop and say how its work grows as the input grows. And you should be able to read every Java listing in the later decks without stopping on the syntax.

---
# A concrete question

- A program keeps a collection of whole numbers.
- It must answer, many times: **is 27 in the collection?**
- It must also accept new numbers as they arrive.
- How should it store them so both jobs are cheap?

> Almost every program does some version of this. A login check asks whether a user name exists. A spell checker asks whether a word is in its dictionary. A compiler asks whether a variable was declared. The question is always the same shape: store items, add items, ask about items. The answer to "how should we store them" is the subject of this whole course, and the first surprise is that there is no single best answer.

---
# The running example

- Numbers arrive in this order: `27, 8, 19, 3`, and later `14`.
- We store them two ways and compare every operation.
- For the search game we use the sorted numbers `1, 2, ..., 32`.
- The target is always `27`.

```diagram
@dir LR
A0[27] -> A1[8] -> A2[19] -> A3[3] -> A4[14 arrives later].amber
```

> Keep these five numbers in mind: twenty-seven, eight, nineteen, three, and then fourteen. Every picture in this deck uses them. The arrows here show arrival order only, not how the numbers are stored; storage is exactly what we are about to choose. For the search game later we switch to the thirty-two numbers one to thirty-two, still looking for twenty-seven, because a longer list makes the difference between strategies easy to see.

---
@type section
# Data structures

---
# What is a data structure?

- A **representation**: how the data is laid out in memory.
- A set of **operations**: what you can do with it, such as add or search.
- The representation decides what each operation costs.
- Choosing a structure means choosing which operations are cheap.

> A data structure is not just a pile of data. It is a layout together with the operations that respect it. An array sorted by value and an array in arrival order hold exactly the same numbers, but they are different data structures, because their operations work differently and cost differently. Whenever you pick a structure you are betting on which operations your program will use most.

---
# What versus how

| | Abstract data type (ADT) | Data structure |
|---|---|---|
| Answers | *What* operations exist and what they mean | *How* they are carried out |
| Example | A set: `add`, `contains`, `size` | Unsorted array, sorted array |
| In Java | An `interface` | A `class` that implements it |
| Cost | Not specified | Determined by the layout |

> An abstract data type is a promise about behaviour: add puts a value in, contains reports whether a value is present, size counts distinct values. It says nothing about memory. A data structure keeps that promise in a particular way. Code that only uses the interface keeps working if you swap one implementation for another; only the speed changes. That separation is what lets you change your mind about the structure later.

---
# The ADT as a Java interface

```java
public interface IntSet {
    boolean add(int x);        // false if x was already present
    boolean contains(int x);
    int size();
}
```

- Three operations, their names, types and meaning. Nothing about storage.
- A set holds each value at most once.

> Read the interface as a contract. Add returns true if the value was new and false if it was already there, so a set never holds duplicates. Contains answers the membership question. Size counts the values. There is no array here, no loop, no memory at all. Any class that provides these three methods with this meaning is a valid set, and client code can use it without knowing which class it got.

---
# One ADT, two data structures

```diagram
@dir TB
I[IntSet | interface: what] --> B[ArrayIntSet | shared array and count]
B --> U[UnsortedIntSet | arrival order]
B --> S[SortedIntSet | increasing order]
```

> Here is the plan for our two implementations. An abstract class holds what they share: an array and a count of how many slots are used. The unsorted version keeps values in the order they arrived. The sorted version keeps them in increasing order at all times. For any sequence of calls both give identical answers, and that is the whole point: the ADT is the same, the data structure is not.

---
# Unsorted: append, then scan

```java
@Override
public boolean contains(int x) {
    for (int i = 0; i < size; i++) {
        if (data[i] == x) {
            return true;
        }
    }
    return false;
}
// ...
ensureRoom();
data[size++] = x;              // append at the end
```

- `add` rejects duplicates with `contains`, then appends.

> Look at the loop in contains: with no order to exploit, it must look at every stored value until it finds a match, and when the value is absent it looks at all of them. Adding is simple once we know the value is new: put it in the next free slot. Notice that add first calls contains to reject duplicates, so add inherits the cost of the scan.

---
# Sorted: halve the range

```java
int low = 0, high = size - 1;
while (low <= high) {
    int mid = low + (high - low) / 2;
    if (data[mid] == x) {
        return true;
    } else if (data[mid] < x) {
        low = mid + 1;
    } else {
        high = mid - 1;
    }
}
return false;
```

> Because the values are in increasing order, one comparison with the middle value tells us which half could still hold x, and the other half is discarded. This is binary search; lecture three studies it in detail. For now notice the payoff: each pass through the loop throws away about half of what is left, instead of one value.

---
# Sorted: shift to make room

```java
int i = size - 1;
while (i >= 0 && data[i] > x) {
    data[i + 1] = data[i];     // shift a bigger value right
    i--;
}
data[i + 1] = x;
size++;
```

- `add` first rejects duplicates with `contains`, as before.
- Keeping order is the price: bigger values move one slot right.

> The sorted version pays for its fast search when it adds. Starting from the end, every value bigger than x moves one slot to the right, and x drops into the gap. Adding a value bigger than everything moves nothing. Adding a value smaller than everything moves every stored value. We will trace this exact loop on the running example in the arrays section.

---
# Same calls, different states

```diagram
@dir TB
@reveal manual
C[add 27, add 8, add 19, add 3]
focus C
--- The same four calls go to both sets.
C -> U[UnsortedIntSet | 27, 8, 19, 3]
C -> S[SortedIntSet | 3, 8, 19, 27]
focus C U S
--- Arrival order on the left, increasing order on the right.
U -> Q[contains 27? both answer true]
S -> Q
focus U S Q
--- Different layouts, identical answers.
```

> These states come from running both classes on the running example. The unsorted set stores twenty-seven, eight, nineteen, three, exactly as they arrived. The sorted set stores three, eight, nineteen, twenty-seven. Ask either one whether twenty-seven is present and both say yes. A client using only the IntSet interface cannot tell them apart, except by timing them on large inputs.

---
# Comparing the two structures

| Operation, $n$ values stored | Unsorted array | Sorted array |
|---|---|---|
| `contains`, worst case | looks at all $n$ | about $\log_2 n$ probes |
| `add`: duplicate check | look at all $n$ | about $\log_2 n$ probes |
| `add`: move values (not counting occasional array growth) | none | up to $n$ shifts |
| `size` | constant | constant |

> Neither column wins everywhere. The sorted array makes the membership question fast, but every insertion may shift many values. The unsorted array appends without shifting but must scan to answer contains. If your program asks far more questions than it adds values, the sorted array is the better bet. If it mostly adds, the balance shifts. Later lectures introduce structures, such as balanced trees and hash tables, that make both operations cheap.

---
# Quiz: what or how?

```quiz
Which statement describes the abstract data type, not a data structure?
- [ ] Values are kept in an array in increasing order.
- [x] `contains(x)` returns true exactly when `x` was added earlier.
- [ ] Adding a value shifts larger values one slot right.
- [ ] Values are kept in arrival order.
```

> The second option is the only one that speaks purely about behaviour: what contains returns, with no mention of memory. The other three each describe a layout or a procedure, so they belong to a particular implementation. A good test is to ask whether the statement would still be true for every correct implementation of IntSet. Only the second one passes that test.

---
@type section
# Algorithms

---
# What makes an algorithm?

- **Finite**: it stops after a finite number of steps on every valid input.
- **Definite**: each step is precise; there is no guessing what to do.
- **Input**: zero or more values it is given.
- **Output**: at least one result that relates to the input.
- **Effective**: each step is basic enough to actually carry out.
- The checklist follows Knuth, *The Art of Computer Programming*, Vol. 1, §1.1.

> An algorithm is a precise procedure that turns input into output. These five properties are the checklist Donald Knuth gives in the first section of The Art of Computer Programming. Finite means it always halts, not just usually. Definite means two people following it make the same moves. Effective means every step can be done with the tools at hand, such as comparing two numbers or adding one to a counter. A procedure missing any of these may be useful, but it is not an algorithm in this sense.

---
# Checking the list on a real procedure

```algorithm
function MAX(A):                 // A has at least one item
  best ← A[0]
  for i ← 1 to len(A) - 1 do
    if A[i] > best then
      best ← A[i]
  return best
```

- Input: a nonempty list. Output: its largest item.
- Finite: the loop runs $n - 1$ times. Definite and effective: compare and copy.

> Walk through the five properties on this short procedure. The input is a nonempty list and the output is the largest item, so both are named. The loop runs exactly n minus one times, so it always stops. Each step is a comparison or a copy, which is both precise and basic. On our running example, twenty-seven, eight, nineteen, three, it returns twenty-seven after three comparisons. The same procedure appears later as generic Java.

---
# When a procedure is not an algorithm

- "Add salt until it tastes right": not definite.
- "Start a mathematical integer at 0 and add 1 until it is negative": not finite.
- "Pick the number the user is thinking of": not effective.
- A procedure can be precise and still wrong: that is a separate question.

> These are deliberately silly, but each fails exactly one property. Tasting right is a judgement, so two cooks would stop at different points. Counting upward from zero in the integers of mathematics never reaches a negative number, so the loop never ends. We say mathematical integer on purpose: a Java int would eventually overflow and wrap around to a negative value. Reading someone's mind is not a step anyone can carry out. And notice the last point: a precise, finite procedure that returns the smallest item when we asked for the largest is still an algorithm, just not a correct one for our problem.

---
# Correctness and efficiency

- **Correct**: for every valid input, it halts with the right output.
- **Efficient**: it uses little time and memory as inputs grow.
- Correctness comes first: a fast wrong answer is still wrong.
- Among correct algorithms, efficiency decides.

> We judge every algorithm twice. Correctness is a yes or no question about all valid inputs, not just the ones we tried. Efficiency is a question of degree: how much time and memory does it need, and how does that change as the input gets bigger. We never trade correctness for speed. But once several algorithms are all correct, efficiency is usually what separates a program that finishes from one that does not.

---
# Tests and arguments

- A test shows the output is right for **one** input.
- A failed test proves a bug; a passed test proves nothing general.
- An argument covers **every** input: an invariant, or induction.
- This course uses both: tests to catch mistakes, arguments to explain why code works.

> Testing and proving answer different questions. If one input gives the wrong answer, the algorithm is wrong, full stop. But a thousand passing tests only show it works on those thousand inputs. To be sure for all inputs we need an argument, such as a loop invariant that stays true on every pass, or induction on the input size. Every Java class behind these decks is tested, and the decks explain why the code works, because we want both kinds of evidence.

---
@type section
# Measuring efficiency

---
# Why not just time it?

- Seconds depend on the machine, the language and the compiler.
- They depend on what else is running at the same moment.
- They depend on the particular input you happened to choose.
- They do not tell you what happens when the input is ten times bigger.

> A stopwatch measures one program on one machine on one input on one day. Run it on a laptop and a server and you get different numbers. Run it twice on the same laptop and you may get different numbers again. None of that tells you the thing you most need to know when you design software: what happens when the data grows. Timing is useful for tuning real code, but it is a poor way to compare algorithms.

---
# Count steps as a function of $n$

- **Input size** $n$: how many values the algorithm is given.
- **Basic step**: one comparison, one assignment, one arithmetic operation.
- Count how many basic steps run, as a formula in $n$.
- Then ask how that formula **grows** as $n$ grows.

> Instead of seconds we count basic steps, and we express the count as a function of the input size. For searching a list, the size is the number of values. Each basic step takes roughly constant time on any machine, so the count captures the algorithm and not the hardware. The question we really care about is the growth of that count, which the next lecture makes precise with Big-O, Big-Omega and Big-Theta.

---
# The efficiency game

- The numbers `1, 2, ..., 32` are sorted in an array.
- Find `27`. A **probe** is one look at one array value.
- Strategy A, linear search: look left to right.
- Strategy B, binary search: look at the middle, keep the half that can hold `27`.
- Try both: [Linear search visualization](../../../visualizations/algorithms/linear_search.html) and [Binary search visualization](../../../visualizations/algorithms/binary_search.html).

> Here is a game you can play with a partner and thirty-two cards face down in order. One person hides a number; the other turns cards over until they find it. Each card turned over is a probe. Strategy A turns cards from the left. Strategy B turns the middle card, and since the cards are in order, one look tells you whether the target is to the left or to the right. Guess how many probes each strategy needs to find twenty-seven before the next slide.

---
# Strategy A: 27 probes

```diagram
@dir LR
P1[1] -> P2[2] -> P3[3] -> D[... 23 more ...] -> P27[27 found].green
```

- Linear search looks at `1, 2, 3, ...` and stops at `27`.
- That is **27** probes. For `32`, or an absent value, it is all **32**.
- Probes grow in step with $n$: double the list, double the worst case.

> Linear search simply walks. It never uses the fact that the numbers are sorted. Running the code confirms twenty-seven probes for twenty-seven, and thirty-two probes for the last value or for a value that is not there at all. If the list had a million numbers the worst case would be a million probes. The work grows in direct proportion to the input size.

---
# Strategy B, step by step

```diagram
@dir TB
@reveal manual
R0[range 1 to 32 | probe 16] 
focus R0
--- Probe 1: 16 is less than 27, so keep 17 to 32.
R0 -> R1[range 17 to 32 | probe 24]
focus R0 R1
--- Probe 2: 24 is less than 27, so keep 25 to 32.
R1 -> R2[range 25 to 32 | probe 28]
focus R1 R2
--- Probe 3: 28 is more than 27, so keep 25 to 27.
R2 -> R3[range 25 to 27 | probe 26]
focus R2 R3
--- Probe 4: 26 is less than 27, so keep 27 alone.
R3 -> R4[range 27 to 27 | probe 27].green
focus R3 R4
--- Probe 5: found.
```

> These five probes are exactly what the Java code printed: sixteen, twenty-four, twenty-eight, twenty-six, twenty-seven. Each probe looks at the middle of the remaining range, with the middle rounded down when the range has an even length. Each probe eliminates about half of what is left. Five probes instead of twenty-seven, and the gap widens dramatically as the list grows.

---
# Why never more than 6 probes

```diagram
@dir LR
H32[32] -> H16[16] -> H8[8] -> H4[4] -> H2[2] -> H1[1]
```

- Each probe leaves at most half of the range: $32 \to 16 \to 8 \to 4 \to 2 \to 1$.
- One value left takes one more probe: at most $\log_2 32 + 1 = 6$.
- Running all targets confirms it: the worst is `32`, or any absent value above it, with 6 probes.

> After each probe the remaining range is at most half as long. Starting from thirty-two it takes five halvings to reach a single value, and one more probe to look at that value. That gives six. We checked every target from one to thirty-two, plus values outside the range, and the maximum was exactly six, reached for the target thirty-two and for any absent value above thirty-two, such as thirty-three. Twenty-seven happened to need five.

---
# Doubling adds one probe

| Sorted values $n$ | Linear, worst case | Binary, worst case |
|---|---|---|
| 32 | 32 | 6 |
| 64 | 64 | 7 |
| 1024 | 1024 | 11 |

- Linear grows like $n$; binary grows like $\log_2 n$.

> These worst cases were computed by running both searches on every target. Doubling the list doubles the linear worst case but adds only one binary probe, because one extra halving handles the extra size. At a thousand and twenty-four values the gap is already about a hundred to one. That difference in growth, not any particular timing, is why we care. It is also why the sorted set's contains method is so much faster than the unsorted one on large inputs.

---
# Quiz: the next doubling

```quiz
Binary search on the sorted numbers 1 to 64 needs how many probes in the worst case?
- [ ] 6
- [x] 7
- [ ] 32
- [ ] 64
```

> Seven. Sixty-four halves to thirty-two, sixteen, eight, four, two and one: six halvings, plus one probe on the last value. The table on the previous slide shows the same number from running the code. Thirty-two and sixty-four are what linear search would need at worst on thirty-two and sixty-four values, so they are the answers you get if you forget that binary search throws away half the range each time.

---
@type section
# Arrays

---
# The array: our first data structure

```diagram
@dir LR
M0[a 0 | 3] -- M1[a 1 | 8] -- M2[a 2 | 19] -- M3[a 3 | 27]
```

- A fixed number of slots of one type, side by side in memory.
- Conceptually, slot `i` sits at: start address $+\; i \times$ slot size.
- So reading or writing `a[i]` costs the same for every `i`: constant time.

> An array is the simplest data structure and the one the others are built from. Conceptually the slots sit next to each other, so the location of slot i is a single multiplication and addition away from the start. That is why indexing takes constant time, whether you ask for slot zero or slot one million. In Java the virtual machine manages the actual memory, but this model is exactly why array indexing is fast.

---
# Arrays in Java

- `int[] a = new int[4];` makes four slots, all `0`.
- The length is fixed: `a.length` never changes.
- Indices run from `0` to `a.length - 1`; others throw an exception.
- To grow, allocate a bigger array and copy.

> Java arrays know their length and check every index. Asking for a slot outside the valid range throws an ArrayIndexOutOfBoundsException instead of reading random memory. What Java arrays cannot do is grow. Our ArrayIntSet handles that by allocating an array twice as long and copying the old values over when it runs out of room. The library class ArrayList does the same kind of thing for you behind its add method.

---
# Growing and sharing: the base class

```java
public abstract class ArrayIntSet implements IntSet {
    protected int[] data = new int[4];
    protected int size = 0;
    @Override
    public int size() {
        return size;
    }

    protected void ensureRoom() {
        if (size == data.length) {
            data = Arrays.copyOf(data, 2 * data.length);
        }
    }
    // ...
}
```

> Here is the shared part of both sets. The field data is the array, and size counts how many of its slots are in use, which is usually fewer than data dot length. The method ensureRoom doubles the array when every slot is full; Arrays dot copyOf allocates the new array and copies the old values into it. Copying costs time proportional to the size, but it happens rarely, and Lecture 7 shows that doubling keeps the average cost of adding constant.

---
# Inserting in the middle, step by step

```diagram
@dir TB
@reveal manual
T0[3, 8, 19, 27 | add 14]
focus T0
--- The sorted set holds four values in its four slots: the array is full.
T0 -> G[3, 8, 19, 27, _, _, _, _] : ensureRoom
focus T0 G
--- Array full: ensureRoom doubles 4 → 8 slots, copying 3, 8, 19, 27.
G -> T1[3, 8, 19, 27, 27, _, _, _] : 27 > 14, shift right
focus G T1
--- Shift 1: 27 moves from slot 3 to slot 4.
T1 -> T2[3, 8, 19, 19, 27, _, _, _] : 19 > 14, shift right
focus T1 T2
--- Shift 2: 19 moves from slot 2 to slot 3.
T2 -> T3[3, 8, 14, 19, 27, _, _, _].green : 8 < 14, stop
focus T2 T3
--- 8 is smaller, so 14 goes into slot 2.
```

> These are the array states printed by an instrumented copy of add on the running example. After four adds the starting array of four slots is full, so add first calls ensureRoom, which copies three, eight, nineteen and twenty-seven into a new array of eight slots. Then the loop starts at the last value and moves each bigger value one slot right, so the old value appears twice for a moment until the next step overwrites it. Two values were bigger than fourteen, so two shifts. Both sets grow first; the unsorted set then simply puts fourteen in slot four, and only the sorted set shifts.

---
# What an array costs

| Operation on an array of $n$ values | Steps |
|---|---|
| Read or write `a[i]` | constant |
| Search an unsorted array, worst case | $n$ comparisons |
| Search a sorted array, worst case | about $\log_2 n$ probes |
| Insert at index `i`, keeping order | $n - i$ shifts |
| Insert at the end, when there is room | constant |

> Read this table as the reason other data structures exist. Arrays are unbeatable for jumping to a known position. Insertion near the front is their weakness: inserting at index zero shifts every value. Linked lists, the topic of lecture six, make insertion at a known place cheap but give up constant-time indexing. That trade-off between fast access and fast change comes back again and again.

---
# Quiz: where does the work go?

```quiz
A sorted array holds 1000 values. Which insertion needs the most shifts?
- [ ] A value larger than every stored value.
- [x] A value smaller than every stored value.
- [ ] A value between the two middle values.
- [ ] They all cost the same.
```

> A value smaller than everything belongs in slot zero, so all one thousand stored values must move one slot to the right. A value larger than everything lands at the end with no shifts. A value between the two middle values shifts exactly the upper half, five hundred of them. The cost of an operation can depend heavily on which input it gets, and the next lecture gives that idea names: best case, worst case and average case.

---
@type section
# The Java toolkit

---
# Classes and interfaces

- An **interface** lists methods a type promises: `IntSet`.
- A **class** provides fields and method bodies: `SortedIntSet`.
- `implements` connects them; `@Override` marks a promised method.
- Client code can declare `IntSet s = new SortedIntSet();`

> This is the pattern the whole course uses. The interface is the abstract data type. The class is a data structure. The declaration on the last line is the key habit: the variable has the interface type, so the rest of the program depends only on the promise. Changing to new UnsortedIntSet is a one-word edit, and nothing else in the program needs to change. The @Override annotation asks the compiler to check that the method really matches one it promised.

---
# Inheritance

```java
public abstract class ArrayIntSet implements IntSet {
// ...
public class UnsortedIntSet extends ArrayIntSet {
// ...
public class SortedIntSet extends ArrayIntSet {
```

- `extends` reuses fields and methods from a parent class.
- `abstract`: the parent cannot be created on its own.
- The subclasses fill in `add` and `contains` their own way.

> Inheritance lets both sets share the array, the count, the size method and ensureRoom, written once. The parent is abstract because a set that stores values but cannot add or search them would be meaningless; Java will not let you write new ArrayIntSet. Each subclass extends it and supplies the two methods where the structures really differ. Protected fields are visible to the subclasses, which is why they can use data and size directly.

---
# Generics

```java
public static <T extends Comparable<T>> T maxOf(List<T> items) {
    if (items.isEmpty()) {
        throw new IllegalArgumentException("no items");
    }
    T best = items.get(0);
    for (T item : items) {
        if (item.compareTo(best) > 0) {
            best = item;
        }
    }
    return best;
}
```

> This is the MAX pseudocode from earlier, written once for many types. The type parameter T stands for any type whose values can be compared with compareTo, such as Integer or String. The compiler checks the types for us, so no casts are needed. Called on the running example it returns twenty-seven; called on the words pear, apple and fig it returns pear, the last in dictionary order. Notice the empty list is rejected explicitly, matching the algorithm's stated input.

---
# Recursion: base case and recursive case

```java
public static int sumTo(int n) {
    if (n <= 0) {
        return 0;                  // base case: no recursion
    }
    return n + sumTo(n - 1);       // recursive case: smaller input
}
```

- **Base case**: an input answered directly, with no further call.
- **Recursive case**: calls itself on a **smaller** input.
- Every chain of calls must reach a base case.

> A recursive method solves a problem by solving a smaller copy of the same problem. Two parts are always needed. The base case stops the chain: for n equal to zero, or any negative n, the sum is empty and so zero. Testing n less than or equal to zero, rather than n equal to zero, means a negative argument stops at once instead of recursing downward forever. The recursive case must make progress toward the base case: n minus one is smaller than n. Forget the base case, or call with an input that never shrinks, and the calls pile up until Java throws a StackOverflowError.

---
# Tracing sumTo(3)

```diagram
@dir TB
@reveal manual
C3[sumTo 3 | waits for sumTo 2]
focus C3
--- The first call cannot finish yet.
C3 -> C2[sumTo 2 | waits] -> C1[sumTo 1 | waits] -> C0[sumTo 0 | returns 0].green
focus C2 C1 C0
--- Calls stack up until the base case answers.
C0 -> R1[1 + 0 = 1] -> R2[2 + 1 = 3] -> R3[3 + 3 = 6].green
focus R1 R2 R3
--- Each waiting call finishes on the way back.
```

> This trace comes from an instrumented copy of the method, and the Java checks assert it line by line. At the deepest point four frames are on the stack, for three, two, one and zero, and three of them are waiting. The base case returns zero, and then each waiting call adds its own n and returns: one, then three, then six. Those waiting calls occupy memory, one frame each, which is why the next lecture counts recursion depth as part of an algorithm's space.

---
# Common mistakes

- Looping to `i <= a.length`: the last index is `a.length - 1`.
- A recursive method with no base case, or one that is never reached.
- Comparing objects with `==`: it compares references; use `equals` or `compareTo`.
- Raw types such as `List` instead of `List<Integer>`: the compiler can no longer check element types and only issues unchecked warnings.
- Forgetting the empty input: `maxOf` of an empty list has no answer.

> Each of these shows up in almost every data structures course. The first throws an exception on the last pass of the loop. The second overflows the stack. The third is subtle: two different Integer or String objects with the same value can compare unequal with double equals. The fourth turns the type errors that generics were added to catch into mere warnings, which are easy to ignore. The last is a reminder to decide, and document, what happens on the smallest inputs.

---
@type section
# Wrap-up

---
# Real systems are built from these parts

- A search engine keeps an **index**: a map from each word to the pages that contain it.
- A navigation app stores roads as a **graph** and searches it for short routes.
- An editor keeps an **undo stack**: the most recent change comes off first.
- Each is an ADT with a carefully chosen data structure behind it.

> Large programs are mostly collections of data structures working together. An index is a map, which we study through search trees and hash tables. Road networks are graphs, and finding routes is the shortest-path problem of lecture seventeen. Undo is a stack, the subject of lecture seven. In each case the designers chose the representation by asking which operations must be fast, exactly the question we asked about our two sets.

---
# Summary

- Data structure = representation + operations; the ADT is the promise, the structure keeps it.
- An algorithm is finite, definite, effective, with input and output.
- Correct first, then efficient; measure growth in $n$, not seconds.
- Sorted data allows halving: about $\log_2 n$ probes instead of $n$.
- Arrays index in constant time but shift to insert in the middle.

> If you remember one sentence, make it the first: the same abstract data type can be kept by different data structures, and the choice decides the cost of every operation. The efficiency game showed how large that difference can be, six probes against thirty-two on a small list and eleven against a thousand and twenty-four on a slightly larger one. The array showed the other side: fast access comes with slow insertion.

---
# Check yourself

- Write an interface for a "counter" ADT and two different classes that implement it.
- Why is "it ran in 2 seconds" not a good description of an algorithm's efficiency?
- Inserting `1` into the sorted set `3, 8, 14, 19, 27` needs how many shifts, and why?

> Try these without looking back. For the first, think of operations such as increment, reset and read, and two storage choices, perhaps a single number versus a list of events. For the second, name at least three things that change the seconds without changing the algorithm. For the third, count how many stored values are bigger than one, and check your answer against the shifting loop.

---
# Next time

- Make "how the work grows" precise: $O$, $\Omega$ and $\Theta$.
- Count operations in loops, nested loops and halving loops.
- Separate the bound from the case: best, worst, average.
- Visualizations: [Growth rates](../../../visualizations/algorithms/growth_rates.html) and [Efficiency comparison](../../../visualizations/algorithms/efficiency_comparison.html).

> Today we said that binary search grows like log n and linear search grows like n, without defining what that means. The next lecture gives the definitions, with constants and thresholds, and shows how to read the growth of a loop directly from its code. Before then, open the growth rates visualization and watch how quickly n squared and two to the n leave the others behind.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 2 (getting started), Chapter 3 (characterizing running times) and Chapter 10 (elementary data structures).
- D. E. Knuth, *The Art of Computer Programming*, Vol. 1, Section 1.1 (the five properties of an algorithm).
- Java SE API documentation: `java.util.Arrays`, `java.util.List`, `java.util.ArrayList`, `java.lang.Comparable`, and in `java.lang` the classes `ArrayIndexOutOfBoundsException` and `StackOverflowError`.
- All code, examples and counts on these slides were written and run for this deck.

> These slides follow the standard presentation of algorithms and data structures found in the textbook chapters listed here, and the five properties of an algorithm follow Knuth. The library facts about arrays, lists, comparison and the two exceptions come from the official Java API documentation. Every probe count, shift and trace in the deck comes from running the Java code written for it, not from a published table.
