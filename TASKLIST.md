# 🚀 PlatePal Developer Sprint Checklist

## Phase 1: Environment & Setup (1.5 hrs)

### 1.1 Next.js Frontend

- [ ] Scaffold a Next.js app with TypeScript and Tailwind CSS.
- [ ] Install Shadcn UI components: Button, Card, Dialog, Input, Tabs, and Toast.
- [ ] Install Firebase, Axios, Lucide icons, and TanStack Query.
- [ ] Configure `.env.local` with Firebase, backend URL, and Cloudinary settings.

### 1.2 Spring Boot Backend

- [ ] Initialize a Java 17 Spring Boot service with Web, Security, JPA, Lombok, and PostgreSQL dependencies.
- [ ] Configure database credentials and third-party API secret placeholders.
- [ ] Configure CORS to allow requests from `http://localhost:3000`.

### 1.3 Database Setup

- [ ] Start a local or hosted PostgreSQL database.
- [ ] Create the `users`, `health_profiles`, `meal_plans`, `recipes`, `grocery_items`, and `logged_meals` tables.
- [ ] Add an index on `users.firebase_uid` and JSONB ingredient support to `recipes`.

## Phase 2: Feature Engineering (3.5 hrs)

### 2.1 Authentication & Health Profile (FR-1)

- [ ] Create a Firebase Web app and enable Email/Password and Google sign-in.
- [ ] Implement `FirebaseJwtFilter` to validate bearer tokens and populate the Spring Security context.
- [ ] Build onboarding fields for calorie targets, household size, weekly BDT budget, and dietary tags.
- [ ] Implement `POST /api/v1/profile` to persist user health targets and dietary tags.

### 2.2 AI Meal Planner (FR-2)

- [ ] Build an OpenAI prompt for a 7-day plan containing 21 meals and 7 snacks, using user health constraints.
- [ ] Parse and persist generated plans and recipes.
- [ ] Add a 5-second timeout and fallback plan for API errors or timeouts.
- [ ] Build a Monday-to-Sunday tabbed calendar with meal cards, prep times, and estimated BDT costs.

### 2.3 Smart Grocery (FR-3)

- [ ] Aggregate ingredient quantities with unit conversion across the active meal plan.
- [ ] Implement `GET /api/v1/grocery-list` and `PATCH /api/v1/grocery-list/{id}/pantry`.
- [ ] Build a category-grouped grocery checklist.
- [ ] Implement Brevo email dispatch at `POST /api/v1/grocery-list/email`.

### 2.4 Visual Scanner (FR-4)

- [ ] Configure a Cloudinary unsigned upload preset and build the photo upload flow.
- [ ] Implement `POST /api/v1/meals/log-image` to extract dish name, calories, and macros from the image.
- [ ] Save logged meals and display macro progress on the dashboard.

## Phase 3: Deployment & Polish (1 hr)

### 3.1 Integration Testing

- [ ] Verify the end-to-end flow: sign up, onboarding, meal plan, grocery list, and photo scan.
- [ ] Verify fallback behavior with an invalid or disabled OpenAI API key.
- [ ] Verify unauthenticated requests are rejected by JWT validation.

### 3.2 Render Deployment

- [ ] Deploy the Spring Boot backend and configure production secrets.
- [ ] Deploy the Next.js frontend with the production backend URL.
- [ ] Smoke-test the live application.
