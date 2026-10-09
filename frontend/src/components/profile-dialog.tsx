"use client";

import { useState, type FormEvent } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import type { HealthProfile } from "@/lib/types";

const dietaryOptions = ["HALAL", "DIABETIC_FRIENDLY", "VEGETARIAN", "VEGAN", "GLUTEN_FREE", "DAIRY_FREE"];

export function ProfileDialog({
  open,
  onOpenChange,
  initialProfile,
  onSave,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialProfile: HealthProfile;
  onSave: (profile: HealthProfile) => Promise<void>;
}) {
  const [profile, setProfile] = useState(initialProfile);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");

  function toggleTag(tag: string) {
    setProfile((current) => ({
      ...current,
      dietaryTags: current.dietaryTags.includes(tag)
        ? current.dietaryTags.filter((value) => value !== tag)
        : [...current.dietaryTags, tag],
    }));
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setPending(true);
    try {
      await onSave(profile);
      onOpenChange(false);
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Could not save profile");
    } finally {
      setPending(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Health profile</DialogTitle>
          <DialogDescription>Meal suggestions use these targets and restrictions. They are not medical advice.</DialogDescription>
        </DialogHeader>
        <form className="grid gap-4" onSubmit={submit}>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label className="grid gap-1.5 text-sm font-medium" htmlFor="calories">
              Daily calorie target
              <Input id="calories" type="number" min="500" max="10000" required value={profile.calorieTarget} onChange={(event) => setProfile({ ...profile, calorieTarget: Number(event.target.value) })} />
            </label>
            <label className="grid gap-1.5 text-sm font-medium" htmlFor="household">
              Household size
              <Input id="household" type="number" min="1" max="20" required value={profile.householdSize} onChange={(event) => setProfile({ ...profile, householdSize: Number(event.target.value) })} />
            </label>
          </div>
          <label className="grid gap-1.5 text-sm font-medium" htmlFor="budget">
            Weekly food budget (BDT)
            <Input id="budget" type="number" min="0" step="100" required value={profile.weeklyBudgetBdt} onChange={(event) => setProfile({ ...profile, weeklyBudgetBdt: Number(event.target.value) })} />
          </label>
          <fieldset className="grid gap-2">
            <legend className="text-sm font-medium">Dietary restrictions</legend>
            <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
              {dietaryOptions.map((tag) => (
                <label key={tag} className="flex min-h-10 items-center gap-2 rounded-md border bg-white px-2.5 text-xs font-medium">
                  <input type="checkbox" checked={profile.dietaryTags.includes(tag)} onChange={() => toggleTag(tag)} />
                  <span>{tag.replaceAll("_", " ")}</span>
                </label>
              ))}
            </div>
          </fieldset>
          {error && <p role="alert" className="text-sm text-red-700">{error}</p>}
          <Button type="submit" disabled={pending}>{pending ? "Saving..." : "Save health profile"}</Button>
        </form>
      </DialogContent>
    </Dialog>
  );
}