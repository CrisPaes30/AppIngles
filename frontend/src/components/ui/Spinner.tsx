import { cn } from '@/utils/cn'

interface Props {
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const sizes = { sm: 'h-4 w-4', md: 'h-8 w-8', lg: 'h-12 w-12' }

export function Spinner({ size = 'md', className }: Props) {
  return (
    <span
      role="status"
      className={cn(
        'inline-block animate-spin rounded-full border-2 border-border border-t-brand',
        sizes[size],
        className,
      )}
    />
  )
}
