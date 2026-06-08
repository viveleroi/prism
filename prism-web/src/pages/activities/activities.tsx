import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { useState, type FormEvent, type ReactNode } from "react";
import { fetchActivities } from "../../api/queries";
import type { ActivityQueryParams, ActivityResult } from "../../api/types";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { BanIcon, PlusIcon, XIcon } from "lucide-react";

interface FilterDef {
  key: string;
  label: string;
  group: string;
  description: string;
  type: "text" | "coordinate" | "bounds" | "boolean";
  placeholder?: string;
  /** When true, this filter can be negated (key sent as `excludeXxx`). */
  negatable?: boolean;
  /** Param name when negated. Required if negatable. */
  excludeKey?: string;
}

const AVAILABLE_FILTERS: FilterDef[] = [
  // Actions
  {
    key: "action",
    excludeKey: "excludeAction",
    negatable: true,
    label: "Action",
    group: "General",
    description: "Action types to filter by",
    type: "text",
    placeholder: "block-break,block-place",
  },
  {
    key: "since",
    label: "Since",
    group: "General",
    description: "Relative time ago",
    type: "text",
    placeholder: "1h, 3d, 1w",
  },
  {
    key: "before",
    label: "Before",
    group: "General",
    description: "Before relative time",
    type: "text",
    placeholder: "1h, 3d, 1w",
  },
  {
    key: "reversed",
    label: "Reversed",
    group: "General",
    description: "Filter by reversed state",
    type: "boolean",
  },

  // Players
  {
    key: "causePlayer",
    excludeKey: "excludeCausePlayer",
    negatable: true,
    label: "Cause Player",
    group: "Players",
    description: "Players who caused the action",
    type: "text",
    placeholder: "Player name",
  },
  {
    key: "affectedPlayer",
    excludeKey: "excludeAffectedPlayer",
    negatable: true,
    label: "Affected Player",
    group: "Players",
    description: "Players affected by the action",
    type: "text",
    placeholder: "Player name",
  },

  // Blocks
  {
    key: "block",
    excludeKey: "excludeBlock",
    negatable: true,
    label: "Block",
    group: "Blocks",
    description: "Affected block names",
    type: "text",
    placeholder: "stone,oak_log",
  },
  {
    key: "blockTag",
    excludeKey: "excludeBlockTag",
    negatable: true,
    label: "Block Tag",
    group: "Blocks",
    description: "Affected block tags",
    type: "text",
    placeholder: "minecraft:logs",
  },
  {
    key: "causeBlock",
    excludeKey: "excludeCauseBlock",
    negatable: true,
    label: "Cause Block",
    group: "Blocks",
    description: "Block that caused the action",
    type: "text",
    placeholder: "tnt,piston",
  },

  // Entities
  {
    key: "entity",
    excludeKey: "excludeEntity",
    negatable: true,
    label: "Entity",
    group: "Entities",
    description: "Affected entity names",
    type: "text",
    placeholder: "creeper,zombie",
  },
  {
    key: "entityTag",
    excludeKey: "excludeEntityTag",
    negatable: true,
    label: "Entity Tag",
    group: "Entities",
    description: "Affected entity tags",
    type: "text",
    placeholder: "minecraft:raiders",
  },
  {
    key: "causeEntity",
    excludeKey: "excludeCauseEntity",
    negatable: true,
    label: "Cause Entity",
    group: "Entities",
    description: "Entity that caused the action",
    type: "text",
    placeholder: "creeper,enderman",
  },

  // Items
  {
    key: "item",
    excludeKey: "excludeItem",
    negatable: true,
    label: "Item",
    group: "Items",
    description: "Affected item names",
    type: "text",
    placeholder: "diamond_sword",
  },
  {
    key: "itemTag",
    excludeKey: "excludeItemTag",
    negatable: true,
    label: "Item Tag",
    group: "Items",
    description: "Affected item tags",
    type: "text",
    placeholder: "minecraft:swords",
  },

  // Location
  {
    key: "world",
    excludeKey: "excludeWorld",
    negatable: true,
    label: "World",
    group: "Location",
    description: "World name",
    type: "text",
    placeholder: "world",
  },
  {
    key: "at",
    label: "At",
    group: "Location",
    description: "Exact coordinate",
    type: "coordinate",
  },
  {
    key: "bounds",
    label: "Bounds",
    group: "Location",
    description: "Bounding box region",
    type: "bounds",
  },
];

