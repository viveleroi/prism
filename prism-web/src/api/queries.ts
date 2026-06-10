import { apiFetch } from "./client";
import type { ActivitiesResponse, ActivityQueryParams, QueueReportResponse, StatusResponse, World } from "./types";

export async function fetchActivities(params: ActivityQueryParams): Promise<ActivitiesResponse> {
  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  }

  return apiFetch<ActivitiesResponse>(`/api/v1/activities?${searchParams.toString()}`);
}

export async function fetchQueueReport(): Promise<QueueReportResponse> {
  return apiFetch<QueueReportResponse>("/api/v1/reports/recording-queue");
}

export async function fetchStatus(): Promise<StatusResponse> {
  return apiFetch<StatusResponse>("/api/v1/status");
}

export async function fetchWorlds(): Promise<World[]> {
  return apiFetch<World[]>("/api/v1/worlds");
}
