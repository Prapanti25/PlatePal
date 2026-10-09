"use client";

import axios from "axios";
import { Camera, ImagePlus, ScanLine } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import Image from "next/image";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import api from "@/lib/api";
import { useAuth } from "@/lib/auth-provider";
import type { LoggedMeal } from "@/lib/types";

export function MealScannerDialog({
  open,
  onOpenChange,
  onLogged,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onLogged: (meal: LoggedMeal) => void;
}) {
  const { user } = useAuth();
  const inputRef = useRef<HTMLInputElement>(null);
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [result, setResult] = useState<LoggedMeal | null>(null);
  const [pending, setPending] = useState(false);

  const cloudName = process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;
  const uploadPreset = process.env.NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET;

  useEffect(() => {
    if (!previewUrl) return;
    return () => URL.revokeObjectURL(previewUrl);
  }, [previewUrl]);

  function chooseFile(nextFile?: File) {
    if (!nextFile) return;
    if (!nextFile.type.startsWith("image/")) {
      toast.error("Choose an image file");
      return;
    }
    if (nextFile.size > 10 * 1024 * 1024) {
      toast.error("Image must be 10 MB or smaller");
      return;
    }
    setResult(null);
    setFile(nextFile);
    setPreviewUrl(URL.createObjectURL(nextFile));
  }

  async function scanMeal() {
    if (!user) {
      toast.error("Sign in before logging a meal");
      return;
    }
    if (!file || !cloudName || !uploadPreset) {
      toast.error("Add a photo and configure Cloudinary upload settings");
      return;
    }
    setPending(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("upload_preset", uploadPreset);
      const upload = await axios.post<{ secure_url: string }>(
        `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
        formData,
      );
      const response = await api.post<LoggedMeal>("/meals/log-image", { imageUrl: upload.data.secure_url });
      setResult(response.data);
      onLogged(response.data);
      toast.success("Meal analyzed and logged");
    } catch (error) {
      const message = axios.isAxiosError(error)
        ? (error.response?.data as { detail?: string; message?: string } | undefined)?.detail
          ?? (error.response?.data as { message?: string } | undefined)?.message
          ?? "Could not analyze this meal"
        : "Could not upload this image";
      toast.error(message);
    } finally {
      setPending(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>Log a meal photo</DialogTitle>
          <DialogDescription>Nutrition values are visual estimates. Confirm them against your serving and needs.</DialogDescription>
        </DialogHeader>
        {!user && <p className="rounded-md bg-amber-50 px-3 py-2 text-sm text-amber-950">Sign in to upload and save a meal.</p>}
        {(!cloudName || !uploadPreset) && <p className="rounded-md bg-amber-50 px-3 py-2 text-sm text-amber-950">Cloudinary is not configured in <code>.env.local</code>.</p>}
        <div
          className="grid min-h-52 place-items-center overflow-hidden rounded-md border-2 border-dashed border-border bg-muted/40 p-4 text-center"
          onDragOver={(event) => event.preventDefault()}
          onDrop={(event) => {
            event.preventDefault();
            chooseFile(event.dataTransfer.files[0]);
          }}
        >
          {previewUrl ? (
            <div className="relative h-64 w-full">
              <Image src={previewUrl} alt="Selected meal" fill unoptimized className="rounded-md object-contain" />
            </div>
          ) : (
            <div className="grid justify-items-center gap-2 text-muted-foreground">
              <ImagePlus className="size-8" aria-hidden="true" />
              <p className="text-sm">Drop a meal photo here or choose one below.</p>
            </div>
          )}
        </div>
        <Input
          ref={inputRef}
          className="sr-only"
          type="file"
          accept="image/*"
          capture="environment"
          onChange={(event) => chooseFile(event.target.files?.[0])}
        />
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => inputRef.current?.click()}>
            <ImagePlus aria-hidden="true" /> Choose photo
          </Button>
          <Button variant="outline" onClick={() => inputRef.current?.click()}>
            <Camera aria-hidden="true" /> Use camera
          </Button>
          <Button onClick={scanMeal} disabled={!file || pending || !user || !cloudName || !uploadPreset}>
            <ScanLine aria-hidden="true" /> {pending ? "Analyzing..." : "Analyze meal"}
          </Button>
        </div>
        {result && (
          <div className="grid grid-cols-2 gap-3 border-t pt-4 sm:grid-cols-4">
            <div><p className="text-xs text-muted-foreground">Dish</p><p className="font-semibold">{result.dishName}</p></div>
            <div><p className="text-xs text-muted-foreground">Calories</p><p className="font-semibold">{result.estimatedCalories} kcal</p></div>
            <div><p className="text-xs text-muted-foreground">Protein</p><p className="font-semibold">{Math.round(result.proteinGrams)} g</p></div>
            <div><p className="text-xs text-muted-foreground">Carbs / fats</p><p className="font-semibold">{Math.round(result.carbsGrams)} / {Math.round(result.fatsGrams)} g</p></div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}