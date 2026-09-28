@title Lecture 2: Time and space complexity
@reveal keep
@align left
@theme light
@lang en-US
@katex ../../../katex/

# Time and space complexity
## Counting steps, and naming how they grow

---
# Where we are

- Lecture 1: a data structure is a representation plus operations.
- We saw linear search need $n$ probes and binary search about $\log_2 n$.
- Today: make "grows like $n$" and "grows like $\log n$" precise.
- Next: linear and binary search, analysed with today's tools.

> Last time we compared two ways of storing a set and played the efficiency game on thirty-two sorted numbers. We described the results informally, saying one method grows like n and the other like log n. Today we define exactly what that means, learn to read growth directly from code, and separate two questions that are often blurred together: which inputs we are talking about, and how we bound the work on them.

---
# By the end of today you can

- Count the primitive operations of a short method as a formula in $n$.
- State the definitions of $O$, $\Omega$ and $\Theta$ with constants $c$ and $n_0$.
- Prove a bound such as $3n^2+5n+2=\Theta(n^2)$ with explicit constants.
- Keep bounds and cases apart: best, worst and average are separate questions.
- Read the growth of single, nested, dependent and halving loops.
- Separate auxiliary space from total space, including the recursion stack.

> Each outcome is something you should be able to do on paper by the end. The proof one matters most: you should be able to pick constants, show the inequality, and check the threshold where it starts to hold. The loop one is what you will use most often in practice, on every algorithm for the rest of the course.

---
# Two correct programs

- Both compute $1 + 2 + \dots + n$ and return `36` for $n = 8$.
- One adds the numbers in a loop.
- The other uses the formula $\frac{n(n+1)}{2}$.
- For $n$ in the billions, which would you ship, and why?

> Here is the motivating question. Both methods are correct: we checked them against each other for every n from zero to three thousand, at fifty million, and at the largest int value. The loop does work in proportion to n, while the formula does the same small amount of work for every n. You already feel which one is better for huge n. The goal today is to be able to say precisely why, and to say it for code much less obvious than this.

---
# The running example: $n = 8$

- Every loop today is traced with $n = 8$.
- Five loop shapes: single, nested, dependent nested, halving, nested halving.
- Insertion sort on 8 values: already sorted, and reversed.
- Every measured count on these slides was produced by running the Java code.

> We keep one input size throughout so the numbers are easy to compare. Each loop shape in the Java code returns how many times its innermost body ran, so the counts on the slides are measured, not worked out by hand. Eight is small enough to check on paper and large enough that the shapes already separate: eight, sixty-four, twenty-eight, four and thirty-two.

---
@type section
# Counting operations

---
# Primitive operations

- An assignment, an arithmetic operation, a comparison.
- Reading or writing one array slot, `a[i]`.
- Calling a method and returning from it.
- Each takes at most a fixed amount of time on a given machine.
- We count them; we do not time them.

> A primitive operation is anything a real machine does in a bounded amount of time, independent of the input size. Different machines take different amounts of time for each, but that only changes the answer by a constant factor. That is exactly the information we are about to throw away on purpose. What remains after throwing it away is the shape of the growth, and that shape is the same on every machine.

---
# Counting the loop

```java
public static long sumLoop(int n) {
    long total = 0;
    for (long i = 1; i <= n; i++) {
        total += i;                // n additions
    }
    return total;
}
```

- Two initialisations, $n+1$ tests of `i <= n`, $n$ additions, $n$ increments, one return.
- Total $3n + 4$ operations: $28$ when $n = 8$.

> Count line by line. The two initial assignments run once. The loop test runs n plus one times, because the last test is the one that fails. The body and the increment each run n times. The return runs once. That is three n plus four. Someone else might count the addition and the assignment in plus-equals separately and get four n plus four. Both are fine: the constant changes, the n does not, and that is why we will soon stop caring about the constant. The counter is a long on purpose: an int counter could never exceed n equal to Integer.MAX_VALUE, because it would wrap around to a negative value first, and the loop would never end.

---
# Counting the formula

```java
public static long sumFormula(int n) {
    return (long) n * (n + 1L) / 2; // a fixed number of operations
}
```

