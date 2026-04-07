import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

interface Props {
  onRetry: () => void;
}

export function ErrorPage({ onRetry }: Props) {
  return (
    <div className="flex min-h-[60vh] items-center justify-center px-8">
      <Card className="w-full max-w-[440px]">
        <CardHeader>
          <CardTitle className="text-xl">Unauthorized</CardTitle>
          <CardDescription>
            Your API key is missing, invalid, or has expired. Enter a valid key to continue.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Button onClick={onRetry} className="w-full bg-brand text-primary-foreground hover:bg-brand/80">
            Enter API key
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
