import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

interface Props {
  onConnect: (key: string) => void;
}

export function ApiKeyModal({ onConnect }: Props) {
  const [key, setKey] = useState("");

  const handleSubmit = () => {
    if (key.trim()) {
      onConnect(key.trim());
    }
  };

  return (
    <Dialog open>
      <DialogContent showCloseButton={false} className="sm:max-w-[420px]">
        <DialogHeader>
          <DialogTitle>API Key Required</DialogTitle>
          <DialogDescription>Enter your Prism web API key (set in prism.conf) to continue.</DialogDescription>
        </DialogHeader>
        <div className="flex flex-col gap-4">
          <Input
            type="password"
            value={key}
            onChange={(e) => setKey(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
            placeholder="API key"
          />
          <Button onClick={handleSubmit} className="w-full bg-brand text-primary-foreground hover:bg-brand/80">
            Connect
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
