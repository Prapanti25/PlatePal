import { fileURLToPath } from "node:url";
import path from "node:path";
import { defineConfig } from "vitest/config";

const projectRoot = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  resolve: {
    alias: { "@": path.resolve(projectRoot, "src") },
  },
  test: {
    environment: "jsdom",
    pool: "threads",
    maxWorkers: 1,
    clearMocks: true,
  },
});