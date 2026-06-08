export interface Coordinate {
  x: number;
  y: number;
  z: number;
}

export interface BlockDetails {
  namespace: string;
  name: string;
  data?: string;
}

export interface EntityDetails {
  type: string;
}

export interface ItemDetails {
  material: string;
  quantity?: number;
  data?: string;
}

export interface CauseDetails {
  type: "player" | "identity" | "block" | "entity" | "translatable" | "string" | "unknown";
  value: string;
  uuid?: string;
  namespace?: string;
}

export interface ActivityResult {
  id?: number;
  actionType: string;
  descriptor: string | null;
  cause: CauseDetails | null;
  world: string | null;
  coordinate: Coordinate | null;
  timestamp: number;
  reversed: boolean;
  count?: number;
  block?: BlockDetails;
  replacedBlock?: BlockDetails;
  entity?: EntityDetails;
  item?: ItemDetails;
  customData?: string;
}

export interface ActivitiesResponse {
  activities: ActivityResult[];
  totalResults: number;
  hasNextPage: boolean;
  count: number;
}

export interface QueueReportResponse {
  queueSize: number;
  queueCapacity: number;
  droppedCount: number;
  actionBreakdown: Record<string, number>;
}

export interface ConnectionStatus {
  connected: boolean;
  active: number;
  idle: number;
  total: number;
  max: number;
  awaiting: number;
}

export interface StatusResponse {
  version: string;
  serverBrand: string;
  serverVersion: string;
  storageType: string;
  storageReady: boolean;
  queueSize: number;
  queueCapacity: number;
  walMode: string;
  purgeActive: boolean;
  connection: ConnectionStatus;
}

export interface ActivityQueryParams {
  action?: string;
  causePlayer?: string;
  affectedPlayer?: string;
  world?: string;
  since?: string;
  before?: string;
  block?: string;
  blockTag?: string;
  causeBlock?: string;
  entity?: string;
  entityTag?: string;
  causeEntity?: string;
  item?: string;
  itemTag?: string;
  at?: string;
  minBound?: string;
  maxBound?: string;
  reversed?: boolean;
  limit?: number;
  offset?: number;
  sort?: "asc" | "desc";
  grouped?: boolean;
  excludeAction?: string;
  excludeCausePlayer?: string;
  excludeAffectedPlayer?: string;
  excludeWorld?: string;
  excludeBlock?: string;
  excludeBlockTag?: string;
  excludeCauseBlock?: string;
  excludeEntity?: string;
  excludeEntityTag?: string;
  excludeCauseEntity?: string;
  excludeItem?: string;
  excludeItemTag?: string;
}
