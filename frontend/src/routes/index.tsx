import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AppLayout } from '@/layouts/AppLayout'
import PrivateRoute from '@/components/PrivateRoute'
import LoginPage    from '@/pages/LoginPage'
import { Dashboard }   from '@/pages/Dashboard'
import { Vocabulary }  from '@/pages/Vocabulary'
import { WordDetail }  from '@/pages/WordDetail'
import { Review }      from '@/pages/Review'
import { Exercise }    from '@/pages/Exercise'
import { Progress }    from '@/pages/Progress'
import { Categories }  from '@/pages/Categories'

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/',
    element: <PrivateRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { index: true,               element: <Navigate to="/dashboard" replace /> },
          { path: 'dashboard',          element: <Dashboard /> },
          { path: 'vocabulary',         element: <Vocabulary /> },
          { path: 'vocabulary/:id',     element: <WordDetail /> },
          { path: 'review',             element: <Review /> },
          { path: 'exercise',           element: <Exercise /> },
          { path: 'progress',           element: <Progress /> },
          { path: 'categories',         element: <Categories /> },
        ],
      },
    ],
  },
])
