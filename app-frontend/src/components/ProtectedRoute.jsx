import { Navigate } from 'react-router-dom';

/**
 * Componente que envuelve rutas privadas.
 * Si no hay token en localStorage, redirige al login.
 */
const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem('token');
    
    if (!token) {
        return <Navigate to="/login" replace />;
    }

    return children;
};

export default ProtectedRoute;