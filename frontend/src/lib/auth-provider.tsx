"use client";

import {
  createContext,
  type ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";
import {
  GoogleAuthProvider,
  createUserWithEmailAndPassword,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  type User,
} from "firebase/auth";
import { firebaseAuth, firebaseConfigured } from "./firebase";

type AuthContextValue = {
  user: User | null;
  ready: boolean;
  configured: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string) => Promise<void>;
  signInWithGoogle: () => Promise<void>;
  signOutUser: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(!firebaseConfigured);

  useEffect(() => {
    if (!firebaseAuth) return;
    return onAuthStateChanged(firebaseAuth, (nextUser) => {
      setUser(nextUser);
      setReady(true);
    });
  }, []);

  const value: AuthContextValue = {
    user,
    ready,
    configured: firebaseConfigured,
    signIn: async (email, password) => {
      if (!firebaseAuth) throw new Error("Firebase is not configured yet.");
      await signInWithEmailAndPassword(firebaseAuth, email, password);
    },
    signUp: async (email, password) => {
      if (!firebaseAuth) throw new Error("Firebase is not configured yet.");
      await createUserWithEmailAndPassword(firebaseAuth, email, password);
    },
    signInWithGoogle: async () => {
      if (!firebaseAuth) throw new Error("Firebase is not configured yet.");
      await signInWithPopup(firebaseAuth, new GoogleAuthProvider());
    },
    signOutUser: async () => {
      if (firebaseAuth) await signOut(firebaseAuth);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}