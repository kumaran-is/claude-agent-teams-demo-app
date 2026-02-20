# Claude Code Agent Teams: Full-Stack Fitness App Demo


[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/kumaran-is/claude-agent-teams-demo-app)
[![Built with Claude Code](https://img.shields.io/badge/Built%20with-Claude%20Code-blueviolet?style=flat&logo=anthropic&logoColor=white)](https://docs.anthropic.com/en/docs/claude-code)
[![Blog: My Setup](https://img.shields.io/badge/Medium-My%20Claude%20Code%20Setup-000000?style=flat&logo=medium&logoColor=white)](https://medium.com/@kumaran.isk/claude-code-beyond-sub-agents-orchestrating-peer-to-peer-ai-with-agent-teams-3406d2169bfd)
[![Blog: Practices](https://img.shields.io/badge/Medium-Practices%20That%20Fixed%20My%20Workflow-000000?style=flat&logo=medium&logoColor=white)](https://medium.com/@kumaran.isk/claude-code-agent-teams-a-7-agent-full-stack-app-playbook-f584a7fa1a69)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?style=flat&logo=springboot&logoColor=white)
![Flutter](https://img.shields.io/badge/Flutter-3.38-02569B?style=flat&logo=flutter&logoColor=white)
![Dart](https://img.shields.io/badge/Dart-3.11-0175C2?style=flat&logo=dart&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-DD2C00?style=flat&logo=firebase&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

This project is a comprehensive demo of a full-stack application built using **Claude Code Agent Teams**. It demonstrates how to orchestrate seven specialized AI agents to handle parallel workstreams, manage shared contracts, and enforce dependency gating in a single terminal session.

## Overview
The application is a high-performance fitness tracker that manages the entire lifecycle of user health data—from workout logging and exercise discovery to nutrition tracking and goal setting. 
![Claude Code Master](./img/fitness_app_screens.png)

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

## Quick Start
Here's a ready-to-paste prompt into a Claude Code session to launch the fitness app:   

```json
  Launch the Fitness App for local development.                                                                                                                                                                           
                                                                                   
  ## What to do

  1. **Start Docker services** (PostgreSQL + Spring Boot backend):
     - cd into `backend/`
     - Run `docker-compose up -d`
     - PostgreSQL runs on port 5433 (not 5432 — conflict with another container)
     - Wait for both containers to be healthy

  2. **Run Flutter on Chrome with DevicePreview**:
     - cd into `mobile/`
     - Kill any existing flutter run processes on port 3457 first
     - Run: `flutter run -d chrome --web-port 3457`
     - DevicePreview is already configured in main.dart — no changes needed

  ## Known context
  - Flutter SDK: Dart 3.10.4 (pubspec uses `>=3.10.0`)
  - Demo mode: tap "Try Demo (skip login)" on the login screen to bypass Firebase auth
  - All screens use mock data in demo mode — no real backend connection needed to see data
  - build_runner outputs are already generated (*.g.dart / *.freezed.dart files exist)
  - If build_runner is needed: `dart run build_runner build --delete-conflicting-outputs`

  ## If flutter is already running
  - Check with: `lsof -i :3457`
  - Kill with: `kill -9 <PID>`
  - Then relaunch

  Open http://localhost:3457 in Chrome once flutter run shows the debug service URL.
```
