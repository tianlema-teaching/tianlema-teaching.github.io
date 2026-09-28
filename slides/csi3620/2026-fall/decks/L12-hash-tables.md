@title Lecture 12: Hash tables
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Hash tables
## Compute where a key lives, then look there

---
# Where we are

- L08 to L11: search trees keep keys in order.
- A balanced tree finds any key in $O(\log n)$ time, worst case.
- Today: give up key order, gain expected $O(1)$ lookups.
- Next time: B-trees, search trees built for disks.

> Over the last four lectures we built search trees, and the balanced ones, AVL and red-black trees, guarantee logarithmic time for search, insert and delete. They also keep keys in sorted order, which lets them answer questions like "the smallest key above 50". Today we trade that order away. A hash table cannot answer ordered questions cheaply, but in exchange it finds a key in a constant number of steps on average. Next lecture returns to trees, this time trees designed for data on disk.

---
# By the end of today you can

- Compute a slot with the division method and with `Math.floorMod`.
- Trace inserts into a chained table and a linear-probing table.
- Explain why open addressing deletes with tombstones.
- State expected costs in terms of the load factor $\alpha$.
- Write `equals` and `hashCode` so that `HashMap` works.

> These are the things you should be able to do on paper by the end. Compute where a key goes. Trace a small table step by step with both collision strategies. Show with a concrete table why emptying a slot breaks a later search. State the expected cost formulas, including the assumption they rest on. And write a key class whose equals and hashCode agree, which is the most common source of real bugs with Java hash maps.

---
# The problem: look up by key, fast

- A campus system maps student IDs to records.
- Every login, every grade view, every door badge asks: who is this ID?
- A sorted array answers in $\Theta(\log n)$ comparisons, but inserts shift $\Theta(n)$ items.
- A balanced tree gives $\Theta(\log n)$ for both, worst case.
- Can a lookup take a constant number of steps, whatever $n$ is?

> Think of any system that stores records by an identifier and is asked about them constantly. Binary search on a sorted array finds a record quickly, but adding a new student means shifting everything after it. A balanced search tree fixes the insert cost, but each lookup still walks a path of logarithmic length. The question for today is whether we can do better on average: compute the location of a record directly from its key, and go straight there.

---
# The map abstract data type

| Operation | Meaning |
|---|---|
| `put(k, v)` | Store value `v` under key `k`; replace an old value |
| `get(k)` | Return the value stored under `k`, or `null` |
| `remove(k)` | Delete the entry for `k`; return its value or `null` |
| `containsKey(k)` | Is there an entry for `k`? |

Keys are distinct. A **set** is a map that stores keys only.

> A map, also called a dictionary or symbol table, stores key-value pairs with distinct keys. Putting a key that is already present replaces its value rather than adding a second entry. These four operations mirror the methods of java dot util dot Map, and our two implementations today use the same names and the same return conventions, which lets us test them against the library later. A set is the special case with no values; Java's HashSet is built exactly that way.

---
# Direct addressing, and why it fails

- Keys from $\{0, 1, \dots, u-1\}$: store key `k` at index `k` of an array of size $u$.
- `get`, `put` and `remove` each take $\Theta(1)$ time, worst case; empty slots hold `null`.
- The **universe** $U$ is the set of all possible keys; the array needs $|U|$ slots.
- Java `int` keys: $|U| = 2^{32}$; at 4 bytes per slot, 16 GiB. `String` keys: unbounded.
- Goal: space $\Theta(n)$ for $n$ stored keys, time close to direct addressing.

> The simplest possible map: if every key is a small nonnegative integer, allocate an array with one slot per possible key and store the value for key k at index k. Every operation is a single array access, constant time in the worst case. This is a direct-address table, the ideal that hashing imitates. The catch is that it needs one slot per possible key, not per stored key. With 32-bit integers that is over four billion slots, sixteen gibibytes before storing a single record, and with strings there is no finite array at all. We want memory proportional to what we store, with lookups close to one array access.

---
# One running example

- Keys: `12, 44, 13, 88, 23`, inserted in that order.
- Table size $m = 7$, slots `0` to `6`.
- Every trace today starts from these five keys.
- Values are not drawn; each key `k` maps to the string `"v" + k`.

```diagram
@dir LR
K12[12] -> K44[44] -> K13[13] -> K88[88] -> K23[23]
```

> Here is the example we will reuse all lecture. Five integer keys, inserted left to right, into a table with seven slots. Seven is small enough to draw and it is prime, which matters for the division method. Every diagram of a table today was produced by running the Java classes from this lecture on exactly these keys, so you can rerun them and compare. Copy the five keys down now.

---
@type section
# Hash functions

---
# Hash functions and collisions

