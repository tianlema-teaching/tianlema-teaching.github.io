---
title: "CSI 4130/5130 - Artificial Intelligence"
description: "Public companion for Fall 2026: course scope, background, how the course is assessed, and optional practice."
---

**Draft companion, Fall 2026.** The course facts below come from the instructor's Lecture 1 materials. They have not yet been checked line by line against the official syllabus. Where anything here differs from the syllabus on Moodle, the syllabus wins.

Instructor: Tianle Ma, Department of Computer Science and Engineering, Oakland University. Reading this page does not enroll you in the course or earn credit.

## What this page is for

Enrolled students get the syllabus, quizzes, grades, submissions, and class announcements on Moodle. This page gives anyone, enrolled or not, a public summary of the course and some optional practice to go with it. Meeting times, rooms, contact details for course staff, and meeting links are shared with enrolled students, not on this page.

## Course at a glance

| Item | Detail |
|---|---|
| Credits | 4 |
| Prerequisites | (EGR 2600 or STA 2221 or STA 2226) and (CSI 3610 or CSI 3620) |
| Tools | Python, NumPy, and PyTorch. You can use Google Colab instead of a local setup. |
| Textbook | Russell and Norvig, *Artificial Intelligence: A Modern Approach*, 4th edition |

## What the course covers

Deep learning is the main focus. The course covers:

- Machine learning fundamentals: models, losses, optimization, and evaluation.
- Deep learning in PyTorch: neural networks, convolutional networks, sequence models, and Transformers.
- Problem solving by search: A*, adversarial search, and game playing.
- Probabilistic reasoning and decision making under uncertainty.
- Reinforcement learning: agents that learn from reward.
- Applications: computer vision, natural language processing and large language models, and student projects.

## Background you should bring

Calculus (derivatives, the chain rule, gradients), linear algebra (vectors, matrices, dot products), probability and statistics, algorithm design and analysis, and proficient Python with NumPy. PyTorch experience helps, but the course teaches it.

The public lessons listed below give a quick check on some of these: vectors, gradients, and probabilities. They are shorter and simpler than the course.

## How the course is assessed

| Component | Weight | What it involves |
|---|---|---|
| Assignments | 40% | Programming and written homework in Python, NumPy, and PyTorch: basic ML models, deep learning models, search methods for problems and games, and reinforcement learning for planning under uncertainty |
| Project | 40% | One of two paths: design your own project that solves a real problem, or enter a public challenge such as a Kaggle, DREAM, or Topcoder competition and achieve satisfactory results. Final presentations take place in the last two lectures, with no extensions. |
| Quizzes and participation | 20% | A short Moodle quiz after each lecture, plus participation in class |

**Late homework (summary).** Once per semester you may take a 24-hour extension on a single homework, if you ask before the original deadline. Otherwise 5% is deducted for each 24 hours late. The deduction stops at 50%. Homework that is 10 or more days late receives 0. Enrolled students should check the syllabus for the full policy and how to request the extension.

## Optional public practice

These draft lessons, on the instructor's general learning site [learn.tianlema.com](https://learn.tianlema.com/), cover ideas from the course. They are not assigned work and are not part of your grade unless the instructor says so on Moodle. Each one comes with reference code and tests you can run on an ordinary laptop, with no GPU.

| Course topic | Public draft lesson |
|---|---|
| Tensor shapes and dot products | [Make vector arithmetic explain itself](https://learn.tianlema.com/lessons/03-vectors-and-shapes/) |
| Train/test separation and leakage | [Test on what the model has not already seen](https://learn.tianlema.com/lessons/04-evaluation-and-leakage/) |
| Loss, gradients, and gradient descent | [Learn one parameter and verify the gradient](https://learn.tianlema.com/lessons/05-learning-a-parameter/) |
| Softmax and cross-entropy | [Turn scores into probabilities without overflow](https://learn.tianlema.com/lessons/06-probabilities-from-scores/) |
| Attention with a causal mask | [Make attention respect the past](https://learn.tianlema.com/lessons/07-causal-attention/) |
| Next-token prediction | [Build a tiny language model and report what it is](https://learn.tianlema.com/lessons/08-tiny-language-model/) |

For the search unit, the instructor's interactive visualizations show uninformed search step by step: [breadth-first search](/visualizations/algorithms/bfs.html), [depth-first search](/visualizations/algorithms/dfs.html), and [BFS versus DFS on the same graph](/visualizations/algorithms/bfs_dfs_comparison.html). [Dijkstra's algorithm](/visualizations/algorithms/dijkstra.html) is the shortest-path idea that uniform-cost search adapts to a single goal. They run offline in the browser and work best on a laptop screen. There is no A* or game-tree visualization.

## Using AI tools

Follow any AI-use rules in the course syllabus. Within those rules, AI assistants are most useful here for checking your understanding: have one trace tensor shapes through your model, generate test cases for your backpropagation, or explain an error message. Check what it tells you. Aim to be able to explain every line of work you submit.

## Privacy

This page contains no student records, grades, accommodations, meeting links, or restricted solutions, and it never will. Being able to read the page does not give you access to any computing resource set aside for the course.