- One addition, one multiplication, one division, conversions to `long`, one return.
- The count is the same for $n = 8$ and for $n = 10^9$.
- The `long` arithmetic matters: in `int`, `n * (n + 1)` overflows for large $n$.

> The formula does a handful of operations no matter what n is. That is the defining feature of constant time: the count does not depend on the input size at all. Notice one practical detail in the code. In int arithmetic the product overflows from n equal to forty-six thousand three hundred forty-one on, which the checker confirms. Both factors must be long: with only the cast on n, the int sum n plus one still wraps around when n is Integer.MAX_VALUE. Written this way, the formula at Integer.MAX_VALUE gives 2,305,843,008,139,952,128, and the checks assert that value. A constant-time method that returns the wrong number is not an improvement, so correctness still comes first.

---
# What is the input size?

| Problem | Natural input size $n$ |
|---|---|
| Search or sort an array | number of elements |
| Sum $1..n$ | the value $n$ itself |
| Add two big integers | number of digits |
| Search a graph | vertices and edges, often both: $V$ and $E$ |

> Before counting, decide what n measures. Usually it is the number of items. For the sum problem it is the value n, because the loop runs that many times. For arithmetic on very large numbers it is the number of digits, since that is what the work depends on. Graph algorithms later in the course use two sizes, the number of vertices and the number of edges, and their costs are stated in both.

---
# Same $n$, different work

```java
long comparisons = 0;
for (int i = 1; i < a.length; i++) {
    int key = a[i];
    int j = i - 1;
    while (j >= 0) {
        comparisons++;
        if (a[j] <= key) {
            break;
        }
        a[j + 1] = a[j];
        j--;
    }
    a[j + 1] = key;
}
return comparisons;
```

> This is insertion sort, which you may have met before (Lecture 5 treats it fully), instrumented to count key comparisons. The inner while loop stops as soon as it meets a value no bigger than the key. So how long it runs depends on the values, not only on how many there are. On a sorted array every inner loop stops after one comparison. On a reversed array every inner loop runs all the way to the front. Same n, very different work.

---
# Best, worst and average case

- **Worst case** $T_{\text{worst}}(n)$: the most work over all inputs of size $n$.
- **Best case** $T_{\text{best}}(n)$: the least work over all inputs of size $n$.
- **Average case** $T_{\text{avg}}(n)$: the expected work under a stated input distribution.
- Insertion sort, $n = 8$: sorted input $7$ comparisons, reversed input $28$.
- In general: sorted $n - 1$, reversed $\frac{n(n-1)}{2}$.

> When the work depends on the values, one number per n is not enough, so we define three functions of n. Each is an ordinary function: for every n it gives a single number. The worst case takes the maximum over all inputs of that size, the best case takes the minimum, and the average case needs an assumption about how inputs are drawn, such as every order of distinct keys being equally likely. Without that stated assumption an average is meaningless.

---
# Comparisons grow apart

```chart
type: line
title: Insertion sort key comparisons
x: 2, 3, 4, 5, 6, 7, 8
ylabel: comparisons
Sorted input: 1, 2, 3, 4, 5, 6, 7
Reversed input: 1, 3, 6, 10, 15, 21, 28
```

> These counts come from running the instrumented sort on sorted and reversed arrays for each size. The sorted line climbs by one per extra element: n minus one. The reversed line climbs by more each time, one, then two, then three and so on, because the new element must pass every earlier one: n times n minus one over two. At n equals eight the gap is already seven against twenty-eight, and it keeps widening.

---
# Quiz: which input is worst?

```quiz
For the insertion sort shown, which input of 8 distinct values makes the most key comparisons?
- [ ] Already sorted in increasing order.
- [x] Sorted in decreasing order.
- [ ] Any input: the count depends only on $n$.
- [ ] An input with the largest value first and the rest sorted.
```

> Decreasing order. Every new element is smaller than all the elements before it, so the inner loop compares it with each of them and moves them all, giving twenty-eight comparisons for eight values. Increasing order is the best case with seven. The third option is exactly what the previous slides disproved. The last option costs only a little more than sorted input, because only the largest value is out of place and one element at a time slides past it.

---
@type section
# Asymptotic notation

---
# Big-O: an upper bound