- A **hash function** maps keys to slots: $h : U \to \{0, 1, \dots, m-1\}$.
- It must be deterministic: the same key always gives the same slot.
- It should be fast and spread keys evenly over the slots.
- A **collision**: two different keys with the same slot.
- When $|U| > m$, collisions are unavoidable (pigeonhole principle).

> A hash function turns a key into a slot number. Determinism is not optional: if put and get computed different slots for the same key, get would look in the wrong place. Speed matters because we hash on every operation. Spreading matters because crowded slots are slow. And since there are more possible keys than slots, some pair of keys must share a slot. So every hash table needs two parts: a hash function, and a plan for collisions.

---
# The division method

$$h(k) = k \bmod m$$

| Key | 12 | 44 | 13 | 88 | 23 |
|---|---|---|---|---|---|
| $k \bmod 7$ | 5 | 2 | 6 | 4 | 2 |

- Keys `44` and `23` collide in slot `2`.

> The division method takes the remainder after dividing by the table size. Check the row: twelve mod seven is five, forty-four is six sevens plus two, thirteen gives six, eighty-eight gives four, and twenty-three is three sevens plus two. So forty-four and twenty-three collide in slot two, and we will see how each strategy handles that. The formula assumes a nonnegative key; negative values need care, which we fix later with floorMod.

---
# Choosing the table size m

- With $m = 2^p$, $k \bmod m$ keeps only the low $p$ bits of $k$.
- Keys that differ only in high bits then all collide.
- CLRS suggests a prime $m$ not too close to a power of 2.
- The rest of the key then influences the slot.
- Alternative, the **multiplication method**: $h(k) = \lfloor m \, (kA \bmod 1) \rfloor$ with $0 < A < 1$; any $m$ works, powers of 2 included.

> If m is a power of two, the remainder is just the last few binary digits. Imagine keys that are all multiples of sixteen: with m equal to sixteen, every one lands in slot zero. A prime table size makes the remainder depend on all the bits of the key, so input patterns are less likely to line up with the table. The multiplication method avoids the issue differently: multiply by a constant between zero and one, keep the fractional part, and scale it to the table size. Knuth suggests A equal to root five minus one over two, about 0.618. We use division for the traces. Java's HashMap uses powers of two anyway, and mixes the high bits into the low bits first, as we will see.

---
# Hashing a string

$$\sum_{i=0}^{n-1} s[i] \cdot 31^{\,n-1-i}$$

- That is $s[0] \cdot 31^{n-1} + \dots + s[n-1]$: `String.hashCode()` as the API documents it, in `int` arithmetic.
- `"cat"`: $99 \cdot 31^2 + 97 \cdot 31 + 116 = 98262$.
- The empty string hashes to `0`.
- Long strings overflow and wrap around, so results can be negative.

> A string is a sequence of characters, and each character is a small integer, its UTF-16 code unit. The Java documentation specifies the hash as a polynomial in thirty-one with the characters as coefficients. For "cat", the character codes are ninety-nine, ninety-seven and one hundred sixteen, and the sum is ninety-eight thousand two hundred sixty-two. Because every character is multiplied by a different power, "cat" and "act" hash differently. The arithmetic is thirty-two-bit, so long strings overflow, and the result can be any int, including negative ones.

---
# The same formula by Horner's rule

```java
public static int stringHash(String s) {
    int h = 0;
    for (int i = 0; i < s.length(); i++) {
        h = 31 * h + s.charAt(i);         // Horner's rule
    }
    return h;
}
```

- One multiply and one add per character: $\Theta(n)$ for length $n$.
- `Checks.java` confirms it equals `s.hashCode()` on several strings.

> Computing each power of thirty-one separately would be wasteful. Horner's rule factors the polynomial: multiply what you have by thirty-one, then add the next character. After the loop, the first character has been multiplied by thirty-one n minus one times, exactly as in the formula. Look at the single line in the loop body; overflow wraps silently in Java int arithmetic, which is exactly what String hashCode does. The check file compares this method with the library on several strings, including one whose hash is the smallest int.

---
@type section
# hashCode and equals

---
# The equals and hashCode contract

- A Java hash map calls `hashCode()` to pick a slot, then `equals()` to find the entry in it.
- If `a.equals(b)`, then `a.hashCode() == b.hashCode()`. Required.
- If `a.hashCode() == b.hashCode()`, the keys may still differ. That is a collision.
- `hashCode` must stay the same while the fields used by `equals` do not change.
- So: compute `hashCode` from exactly the fields that `equals` compares.

> Every Java hash map works in two phases. First it calls hashCode on the key and compresses the result to a slot, which rules out everything in other slots. Then, among the few entries in that slot, it calls equals to find the matching key. The rules for the two methods are written in the documentation of java dot lang dot Object. Equal objects must have equal hash codes, because otherwise two equal keys could be sent to different slots and the second phase would never compare them. The converse is not required: different keys may share a hash code, and the table handles that like any collision. The practical rule is simple. Whatever fields your equals compares, feed exactly those fields into hashCode.

