# Claude Code Agent Teams: Full-Stack Fitness App Demo

This project is a comprehensive demo of a full-stack application built using **Claude Code Agent Teams**. It demonstrates how to orchestrate seven specialized AI agents to handle parallel workstreams, manage shared contracts, and enforce dependency gating in a single terminal session.

## Overview
The application is a high-performance fitness tracker that manages the entire lifecycle of user health data—from workout logging and exercise discovery to nutrition tracking and goal setting. 

The build process utilizes a **7-agent "Dream Team"** running simultaneously in `tmux`, coordinated by a **16-task dependency map** that ensures backend APIs and database schemas are completed before frontend development begins.

## Tech Stack
*   **Mobile Frontend:** Flutter / Dart (iOS + Android)
*   **Backend API:** Java / Spring Boot WebFlux (Reactive REST)
*   **Database:** PostgreSQL
*   **Authentication:** Firebase Auth with JWT validation
*   **Notifications:** Firebase Cloud Messaging
*   **Deployment:** Docker containerization

## Key Features
*   **Workout & Exercise Tracking:** Create and log workout sessions, track sets, and search a comprehensive exercise library filterable by muscle group.
*   **Progress Metrics:** Monitor weekly stats, streaks, and personal records.
*   **Nutrition Logging:** Log daily macros including calories, protein, carbs, and fat.
*   **Goal Setting:** Set and track targets for body weight and workout frequency.
*   **Automated Quality Gates:** Integrated **code reviews** and **security audits** (Auth, OWASP, secrets scanning) are triggered automatically as modules are completed.

## Agent Team Architecture
The development is handled by a specialized team of agents, each defined by unique roles in `.claude/agents/`:

| Agent | Role | Responsibility |
| :--- | :--- | :--- |
| **team-lead** | Orchestrator | Manages the task board, spawns teammates, and resolves blockers. |
| **architect** | System Design | Owns architecture docs, API contracts, and Mermaid diagrams. |
| **db-designer** | Database | Manages PostgreSQL schema, migrations, and indexing. |
| **java-backend** | Backend Dev | Builds the Spring Boot WebFlux REST API. |
| **flutter-dev** | Mobile Dev | Develops the Flutter mobile application. |
| **code-reviewer** | Quality Assurance | Conducts incremental reviews of Java and Flutter code. |
| **security-auditor**| Security | Performs audits on auth modules and scans for vulnerabilities. |

## Development Workflow
1.  **Orchestration over Coordination:** Agents communicate directly via peer messaging to share contracts (e.g., the backend teammate sends JWT payloads directly to the frontend teammate), removing the need for a human "middleman".
2.  **Dependency Gating:** Tasks are strictly blocked until upstream requirements are met. For example, the `flutter-dev` teammate is blocked from building the workout screen until the `java-backend` teammate finishes the workout API.
3.  **Real-time Visibility:** The entire build runs in a split `tmux` session, allowing you to watch all seven agents work in parallel panes.

## How to Run the Build
This build is triggered by a single **Master Launch Prompt** that defines the team, the tech stack, and the dependency order. 

1. Start a new `tmux` session: `tmux new-session -s fitness-app`.
2. Launch `claude`.
3. Paste the **Master Launch Prompt** (provided in the playbook documentation) to spawn the team and begin the 16-task build process.
