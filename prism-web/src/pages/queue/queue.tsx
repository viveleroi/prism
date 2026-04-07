import { useQuery } from "@tanstack/react-query";
import { fetchQueueReport } from "../../api/queries";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

export function QueuePage() {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ["queue-report"],
    queryFn: fetchQueueReport,
  });

  const sortedBreakdown = data ? Object.entries(data.actionBreakdown).sort(([, a], [, b]) => b - a) : [];

  return (
    <div>
      <Button onClick={() => refetch()} className="bg-brand text-primary-foreground hover:bg-brand/80">
        Refresh
      </Button>

      {isLoading && <p className="text-sm text-muted-foreground my-3">Loading...</p>}
      {error && <p className="text-sm text-muted-foreground my-3">Error: {error.message}</p>}

      {data && (
        <>
          <Card className="my-4">
            <CardContent>
              <div className="grid grid-cols-[repeat(auto-fit,minmax(150px,1fr))] gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-brand">{data.queueSize}</div>
                  <div className="text-xs text-muted-foreground mt-1">Queue Size</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-brand">{data.queueCapacity}</div>
                  <div className="text-xs text-muted-foreground mt-1">Capacity</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-brand">{data.droppedCount}</div>
                  <div className="text-xs text-muted-foreground mt-1">Dropped</div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Action Type</TableHead>
                    <TableHead>Count</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sortedBreakdown.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={2}>Queue is empty</TableCell>
                    </TableRow>
                  ) : (
                    sortedBreakdown.map(([action, count]) => (
                      <TableRow key={action}>
                        <TableCell>{action}</TableCell>
                        <TableCell>{count}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}
