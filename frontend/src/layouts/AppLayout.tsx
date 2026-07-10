import { useState, useCallback } from 'react'
import { Outlet } from 'react-router-dom'
import { Sidebar } from '@/components/layout/Sidebar'
import { Header } from '@/components/layout/Header'
import { BottomNav } from '@/components/layout/BottomNav'

export function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const open  = useCallback(() => setSidebarOpen(true),  [])
  const close = useCallback(() => setSidebarOpen(false), [])

  return (
    <div className="flex h-screen overflow-hidden bg-surface">
      {/* Overlay mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-30 bg-black/60 backdrop-blur-sm md:hidden"
          onClick={close}
          aria-hidden
        />
      )}

      {/* Sidebar — slide-in mobile / fixed desktop */}
      <div
        className={[
          'fixed inset-y-0 left-0 z-40 md:relative md:flex md:flex-shrink-0',
          'transition-transform duration-300 ease-in-out',
          sidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0',
        ].join(' ')}
      >
        <Sidebar onClose={close} />
      </div>

      <div className="flex flex-1 flex-col overflow-hidden">
        <Header onMenuClick={open} />
        {/* pb-16 on mobile to clear the bottom nav bar */}
        <main className="flex-1 overflow-y-auto p-4 pb-20 md:p-6 md:pb-6">
          <Outlet />
        </main>
      </div>

      <BottomNav />
    </div>
  )
}
