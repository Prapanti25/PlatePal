export type HealthProfile = {
  id?: number;
  firebaseUid?: string;
  calorieTarget: number;
  householdSize: number;
  weeklyBudgetBdt: number;
  dietaryTags: string[];
};

export type Ingredient = {
  name: string;
  category: string;
  quantity: number;
  unit: string;
};

export type Recipe = {
  id?: number;
  mealType: string;
  name: string;
  prepMinutes: number;
  estimatedCostBdt: number;
  ingredients: Ingredient[];
  instructions: string[];
};

export type PlanDay = {
  dayNumber: number;
  date: string;
  dayName: string;
  meals: Recipe[];
  snack: Recipe;
};

export type MealPlan = {
  id: number;
  startsOn: string;
  endsOn: string;
  fallbackUsed: boolean;
  days: PlanDay[];
};

export type GroceryItem = {
  id: number;
  name: string;
  category: string;
  quantity: number;
  unit: string;
  inPantry: boolean;
  checked: boolean;
};

export type LoggedMeal = {
  id: number;
  imageUrl: string;
  dishName: string;
  estimatedCalories: number;
  proteinGrams: number;
  carbsGrams: number;
  fatsGrams: number;
  loggedAt: string;
};