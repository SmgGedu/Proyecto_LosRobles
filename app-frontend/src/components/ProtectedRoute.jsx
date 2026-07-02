import { Navigate } from 'react-router-dom';

/**
 * Componente que envuelve rutas privadas.
 * Si no hay token en localStorage, redirige al login.
 * Si se indica `roles`, además verifica que el rol del usuario esté incluido,
 * redirigiendo a /dashboard si no tiene permiso.
 */

const ProtectedRoute = ({ children, roles }) => {
    const token = localStorage.getItem('token');

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    if (roles && roles.length > 0) {
        const role = localStorage.getItem('role');
        if (!roles.includes(role)) {
            return <Navigate to="/dashboard" replace />;
        }
    }

    return children;
};
export default ProtectedRoute;