$$\begin{aligned} & f(n) = O(g(n)) \iff \\ & \text{some } c > 0,\ n_0 \ge 1 \text{ give} \\ & 0 \le f(n) \le c\, g(n) \text{ for all } n \ge n_0 \end{aligned}$$

- From some threshold $n_0$ on, $f$ never exceeds a constant multiple of $g$.
- It says nothing about small $n$, and nothing about how far below $g$ it is.
- Example: $3n + 4 = O(n)$ with $c = 7$, $n_0 = 1$, since $4 \le 4n$.

> Read the definition slowly. There must exist one constant c and one threshold n zero such that, for every n at or beyond the threshold, f of n is at most c times g of n. Big-O is an upper bound only: it says f grows no faster than g. It is perfectly true, if unhelpful, to say three n plus four is O of n squared. We assume, as usual, that the functions are nonnegative for the n we care about.

---
# Big-Omega: a lower bound

$$\begin{aligned} & f(n) = \Omega(g(n)) \iff \\ & \text{some } c > 0,\ n_0 \ge 1 \text{ give} \\ & 0 \le c\, g(n) \le f(n) \text{ for all } n \ge n_0 \end{aligned}$$

- From $n_0$ on, $f$ is at least a constant multiple of $g$.
- Example: $3n + 4 = \Omega(n)$ with $c = 3$, $n_0 = 1$.
- Also true, and weaker: $3n + 4 = \Omega(1)$.

> Big-Omega mirrors Big-O. Now the constant multiple of g must sit below f from the threshold on. It says f grows at least as fast as g. Again it can be loose: every positive function that stays at least some fixed positive constant is Omega of one. A lower bound is what you use to say a method cannot do better than some rate, for example that any comparison sort needs Omega of n log n comparisons in the worst case, a result Lecture 4 argues.

---
# Big-Theta: a tight bound

$$\begin{aligned} & f(n) = \Theta(g(n)) \iff \\ & \text{some } c_1, c_2 > 0,\ n_0 \ge 1 \text{ give} \\ & 0 \le c_1 g(n) \le f(n) \le c_2 g(n) \\ & \text{for all } n \ge n_0 \end{aligned}$$

- $f$ is sandwiched between two multiples of $g$.
- $f = \Theta(g)$ exactly when $f = O(g)$ and $f = \Omega(g)$.
- Example: $3n + 4 = \Theta(n)$ with $c_1 = 3$, $c_2 = 7$, $n_0 = 1$.
- But $3n + 4$ is not $\Theta(n^2)$.

> Theta combines the two: one constant multiple of g below f and another above it, from a common threshold on. That is why Theta equals O and Omega together; if you have the two separate thresholds, take the larger one. Theta is the most informative of the three, because it pins the growth rate down exactly. When you can prove Theta, say Theta; saying O when you know Theta throws information away.

---
# A worked bound, pictured

```chart
type: line
title: f(n) = 3n² + 5n + 2 between 3n² and 10n²
x: 1, 2, 3, 4, 5, 6, 7, 8
ylabel: value
10n²: 10, 40, 90, 160, 250, 360, 490, 640
f(n): 10, 24, 44, 70, 102, 140, 184, 234
3n²: 3, 12, 27, 48, 75, 108, 147, 192
```

> Here is the function we will prove bounds for, with the two multiples of n squared we will use. From n equal to one on, f stays between three n squared and ten n squared; at n equal to one it touches the upper line exactly, ten against ten. A picture of eight points is evidence, not proof, because the definition talks about every n beyond the threshold. The next two slides give the proof.

---
# Proof: the upper bound

- Claim: $3n^2 + 5n + 2 \le 10n^2$ for all $n \ge 1$.
- For $n \ge 1$: $5n \le 5n^2$ and $2 \le 2n^2$.
- Add: $3n^2 + 5n + 2 \le 3n^2 + 5n^2 + 2n^2 = 10n^2$.
- So $3n^2+5n+2 = O(n^2)$ with $c = 10$, $n_0 = 1$.