---
# A key class that keeps the contract

```java
@Override
public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (!(o instanceof Point)) {
        return false;
    }
    Point p = (Point) o;
    return x == p.x && y == p.y;
}
@Override
public int hashCode() {
    return Objects.hash(x, y);            // uses the same fields as equals
}
```

> Look at the last line of equals and the line inside hashCode: both use x and y, and nothing else. Objects dot hash combines the fields into one int, so two points with the same coordinates always produce the same hash code. The fields are final, so the hash code cannot change after the point goes into a map. With this class, a HashSet containing point one-two reports that it contains a new point one-two, and the check file verifies that for both HashSet and our own map.

---
# What breaks when hashCode is missing

```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof BrokenPoint)) {
        return false;
    }
    BrokenPoint p = (BrokenPoint) o;
    return x == p.x && y == p.y;
}
// hashCode() is inherited from Object:
// equal points usually get different hash codes
```

- `set.add(new BrokenPoint(1, 2))`, then `set.contains(new BrokenPoint(1, 2))`: usually `false`.
- `javac -Xlint:all` warns: overrides equals but not hashCode.

> This class overrides equals but forgets hashCode, so it inherits the identity-based hash code from Object, which usually differs for two separately created objects. The two points are equal, yet the set looks in the wrong slot and answers false. Say "usually", because two identity hash codes can coincide by chance, which makes the bug intermittent and even harder to find. The check file runs two hundred such pairs and confirms that whenever the hash codes differ, HashSet misses the equal key. The compiler's lint option catches this mistake for you. The lecture's BrokenPoint.java carries @SuppressWarnings("overrides") on purpose, because it exists to show the bug; delete that line and recompile with -Xlint:all to see the warning.

---
# From hashCode to a slot index

- `hashCode()` returns any `int`, including negatives.
- `"HashMap".hashCode()` is `-1932803762`.
- `-1932803762 % 16` is `-2`: an invalid index.
- `Math.abs(h) % m` fails only when `h` is `Integer.MIN_VALUE` and $m$ is not a power of 2.
- `Math.floorMod(h, m)` is always in `0..m-1`: here `14`.
- If $m$ is a power of 2, `h & (m - 1)` also gives `14`.

> Java's percent operator keeps the sign of the dividend, so a negative hash code gives a negative remainder and an ArrayIndexOutOfBoundsException. Taking the absolute value first almost fixes it, but the absolute value of the smallest int overflows back to itself, and the string "polygenelubricants" really does hash to that value. That negative number is divisible by every power of two, so the remainder happens to be zero then, and negative for any other m. Math dot floorMod returns a result with the sign of the divisor, which is what we want. When the table size is a power of two, a bitwise AND with m minus one keeps the low bits and gives the same answer.

---
# Quiz: a safe slot index

```quiz
A table has m = 7 slots and h is any int hash code. Which expression always gives a valid index?
- [ ] `h % 7`
- [ ] `Math.abs(h) % 7`
- [x] `Math.floorMod(h, 7)`
- [ ] `Math.abs(h % 8)`
```

> The answer is floorMod. The plain remainder is negative for negative h. Math dot abs of h fails for the smallest int, whose absolute value is still negative: Math dot abs of Integer dot MIN_VALUE, mod seven, is minus two. The last option is never negative, but it ranges from zero to seven, and seven is one past the last slot; for h equal to fifteen it gives seven. floorMod with a positive divisor always returns zero through six. The check file asserts each of these values.

---
@type section
# Separate chaining

---
# Separate chaining

- Each slot holds a linked list, a **chain**, of the entries that hash there.
- `put`: search the chain; replace the value, or add a new node at the head.
- `get` and `remove`: search only the chain of slot $h(k)$.
- Chains can grow beyond one entry, so $n$ may exceed $m$.

```algorithm
function CHAIN-GET(T, k):
  for each node e in list T[h(k)] do
    if e.key = k then return e.value
  return null
```

> Separate chaining resolves collisions by letting a slot hold more than one entry. Each slot is the head of a singly linked list, the same structure from L06. To put a key, walk its chain; if the key is there, replace the value, otherwise link a new node at the front, which is constant time. Get and remove walk only one chain, never the whole table. Inserting at the head follows CLRS; in current OpenJDK, HashMap appends at the tail, and either is fine because we must search the chain first anyway.

---
# Chaining trace: the first three puts

