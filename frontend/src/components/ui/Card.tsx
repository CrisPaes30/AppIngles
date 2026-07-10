import { cn } from '@/utils/cn'
import type { HTMLAttributes } from 'react'

interface Props extends HTMLAttributes<HTMLDivElement> {
  hover?: boolean
}

export function Card({ hover, className, children, ...rest }: Props) {
  return (
    <div
      className={cn('card', hover && 'card-hover cursor-pointer', className)}
      {...rest}
    >
      {children}
    </div>
  )
}
