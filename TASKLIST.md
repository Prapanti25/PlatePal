# 🚀 PlatePal Developer Sprint Checklist

## Phase 1: Environment & Setup (1.5 hrs)

### 1.1 Next.js Frontend

- [x] Scaffold a Next.js app with TypeScript and Tailwind CSS.
- [x] Install Shadcn UI components: Button, Card, Dialog, Input, Tabs, and Sonner toast.
- [x] Install Firebase, Axios, Lucide icons, and TanStack Query.
- [x] Add `.env.local` and `.env.example` with the local backend URL and Firebase/Cloudinary placeholders.
- [ ] Fill in Firebase and Cloudinary project values.

### 1.2 Spring Boot Backend

- [x] Initialize a Java 17 Spring Boot service with Web, Security, JPA, Lombok, and PostgreSQL dependencies.
- [x] Configure environment-backed database settings and third-party API secret placeholders.
- [x] Configure CORS to allow requests from `http://localhost:3000`.

### 1.3 Database Setup

- [ ] Start a local or hosted PostgreSQL database.
- [x] Add `backend/src/main/resources/schema.sql` defining the six core tables.
- [ ] Apply the schema to a running PostgreSQL database.
- [x] Add an index on `users.firebase_uid` and JSONB ingredient support to `recipes`.

## Phase 2: Feature Engineering (3.5 hrs)

### 2.1 Authentication & Health Profile (FR-1)

- [ ] Create a Firebase Web app and enable Email/Password and Google sign-in.
- [x] Implement `FirebaseJwtFilter` to validate bearer tokens and populate the Spring Security context.
- [x] Build onboarding fields for calorie targets, household size, weekly BDT budget, and dietary tags.
- [x] Implement `POST /api/v1/profile` to persist user health targets and dietary tags.

### 2.2 AI Meal Planner (FR-2)

- [x] Build an OpenAI prompt for a 7-day plan containing 21 meals and 7 snacks, using user health constraints.
- [x] Parse and persist generated plans and recipes.
- [x] Add a 5-second timeout and fallback plan for API errors or timeouts.
- [x] Build a Monday-to-Sunday tabbed calendar with meal cards, prep times, and estimated BDT costs.

### 2.3 Smart Grocery (FR-3)

- [x] Aggregate ingredient quantities with unit conversion across the active meal plan.
- [x] Implement `GET /api/v1/grocery-list` and `PATCH /api/v1/grocery-list/{id}/pantry`.
- [x] Build a category-grouped grocery checklist.
- [x] Implement Brevo email dispatch at `POST /api/v1/grocery-list/email`.

### 2.4 Visual Scanner (FR-4)

- [ ] Configure a Cloudinary unsigned upload preset.
- [x] Build the direct Cloudinary upload flow with drag-and-drop and camera input.
- [x] Implement `POST /api/v1/meals/log-image` to extract dish name, calories, and macros from the image.
- [x] Save logged meals and display same-day macro progress on the dashboard.

Provider credentials and a running PostgreSQL database are still required to verify live integrations.

## Phase 3: Deployment & Polish (1 hr)

### 3.1 Integration Testing

- [x] Add GitHub Actions checks for frontend lint/build and credential-independent backend tests.
- [ ] Verify the end-to-end flow: sign up, onboarding, meal plan, grocery list, and photo scan.
- [ ] Verify fallback behavior with an invalid or disabled OpenAI API key.
- [ ] Verify unauthenticated requests are rejected by JWT validation.

### 3.2 Render Deployment

- [x] Add a Java 17 backend Dockerfile and Render Blueprint for the frontend, backend, and PostgreSQL service.
- [ ] Deploy the Spring Boot backend and configure production secrets.
- [ ] Deploy the Next.js frontend with the production backend URL.
- [ ] Smoke-test the live application.
