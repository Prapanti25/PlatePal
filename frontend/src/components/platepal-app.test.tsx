import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it } from "vitest";
import { PlatePalApp } from "./platepal-app";
import { AuthProvider } from "@/lib/auth-provider";

afterEach(cleanup);

function renderApp() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <PlatePalApp />
      </AuthProvider>
    </QueryClientProvider>,
  );
}

describe("PlatePal dashboard", () => {
  it("renders the preview dashboard and sample meal data", () => {
    renderApp();

    expect(screen.getByRole("heading", { name: "A good week starts here." })).toBeTruthy();
    expect(screen.getByText(/Preview mode/)).toBeTruthy();
    expect(screen.getByRole("heading", { name: "Savory lentil oats" })).toBeTruthy();
    expect(screen.getByRole("progressbar", { name: "Daily calorie target" })).toBeTruthy();
  });

  it("opens the seven-day plan and switches calendar days", async () => {
    const user = userEvent.setup();
    renderApp();
    const navigation = within(screen.getByRole("navigation", { name: "Main navigation" }));
    await user.click(navigation.getByRole("button", { name: "Meal plan" }));

    expect(screen.getAllByRole("tab")).toHaveLength(7);
    expect(screen.getByRole("heading", { name: "Savory lentil oats" })).toBeTruthy();
    await user.click(screen.getByRole("tab", { name: /Tue/ }));
    expect(screen.getByRole("heading", { name: "Chickpea flour pancakes" })).toBeTruthy();
  });

  it("toggles pantry state in the grocery view", async () => {
    const user = userEvent.setup();
    renderApp();
    const navigation = within(screen.getByRole("navigation", { name: "Main navigation" }));
    await user.click(navigation.getByRole("button", { name: "Groceries" }));
    const rice = screen.getByRole("checkbox", { name: "Brown rice already in pantry" });

    expect((rice as HTMLInputElement).checked).toBe(false);
    await user.click(rice);
    expect((rice as HTMLInputElement).checked).toBe(true);
  });

  it("saves preview health profile values for the current session", async () => {
    const user = userEvent.setup();
    renderApp();
    await user.click(within(screen.getByRole("main")).getByRole("button", { name: "Health profile" }));
    const calories = screen.getByRole("spinbutton", { name: "Daily calorie target" });
    await user.clear(calories);
    await user.type(calories, "2200");
    await user.click(screen.getByRole("button", { name: "Save health profile" }));

    await waitFor(() => expect(screen.queryByRole("dialog")).toBeNull());
    expect(screen.getByText("of 2,200 kcal")).toBeTruthy();
  });

  it("disables sign-in when Firebase configuration is missing", async () => {
    const user = userEvent.setup();
    renderApp();
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    const dialog = screen.getByRole("dialog");

    expect(within(dialog).getByText(/Firebase is not configured/)).toBeTruthy();
    expect((within(dialog).getByRole("textbox", { name: "Email" }) as HTMLInputElement).disabled).toBe(true);
    expect(within(dialog).getByRole("button", { name: "Continue with Google" }).hasAttribute("disabled")).toBe(true);
  });

  it("requires sign-in before generating a saved meal plan", async () => {
    const user = userEvent.setup();
    renderApp();
    await user.click(screen.getByRole("button", { name: "Generate week" }));

    expect(within(screen.getByRole("dialog")).getByText(/Firebase is not configured/)).toBeTruthy();
  });

  it("requires sign-in before emailing a grocery list", async () => {
    const user = userEvent.setup();
    renderApp();
    await user.click(within(screen.getByRole("navigation", { name: "Main navigation" })).getByRole("button", { name: "Groceries" }));
    await user.click(screen.getByRole("button", { name: "Email list" }));

    expect(within(screen.getByRole("dialog")).getByText(/Firebase is not configured/)).toBeTruthy();
  });

  it("explains why meal scanning is unavailable without Cloudinary setup", async () => {
    const user = userEvent.setup();
    renderApp();
    await user.click(screen.getByRole("button", { name: "Log a meal" }));
    const dialog = screen.getByRole("dialog");

    expect(within(dialog).getByText(/Cloudinary is not configured/)).toBeTruthy();
    expect(within(dialog).getByText(/Sign in to upload and save a meal/)).toBeTruthy();
    expect((within(dialog).getByRole("button", { name: "Analyze meal" }) as HTMLButtonElement).disabled).toBe(true);
  });
});