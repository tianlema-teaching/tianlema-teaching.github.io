---
title: "CSI 3620 - Data Structures and Algorithms"
description: "Public companion for Fall 2026: course scope, how the course is assessed, and optional practice."
---

**Draft companion, Fall 2026.** The course facts below come from the instructor's Lecture 1 materials. They have not yet been checked line by line against the official syllabus. Where anything here differs from the syllabus on Moodle, the syllabus wins.

Instructor: Tianle Ma, Oakland University. Reading this page does not enroll you in the course or earn credit.

## What this page is for

Enrolled students get the syllabus, assignments, quizzes, grades, submissions, and class announcements on Moodle. This page gives anyone, enrolled or not, a public summary of the course and some optional practice to go with it. Meeting times, rooms, contact details for course staff, and enrollment codes are shared with enrolled students, not on this page.

## Course at a glance

| Item | Detail |
|---|---|
| Credits | 4 |
| Prerequisite | CSI 2300, plus major standing in IT |
| Credit restriction | Credit is given for only one of CSI 3610 and CSI 3620 |
| Programming language for assignments | Java |
| Recommended textbook | An interactive zyBooks text (subscription instructions are given to enrolled students) |
| Reference | Cormen, Leiserson, Rivest, and Stein, *Introduction to Algorithms*, 4th edition. A dense, theoretical reference. All testable material comes from lectures and assignments. |

## What you will be able to do

By the end of the course, students should be able to:

1. Implement classes and inheritance in Java.
2. Implement and use linked lists, binary trees, stacks, queues, and priority queues.
3. Describe and use traversal, search, and sorting algorithms, and discuss their performance.
4. Describe the concepts and basic operations of hash tables and B-trees.
5. Model more than one algorithmic solution to a problem and compare them.
6. Write the running-time analysis of a given algorithm using Big-O notation.

## Topics, in teaching order

| Unit | Topics | Status |
|---|---|---|
| Foundations | What data structures and algorithms are; time and space complexity; Big-O, Big-Omega, and Big-Theta; asymptotic analysis | Covered |
| Searching and sorting | Linear and binary search; bubble, selection, insertion, merge, quick, and radix sort | Covered |
| Linear structures | Linked lists; stacks and queues | Upcoming |
| Trees | Trees and binary search trees; tree traversals; balanced trees, AVL trees, and red-black trees | Upcoming |
| Hashing and external storage | Hash tables; B-trees and B+ trees | Upcoming |
| Priority queues | Priority queues and heaps | Upcoming |
| Graphs | Representations, BFS and DFS; topological sort and cycle detection; shortest paths (Dijkstra, Bellman-Ford, Floyd-Warshall); minimum spanning trees (Kruskal, Prim) | Upcoming |
| Algorithm design | Greedy algorithms; dynamic programming; backtracking | Upcoming |

The slides for every topic, covered and upcoming, are on the [lecture slides page](/csi3620/2026-fall/lectures/). This list gives the order only. The schedule on Moodle, including exam dates, is authoritative.

## How the course is assessed

| Component | Weight |
|---|---|
| Homework assignments | 30% |
| Quizzes and participation | 20% |
| Midterm exam (in person) | 20% |
| Final exam (in person, cumulative) | 30% |

An optional project can add up to 10%. Details are announced in class.

**Late homework (summary).** Once per semester you may take a 24-hour extension on a single homework, if you ask before the original deadline. Otherwise 5% is deducted for each 24 hours late, and a partial day counts as a full day. The deduction stops at 50%. Homework that is 10 or more days late receives 0. Enrolled students should check the syllabus for the full policy and how to request the extension.

## Lecture slides

The [lecture slides page](/csi3620/2026-fall/lectures/) has slides for all 21 topics, in teaching order. Each lecture opens as a presentation, with the spoken explanation for each slide one key away, or as a printable study guide. The Java examples in the slides are tested.

## Optional public practice

These draft lessons, on the instructor's general learning site [learn.tianlema.com](https://learn.tianlema.com/), cover ideas from the course. They are not assigned work and are not part of your grade unless the instructor says so on Moodle. The lessons use Python. Your assignments use Java. The reasoning carries over from one to the other, but the syntax does not.

