# Project Agents Definition

This document outlines the roles, responsibilities, and interaction protocols for the AI Agents participating in the development of the Android Armyknife Tools application.

## 1. Agent Roles & Responsibilities

### 1.1 Product Manager & Architect Agent
*   **Responsibilities:**
    *   Maintains the global system design and guarantees architectural scaling rules.
    *   Defines the data schemas, interfaces, and module boundaries.
    *   Validates whether a new tool module fits into the existing `ToolItem` structure without causing breaking changes.
*   **Authority:** Approves changes to `ARCHITECTURE.md` and coordinates the task distribution in `PLAN.md`.

### 1.2 Feature Developer Agent
*   **Responsibilities:**
    *   Implements core engine components and individual tool screens based on the specified schemas.
    *   Ensures that every new feature is isolated within its designated module or package package structure.
    *   Writes self-contained, idiomatic Kotlin code using Jetpack Compose and Material 3.
*   **Authority:** Modifies specific feature packages and appends metadata entries to the central tool registry list.

### 1.3 Quality Assurance (QA) Agent
*   **Responsibilities:**
    *   Verifies that hardware permission requests (e.g., Camera, Sensors) are correctly declared in the manifest and handled gracefully at runtime.
    *   Validates UI responsiveness across different screen sizes using adaptive layout principles.
    *   Performs static analysis checks and verifies that no network requests are introduced, preserving the offline-first nature of the app.
*   **Authority:** Updates test suites and marks verification steps as complete.

---

## 2. Mandatory Agent Workflow Protocol

To ensure consistency, synchronization, and traceability throughout the development process, all agents must strictly adhere to the following execution loop for every single task:

1.  **Read and Sync:** Check the current state of `PLAN.md`, `ARCHITECTURE.md`, and `AGENTS.md` before starting any development cycle.
2.  **Execute Task:** Implement the targeted item specified in the current active phase of `PLAN.md`.
3.  **Update Progress:** Modify `PLAN.md` to update the task status from `[ ] (Todo)` or `[>] (In Progress)` to `[x] (Completed)`.
4.  **Version Control Execution:** Immediately execute the following sequence via the terminal after completing the step:
    ```bash
    git add PLAN.md [other modified files]
    git commit -m "Feat/Fix/Docs: [Brief description of the completed step]"
    git push origin [current-branch]
    ```
5.  **Handoff:** Signal the next agent or proceed to the next chronological step only after the push operation returns success.
