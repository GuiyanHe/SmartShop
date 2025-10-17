# SmartShop
This is course project for IUI

# Development Workflow

This document outlines the development process for this project. We use GitHub Issues to manage tasks and Pull Requests to ensure code quality.

**The Golden Rule: All changes must be made through a Pull Request. Never push directly to the `main` branch.**

---

## 1. Task Management (GitHub Issues)

All work—features, bugs, or chores—is tracked using GitHub Issues.

#### Creating an Issue:

* **Title:** Write a clear and descriptive title (e.g., "Implement User Profile UI").
* **Description:**
    * Clearly explain the task. What is the goal? Why is it needed?
    * Include **Acceptance Criteria** using a task list (`- [ ]`) to define what "done" means.
* **Labels:** Add relevant labels (e.g., `bug`, `feature`, `enhancement`).
* **Assignees:** Assign the issue to the person responsible for the work.

---

## 2. Code Contribution (Pull Requests)

The Pull Request (PR) process is mandatory for all code contributions.

#### Workflow Steps:

1.  **Create a Branch:**
    * Pull the latest changes from the `main` branch.
    * Create a new branch with a descriptive name.
    * **Branch Naming Convention:**
        * For features: `feature/short-description` (e.g., `feature/user-login`)
        * For bug fixes: `fix/short-description` (e.g., `fix/login-button-crash`)

2.  **Develop and Commit:**
    * Write your code on your new branch.
    * Make small, logical commits with clear messages.

3.  **Open a Pull Request:**
    * Push your branch to the remote repository.
    * Open a Pull Request (PR) against the `main` branch.
    * **Link the Issue:** In the PR description, use a keyword to link it to the corresponding issue (e.g., `Closes #25`, `Fixes #30`). This will automatically close the issue when the PR is merged.
    * **Request Reviewers:** Select at least one team member to review your code.

4.  **Code Review:**
    * Reviewers will check the code for quality, correctness, and style.
    * Address any feedback by pushing new commits to your branch. The PR will update automatically.

5.  **Merge:**
    * Once the PR is approved by at least one reviewer, it can be merged into `main`.

## 3. Code Review Guidelines

Code review is a critical step to maintain high-quality code and share knowledge. As a reviewer, your responsibility is to ensure the incoming code is correct, maintainable, and adheres to our project standards.

#### Reviewer's Checklist:

When reviewing a Pull Request, please check the following aspects. Use the "Request changes" or "Comment" features for feedback, and "Approve" when you are satisfied.

##### **Required Checks (Blocking)**

These checks must pass before a PR can be merged.

*   **[ ] No Unauthorized Cross-Module Changes:**
    *   Review the `Files changed` tab to ensure all modifications are strictly confined to the modules or files relevant to the task.
    *   If the PR unintentionally modifies a module owned by another team member, you **must** `Request changes` and ask the author to remove the unrelated modifications. This prevents conflicts and regressions.

*   **[ ] No Potential Bugs:**
    *   **Correctness:** Does the code achieve its intended goal? Does it cover all business logic and edge cases?
    *   **Error Handling:** Are there proper `try-catch` blocks? Are exceptions like network failures or `null` values handled gracefully?
    *   **Resource Management:** Are there potential memory leaks (e.g., holding a `Context` in a `ViewModel`, not closing streams or cursors)?
    *   **Concurrency:** Is the code thread-safe in a multi-threaded environment?

##### **Optional Checks (Non-Blocking)**

These are best practices that improve code quality but are not strict blockers for merging.

*   **[ ] Code Style and Quality:**
    *   **Naming Conventions:** Are variables, functions, and classes named clearly and descriptively?
    *   **Readability:** Is the code easy to understand? Should complex logic be simplified or commented?
    *   **Reusability:** Is there duplicated code that could be extracted into a shared function or class?
    *   **Best Practices:** Does the code follow official Kotlin/Android coding conventions?


---

## 4. Project Development Requirements

To ensure a smooth and organized development process, please adhere to the following guidelines.

1.  **Maintain Your Code Module:**
    *   Each team member is the "owner" of their assigned module(s). You are responsible for your module as its Product Manager, Developer, and Tester.
    *   Take full ownership of its functionality, quality, and maintenance.

2.  **Create an Issue and Define Your Plan Before Coding:**
    *   Before writing any code for a new feature or a bug fix, you **must** create a corresponding GitHub Issue.
    *   **In the issue description, you must outline your design and implementation plan.** This includes:
        *   **Implementation Details:** Clearly define field definitions, interaction logic, and other technical specifics.
        *   **Cross-Module Impact:** If your plan affects other modules, you must `@mention` the respective owners to review your proposed changes for feasibility and impact.
    *   This ensures all work, design decisions, and dependencies are tracked, reviewed, and agreed upon before implementation begins.

3.  **Use SharedPreferences for Data Persistence:**
    *   For the sake of simplicity and consistency across the project, all data persistence must be implemented using `SharedPreferences`.
    *   Avoid using other persistence methods like Room, SQLite, or Files unless explicitly approved for a specific reason.

4.  **Use JSON for Data Transfer:**
    *   All data transferred between components or over a network must be in **JSON format**. This ensures a consistent and standardized data structure throughout the application.

---

## 5. Reviewer Assignment

To ensure all code is reviewed effectively, please follow the assignment table below.

#### Default Reviewer Assignments

This table shows the default reviewer for each team member.

| Author (Code Submitter) | Default Reviewer |
| ------------------ | --------- |
| Aviral Agarwal     | Qinyao Hou |
| Lynn Zhou          | Guiyan He |
| Guiyan He          | Yi-Hsin Chiang |
| Qinyao Hou         | Lynn Zhou  |
| Yi-Hsin Chiang     | Aviral Agarwal |

#### Flexibility in Assignment

The assignments above are the default. However, you are encouraged to use your judgment. **If you believe your code is more relevant to another team member's expertise, you may assign that person as the reviewer instead.**

When opening your Pull Request, please select the appropriate reviewer in the "Reviewers" section on GitHub.

## 6. Project Architecture

To help everyone get started quickly, the project has been initialized with a recommended architecture.

#### Default Architecture: MVVM + Jetpack

The project is set up using the **MVVM (Model-View-ViewModel)** architecture, which is Google's recommended standard for modern Android development.

The basic development flow for MVVM is as follows:

1.  **Model (Data Layer):**
    *   This layer is responsible for all data operations.
    *   Create a **Repository** class (e.g., `UserRepository`) to handle data logic. This repository will fetch data from a remote source (network) or local source (`SharedPreferences`).

2.  **ViewModel (Logic Layer):**
    *   The `ViewModel` (e.g., `UserViewModel`) acts as a bridge between the Model and the View.
    *   It requests data from the `Repository` and exposes it to the View, usually via `LiveData`.
    *   It should contain all the presentation logic and handle user interactions, but it **never** holds a direct reference to a `View` (Activity/Fragment).

3.  **View (UI Layer):**
    *   This is your `Activity` or `Fragment`. Its only job is to display data and forward user input to the `ViewModel`.
    *   The `View` **observes** `LiveData` objects in the `ViewModel` and updates the UI automatically when the data changes.

#### Flexibility in Your Module

While MVVM is the recommended default, you have the freedom to use the architecture you are most comfortable with **within your own assigned module**.

You are free to implement **MVC, MVP, MVI**, or any other standard architecture as long as it does not negatively impact other modules. The key is to keep your module's code clean, maintainable, and well-structured.

