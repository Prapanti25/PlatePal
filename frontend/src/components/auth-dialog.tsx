"use client";

import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { useAuth } from "@/lib/auth-provider";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export function AuthDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const { configured, signIn, signUp, signInWithGoogle } = useAuth();
  const [mode, setMode] = useState<"sign-in" | "sign-up">("sign-in");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [pending, setPending] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    try {
      if (mode === "sign-up") await signUp(email, password);
      else await signIn(email, password);
      onOpenChange(false);
      toast.success(mode === "sign-up" ? "Account created" : "Welcome back");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Could not authenticate");
    } finally {
      setPending(false);
    }
  }

  async function googleSignIn() {
    setPending(true);
    try {
      await signInWithGoogle();
      onOpenChange(false);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Google sign-in failed");
    } finally {
      setPending(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{mode === "sign-up" ? "Create your PlatePal account" : "Sign in to PlatePal"}</DialogTitle>
          <DialogDescription>Use your account to sync your meal plan, groceries, and nutrition log.</DialogDescription>
        </DialogHeader>
        {!configured && (
          <p className="rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-950">
            Firebase is not configured. Add the Firebase values to <code>.env.local</code> and restart the frontend.
          </p>
        )}
        <form className="grid gap-3" onSubmit={submit}>
          <label className="grid gap-1.5 text-sm font-medium" htmlFor="auth-email">
            Email
            <Input id="auth-email" type="email" autoComplete="email" required value={email} onChange={(event) => setEmail(event.target.value)} disabled={!configured || pending} />
          </label>
          <label className="grid gap-1.5 text-sm font-medium" htmlFor="auth-password">
            Password
            <Input id="auth-password" type="password" autoComplete={mode === "sign-up" ? "new-password" : "current-password"} minLength={6} required value={password} onChange={(event) => setPassword(event.target.value)} disabled={!configured || pending} />
          </label>
          <Button type="submit" disabled={!configured || pending}>
            {pending ? "Please wait..." : mode === "sign-up" ? "Create account" : "Sign in with email"}
          </Button>
        </form>
        <Button variant="outline" onClick={googleSignIn} disabled={!configured || pending}>
          Continue with Google
        </Button>
        <p className="text-center text-sm text-muted-foreground">
          {mode === "sign-up" ? "Already registered?" : "New to PlatePal?"}{" "}
          <button
            className="font-semibold text-foreground underline underline-offset-4"
            onClick={() => setMode(mode === "sign-up" ? "sign-in" : "sign-up")}
            type="button"
          >
            {mode === "sign-up" ? "Sign in" : "Create an account"}
          </button>
        </p>
      </DialogContent>
    </Dialog>
  );
}