```diagram
@dir TB
@reveal manual
C0[empty table | m = 7]
focus C0
--- Seven empty chains.
C0 -> C1[slot 5: 12] : put 12, 12 mod 7 = 5
focus C0 C1
--- 12 starts the chain of slot 5.
C1 -> C2[slot 2: 44 | slot 5: 12] : put 44, 44 mod 7 = 2
focus C1 C2
--- 44 goes to slot 2.
C2 -> C3[slot 2: 44 | slot 5: 12 | slot 6: 13] : put 13, 13 mod 7 = 6
focus C2 C3
--- 13 goes to slot 6. No collisions yet.
```

> Only nonempty slots are drawn in each box. Twelve mod seven is five, so twelve heads the chain of slot five. Forty-four mod seven is two. Thirteen mod seven is six. Each put first searched a chain that turned out to be empty, then linked a single node. So far every chain has length at most one, which is the best case: every lookup reads at most one node.

---
# Chaining trace: 88, then a collision

```diagram
@dir TB
@reveal manual
D0[slot 2: 44 | slot 5: 12 | slot 6: 13]
focus D0
--- Three keys stored.
D0 -> D1[slot 2: 44 | slot 4: 88 | slot 5: 12 | slot 6: 13] : put 88, 88 mod 7 = 4
focus D0 D1
--- 88 goes to the empty slot 4.
D1 -> D2[slot 2: 23 -> 44 | slot 4: 88 | slot 5: 12 | slot 6: 13] : put 23, 23 mod 7 = 2
focus D1 D2
--- 23 collides with 44 and becomes the new head of chain 2.
```

> Eighty-eight lands in slot four with no conflict. Twenty-three hashes to slot two, which already holds forty-four. The put walks that chain, finds no twenty-three, and links a new node at the head, so the chain reads twenty-three then forty-four. A search for forty-four now reads two nodes. A search for thirty, which also hashes to slot two, reads both nodes and then reports not found. Five keys in seven slots: the load factor is five sevenths.

---
# Chaining in Java: get

```java
private int indexFor(Object key, int m) {
    return Math.floorMod(key.hashCode(), m);   // always in 0..m-1
}
public V get(Object key) {
    for (Node<K, V> e = table[indexFor(key, table.length)]; e != null; e = e.next) {
        if (e.key.equals(key)) {
            return e.value;
        }
    }
    return null;
}
```

- `table` is an array of chain heads: `Node<K, V>[]`.
- `remove` walks the same chain with a trailing `prev` pointer and unlinks the match, as in L06.

> The helper compresses the hash code with floorMod, so negative hash codes are safe. The get loop is an ordinary linked-list walk starting at the head of one slot. Notice the equals call: that is the second phase from the contract slide. Get returns null for a missing key; that is also what it returns for a key stored with a null value, and containsKey tells the two apart, as in java dot util dot HashMap. Our class rejects null keys to keep the code short. Remove is linked-list deletion with a trailing pointer, as in L06: if the match is the head, the slot skips it, otherwise the previous node does. Deletion in chaining is immediate, with no leftover marks; open addressing will not be so easy.

---
# Chaining in Java: put

```java
public V put(K key, V value) {
    // ...
    int i = indexFor(key, table.length);
    for (Node<K, V> e = table[i]; e != null; e = e.next) {
        if (e.key.equals(key)) {          // key present: replace the value
            // ...
        }
    }
    table[i] = new Node<>(key, value, table[i]);   // new key: insert at the head
    size++;
    if (size > maxLoad * table.length) {
        resize(2 * table.length);         // double: amortized O(1) per put
    }
    return null;
}
```

> Two parts are left out here, each marked by a comment with three dots: the first lines, which reject a null key, and, inside the loop, the lines that swap in the new value and return the old one. Focus on the line that creates the node: the new node's next field is the old head, and the slot now points at the new node. That is insertion at the head of a linked list in one line. After counting the new key, put checks the load factor and doubles the table when it gets too full. We come back to resizing later today.

---
# Load factor and the cost of chaining

$$\alpha = \frac{n}{m} \quad \text{(average chain length)}$$

- **Simple uniform hashing**: each key is equally likely to hash to any slot, independently of other keys (CLRS 4th ed. calls this independent uniform hashing).
- Under it, expected search time is $\Theta(1 + \alpha)$, found or not.
- If $m$ grows with $n$ so that $\alpha = O(1)$, expected time is $O(1)$.
- Worst case: all $n$ keys in one chain, $\Theta(n)$ per search.
- Space: $\Theta(m + n)$.

> The load factor alpha is the number of stored keys divided by the number of slots, which is also the average chain length. Simple uniform hashing is an assumption about the hash function and the keys together: it says keys scatter as if at random. Under that assumption, CLRS shows a search costs constant time to hash plus the expected alpha nodes of one chain when the key is absent, and about one plus alpha over two nodes when it is present. Both are theta of one plus alpha. The worst case is still linear, because an unlucky or adversarial key set can land in one slot. The guarantee is about expectation, not every input.