> The trick is to raise every lower-order term to the highest power, which is allowed because n is at least one. Five n is at most five n squared, and two is at most two n squared. Adding the three pieces gives ten n squared. So c equals ten and n zero equals one satisfy the definition. The Java checker also confirmed the inequality for every n from one to one hundred thousand, but it is the algebra that covers all n.

---
# Proof: the lower bound, and Theta

- For $n \ge 1$: $5n + 2 > 0$, so $3n^2 \le 3n^2 + 5n + 2$.
- So $3n^2+5n+2 = \Omega(n^2)$ with $c = 3$, $n_0 = 1$.
- Together: $3n^2 + 5n + 2 = \Theta(n^2)$ with $c_1 = 3$, $c_2 = 10$, $n_0 = 1$.

> The lower bound is easier: dropping positive terms can only make the expression smaller, so three n squared is below f for every positive n. With both bounds from the same threshold, the Theta definition is satisfied with c one equal to three, c two equal to ten, and n zero equal to one. This pattern of raising terms for the upper bound and dropping them for the lower bound handles every polynomial with a positive leading coefficient.

---
# Constants are not unique

- A tighter upper constant needs a later threshold.
- $c_2 = 4$: need $3n^2 + 5n + 2 \le 4n^2$, that is $n^2 - 5n - 2 \ge 0$.
- $n = 5$: $f(5) = 102 > 100$. Fails.
- $n = 6$: $f(6) = 140 \le 144$. Holds, and keeps holding: $n_0 = 6$.
- For $n \ge 6$: $n^2 \ge 6n = 5n + n \ge 5n + 6 > 5n + 2$.
- Any valid pair $(c, n_0)$ proves the bound; you need only one.

> The definition asks for some constants, not the best ones. If you insist on c equal to four, the inequality fails at five, where f is one hundred and two and four times twenty-five is one hundred. It holds from six on, and the chain on the slide proves it: for n at least six, n squared is at least six n, which is five n plus n, which is at least five n plus six, which is more than five n plus two. The Java checker confirms both facts. Choose whatever pair makes your proof simplest.

---
# Proving a bound is false

- Claim to refute: $n^2 = O(n)$.
- Suppose $n^2 \le c\,n$ for all $n \ge n_0$.
- Divide by $n$: $n \le c$ for all $n \ge n_0$.
- Pick $n = \max(n_0, \lceil c \rceil + 1)$: then $n > c$. Contradiction.

> To show a bound fails, assume constants exist and derive something impossible. If n squared were at most c times n from some threshold on, then n itself would be at most c from that threshold on, but n grows without limit and c is fixed. Any n larger than both c and the threshold breaks the inequality. The same argument shows n squared is not Theta of n, and more generally that n to a higher power is never O of n to a lower power.

---
# The "=" means "is a member of"

- $O(g)$ is a **set**: all functions that grow no faster than $g$.
- $3n + 4 = O(n)$ really means $3n + 4 \in O(n)$.
- So $n = O(n^2)$ is true, but "$O(n^2) = n$" is meaningless.
- The equals sign is one-way; read it as "is".

> This notation trips people up because the equals sign is not a real equality. O of n squared names a whole collection of functions, including n, n log n, and five n squared plus seven. Writing n equals O of n squared says n belongs to that collection. You cannot flip it around, and two functions that are both O of n need not be equal to each other. Many books write the membership symbol instead; both mean the same thing.

---
# Quiz: which statement is true?

```quiz
Let $f(n) = 3n^2 + 5n + 2$. Which statement is true?
- [ ] $f(n) = \Theta(n^3)$
- [ ] $f(n) = \Omega(n^3)$
- [x] $f(n) = O(n^3)$
- [ ] $f(n) = O(n)$
```

> O of n cubed is true: f is at most ten n squared, which is at most ten n cubed for n at least one, so c equal to ten and n zero equal to one work. It is a loose upper bound, but a true one. Omega of n cubed and Theta of n cubed are false for the same reason n squared is not O of n: f would have to keep up with a faster-growing function. O of n is false because f grows quadratically.

---
@type section
# Bounds and cases

---
# Two separate questions

- **Which inputs?** Choose a case: best, worst or average. That gives a function of $n$.
- **How do we describe that function?** Choose a bound: $O$, $\Omega$ or $\Theta$.
- Any bound can describe any case.
- A full statement names both: "the worst-case time is $\Theta(n^2)$".

