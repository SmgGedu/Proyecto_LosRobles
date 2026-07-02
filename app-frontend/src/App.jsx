import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './features/auth/Login';
import MainLayout from './components/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';
import RegistroVisita from './features/visitantes/RegistroVisita';
import VisitantesActivos from './features/visitantes/VisitantesActivos';
import Inicio from './features/inicio/Inicio';
import AdminPanel from './features/admin/AdminPanel';
import RegistrarUsuario from './features/admin/RegistrarUsuario';
import GestionUsuarios from './features/admin/GestionUsuarios';
import GestionDepartamentos from './features/admin/GestionDepartamentos';

function App() {
  return (
    <Router>
      <Routes>
        {/* RUTA PÚBLICA: Pantalla de acceso */}
        <Route path="/login" element={<Login />} />

        {/* RUTAS PRIVADAS: Requieren Token y usan el diseño con Sidebar */}
        <Route path="/" element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }>
          {/* Redirección automática al entrar al sistema */}
          <Route index element={<Navigate to="/dashboard" />} />

          <Route path="dashboard" element={<Inicio />} />
          <Route path="activos" element={<VisitantesActivos />} />
          <Route path="nuevo-ingreso" element={<RegistroVisita />} />

          {/* RUTAS DE ADMINISTRADOR: Solo accesibles para rol Administrador */}
          <Route path="admin" element={
            <ProtectedRoute roles={['Administrador']}>
              <AdminPanel />
            </ProtectedRoute>
          } />
          <Route path="admin/registrar-usuario" element={
            <ProtectedRoute roles={['Administrador']}>
              <RegistrarUsuario />
            </ProtectedRoute>
          } />
          <Route path="admin/usuarios" element={
            <ProtectedRoute roles={['Administrador']}>
              <GestionUsuarios />
            </ProtectedRoute>
          } />
          <Route path="admin/departamentos" element={
            <ProtectedRoute roles={['Administrador']}>
              <GestionDepartamentos />
            </ProtectedRoute>
          } />
        </Route>

        {/* Captura cualquier ruta inexistente y la manda al login */}
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
