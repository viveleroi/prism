import * as React from "react";
import { Popover as PopoverPrimitive } from "radix-ui";
import { CheckIcon, ChevronDownIcon } from "lucide-react";

import { cn } from "@/lib/utils";

type SelectContextValue = {
  value: string | undefined;
  onValueChange: (value: string) => void;
  setOpen: (open: boolean) => void;
  registerLabel: (value: string, label: React.ReactNode) => void;
  labels: Record<string, React.ReactNode>;
};

const SelectContext = React.createContext<SelectContextValue | null>(null);

function useSelectContext() {
  const ctx = React.useContext(SelectContext);
  if (!ctx) throw new Error("Select subcomponent used outside of <Select>");
  return ctx;
}

interface SelectProps {
  value?: string;
  defaultValue?: string;
  onValueChange?: (value: string) => void;
  children: React.ReactNode;
}

function Select({ value: valueProp, defaultValue, onValueChange, children }: SelectProps) {
  const [internalValue, setInternalValue] = React.useState(defaultValue);
  const isControlled = valueProp !== undefined;
  const value = isControlled ? valueProp : internalValue;

  const [open, setOpen] = React.useState(false);
  const [labels, setLabels] = React.useState<Record<string, React.ReactNode>>({});

  const registerLabel = React.useCallback((itemValue: string, label: React.ReactNode) => {
    setLabels((prev) => (prev[itemValue] === label ? prev : { ...prev, [itemValue]: label }));
  }, []);

  const handleValueChange = React.useCallback(
    (next: string) => {
      if (!isControlled) setInternalValue(next);
      onValueChange?.(next);
      setOpen(false);
    },
    [isControlled, onValueChange],
  );

  const ctx = React.useMemo<SelectContextValue>(
    () => ({
      value,
      onValueChange: handleValueChange,
      setOpen,
      registerLabel,
      labels,
    }),
    [value, handleValueChange, registerLabel, labels],
  );

  return (
    <SelectContext.Provider value={ctx}>
      <PopoverPrimitive.Root open={open} onOpenChange={setOpen}>
        {children}
      </PopoverPrimitive.Root>
    </SelectContext.Provider>
  );
}

function SelectTrigger({ className, children, ...props }: React.ComponentProps<typeof PopoverPrimitive.Trigger>) {
  return (
    <PopoverPrimitive.Trigger
      data-slot="select-trigger"
      className={cn(
        "flex h-8 w-full items-center justify-between gap-2 rounded-lg border border-input bg-popover pl-2.5 pr-2 py-1 text-sm outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50",
        className,
      )}
      {...props}
    >
      {children}
      <ChevronDownIcon className="size-3.5 opacity-60 shrink-0" />
    </PopoverPrimitive.Trigger>
  );
}

interface SelectValueProps {
  placeholder?: React.ReactNode;
  className?: string;
}

function SelectValue({ placeholder, className }: SelectValueProps) {
  const { value, labels } = useSelectContext();
  const label = value !== undefined ? labels[value] : undefined;
  const display = label ?? (value !== undefined ? value : placeholder);
  const isPlaceholder = label === undefined && value === undefined;
  return (
    <span data-slot="select-value" className={cn("truncate", isPlaceholder && "text-muted-foreground", className)}>
      {display}
    </span>
  );
}

function SelectContent({
  className,
  children,
  align = "start",
  sideOffset = 4,
  ...props
}: React.ComponentProps<typeof PopoverPrimitive.Content>) {
  return (
    <PopoverPrimitive.Portal>
      <PopoverPrimitive.Content
        data-slot="select-content"
        align={align}
        sideOffset={sideOffset}
        className={cn(
          "z-50 min-w-(--radix-popover-trigger-width) origin-(--radix-popover-content-transform-origin) overflow-hidden rounded-lg bg-popover p-1 text-popover-foreground shadow-md ring-1 ring-foreground/10 outline-hidden data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95",
          className,
        )}
        {...props}
      >
        {children}
      </PopoverPrimitive.Content>
    </PopoverPrimitive.Portal>
  );
}

interface SelectItemProps extends React.ComponentProps<"div"> {
  value: string;
  disabled?: boolean;
}

function SelectItem({ className, children, value, disabled, ...props }: SelectItemProps) {
  const { value: selected, onValueChange, registerLabel } = useSelectContext();
  const isSelected = selected === value;

  React.useEffect(() => {
    registerLabel(value, children);
  }, [value, children, registerLabel]);

  return (
    <div
      data-slot="select-item"
      data-disabled={disabled ? "" : undefined}
      role="option"
      aria-selected={isSelected}
      onClick={() => {
        if (disabled) return;
        onValueChange(value);
      }}
      className={cn(
        "relative flex w-full cursor-default select-none items-center gap-2 rounded-md py-1.5 pl-2.5 pr-8 text-sm outline-none hover:bg-accent hover:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50",
        className,
      )}
      {...props}
    >
      <span className="truncate">{children}</span>
      {isSelected && (
        <span className="absolute right-2 flex size-3.5 items-center justify-center">
          <CheckIcon className="size-3.5" />
        </span>
      )}
    </div>
  );
}

export { Select, SelectContent, SelectItem, SelectTrigger, SelectValue };
