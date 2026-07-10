import { RouterProvider } from 'react-router-dom'
import { QueryProvider } from '@/contexts/QueryProvider'
import { ToastProvider } from '@/contexts/ToastContext'
import { AuthProvider } from '@/contexts/AuthContext'
import { router } from '@/routes'

export default function App() {
  return (
    <AuthProvider>
      <QueryProvider>
        <ToastProvider>
          <RouterProvider router={router} />
        </ToastProvider>
      </QueryProvider>
    </AuthProvider>
  )
}