---
@type section
# Open addressing

---
# Open addressing: keys live in the array

- No chains: every key sits in one slot of the array itself.
- On a collision, **probe** other slots in a fixed order.
- The **probe sequence** of key $k$: $h(k, 0), h(k, 1), \dots, h(k, m-1)$.
- Each slot holds at most one key, so $\alpha = n/m \le 1$.
- Saves the node objects and the pointers of chaining.

> Open addressing stores all keys directly in the table array. When a key's first choice is occupied, it tries a second slot, then a third, following a sequence determined by the key. Search follows the same sequence, so it finds the key where insert put it. There are no nodes and no next pointers, which saves memory and keeps data together in one array. The price is that the table can fill up: at most one key per slot means the load factor can never exceed one, and in practice we keep it well below.

---
# Linear probing

$$h(k, i) = (h(k) + i) \bmod m$$

```algorithm
function PROBE-INSERT(T, k):     // assumes k is absent and a slot is free
  i ← h(k)
  while T[i] is occupied do
    i ← (i + 1) mod m            // next slot, wrapping around
  T[i] ← k
function PROBE-SEARCH(T, k):
  i ← h(k)
  for j ← 1 to m do              // at most m probes
    if T[i] is empty then return not found
    if T[i] = k then return i
    i ← (i + 1) mod m
  return not found
```

> Linear probing is the simplest probe sequence: start at the home slot, and step one slot to the right each time, wrapping from the last slot back to zero. Insert walks until it finds a free slot. Search walks the same path and stops at the first empty slot, because if the key had been inserted, insert would have stopped there at the latest. If the table has no empty slot at all, search gives up after m probes, having seen every slot. That stopping rule is the reason deletion will need special care. This pseudocode leaves out deletion; we add it in the next section.

---
# Linear probing trace: five puts

```diagram
@dir TB
@reveal manual
L0[_, _, _, _, _, 12, _]
focus L0
--- 12 goes home to slot 5.
L0 -> L1[_, _, 44, _, _, 12, _] : put 44, home 2
focus L0 L1
--- 44 goes home to slot 2.
L1 -> L2[_, _, 44, _, 88, 12, 13] : put 13, then 88
focus L1 L2
--- 13 goes to slot 6 and 88 to slot 4, both at home.
L2 -> L3[_, _, 44, 23, 88, 12, 13] : put 23, home 2 is taken
focus L2 L3
--- 23 probes slot 2, finds 44, and moves to slot 3.
```

> Each box is the whole table, slots zero to six from left to right, with an underscore for an empty slot. The first four keys all find their home slot free. Twenty-three's home is slot two, which holds forty-four, so it probes slot three, finds it empty and stops. A later search for twenty-three retraces exactly that path: slot two, not a match, slot three, found, two probes in all. The keys now occupy one unbroken run from slot two to slot six.

---
# Primary clustering

```diagram
@dir LR
@reveal manual
P2[2: 44] -> P3[3: 23] -> P4[4: 88] -> P5[5: 12] -> P6[6: 13] -> P0[0: empty]
focus P2
--- Search for 51: 51 mod 7 = 2, so start at slot 2.
focus P2 P3 P4 P5 P6
--- Every slot of the run is occupied and none holds 51.
focus P0
--- Wrap around to slot 0: empty. Six probes to say not found.
```

- A **cluster** is a run of occupied slots.
- Any key hashing into a cluster lands at its end and lengthens it.

> Search for fifty-one, which is not in the table. Its home is slot two, and it must walk the whole run, wrapping to slot zero, before it meets an empty slot: six probes for five keys. That run is called a cluster. Clusters feed themselves: a new key whose home falls anywhere inside the run lands just past its end, making the run longer. So long runs grow faster than short ones. This effect, primary clustering, is the main weakness of linear probing, and it gets severe as the table fills.

---
# Other probe sequences