> This is the most important clarification of the lecture. A case picks out a function: for each n, the worst case is the largest amount of work on any input of size n. A bound describes a function: it says how that function grows. The two choices are independent. You can give an upper bound on the best case, a lower bound on the worst case, or a tight bound on the average case. A precise sentence names the case and the bound.

---
# A shortcut worth unlearning

- A popular summary: "$O$ = worst case, $\Omega$ = best case, $\Theta$ = average case."
- It is inaccurate: $O$, $\Omega$, $\Theta$ are bounds, not cases.
- Insertion sort's **worst** case is $\Theta(n^2)$: so it is both $O(n^2)$ and $\Omega(n^2)$.
- Its **best** case is $\Theta(n)$: a tight bound on the best case.
- "$\Theta(n^2)$ for every input" would be false: sorted input takes linear time.

> You may have met this shortcut; it is common because worst-case upper bounds are what people quote most often. But the notation does not carry that meaning. Insertion sort shows why it matters. Its worst case is Theta of n squared, which includes an Omega bound on the worst case, and its best case is Theta of n, which is a Theta bound on the best case. Keeping the two questions separate avoids statements that sound precise but say the wrong thing.

---
# Insertion sort, every combination

| Case, $n$ values | Exact comparisons | Tight bound |
|---|---|---|
| Best: already sorted | $n - 1$ | $\Theta(n)$ |
| Worst: reversed | $\frac{n(n-1)}{2}$ | $\Theta(n^2)$ |
| Average: all orders of distinct keys equally likely | about $n^2/4$ | $\Theta(n^2)$ |
| Any input | between the two | $O(n^2)$ and $\Omega(n)$ |

> Read the last row carefully: for every input, the work is at most quadratic and at least linear, so O of n squared and Omega of n are true statements about the running time on all inputs. Neither is tight for all inputs, because different inputs sit at different ends. The first three rows are tight statements about particular cases. The best and worst counts were checked in Java for every n from two to two hundred. For the average, n squared over four is the leading term: averaging over all 40,320 orders of eight distinct keys gives about 19.3 comparisons, the extra coming from lower-order terms.

---
@type section
# Growth classes and rules

---
# Common growth classes

| Class | $n = 8$ | $n = 16$ | $n = 32$ |
|---|---|---|---|
| $1$ | 1 | 1 | 1 |
| $\log_2 n$ | 3 | 4 | 5 |
| $n$ | 8 | 16 | 32 |
| $n \log_2 n$ | 24 | 64 | 160 |
| $n^2$ | 64 | 256 | 1024 |
| $2^n$ | 256 | 65536 | about $4.29 \times 10^9$ |
| $n!$ | 40320 | about $2.09 \times 10^{13}$ | about $2.63 \times 10^{35}$ |

> These values were computed exactly in Java, with BigInteger for the last two rows, and the largest are rounded here: two to the thirty-two is exactly 4,294,967,296 and sixteen factorial is 20,922,789,888,000. Each row eventually dwarfs the one above it. The first four rows are what efficient algorithms usually look like. Quadratic is fine for thousands of items and painful for millions. Exponential and factorial grow so fast that doubling n from sixteen to thirty-two turns sixty-five thousand into four billion, and twenty trillion into a number with thirty-six digits.

---
# See the classes grow

- The same ordering holds for every large enough $n$: $1 \prec \log n \prec n \prec n\log n \prec n^2 \prec 2^n \prec n!$
- Here $f \prec g$ means $f = O(g)$ but $f$ is not $\Omega(g)$.
- Small $n$ can mislead: $n^2 < 2^n$ fails for $n = 2, 3, 4$.
- Explore: [Growth rates visualization](../../../visualizations/algorithms/growth_rates.html).

> The ordering is about large n. For small n the curves cross in surprising ways: at n equal to two both are four, at n equal to three, n squared is nine and two to the n is eight, and at four they are equal at sixteen. From five on, two to the n stays ahead. Open the growth rates visualization and drag the range of n from small to large; watch the crossings disappear and the order settle. This is why asymptotic claims always include "for large enough n".

---
# Simplification rules

