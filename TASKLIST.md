# 🚀 PlatePal Developer Sprint Checklist

## Phase 1: Environment & Setup (1.5 hrs)

- [ ] **Next.js Frontend:** Scaffold app, install Tailwind CSS, Shadcn UI, Firebase, Lucide icons, and set up `.env.local`.
- [ ] **Spring Boot Backend:** Initialize Java 17 service with Web, Security, JPA, Lombok, and PostgreSQL driver.
- [ ] **Database Setup:** Execute SQL DDL script to create `users`, `health_profiles`, `meal_plans`, `recipes`, `grocery_items`, and `logged_meals` tables.
- [ ] **Authentication Filter:** Implement `FirebaseJwtFilter` in Spring Security to decode JWT bearer tokens.

## Phase 2: Feature Engineering (3.5 hrs)

- [ ] **FR-1 Health Profile:** Build Next.js onboarding form & Spring Boot `POST /api/v1/profile` endpoint to persist user health targets and dietary tags.
- [ ] **FR-2 AI Meal Planner:** Implement OpenAI prompt builder for 7-day schedules + 5-second timeout fallback mechanism. Build 7-day tabbed calendar UI.
- [ ] **FR-3 Smart Grocery:** Build ingredient unit-aggregation logic, interactive category checklist UI, and Brevo API email dispatch (`POST /api/v1/grocery-list/email`).
- [ ] **FR-4 Visual Scanner:** Implement Cloudinary direct image upload client + Spring Boot Vision API macro extraction endpoint (`POST /api/v1/meals/log-image`).

## Phase 3: Deployment & Polish (1 hr)

- [ ] **Testing:** Verify end-to-end user flow from onboarding to meal plan generation and photo scanning.
- [ ] **Cloud Deployment:** Deploy Spring Boot backend and Next.js frontend services independently on Render.
