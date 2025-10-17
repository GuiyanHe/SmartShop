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