- Drop constant factors: $7n^2 = \Theta(n^2)$.
- Drop lower-order terms: $n^2 + 100n + 5 = \Theta(n^2)$.
- A polynomial of degree $k$ with positive leading coefficient is $\Theta(n^k)$.
- Keep the dominant term only: $n^2 + n \log n = \Theta(n^2)$.

> These rules all follow from the definitions, using the same raise-and-drop argument as the worked proof. Constant factors disappear into c. Lower-order terms are eventually smaller than any fixed fraction of the leading term, so they disappear too. The only thing you cannot drop is a factor that grows, such as the log in n log n: n log n is not Theta of n, because log n exceeds every constant eventually.

---
# Sum and product rules

- **Sum**, for code that runs one part after another:
  - $O(f) + O(g) = O(\max(f, g))$. A $\Theta(n)$ loop then a $\Theta(n^2)$ loop is $\Theta(n^2)$.
- **Product**, for a loop whose body costs something:
  - $n$ iterations of a $\Theta(g)$ body give $\Theta(n \cdot g)$.
  - A loop that calls a $\Theta(\log n)$ method $n$ times: $\Theta(n \log n)$.

> Sequential pieces add, and a sum of two nonnegative functions is within a factor of two of the larger one, so only the larger survives. Loops multiply: if each of n iterations costs Theta of g, the total is Theta of n times g. For the product rule to give Theta, each iteration must really cost Theta of g; when iterations cost different amounts, as in a dependent loop, add them up instead of multiplying.

---
# Log bases do not matter

- Change of base: $\log_a n = \dfrac{\log_b n}{\log_b a}$.
- $\log_b a$ is a constant, so $\log_2 n = \Theta(\log_{10} n)$.
- That is why we write $O(\log n)$ with no base.
- Bases of **exponentials** do matter: $3^n$ is not $O(2^n)$, since $3^n / 2^n = 1.5^n$ grows.

> Logarithms to different bases differ only by a constant factor, which asymptotic notation ignores, so the base can be left off. Exponentials are different: three to the n divided by two to the n is one point five to the n, which grows without bound, so no constant can make two to the n catch up. The same care applies inside exponents: two to the two n is four to the n, not Theta of two to the n.

---
@type section
# Analysing loops

---
# Single and nested loops

```java
for (int i = 0; i < n; i++) {
    count++;
}
// ...
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        count++;
    }
}
```

- Single: $n$ iterations, $8$ when $n = 8$: $\Theta(n)$.
- Nested, independent bounds: $n \cdot n$, $64$ when $n = 8$: $\Theta(n^2)$.

> The single loop runs its body n times, so it is Theta of n. In the nested loop the inner bound does not depend on i, so every outer iteration runs the inner loop the full n times, and the product rule gives n squared. The measured counts for n equal to eight are eight and sixty-four. The loop control itself, the tests and increments, only adds a constant per iteration and does not change the class.

---
# Dependent nested loops

```java
for (int i = 0; i < n; i++) {
    for (int j = 0; j < i; j++) {
        count++;
    }
}
```

- Inner loop runs $i$ times: $0 + 1 + \dots + (n-1) = \frac{n(n-1)}{2}$.
- $n = 8$: $28$ iterations, not $64$.
- Still $\Theta(n^2)$: $\frac{n(n-1)}{2} \ge \frac{n^2}{4}$ for $n \ge 2$, and $\le \frac{n^2}{2}$ for $n \ge 1$.
- So $c_1 = 1/4$, $c_2 = 1/2$, $n_0 = 2$.

> Now the inner bound depends on i, so we add instead of multiplying. Row by row the inner loop runs zero, one, two, and so on up to n minus one times, which sums to n times n minus one over two. That is twenty-eight for n equal to eight, less than half of sixty-four, yet still quadratic: half of n squared is still n squared up to a constant. The lower bound on the slide holds because n minus one is at least n over two once n is at least two, and the upper bound holds because n minus one is less than n. The checker confirms both for every n up to two thousand.

---
# Halving loops, step by step

