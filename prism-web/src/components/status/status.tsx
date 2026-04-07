import { useAuth } from "../../auth/auth-context";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Button } from "@/components/ui/button";

export function StatusDropdown() {
  const { status: data, statusError: error } = useAuth();

  const ready = data?.storageReady ?? false;
  const connected = data?.connection?.connected ?? false;
  const healthy = ready && connected;

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button variant="outline" className="rounded-full gap-2">
          <span className={`w-2 h-2 rounded-full inline-block ${healthy ? "bg-[#22c55e]" : "bg-[#ef4444]"}`} />
          Status
        </Button>
      </PopoverTrigger>
      <PopoverContent align="end" className="w-[320px] shadow-[0_20px_50px_rgba(0,0,0,0.4)]">
        {error && <p className="text-sm text-muted-foreground py-2">Error: {error.message}</p>}
        {data && (
          <div className="flex flex-col">
            <Row label="Version" value={data.version} />
            <Row label="Server" value={`${data.serverBrand} ${data.serverVersion}`} />
            <Row label="Storage" value={data.storageType} />
            <Row label="Storage Ready" value={data.storageReady ? "Yes" : "No"} />
            <Row label="Connection" value={data.connection.connected ? "Connected" : "Disconnected"} />
            <Row
              label="Pool"
              value={`${data.connection.active} active / ${data.connection.idle} idle / ${data.connection.total} of ${data.connection.max}`}
            />
            {data.connection.awaiting > 0 && <Row label="Awaiting" value={String(data.connection.awaiting)} />}
            <Row label="WAL" value={data.walMode} />
            <Row label="Purge" value={data.purgeActive ? "Active" : "Inactive"} last />
          </div>
        )}
      </PopoverContent>
    </Popover>
  );
}

function Row({ label, value, last }: { label: string; value: string; last?: boolean }) {
  return (
    <div className={`flex justify-between py-2.5 ${last ? "" : "border-b"}`}>
      <span className="text-sm text-muted-foreground">{label}</span>
      <span className="text-sm font-semibold">{value}</span>
    </div>
  );
}