| Course topic | Public draft lesson |
|---|---|
| Precise contracts, boundary cases, testing | [A function is a promise you can test](https://learn.tianlema.com/lessons/01-functions-and-contracts/) |
| Binary search, loop invariants, logarithmic growth | [Find the first possible position](https://learn.tianlema.com/lessons/02-search-and-invariants/) |
| Loops, lists, and dictionaries, if you want a refresher | [Trace a loop before you trust it](https://learn.tianlema.com/basics/02-loops/) and [Count things with lists and dictionaries](https://learn.tianlema.com/basics/03-lists-and-dictionaries/) |

A good way to use a lesson: before you run anything, predict the output for the boundary cases. Then run the tests and explain any result that surprised you.

## Interactive algorithm visualizations

The instructor's step-by-step visualizations run in your browser, with no installation, account, or network connection after the page loads. Each one opens as a standalone page; use your browser's Back button to return here. The whole collection is on [one index page](/visualizations/algorithms/index.html).

| Unit | Visualizations |
|---|---|
| Foundations | [Growth rates and Big-O](/visualizations/algorithms/growth_rates.html), [Why efficiency matters](/visualizations/algorithms/efficiency_comparison.html), [Master theorem calculator](/visualizations/algorithms/master_theorem.html) |
| Searching and sorting | [Linear search](/visualizations/algorithms/linear_search.html), [Binary search](/visualizations/algorithms/binary_search.html), [Bubble sort](/visualizations/algorithms/bubble_sort.html), [Selection sort](/visualizations/algorithms/selection_sort.html), [Insertion sort](/visualizations/algorithms/insertion_sort.html), [Merge sort](/visualizations/algorithms/merge_sort.html), [Quicksort](/visualizations/algorithms/quicksort.html), [Lomuto partition](/visualizations/algorithms/quicksort_partition_lomuto.html), [Hoare partition](/visualizations/algorithms/quicksort_partition_hoare.html) |
| Priority queues | [Binary heap](/visualizations/algorithms/binary_heap.html), [Heapsort](/visualizations/algorithms/heapsort.html) |
| Graphs | [BFS](/visualizations/algorithms/bfs.html), [DFS](/visualizations/algorithms/dfs.html), [BFS versus DFS](/visualizations/algorithms/bfs_dfs_comparison.html), [Topological sort](/visualizations/algorithms/topological_sort.html), [Dijkstra](/visualizations/algorithms/dijkstra.html), [Bellman-Ford](/visualizations/algorithms/bellman_ford.html), [Floyd-Warshall](/visualizations/algorithms/floyd_warshall.html), [Kruskal](/visualizations/algorithms/kruskal.html), [Prim](/visualizations/algorithms/prim.html) |
| Algorithm design | Greedy: [Activity selection](/visualizations/algorithms/activity_selection.html), [Huffman coding](/visualizations/algorithms/huffman.html), [Fractional knapsack](/visualizations/algorithms/fractional_knapsack.html). Dynamic programming: [Fibonacci: recursion versus a table](/visualizations/algorithms/fibonacci.html), [Memoization call counts](/visualizations/algorithms/dp_fibonacci.html), [Coin change](/visualizations/algorithms/coin_change.html), [0/1 knapsack](/visualizations/algorithms/knapsack.html), [Longest common subsequence](/visualizations/algorithms/lcs.html), [Edit distance](/visualizations/algorithms/edit_distance.html) |

The collection also has pages beyond the course topics: randomized algorithms ([Randomized quicksort](/visualizations/algorithms/quicksort_randomized.html), [Quickselect](/visualizations/algorithms/quickselect.html)) and complexity theory ([P versus NP](/visualizations/algorithms/p_vs_np.html), [Vertex cover approximation](/visualizations/algorithms/vertex_cover.html)). There are no visualizations yet for linked lists, stacks and queues, binary search trees and traversals, balanced trees, hash tables, B-trees, or backtracking. The lecture slides trace those topics step by step instead.

**How to use them.** Predict the next step before you reveal it. On step-by-step pages, the right arrow or Space moves forward and the left arrow moves back. Press `?` with the presenter tools on to list every key. The presenter tools (laser, spotlight, pen) are for projecting in class and are off by default, so a stray letter key never draws over the page: add `?present` to the end of a visualization's address to turn them on in your browser, then press `T`. `?present=off` turns them off again. The visualizations are optional and not assigned work. They show one worked input at a time, so an animation is not a proof of correctness or of running time.

**Limits.** They are designed for a laptop or projector; on a phone, many pages need zooming or sideways scrolling. The animations are visual and have not been reviewed for screen-reader use; the textbook and the lectures cover the same algorithms in words. They were checked for algorithm and wording errors in September 2026 but have not yet had an independent review. Enrolled students can report a mistake through the usual course channels.

## Using AI tools

Follow any AI-use rules in the course syllabus. Within those rules, you learn more if you first try the problem yourself, ask a tool for a hint or a counterexample rather than a complete solution, and check any code it gives you against tests you wrote. Do not paste other students' work, grades, or course credentials into any tool.

## Privacy

This page contains no student records, grades, accommodations, meeting links, or restricted solutions, and it never will. Being able to read the page does not give you access to any computing resource set aside for the course.