```diagram
@dir LR
@reveal manual
I8[i = 8 | count 1]
focus I8
--- Start at n.
I8 -> I4[i = 4 | count 2] -> I2[i = 2 | count 3] -> I1[i = 1 | count 4]
focus I4 I2 I1
--- Each pass halves i with integer division.
I1 -> I0[i = 0 | stop].gray
focus I1 I0
--- 1 / 2 is 0 in Java, and the test i >= 1 fails.
```

```java
for (int i = n; i >= 1; i /= 2) {
    count++;
}
```

> This is the loop from the halving method, run with n equal to eight: the body runs for i equal to eight, four, two and one, four times in all. In general it runs floor of log base two of n, plus one, times, which the Java checker confirmed for every n up to two thousand. For a thousand it runs ten times, and for a million twenty. Any loop that shrinks its range by a constant factor each pass is logarithmic; binary search is the famous example.

---
# A loop around a halving loop

```java
for (int i = 0; i < n; i++) {
    for (int j = n; j >= 1; j /= 2) {
        count++;
    }
}
```

- Inner loop: $\lfloor \log_2 n \rfloor + 1$ iterations, independent of `i`.
- Total $n(\lfloor \log_2 n \rfloor + 1)$: $8 \times 4 = 32$ when $n = 8$.
- Product rule: $\Theta(n \log n)$.

> Here the inner loop halves from n each time, and its bound does not depend on i, so the product rule applies directly: n outer iterations times about log n inner iterations. The measured count for n equal to eight is thirty-two, eight times four. This is the shape merge sort will have in Lecture 4: linear work at each of logarithmically many levels.

---
# Loop versus formula, revisited

| Method | Operations | Time | Extra space |
|---|---|---|---|
| `sumLoop(n)` | $3n + 4$ | $\Theta(n)$ | $\Theta(1)$ |
| `sumFormula(n)` | fixed | $\Theta(1)$ | $\Theta(1)$ |

- For every $n \ge 0$ both return the same value; checked for $n = 0..3000$ and $n = 5 \times 10^7$.
- For large $n$ the formula wins; for $n = 8$ nobody could tell them apart.

> Now we can answer the opening question precisely. The loop is Theta of n and the formula is Theta of one; both use constant extra space. Here there is no best or worst case to separate, because each method does the same work for every input of a given n. The asymptotic difference is what matters at a billion. At n equal to eight the difference is a few dozen operations, far too small to notice, which is the other half of the lesson: asymptotics are about large inputs.

---
# Quiz: count the halving loop

```quiz
How many times does the body of `for (int i = n; i >= 1; i /= 2)` run when `n = 1000`?
- [ ] 9
- [x] 10
- [ ] 500
- [ ] 1000
```

> Ten. The values of i are one thousand, five hundred, two hundred fifty, one hundred twenty-five, sixty-two, thirty-one, fifteen, seven, three and one, and the next halving gives zero, which fails the test. The formula agrees: floor of log base two of a thousand is nine, and one more makes ten. Nine is what you get if you forget the pass with i equal to one. Five hundred is what a loop stepping down by 2 would give: one thousand, nine hundred ninety-eight, and so on down to two.

---
@type section
# Space complexity

---
# Auxiliary versus total space

```java
public static int[] reversedCopy(int[] a) {
    int[] out = new int[a.length];         // Theta(n) auxiliary
    for (int i = 0; i < a.length; i++) {
        out[a.length - 1 - i] = a[i];
    }
    return out;
}
public static void reverseInPlace(int[] a) {
    for (int i = 0, j = a.length - 1; i < j; i++, j--) {
        int tmp = a[i];                    // Theta(1) auxiliary
        a[i] = a[j];
        a[j] = tmp;
    }
}
```

- **Auxiliary** space: memory used beyond the input. **Total**: input plus auxiliary.
- Reversing into a copy: $\Theta(n)$ auxiliary. Swapping in place: $\Theta(1)$.

> Space complexity counts memory the same way time complexity counts operations. Total space includes the input, so every method that reads an array of n values has total space at least n; that is why the more useful measure is usually auxiliary space, the extra memory the algorithm itself allocates. The copying version allocates a second array of n slots. The in-place version swaps pairs from the two ends using one temporary variable, so its auxiliary space is constant.

---
# The recursion stack is space too