- **Quadratic probing**: $h(k, i) = (h'(k) + c_1 i + c_2 i^2) \bmod m$.
- Jumps grow, so clusters do not merge as fast.
- Keys with the same home still share a sequence: *secondary clustering*.
- **Double hashing**: $h(k, i) = (h_1(k) + i \cdot h_2(k)) \bmod m$.
- $h_2(k)$ must be relatively prime to $m$ so every slot is reachable.

> Two alternatives reduce clustering. Quadratic probing makes the step size grow with each probe, so a key that collides jumps away from the crowd. Its constants and table size must be chosen carefully so that the sequence can reach enough slots. Keys with the same home slot still follow identical sequences, a milder problem called secondary clustering. Double hashing uses a second hash function for the step size, so two keys with the same home usually step differently. If m is prime, any step from one to m minus one visits every slot. Linear probing remains popular in practice because consecutive slots are cache friendly.

---
# Expected probes in open addressing

- **Uniform hashing**: each key's probe sequence is equally likely to be any of the $m!$ orders of the slots (CLRS 4th ed.: independent uniform permutation hashing).
- Under it, with $\alpha < 1$ and no deletions, an unsuccessful search takes at most $\frac{1}{1-\alpha}$ expected probes (CLRS).
- An insertion costs at most the same, on average.
- Linear probing has only $m$ distinct sequences, so it falls short of this assumption.

> This is a stronger assumption than simple uniform hashing: it is about the entire probe sequence, not just the first slot. Under it, CLRS proves that an unsuccessful search expects at most one over one minus alpha probes. The intuition: the first probe hits an occupied slot with probability alpha, the second with probability about alpha again, and so on, a geometric series. Insertion is an unsuccessful search followed by one write. Linear probing cannot meet the assumption, because the home slot determines the whole sequence; clustering makes it slower than this bound as alpha grows. With tombstones, count them in the load.

---
# The bound grows fast near a full table

```chart
type: line
title: Upper bound on expected probes, unsuccessful search
x: 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9
ylabel: probes
ymin: 0
ymax: 10
1 over 1 minus alpha: 1.11, 1.25, 1.43, 1.67, 2, 2.5, 3.33, 5, 10
```

At $\alpha = 0.5$ the bound is 2 probes; at $\alpha = 0.9$ it is 10.

> The horizontal axis is the load factor and the vertical axis is the bound. Up to about one half, the bound stays at two probes or fewer. Past three quarters it climbs steeply, and at ninety percent full it reaches ten. This is why open-addressing tables resize long before they are full; our linear-probing class keeps the load at most one half by default. Remember this is a bound under uniform hashing; linear probing does worse at the same load.

---
# Quiz: linear probing

```quiz
Use our table [_, _, 44, 23, 88, 12, 13] with m = 7. How many slots does a search for 30 probe? (30 mod 7 = 2)
- [ ] 1
- [ ] 2
- [ ] 5
- [x] 6
```

> The answer is six. The search starts at slot two and must continue while slots are occupied: two, three, four, five and six all hold keys other than thirty. It wraps to slot zero, which is empty, and stops there. So six slots are examined, the same as for fifty-one, because both keys share home slot two. Any key whose home lies inside the cluster pays for the length of the cluster. A key with home slot zero or one would need a single probe.

---
@type section
# Deletion and resizing

---
# Why simply emptying a slot fails

```diagram
@dir TB
@reveal manual
N0[_, _, 44, 23, 88, 12, 13]
focus N0
--- 23 sits in slot 3 because slot 2 was taken.
N0 -> N1[_, _, _, 23, 88, 12, 13] : remove 44 by emptying slot 2
focus N0 N1
--- The slot that pushed 23 onward is now empty.
N1 -> N2[search 23: slot 2 is empty, stop | answer: not found] : wrong
focus N1 N2
--- The search stops at the hole before it reaches 23.
```

> Search stops at the first empty slot. That rule was correct because insert never skips an empty slot. Emptying slot two breaks the reasoning: twenty-three was placed in slot three precisely because slot two was occupied at the time. Now a search for twenty-three looks at slot two, sees empty, and concludes the key is absent, though it is still in the table. The class NaiveDeleteTable in the Java folder does exactly this, and the check file confirms that it loses key twenty-three.

---
# Tombstones

```diagram
@dir TB
@reveal manual
T0[_, _, X, 23, 88, 12, 13]
focus T0
--- A tombstone X replaces 44.
T0 -> T1[search 23: X, keep going | slot 3: found] : 2 probes
focus T0 T1
--- Search treats X as occupied and continues.
```

- Search: a tombstone never matches, but the search continues past it.
- Insert: may reuse the first tombstone on its path, after checking the key is absent.
- Tombstones count toward the load; a rebuild removes them all.

> A tombstone is a special marker that means "a key used to be here". Search treats it as occupied, so it keeps probing and finds twenty-three in slot three, as before the deletion. Insert may reuse a tombstone slot, but only after it has searched to an empty slot and confirmed the key is not stored further along; otherwise a key could appear twice. The catch: tombstones make searches longer and never disappear on their own. After many deletions the table can be full of tombstones, so implementations count them and rebuild the table.

---
# Linear probing in Java: find

```java
private int find(Object key) {
    int m = keys.length;
    for (int j = 0, i = home(key); j < m; j++, i = (i + 1) % m) {
        if (keys[i] == null) {
            return -1;                    // a never-used slot ends the search
        }
        if (keys[i] != TOMBSTONE && keys[i].equals(key)) {
            return i;
        }
    }
    return -1;                            // probed every slot
}
```

- `keys` is an `Object[]`: `null`, `TOMBSTONE`, or a key.
- `remove(k)` calls `find(k)`, then sets `keys[i] = TOMBSTONE`, never `null`.

> The array keys holds one of three things per slot: null for a slot never used, a single shared TOMBSTONE object for a removed slot, or a real key. Look at the two if statements. Only null stops the search early; a tombstone falls through both tests and the loop moves on. The counter j bounds the loop at m probes, so the search ends even if there were no null slot at all. The check against TOMBSTONE uses reference comparison on purpose: it is one specific object. Remove reuses find, so it follows the same probe path, and writes the tombstone instead of null. It lowers the size and raises a tombstone counter, and put uses the sum of the two to decide when to rebuild.

---
# Quiz: tombstones

```quiz
Why does a search continue past a tombstone instead of stopping there?
- [ ] A tombstone always holds the key being searched for.
- [x] A key further along may have been placed there while the slot was occupied.
- [ ] Stopping would make the search take more probes.
- [ ] Tombstones are only used by chaining.
```

> The second option is correct. When the key further along was inserted, the tombstone's slot held a live key, which pushed the new key onward. The search must retrace that same path, so it cannot stop where the path used to be blocked. A tombstone never holds a live key, so the first option is wrong. Continuing costs probes; it does not save them. And chaining does not need tombstones at all, because unlinking a list node does not break any other key's path.

---
# Resize and rehash

- When $\alpha$ passes a threshold, allocate a larger table, usually twice the size.
- Reinsert every key: its slot depends on $m$, so it may move.
- Our chaining map: after the sixth key, $6/7 > 0.75$, so $m$ becomes 14.
- `44 → 2`, `88 → 4`, `23 → 9`, `51 → 9`, `12 → 12`, `13 → 13`.
- Our `resize` walks every old chain and inserts each entry at the head of its new chain.
- A probing rebuild also drops every tombstone.

> A table has a fixed number of slots, but the number of keys keeps growing, and our costs depend on alpha. So when alpha passes a limit, we build a table with twice as many slots and insert every key again. You cannot copy the old array over, because each key's slot is its hash modulo m, and m changed. In our running example, adding fifty-one makes six keys in seven slots, above three quarters, so the table grows to fourteen. Recompute a few: twenty-three mod fourteen is nine, and fifty-one mod fourteen is also nine, so they now share a chain. Our resize walks the old chains and links each entry into the new table with the same insert-at-head step as put; it needs no duplicate search, because the old table had none.

---
# Why doubling gives amortized O(1)

- One `put` that triggers a resize costs $\Theta(n)$.
- With doubling, resizes happen at sizes about $n, n/2, n/4, \dots$
- Total rehash work over $n$ puts: less than $n + n/2 + n/4 + \dots < 2n$.
- So $n$ puts cost $O(n)$ in total: **amortized** $O(1)$ each, expected.
- Growing by a fixed amount instead, say 10 slots, gives $\Theta(n^2)$ total.

> Amortized analysis, which CLRS treats in Chapter 16, spreads the cost of rare expensive operations over many cheap ones. The last resize before we reach n keys moved about n keys, the one before moved about half as many, and so on. The geometric series is less than 2n, so rehashing adds a constant per put on average over the sequence. Each ordinary put is expected constant time under the hashing assumption, so the combined claim is expected amortized constant time. Adding ten slots at a time would resize every few puts, and the total becomes quadratic.

---
@type section
# Hash tables in Java

---
# java.util.HashMap: API facts

- `new HashMap<>()`: default initial capacity 16, default load factor 0.75.
- Past capacity times load factor, it rehashes to about twice the buckets.
- No guarantee about iteration order; it may change over time.
- Permits one `null` key and `null` values.
- `HashSet` is backed by a `HashMap` instance.
- Need an order? `LinkedHashMap` keeps insertion order; `TreeMap` keeps keys sorted.

> These facts are stated in the Java SE API documentation, so you can rely on them. The defaults are sixteen buckets and a three-quarter load factor, the same threshold our chaining class uses. The documentation explicitly refuses to promise any iteration order, so code that prints a HashMap and expects a particular order is wrong even if it happens to work today. HashSet stores its elements as keys of an internal HashMap. When you do need an order, choose the class that promises one: LinkedHashMap for insertion order, TreeMap for sorted keys.

---
# Inside HashMap, in current OpenJDK

- Table sizes are powers of two; the index is `hash & (table.length - 1)`.
- `hash` mixes the high bits in first: `h ^ (h >>> 16)`.
- A bin with many collisions becomes a balanced tree: a red-black tree, as in L11.
- In current OpenJDK: when a bin grows past 8 entries and the table has at least 64 bins.
- Worst case per bin: $O(\log n)$ instead of $\Theta(n)$ when keys are `Comparable`.
- These are implementation details, not API guarantees.

> These points come from the OpenJDK source, not from the specification, so they can change between releases. Power-of-two sizes make the index a fast bitwise AND, and the shift-and-xor step mixes high bits into the low bits that the mask keeps, which answers the earlier worry about power-of-two division. Since Java 8, a bin that collects many entries is converted into a red-black tree, the structure from Lecture 11. Smaller tables resize instead of converting. The tree bins give logarithmic search within a bin, which helps most when keys are comparable.

---
# Hash table or balanced search tree?

| Question | `HashMap` | `TreeMap` |
|---|---|---|
| `get`, `put`, `remove` | $O(1)$ expected | $O(\log n)$ worst case |
| Worst case per operation | $\Theta(n)$ in our classes | $O(\log n)$ |
| Keys in sorted order | No | Yes |
| Smallest key above `x` | Scan everything | $O(\log n)$ |
| Key needs | `equals`, `hashCode` | `compareTo` or a `Comparator` |

> Neither structure wins everywhere. A hash table is the default choice for plain lookups by exact key, because its expected cost does not grow with n. A balanced tree gives guaranteed logarithmic time and answers ordered questions: minimum, maximum, next larger key, all keys in a range. The middle row says "in our classes" because the library HashMap limits bad bins with trees, as on the previous slide. Choose by the questions your program asks.

---
# Common mistakes and edge cases

- Overriding `equals` without `hashCode`, or hashing different fields.
- Mutating a key after inserting it.
- `h % m` or `Math.abs(h) % m` with a negative hash code.
- Emptying a slot on removal in open addressing.
- Expecting `HashMap` iteration in insertion or sorted order.
- Treating `get(k) == null` as "absent" when `null` values are allowed.

> Here is the list to check your own code against. The first two are contract violations and cause lookups to miss keys that are present; a key mutated after insertion stays in the slot of its old hash code, so prefer immutable keys such as String, Integer or records. The third throws an index exception, often only for rare inputs, which makes it slip through testing. The fourth loses keys after deletion. The fifth works by accident until the table resizes. The last one: if a map can store null values, get returns null both for a missing key and for a key mapped to null, so use containsKey when the difference matters.

---
@type section
# Wrap-up

---
# Summary

- A hash function maps keys to slots; collisions are unavoidable.
- Chaining keeps lists per slot: $\Theta(1 + \alpha)$ expected search.
- Open addressing probes the array: at most $1/(1-\alpha)$ expected probes, unsuccessful (uniform hashing).
- Deletion in open addressing needs tombstones.
- Doubling on resize keeps `put` at amortized $O(1)$ expected.
- Worst case stays $\Theta(n)$; the fast costs are expectations under assumptions.

> Hashing computes a location instead of searching for one. Every table needs a collision strategy, and we saw the two families. Their costs depend on the load factor, which resizing keeps bounded. The constant-time claims are expected values under assumptions about how keys spread; they are not worst-case guarantees. For keys with a meaningful order, or when a worst-case bound matters, the balanced trees from Lectures 10 and 11 remain the right tool.

---
# Check yourself

- Insert `5, 12, 19` into linear probing with $m = 7$. Where does each land, and why?
- Why may chaining run with $\alpha > 1$ but open addressing may not?
- Two keys have equal hash codes but are not equal. Is that a bug? Explain.

> Work these without the slides. The first has three keys with the same home slot, so watch the cluster form. The second asks you to connect the load factor to what a slot can hold in each strategy. The third tests whether you remember which direction of the contract is required. Next time: B-trees, where the cost that matters is how many blocks we read from disk, and the tree is built wide and shallow to keep that number small.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 11 (hash tables) and Chapter 16 (amortized analysis).
- Knuth, *The Art of Computer Programming*, Vol. 3, *Sorting and Searching*, Section 6.4 (hashing).
- Java SE API documentation: `java.lang.Object` (`equals`, `hashCode`), `java.lang.String` (`hashCode`), `java.util.HashMap`, `java.util.HashSet`, `Math.floorMod`.
- OpenJDK source code of `java.util.HashMap` (for the details marked "in current OpenJDK").

> The definitions, the division and multiplication methods, the cost theorems and the amortized argument follow CLRS. The constant for the multiplication method is Knuth's suggestion. Library behavior is taken from the Java SE API documentation; statements about tree bins and bit mixing describe the current OpenJDK implementation and are labeled as such on the slides. All traces were generated by the lecture's own Java code, and its check file also compares both map classes with java dot util dot HashMap on fifty thousand random operations per seed.