type FilterValues = Record<string, string>;
type FilterNegations = Record<string, boolean>;

function CoordinateInput({
  value,
  onChange,
  placeholder,
}: {
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
}) {
  const parts = value ? value.split(",") : ["", "", ""];
  const update = (i: number, v: string) => {
    const next = [...parts];
    next[i] = v;
    onChange(next.join(","));
  };

  return (
    <div className="flex gap-1.5 items-center">
      <Input
        type="number"
        value={parts[0] ?? ""}
        onChange={(e) => update(0, e.target.value)}
        placeholder={placeholder ?? "x"}
        className="w-20"
      />
      <Input
        type="number"
        value={parts[1] ?? ""}
        onChange={(e) => update(1, e.target.value)}
        placeholder="y"
        className="w-20"
      />
      <Input
        type="number"
        value={parts[2] ?? ""}
        onChange={(e) => update(2, e.target.value)}
        placeholder="z"
        className="w-20"
      />
    </div>
  );
}

export function ActivitiesPage() {
  const [activeFilters, setActiveFilters] = useState<string[]>([]);
  const [filterValues, setFilterValues] = useState<FilterValues>({});
  const [filterNegations, setFilterNegations] = useState<FilterNegations>({});
  const [addFilterOpen, setAddFilterOpen] = useState(false);
  const [selectedActivity, setSelectedActivity] = useState<ActivityResult | null>(null);

  const [limit, setLimit] = useState(100);
  const [sort, setSort] = useState<"asc" | "desc">("desc");
  const [grouped, setGrouped] = useState(true);
  const [offset, setOffset] = useState(0);

  const buildParams = (): ActivityQueryParams => {
    const params: ActivityQueryParams = { limit, offset, sort, grouped };

    for (const key of activeFilters) {
      const val = filterValues[key];
      if (val === undefined || val === "") continue;

      if (key === "reversed") {
        params.reversed = val === "true";
      } else if (key === "bounds") {
        const coordParts = val.split("|");
        if (coordParts.length === 2) {
          params.minBound = coordParts[0];
          params.maxBound = coordParts[1];
        }
      } else {
        const def = AVAILABLE_FILTERS.find((f) => f.key === key);
        const paramKey = def?.negatable && filterNegations[key] && def.excludeKey ? def.excludeKey : key;
        (params as Record<string, string>)[paramKey] = val;
      }
    }

    return params;
  };

  const [queryParams, setQueryParams] = useState<ActivityQueryParams>({
    limit: 100,
    offset: 0,
    sort: "desc",
    grouped: true,
  });

  const { data, isLoading, error } = useQuery({
    queryKey: ["activities", queryParams],
    queryFn: () => fetchActivities(queryParams),
    placeholderData: keepPreviousData,
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setOffset(0);
    const params = buildParams();
    params.offset = 0;
    setQueryParams(params);
  };

  // Paging keeps the executed query (filters + limit) fixed and only moves the
  // offset, so editing the form without searching can't desync the page math.
  const handlePrev = () => {
    const activeLimit = queryParams.limit ?? 100;
    const newOffset = Math.max(0, (queryParams.offset ?? 0) - activeLimit);
    setOffset(newOffset);
    setQueryParams({ ...queryParams, offset: newOffset });
  };

  const handleNext = () => {
    const activeLimit = queryParams.limit ?? 100;
    const newOffset = (queryParams.offset ?? 0) + activeLimit;
    setOffset(newOffset);
    setQueryParams({ ...queryParams, offset: newOffset });
  };

  const addFilter = (key: string) => {
    if (!activeFilters.includes(key)) {
      setActiveFilters([...activeFilters, key]);
    }
    setAddFilterOpen(false);
  };

  const removeFilter = (key: string) => {
    setActiveFilters(activeFilters.filter((f) => f !== key));
    setFilterValues((prev) => {
      const next = { ...prev };
      delete next[key];
      return next;
    });
    setFilterNegations((prev) => {
      const next = { ...prev };
      delete next[key];
      return next;
    });
  };

  const updateFilter = (key: string, value: string) => {
    setFilterValues((prev) => ({ ...prev, [key]: value }));
  };

  const toggleNegate = (key: string) => {
    setFilterNegations((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp * 1000).toLocaleString();
  };

  const formatAffected = (activity: ActivityResult) => {
    if (activity.block?.name) return activity.block.name;
    if (activity.entity?.type) return activity.entity.type;
    if (activity.item?.material) {
      const qty = activity.item.quantity;
      return qty && qty > 1 ? `${activity.item.material} x${qty}` : activity.item.material;
    }
    return activity.descriptor ?? "";
  };

  const activeLimit = queryParams.limit ?? 100;
  const page = Math.floor((queryParams.offset ?? 0) / activeLimit) + 1;
  const totalPages = data ? Math.ceil(data.totalResults / activeLimit) : 0;

  const unusedFilters = AVAILABLE_FILTERS.filter((f) => !activeFilters.includes(f.key));
  const groupedUnusedFilters = unusedFilters.reduce<Record<string, FilterDef[]>>((acc, f) => {
    (acc[f.group] ??= []).push(f);
    return acc;
  }, {});

  return (
    <div>
      <Card className="mb-6">
        <CardContent>
          <form onSubmit={handleSubmit}>
            {activeFilters.length > 0 && (
              <div className="flex flex-col gap-3 mb-4">
                {activeFilters.map((key) => {
                  const def = AVAILABLE_FILTERS.find((f) => f.key === key)!;
                  const negated = !!filterNegations[key];
                  return (
                    <div key={key} className="flex items-end gap-2">
                      <div className="flex flex-col gap-1.5 flex-1">
                        <Label className="text-muted-foreground">
                          {def.label}
                          {def.negatable && negated && (
                            <span className="ml-2 text-xs font-medium text-destructive">excluded</span>
                          )}
                        </Label>
                        {def.type === "text" && (
                          <Input
                            value={filterValues[key] ?? ""}
                            onChange={(e) => updateFilter(key, e.target.value)}
                            placeholder={def.placeholder}
                          />
                        )}
                        {def.type === "coordinate" && (
                          <CoordinateInput value={filterValues[key] ?? ",,"} onChange={(v) => updateFilter(key, v)} />
                        )}
                        {def.type === "bounds" && (
                          <div className="flex gap-3 items-center">
                            <CoordinateInput
                              value={(filterValues[key] ?? "|").split("|")[0] ?? ",,"}
                              onChange={(v) => {
                                const max = (filterValues[key] ?? "|").split("|")[1] ?? ",,";
                                updateFilter(key, `${v}|${max}`);
                              }}
                              placeholder="x1"
                            />
                            <span className="text-sm text-muted-foreground">to</span>
                            <CoordinateInput
                              value={(filterValues[key] ?? "|").split("|")[1] ?? ",,"}
                              onChange={(v) => {
                                const min = (filterValues[key] ?? "|").split("|")[0] ?? ",,";
                                updateFilter(key, `${min}|${v}`);
                              }}
                              placeholder="x2"
                            />
                          </div>
                        )}
                        {def.type === "boolean" && (
                          <Select value={filterValues[key] ?? ""} onValueChange={(v) => updateFilter(key, v)}>
                            <SelectTrigger className="w-[80px]">
                              <SelectValue placeholder="Any" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="true">Yes</SelectItem>
                              <SelectItem value="false">No</SelectItem>
                            </SelectContent>
                          </Select>
                        )}
                      </div>
                      {def.negatable && (
                        <Button
                          type="button"
                          variant={negated ? "default" : "ghost"}
                          size="icon-sm"
                          onClick={() => toggleNegate(key)}
                          className="mb-0.5"
                          title={negated ? "Negated (click to include)" : "Click to negate (exclude)"}
                        >
                          <BanIcon className="size-3.5" />
                        </Button>
                      )}
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon-sm"
                        onClick={() => removeFilter(key)}
                        className="mb-0.5"
                      >
                        <XIcon className="size-3.5" />
                      </Button>
                    </div>
                  );
                })}
              </div>
            )}

            <div className="flex gap-3 flex-wrap items-end">
              {unusedFilters.length > 0 && (
                <Popover open={addFilterOpen} onOpenChange={setAddFilterOpen}>
                  <PopoverTrigger asChild>
                    <Button type="button" variant="outline" size="sm" className="gap-1.5">
                      <PlusIcon className="size-3.5" />
                      Add Filter
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent align="start" className="w-[240px] p-1 max-h-[400px] overflow-y-auto">
                    {Object.entries(groupedUnusedFilters).map(([group, filters]) => (
                      <div key={group}>
                        <div className="px-3 py-1.5 text-xs font-medium text-muted-foreground">{group}</div>
                        {filters.map((filter) => (
                          <button
                            key={filter.key}
                            type="button"
                            onClick={() => addFilter(filter.key)}
                            className="w-full flex flex-col gap-0.5 rounded-md px-3 py-2 text-left hover:bg-muted transition-colors"
                          >
                            <span className="text-sm font-medium">{filter.label}</span>
                            <span className="text-xs text-muted-foreground">{filter.description}</span>
                          </button>
                        ))}
                      </div>
                    ))}
                  </PopoverContent>
                </Popover>
              )}

              <div className="flex gap-3 items-end ml-auto">
                <div className="flex flex-col gap-1.5">
                  <Label className="text-muted-foreground text-xs">Sort</Label>
                  <Select value={sort} onValueChange={(v) => setSort(v as "asc" | "desc")}>
                    <SelectTrigger className="w-[140px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="desc">Newest first</SelectItem>
                      <SelectItem value="asc">Oldest first</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="flex flex-col gap-1.5">
                  <Label className="text-muted-foreground text-xs">Limit</Label>
                  <Input
                    type="number"
                    value={limit}
                    onChange={(e) => setLimit(parseInt(e.target.value) || 100)}
                    min={1}
                    max={1000}
                    className="w-20"
                  />
                </div>
                <div className="flex flex-col gap-1.5">
                  <Label className="text-muted-foreground text-xs">Grouped</Label>
                  <Select value={String(grouped)} onValueChange={(v) => setGrouped(v === "true")}>
                    <SelectTrigger className="w-[80px]">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="true">Yes</SelectItem>
                      <SelectItem value="false">No</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <Button type="submit" className="bg-brand text-primary-foreground hover:bg-brand/80">
                  Search
                </Button>
              </div>
            </div>
          </form>
        </CardContent>
      </Card>

      {isLoading && <p className="text-sm text-muted-foreground mb-3">Loading...</p>}
      {error && <p className="text-sm text-muted-foreground mb-3">Error: {error.message}</p>}

      {data && (
        <>
          <p className="text-sm text-muted-foreground mb-3">
            Showing {data.count} of {data.totalResults} results
          </p>
          <Card>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Time</TableHead>
                    <TableHead>Action</TableHead>
                    <TableHead>Cause</TableHead>
                    <TableHead>Affected</TableHead>
                    <TableHead>World</TableHead>
                    <TableHead>Count</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.activities.map((activity, i) => (
                    <TableRow
                      key={activity.id ?? i}
                      onClick={() => setSelectedActivity(activity)}
                      className="cursor-pointer hover:bg-muted/50"
                    >
                      <TableCell>{formatTime(activity.timestamp)}</TableCell>
                      <TableCell>{activity.actionType}</TableCell>
                      <TableCell>{activity.cause?.value ?? ""}</TableCell>
                      <TableCell>{formatAffected(activity)}</TableCell>
                      <TableCell>{activity.world}</TableCell>
                      <TableCell>{activity.count ?? 1}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <div className="flex items-center gap-4 mt-4 justify-center">
            <Button variant="outline" onClick={handlePrev} disabled={page <= 1}>
              Previous
            </Button>
            <span className="text-sm text-muted-foreground">
              Page {page} of {totalPages}
            </span>
            <Button variant="outline" onClick={handleNext} disabled={!data.hasNextPage}>
              Next
            </Button>
          </div>
        </>
      )}

      <ActivityDetailsDialog activity={selectedActivity} onClose={() => setSelectedActivity(null)} />
    </div>
  );
}

function ActivityDetailsDialog({ activity, onClose }: { activity: ActivityResult | null; onClose: () => void }) {
  return (
    <Dialog open={!!activity} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-lg max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Activity Details</DialogTitle>
          <DialogDescription className="sr-only">All available details for the selected activity.</DialogDescription>
        </DialogHeader>
        {activity && (
          <div className="flex flex-col gap-4">
            {activity.id == null && (
              <div className="rounded-md border border-sky-500/40 bg-sky-500/15 px-3 py-2 text-xs text-foreground/80">
                This is a grouped result. Per-row details like ID and coordinates aren't available — re-run with
                grouping off to see them.
              </div>
            )}
            <DetailSection title="General">
              {activity.id != null && <DetailRow label="ID" value={activity.id} />}
              <DetailRow label="Action" value={activity.actionType} />
              <DetailRow
                label="Time"
                value={`${new Date(activity.timestamp * 1000).toLocaleString()} (${activity.timestamp})`}
              />
              <DetailRow label="Descriptor" value={activity.descriptor} />
              <DetailRow label="World" value={activity.world} />
              {activity.coordinate && (
                <DetailRow
                  label="Coordinate"
                  value={`${activity.coordinate.x}, ${activity.coordinate.y}, ${activity.coordinate.z}`}
                />
              )}
              <DetailRow label="Reversed" value={activity.reversed ? "Yes" : "No"} />
              <DetailRow label="Count" value={activity.count} last />
            </DetailSection>

            {activity.cause && (
              <DetailSection title="Cause">
                <DetailRow label="Type" value={activity.cause.type} />
                <DetailRow
                  label="Value"
                  value={activity.cause.value}
                  last={!activity.cause.namespace && !activity.cause.uuid}
                />
                {activity.cause.namespace && (
                  <DetailRow label="Namespace" value={activity.cause.namespace} last={!activity.cause.uuid} />
                )}
                {activity.cause.uuid && <DetailRow label="UUID" value={activity.cause.uuid} preformatted last />}
              </DetailSection>
            )}

            {activity.block && (
              <DetailSection title="Block">
                <DetailRow label="Namespace" value={activity.block.namespace} />
                <DetailRow label="Name" value={activity.block.name} />
                <DetailRow label="Data" value={activity.block.data} preformatted last />
              </DetailSection>
            )}

            {activity.replacedBlock && (
              <DetailSection title="Replaced Block">
                <DetailRow label="Namespace" value={activity.replacedBlock.namespace} />
                <DetailRow label="Name" value={activity.replacedBlock.name} />
                <DetailRow label="Data" value={activity.replacedBlock.data} preformatted last />
              </DetailSection>
            )}

            {activity.entity && (
              <DetailSection title="Entity">
                <DetailRow label="Type" value={activity.entity.type} last />
              </DetailSection>
            )}

            {activity.item && (
              <DetailSection title="Item">
                <DetailRow label="Material" value={activity.item.material} />
                <DetailRow label="Quantity" value={activity.item.quantity} />
                <DetailRow label="Data" value={activity.item.data} preformatted last />
              </DetailSection>
            )}

            {activity.customData && (
              <DetailSection title="Custom Data">
                <pre className="text-xs whitespace-pre-wrap break-all bg-muted/30 rounded-md p-2.5">
                  {activity.customData}
                </pre>
              </DetailSection>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

function DetailSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section>
      <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground mb-1">{title}</h3>
      <dl className="flex flex-col">{children}</dl>
    </section>
  );
}

function DetailRow({
  label,
  value,
  last,
  preformatted,
}: {
  label: string;
  value: string | number | null | undefined;
  last?: boolean;
  preformatted?: boolean;
}) {
  if (preformatted && (value === null || value === undefined || value === "")) {
    return null;
  }
  const display = value === null || value === undefined || value === "" ? "—" : String(value);
  return (
    <div className={`flex justify-between gap-4 py-2 ${last ? "" : "border-b"}`}>
      <dt className="text-sm text-muted-foreground shrink-0">{label}</dt>
      <dd className={`text-sm font-medium text-right break-all ${preformatted ? "font-mono text-xs" : ""}`}>
        {display}
      </dd>
    </div>
  );
}