```java
public static long sumRecursive(int n) {
    if (n <= 0) {
        return 0;
    }
    return n + sumRecursive(n - 1); // n + 1 frames are live at the bottom
}
```

- Each call waits for the next: depth $n + 1$ for $n \ge 0$, so $\Theta(n)$ auxiliary space.
- The loop version computes the same sum in $\Theta(1)$ extra space.
- Very deep recursion throws `StackOverflowError`; the depth limit depends on the JVM's stack size.

> Every active method call keeps a frame on the call stack, holding its parameter and where to return. When sumRecursive reaches zero, all n plus one calls are still waiting, so the auxiliary space is Theta of n even though no array is allocated. The loop does the same arithmetic with two variables. Recursion is often the clearest way to express an algorithm, but its depth is part of the cost, and balanced splits keep that depth logarithmic.

---
@type section
# Wrap-up

---
# Common mistakes

- Saying $O$ when you have shown $\Theta$: true, but it hides the tight result.
- Stating a bound without the case: "insertion sort is $\Theta(n^2)$" is false on sorted input.
- Multiplying loop bounds when the inner loop depends on the outer index. Add them instead.
- Judging growth from small $n$: curves cross before they settle.
- Forgetting the recursion stack when counting space.

> Each of these produces a sentence that sounds technical but is wrong or incomplete. The second is the one to watch most closely: attach a case to every tight bound. For the third, multiplying by the largest inner bound always gives a valid upper bound, and for the dependent loop we saw it happens to be tight; for other dependent loops it can be loose, so write the sum when you want a tight answer. The last is easy to overlook because recursive code allocates nothing visible.

---
# Summary

- Count primitive operations as a function of the input size $n$.
- $O$: upper bound; $\Omega$: lower bound; $\Theta$: both. Each needs $c$ and $n_0$.
- Cases pick a function; bounds describe it. Name both.
- Drop constants and lower-order terms; add sequential parts, multiply nested ones.
- Halving loops are logarithmic; the recursion stack counts as space.

> The one idea that ties this together is that we describe how work grows, not how long it takes. The definitions let us prove such descriptions, as we did for three n squared plus five n plus two. The case-versus-bound distinction lets us state them precisely, as with insertion sort's linear best case and quadratic worst case. And the loop patterns let us read growth off code at a glance, which we will do for every algorithm from now on.

---
# Check yourself

- Prove $2n^3 + 7n + 1 = \Theta(n^3)$ and give your constants $c_1$, $c_2$, $n_0$.
- A method has a $\Theta(n)$ best case and a $\Theta(n^2)$ worst case. What can you say about every input?
- Write a loop whose body runs $\Theta(\log n)$ times without dividing by 2.

> For the first, follow the raise-and-drop pattern and then test your threshold at n equal to one. For the second, combine the best and worst cases into an O and an Omega statement that holds for all inputs, as in the insertion sort table. For the third, think about any loop that multiplies or divides its index by a constant each pass.

---
# Next time

- Linear and binary search, analysed with today's tools.
- Best, worst and average cases of linear search; the sorted precondition.
- A loop invariant that proves binary search correct.
- Compare the costs: [Efficiency comparison visualization](../../../visualizations/algorithms/efficiency_comparison.html).

> Next lecture applies everything from today to the two searches from the efficiency game. We will state their cases precisely, including the assumption behind the average case, prove binary search correct with an invariant, and count its probes exactly. The efficiency comparison visualization puts several growth classes side by side on real operation counts; try it before then.

---
# Sources

- Cormen, Leiserson, Rivest, Stein, *Introduction to Algorithms*, 4th ed. (CLRS), Chapter 2 (analysing insertion sort) and Chapter 3 (characterizing running times: $O$, $\Omega$, $\Theta$).
- Java SE API documentation: `java.math.BigInteger`, `java.lang.StackOverflowError`.
- Every measured count, table and chart on these slides was produced by running the Java code written for this deck.

> The definitions of O, Omega and Theta follow the standard presentation in the textbook chapters listed, including the convention of reading the equals sign as membership. The insertion sort analysis follows the textbook's chapter on getting started. Every measured number on these slides, from the loop counts to the factorials, was computed by the accompanying Java code rather than taken from a published table.
