"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Apple,
  CalendarDays,
  Camera,
  ChevronRight,
  CircleUserRound,
  ClipboardList,
  Leaf,
  LogOut,
  Mail,
  Salad,
  Settings2,
  Sparkles,
  Wallet,
} from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { AuthDialog } from "@/components/auth-dialog";
import { MealScannerDialog } from "@/components/meal-scanner-dialog";
import { ProfileDialog } from "@/components/profile-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/lib/auth-provider";
import api from "@/lib/api";
import type { GroceryItem, HealthProfile, LoggedMeal, MealPlan, PlanDay, Recipe } from "@/lib/types";

type View = "today" | "plan" | "groceries";

const demoProfile: HealthProfile = {
  calorieTarget: 2000,
  householdSize: 2,
  weeklyBudgetBdt: 6500,
  dietaryTags: ["HALAL"],
};

const recipe = (mealType: string, name: string, cost: number, prepMinutes: number): Recipe => ({
  mealType,
  name,
  prepMinutes,
  estimatedCostBdt: cost,
  ingredients: [],
  instructions: [],
});

function localDateKey(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function makeDemoPlan(startDate: string): MealPlan {
  const monday = new Date(`${startDate}T12:00:00`);
  const mealSets = [
    ["Savory lentil oats", "Red lentil dal with brown rice", "Chickpea and pumpkin curry", "Guava"],
    ["Chickpea flour pancakes", "Vegetable khichuri", "Potato and green bean stew", "Cucumber sticks"],
    ["Banana chia oats", "Chickpea spinach bowl", "Vegetable lentil soup", "Papaya"],
    ["Savory rice bowl", "Pumpkin dal with spinach", "Chickpea cauliflower curry", "Orange"],
    ["Lentil spinach pancakes", "Vegetable chickpea rice", "Green bean lentil curry", "Apple"],
    ["Pumpkin oats", "Spinach chickpea dal", "Cauliflower lentil stew", "Guava"],
    ["Chickpea breakfast bowl", "Pumpkin lentil khichuri", "Mixed vegetable stew", "Carrot sticks"],
  ];
  const days: PlanDay[] = mealSets.map((set, index) => {
    const date = new Date(monday);
    date.setDate(monday.getDate() + index);
    return {
      dayNumber: index + 1,
      date: localDateKey(date),
      dayName: date.toLocaleDateString("en", { weekday: "long" }),
      meals: [recipe("BREAKFAST", set[0], 180, 20), recipe("LUNCH", set[1], 310, 35), recipe("DINNER", set[2], 340, 35)],
      snack: recipe("SNACK", set[3], 70, 5),
    };
  });
  return { id: 0, startsOn: days[0].date, endsOn: days[6].date, fallbackUsed: false, days };
}

const demoGroceries: GroceryItem[] = [
  { id: 1, name: "Brown rice", category: "Pantry", quantity: 1.2, unit: "kg", inPantry: false, checked: false },
  { id: 2, name: "Red lentils", category: "Pantry", quantity: 850, unit: "g", inPantry: true, checked: false },
  { id: 3, name: "Chickpeas", category: "Pantry", quantity: 650, unit: "g", inPantry: false, checked: false },
  { id: 4, name: "Spinach", category: "Produce", quantity: 420, unit: "g", inPantry: false, checked: false },
  { id: 5, name: "Pumpkin", category: "Produce", quantity: 700, unit: "g", inPantry: false, checked: false },
  { id: 6, name: "Carrot", category: "Produce", quantity: 380, unit: "g", inPantry: true, checked: false },
  { id: 7, name: "Olive oil", category: "Pantry", quantity: 120, unit: "ml", inPantry: true, checked: false },
  { id: 8, name: "Guava", category: "Produce", quantity: 3, unit: "piece", inPantry: false, checked: false },
];

const demoMeals: LoggedMeal[] = [{
  id: 1,
  imageUrl: "",
  dishName: "Vegetable dal bowl",
  estimatedCalories: 430,
  proteinGrams: 19,
  carbsGrams: 64,
  fatsGrams: 11,
  loggedAt: "2026-10-09T12:00:00Z",
}];

const initialEmptyProfile: HealthProfile = {
  calorieTarget: 2000,
  householdSize: 1,
  weeklyBudgetBdt: 0,
  dietaryTags: [],
};

function categoryGroup(category: string) {
  const value = category.toLowerCase();
  if (value.includes("produce") || value.includes("vegetable") || value.includes("fruit")) return "Produce";
  if (value.includes("meat") || value.includes("fish") || value.includes("protein")) return "Meat & Fish";
  if (value.includes("dairy")) return "Dairy";
  return "Spices & Pantry";
}

function formatBdt(value: number) {
  return new Intl.NumberFormat("en-BD", { maximumFractionDigits: 0 }).format(value);
}

export function PlatePalApp() {
  const { user, signOutUser } = useAuth();
  const queryClient = useQueryClient();
  const [view, setView] = useState<View>("today");
  const [authOpen, setAuthOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [scannerOpen, setScannerOpen] = useState(false);
  const [previewProfile, setPreviewProfile] = useState(demoProfile);
  const [previewItems, setPreviewItems] = useState(demoGroceries);
  const [previewPlan] = useState(() => makeDemoPlan("2026-10-05"));
  const [previewMeals] = useState<LoggedMeal[]>(demoMeals);
  const [selectedDay, setSelectedDay] = useState(0);
  const [emailRecipient, setEmailRecipient] = useState("");

  const profileQuery = useQuery({
    queryKey: ["profile"],
    queryFn: async () => (await api.get<HealthProfile>("/profile")).data,
    enabled: Boolean(user),
    retry: false,
  });
  const planQuery = useQuery({
    queryKey: ["active-plan"],
    queryFn: async () => (await api.get<MealPlan>("/meal-plans/active")).data,
    enabled: Boolean(user),
    retry: false,
  });
  const groceryQuery = useQuery({
    queryKey: ["grocery-list"],
    queryFn: async () => (await api.get<GroceryItem[]>("/grocery-list")).data,
    enabled: Boolean(user),
    retry: false,
  });
  const mealsQuery = useQuery({
    queryKey: ["recent-meals"],
    queryFn: async () => (await api.get<LoggedMeal[]>("/meals/today")).data,
    enabled: Boolean(user),
    retry: false,
  });

  const profile = profileQuery.data ?? (user ? initialEmptyProfile : previewProfile);
  const plan = planQuery.data ?? (!user ? previewPlan : null);
  const groceries = groceryQuery.data ?? (user ? [] : previewItems);
  const loggedMeals = mealsQuery.data ?? (user ? [] : previewMeals);

  const profileMutation = useMutation({
    mutationFn: async (value: HealthProfile) => api.post("/profile", value),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profile"] }),
  });
  const generateMutation = useMutation({
    mutationFn: async () => (await api.post<MealPlan>("/meal-plans/generate")).data,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["active-plan"] }),
        queryClient.invalidateQueries({ queryKey: ["grocery-list"] }),
      ]);
      setSelectedDay(0);
      toast.success("Your seven-day plan is ready");
    },
    onError: () => toast.error("Complete your profile and check your backend connection first"),
  });
  const pantryMutation = useMutation({
    mutationFn: async ({ id, inPantry }: { id: number; inPantry: boolean }) =>
      api.patch(`/grocery-list/${id}/pantry`, { inPantry }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["grocery-list"] }),
    onError: () => toast.error("Could not update pantry status"),
  });
  const emailMutation = useMutation({
    mutationFn: async (email: string) => api.post("/grocery-list/email", { email }),
    onSuccess: () => toast.success("Shopping list emailed"),
    onError: () => toast.error("Could not email the list. Check Brevo settings."),
  });

  async function saveProfile(value: HealthProfile) {
    if (user) {
      await profileMutation.mutateAsync(value);
      toast.success("Health profile saved");
    } else {
      setPreviewProfile(value);
      toast.success("Preview profile saved in this session");
    }
  }

  function togglePantry(item: GroceryItem) {
    if (user) {
      pantryMutation.mutate({ id: item.id, inPantry: !item.inPantry });
      return;
    }
    setPreviewItems((items) => items.map((entry) => entry.id === item.id ? { ...entry, inPantry: !entry.inPantry } : entry));
  }

  function requestPlan() {
    if (!user) {
      setAuthOpen(true);
      toast.info("Sign in to generate and save a meal plan");
      return;
    }
    if (!profileQuery.data) {
      setProfileOpen(true);
      toast.info("Save your health profile before generating a plan");
      return;
    }
    generateMutation.mutate();
  }

  function requestEmail() {
    if (!user) {
      setAuthOpen(true);
      return;
    }
    const recipient = emailRecipient || user.email || "";
    if (!recipient) {
      toast.error("Enter an email address for the shopping list");
      return;
    }
    emailMutation.mutate(recipient);
  }

  const selectedPlanDay = plan?.days[selectedDay] ?? null;
  const dailyCalories = loggedMeals.reduce((total, meal) => total + meal.estimatedCalories, 0);
  const calorieTarget = profile.calorieTarget || 2000;
  const progress = Math.min(100, Math.round((dailyCalories / calorieTarget) * 100));
  const weeklyEstimated = plan?.days.reduce(
    (sum, day) => sum + [...day.meals, day.snack].reduce((daySum, meal) => daySum + meal.estimatedCostBdt, 0),
    0,
  ) ?? 0;
  const weeklyBudgetProgress = profile.weeklyBudgetBdt > 0
    ? Math.min(100, Math.round((weeklyEstimated / profile.weeklyBudgetBdt) * 100))
    : 0;
  const groupedGroceries = groceries.reduce<Record<string, GroceryItem[]>>((groups, item) => {
    const group = categoryGroup(item.category);
    groups[group] = [...(groups[group] ?? []), item];
    return groups;
  }, {});

  const navigation = [
    { id: "today" as const, label: "Today", icon: Leaf },
    { id: "plan" as const, label: "Meal plan", icon: CalendarDays },
    { id: "groceries" as const, label: "Groceries", icon: ClipboardList },
  ];

  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="sticky top-0 z-30 border-b bg-background/95 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-[1440px] items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
          <a href="#home" className="flex shrink-0 items-center gap-2.5" aria-label="PlatePal home">
            <span className="grid size-9 place-items-center rounded-md bg-primary text-primary-foreground"><Salad className="size-5" aria-hidden="true" /></span>
            <span className="font-heading text-lg font-bold">PlatePal</span>
          </a>
          <nav className="hidden items-center gap-1 md:flex" aria-label="Main navigation">
            {navigation.map(({ id, label, icon: Icon }) => (
              <Button key={id} variant={view === id ? "secondary" : "ghost"} onClick={() => setView(id)} aria-current={view === id ? "page" : undefined}>
                <Icon aria-hidden="true" /> {label}
              </Button>
            ))}
          </nav>
          <div className="flex items-center gap-2">
            {user ? (
              <>
                <span className="hidden max-w-36 truncate text-sm text-muted-foreground sm:inline">{user.displayName || user.email}</span>
                <Button variant="outline" size="sm" onClick={() => void signOutUser()}><LogOut aria-hidden="true" /> Sign out</Button>
              </>
            ) : (
              <Button variant="outline" size="sm" onClick={() => setAuthOpen(true)}><CircleUserRound aria-hidden="true" /> Sign in</Button>
            )}
          </div>
        </div>
        <nav className="flex justify-center gap-1 border-t px-2 py-1 md:hidden" aria-label="Mobile navigation">
          {navigation.map(({ id, label, icon: Icon }) => (
            <Button key={id} size="sm" variant={view === id ? "secondary" : "ghost"} onClick={() => setView(id)} aria-current={view === id ? "page" : undefined}>
              <Icon aria-hidden="true" /> {label}
            </Button>
          ))}
        </nav>
      </header>

      <div className="mx-auto grid max-w-[1440px] lg:grid-cols-[220px_minmax(0,1fr)]">
        <aside className="hidden min-h-[calc(100vh-4rem)] border-r px-4 py-7 lg:block">
          <p className="px-3 text-[11px] font-semibold uppercase tracking-[0.12em] text-muted-foreground">Your kitchen</p>
          <nav className="mt-3 grid gap-1" aria-label="Workspace">
            {navigation.map(({ id, label, icon: Icon }) => (
              <button key={id} onClick={() => setView(id)} aria-current={view === id ? "page" : undefined} className={`flex min-h-10 items-center gap-3 rounded-md px-3 text-left text-sm font-medium transition-colors ${view === id ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-secondary hover:text-foreground"}`}>
                <Icon className="size-4" aria-hidden="true" /> {label}
              </button>
            ))}
          </nav>
          <div className="mt-9 border-t pt-5">
            <p className="px-3 text-[11px] font-semibold uppercase tracking-[0.12em] text-muted-foreground">Household</p>
            <button onClick={() => setProfileOpen(true)} className="mt-3 flex w-full items-center gap-3 rounded-md px-3 py-2 text-left text-sm hover:bg-secondary">
              <span className="grid size-8 place-items-center rounded-full bg-accent text-accent-foreground"><Settings2 className="size-4" aria-hidden="true" /></span>
              <span className="min-w-0 flex-1"><span className="block font-medium">Health profile</span><span className="block truncate text-xs text-muted-foreground">{profile.householdSize} people · {profile.dietaryTags.length} tags</span></span>
              <ChevronRight className="size-4 text-muted-foreground" aria-hidden="true" />
            </button>
          </div>
          <div className="mt-auto pt-10">
            <p className="px-3 text-xs leading-5 text-muted-foreground">Meals are suggestions, not medical advice.</p>
          </div>
        </aside>

        <main id="home" className="min-w-0 px-4 py-6 sm:px-6 sm:py-8 lg:px-10">
          <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm font-medium text-primary">Your kitchen, in rhythm</p>
              <h1 className="mt-1 font-heading text-3xl font-semibold tracking-tight sm:text-4xl">A good week starts here.</h1>
              <p className="mt-2 max-w-2xl text-sm text-muted-foreground">Plan nourishing meals, keep your shopping in order, and notice your daily balance.</p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="outline" onClick={() => setProfileOpen(true)}><Settings2 aria-hidden="true" /> Health profile</Button>
              <Button onClick={() => setScannerOpen(true)}><Camera aria-hidden="true" /> Log a meal</Button>
            </div>
          </div>

          {!user && (
            <div className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-md border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-950">
              <p><strong>Preview mode.</strong> The dashboard uses sample data until Firebase is configured and you sign in.</p>
              <Button variant="outline" size="sm" onClick={() => setAuthOpen(true)}>Connect account</Button>
            </div>
          )}
          {user && !profileQuery.data && !profileQuery.isLoading && (
            <div className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-md border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-950">
              <p><strong>Set up your health profile</strong> to personalize meal plans.</p>
              <Button variant="outline" size="sm" onClick={() => setProfileOpen(true)}>Complete profile</Button>
            </div>
          )}

          {view === "today" && (
            <div className="grid gap-7 xl:grid-cols-[minmax(0,1.55fr)_minmax(280px,0.8fr)]">
              <div className="min-w-0">
                <section className="grid gap-5 border-b pb-6 sm:grid-cols-[1fr_auto] sm:items-center">
                  <div>
                    <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground"><Apple className="size-4 text-accent-foreground" aria-hidden="true" /> TODAY&apos;S BALANCE</div>
                    <div className="mt-3 flex flex-wrap items-baseline gap-x-2">
                      <span className="font-heading text-4xl font-semibold tabular-nums">{dailyCalories.toLocaleString()}</span>
                      <span className="text-sm text-muted-foreground">of {calorieTarget.toLocaleString()} kcal</span>
                    </div>
                    <div className="mt-3 h-2 max-w-xl overflow-hidden rounded-full bg-secondary" role="progressbar" aria-label="Daily calorie target" aria-valuenow={dailyCalories} aria-valuemin={0} aria-valuemax={calorieTarget}>
                      <div className="h-full rounded-full bg-primary transition-[width]" style={{ width: `${progress}%` }} />
                    </div>
                    <p className="mt-2 text-xs text-muted-foreground">{Math.max(calorieTarget - dailyCalories, 0).toLocaleString()} kcal remaining in today&apos;s target</p>
                  </div>
                  <div className="grid grid-cols-3 gap-4 sm:min-w-64">
                    <MacroValue label="Protein" amount={loggedMeals.reduce((sum, meal) => sum + meal.proteinGrams, 0)} color="bg-primary" />
                    <MacroValue label="Carbs" amount={loggedMeals.reduce((sum, meal) => sum + meal.carbsGrams, 0)} color="bg-[#d59a30]" />
                    <MacroValue label="Fats" amount={loggedMeals.reduce((sum, meal) => sum + meal.fatsGrams, 0)} color="bg-[#c86a4e]" />
                  </div>
                </section>

                <section className="py-6">
                  <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">{selectedPlanDay?.dayName ?? "This week"}</p>
                      <h2 className="mt-1 font-heading text-2xl font-semibold">On the menu</h2>
                    </div>
                    <Button variant="outline" size="sm" onClick={requestPlan} disabled={generateMutation.isPending}>
                      <Sparkles aria-hidden="true" /> {generateMutation.isPending ? "Planning..." : planQuery.data ? "Regenerate week" : "Generate week"}
                    </Button>
                  </div>
                  {selectedPlanDay ? (
                    <div className="divide-y border-y">
                      {[...selectedPlanDay.meals, selectedPlanDay.snack].map((meal) => <MealRow key={`${meal.mealType}-${meal.name}`} meal={meal} />)}
                    </div>
                  ) : (
                    <EmptyState title="No meal plan yet" detail="Complete your health profile, then generate a seven-day plan." action={<Button onClick={requestPlan}><Sparkles aria-hidden="true" /> Generate meal plan</Button>} />
                  )}
                </section>

                <section className="grid gap-4 border-t pt-5 sm:grid-cols-[minmax(0,1fr)_210px] sm:items-center">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">A little inspiration</p>
                    <h2 className="mt-1 font-heading text-xl font-semibold">Color your plate.</h2>
                    <p className="mt-1 text-sm text-muted-foreground">A mix of grains, vegetables, and protein can make everyday meals more satisfying.</p>
                  </div>
                  <div role="img" aria-label="A fresh bowl of colorful vegetables" className="h-28 rounded-md bg-cover bg-center" style={{ backgroundImage: "url('https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=700&q=80')" }} />
                </section>
              </div>

              <aside className="grid content-start gap-6 border-t pt-6 xl:border-l xl:border-t-0 xl:pl-7 xl:pt-0">
                <section>
                    <div className="flex items-center justify-between gap-3">
                    <div><p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">Weekly budget</p><h2 className="mt-1 font-heading text-2xl font-semibold">৳{formatBdt(weeklyEstimated)}</h2></div>
                    <span className="grid size-11 place-items-center rounded-md bg-secondary text-primary"><Wallet className="size-5" aria-hidden="true" /></span>
                  </div>
                  <p className="mt-2 text-sm text-muted-foreground">Estimated against a ৳{formatBdt(profile.weeklyBudgetBdt)} household budget.</p>
                  <div className="mt-3 h-2 overflow-hidden rounded-full bg-secondary"><div className="h-full bg-[#d59a30]" style={{ width: `${weeklyBudgetProgress}%` }} /></div>
                </section>

                <section className="border-t pt-5">
                  <div className="flex items-center justify-between">
                    <div><p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">Shopping list</p><h2 className="mt-1 font-heading text-xl font-semibold">{groceries.length} ingredients</h2></div>
                    <Button variant="ghost" size="sm" onClick={() => setView("groceries")}>View list <ChevronRight aria-hidden="true" /></Button>
                  </div>
                  <div className="mt-3 divide-y border-y">
                    {groceries.slice(0, 5).map((item) => <label key={item.id} className="flex min-h-11 items-center gap-3 py-2 text-sm">
                      <input type="checkbox" checked={item.inPantry} onChange={() => togglePantry(item)} aria-label={`${item.name} already in pantry`} className="size-4 accent-primary" />
                      <span className={`min-w-0 flex-1 truncate ${item.inPantry ? "text-muted-foreground line-through" : ""}`}>{item.name}</span>
                      <span className="shrink-0 text-xs text-muted-foreground">{item.quantity} {item.unit}</span>
                    </label>)}
                    {!groceries.length && <p className="py-4 text-sm text-muted-foreground">Generate a meal plan to build your list.</p>}
                  </div>
                </section>

                <section className="border-t pt-5">
                  <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">Household</p>
                  <div className="mt-3 flex items-center justify-between gap-3">
                    <span className="text-sm">{profile.householdSize} people · {profile.dietaryTags.length ? profile.dietaryTags.join(", ").replaceAll("_", " ") : "No restrictions"}</span>
                    <Button variant="outline" size="icon" aria-label="Edit health profile" title="Edit health profile" onClick={() => setProfileOpen(true)}><Settings2 aria-hidden="true" /></Button>
                  </div>
                </section>
              </aside>
            </div>
          )}

          {view === "plan" && (
            <section>
              <div className="mb-5 flex flex-wrap items-end justify-between gap-4">
                <div><p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">Seven-day calendar</p><h2 className="mt-1 font-heading text-3xl font-semibold">Meal plan</h2></div>
                <Button onClick={requestPlan} disabled={generateMutation.isPending}><Sparkles aria-hidden="true" /> {generateMutation.isPending ? "Planning..." : "Generate 7 days"}</Button>
              </div>
              {plan ? <>
                {plan.fallbackUsed && <p className="mb-4 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-950">Showing the local fallback plan because the AI service was unavailable.</p>}
                <div className="flex gap-1 overflow-x-auto border-b pb-2" role="tablist" aria-label="Days of the week">
                  {plan.days.map((day, index) => <button key={day.date} role="tab" aria-selected={index === selectedDay} onClick={() => setSelectedDay(index)} className={`min-w-16 rounded-md px-3 py-2 text-sm font-medium ${index === selectedDay ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-secondary"}`}>
                    <span className="block text-xs opacity-75">{day.dayName.slice(0, 3)}</span>{new Date(`${day.date}T12:00:00`).getDate()}
                  </button>)}
                </div>
                {selectedPlanDay && <div className="grid gap-5 pt-5 md:grid-cols-2">
                  {[...selectedPlanDay.meals, selectedPlanDay.snack].map((meal) => <article key={`${meal.mealType}-${meal.name}`} className="border-b pb-4">
                    <div className="flex items-start justify-between gap-3"><div><p className="text-xs font-semibold uppercase tracking-[0.1em] text-primary">{meal.mealType}</p><h3 className="mt-1 font-heading text-lg font-semibold">{meal.name}</h3></div><span className="shrink-0 text-sm text-muted-foreground">৳{formatBdt(meal.estimatedCostBdt)}</span></div>
                    <p className="mt-2 text-sm text-muted-foreground">{meal.prepMinutes} min prep · {meal.ingredients.length} ingredients</p>
                    {meal.ingredients.length > 0 && <p className="mt-2 text-xs leading-5 text-muted-foreground">{meal.ingredients.map((ingredient) => `${ingredient.quantity} ${ingredient.unit} ${ingredient.name}`).join(" · ")}</p>}
                  </article>)}
                </div>}
              </> : <EmptyState title="Your calendar is ready" detail="Save a health profile to generate a personalized week." action={<Button onClick={() => setProfileOpen(true)}>Set health targets</Button>} />}
            </section>
          )}

          {view === "groceries" && (
            <section className="max-w-4xl">
              <div className="mb-5 flex flex-wrap items-end justify-between gap-4">
                <div><p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">From your active meal plan</p><h2 className="mt-1 font-heading text-3xl font-semibold">Grocery list</h2></div>
                <span className="text-sm text-muted-foreground">{groceries.filter((item) => item.inPantry).length} already in pantry</span>
              </div>
              {Object.entries(groupedGroceries).map(([group, items]) => <details key={group} open className="border-t last:border-b">
                <summary className="flex min-h-12 cursor-pointer list-none items-center gap-2 py-3 font-semibold marker:hidden"><span className="flex-1">{group}</span><span className="mr-2 text-xs font-normal text-muted-foreground">{items.length} items</span><ChevronRight className="size-4 rotate-90 text-muted-foreground" aria-hidden="true" /></summary>
                <div className="divide-y pb-2">
                  {items.map((item) => <label key={item.id} className="flex min-h-12 items-center gap-3 py-2 text-sm">
                    <input type="checkbox" checked={item.inPantry} onChange={() => togglePantry(item)} aria-label={`${item.name} already in pantry`} className="size-4 accent-primary" />
                    <span className={`min-w-0 flex-1 ${item.inPantry ? "text-muted-foreground line-through" : ""}`}>{item.name}<span className="ml-2 text-xs text-muted-foreground">{item.category}</span></span>
                    <span className="shrink-0 tabular-nums">{item.quantity} {item.unit}</span>
                  </label>)}
                </div>
              </details>)}
              {!groceries.length && <EmptyState title="No ingredients yet" detail="Generate a meal plan and the grocery list will be aggregated for you." action={<Button onClick={requestPlan}><Sparkles aria-hidden="true" /> Generate plan</Button>} />}
              <form className="mt-7 flex flex-col gap-2 border-t pt-5 sm:flex-row" onSubmit={(event) => { event.preventDefault(); requestEmail(); }}>
                <label className="sr-only" htmlFor="shopping-email">Email shopping list to</label>
                <Input id="shopping-email" type="email" placeholder="Email shopping list to" value={emailRecipient || user?.email || ""} onChange={(event) => setEmailRecipient(event.target.value)} />
                <Button type="submit" variant="outline" disabled={emailMutation.isPending || !groceries.length}><Mail aria-hidden="true" /> {emailMutation.isPending ? "Sending..." : "Email list"}</Button>
              </form>
            </section>
          )}

          {user && (profileQuery.isError || planQuery.isError || groceryQuery.isError) && (
            <p role="status" className="mt-6 border-t pt-3 text-sm text-muted-foreground">Some synced data could not be loaded. Check the backend connection and your saved profile.</p>
          )}
        </main>
      </div>

      <AuthDialog open={authOpen} onOpenChange={setAuthOpen} />
      <ProfileDialog
        key={`${profile.calorieTarget}-${profile.householdSize}-${profile.weeklyBudgetBdt}-${profile.dietaryTags.join(",")}`}
        open={profileOpen}
        onOpenChange={setProfileOpen}
        initialProfile={profile}
        onSave={saveProfile}
      />
      <MealScannerDialog
        open={scannerOpen}
        onOpenChange={setScannerOpen}
        onLogged={() => queryClient.invalidateQueries({ queryKey: ["recent-meals"] })}
      />
    </div>
  );
}

function MacroValue({ label, amount, color }: { label: string; amount: number; color: string }) {
  return <div className="min-w-16"><p className="text-xs text-muted-foreground">{label}</p><p className="mt-1 font-semibold tabular-nums">{Math.round(amount)}<span className="ml-0.5 text-xs font-normal text-muted-foreground">g</span></p><div className="mt-2 h-1 rounded-full bg-secondary"><div className={`h-full rounded-full ${color}`} style={{ width: `${Math.min(100, amount)}%` }} /></div></div>;
}

function MealRow({ meal }: { meal: Recipe }) {
  return <article className="flex min-h-[76px] items-center gap-4 py-3">
    <span className="grid size-10 shrink-0 place-items-center rounded-md bg-secondary text-primary"><Leaf className="size-4" aria-hidden="true" /></span>
    <div className="min-w-0 flex-1"><p className="text-[11px] font-semibold uppercase tracking-[0.1em] text-muted-foreground">{meal.mealType}</p><h3 className="truncate font-medium">{meal.name}</h3><p className="text-xs text-muted-foreground">{meal.prepMinutes} min · ৳{formatBdt(meal.estimatedCostBdt)}</p></div>
    <ChevronRight className="size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
  </article>;
}

function EmptyState({ title, detail, action }: { title: string; detail: string; action: React.ReactNode }) {
  return <div className="grid justify-items-start gap-2 border-y py-7"><p className="font-heading text-lg font-semibold">{title}</p><p className="max-w-lg text-sm text-muted-foreground">{detail}</p><div className="mt-2">{action}</div></div>;
}