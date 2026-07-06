import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './features/auth/Login';
import MainLayout from './components/MainLayout';
import MobileLayout from './components/MobileLayout';
import ProtectedRoute from './components/ProtectedRoute';
import RegistroVisita from './features/visitantes/RegistroVisita';
import VisitantesActivos from './features/visitantes/VisitantesActivos';
import Inicio from './features/inicio/Inicio';
import AdminPanel from './features/admin/AdminPanel';
import RegistrarUsuario from './features/admin/RegistrarUsuario';
import GestionUsuarios from './features/admin/GestionUsuarios';
import GestionDepartamentos from './features/admin/GestionDepartamentos';
import NuevaInvitacion from './features/invitaciones/NuevaInvitacion';
import MisInvitaciones from './features/invitaciones/MisInvitaciones';
import EscanearQR from './features/escaneo/EscanearQR';

/** Aterrizaje por defecto dentro de /m, según el rol guardado en el login. */
const MobileHomeRedirect = () => {
  const role = localStorage.getItem('role');
  return <Navigate to={role === 'Residente' ? '/m/nueva-invitacion' : '/m/escanear'} replace />;
};

function App() {
  return (
    <Router>
      <Routes>
        {/* RUTA PÚBLICA: Pantalla de acceso */}
        <Route path="/login" element={<Login />} />

        {/* RUTAS PRIVADAS DE ESCRITORIO: Solo Administrador y Conserje */}
        <Route path="/" element={
          <ProtectedRoute roles={['Administrador', 'Conserje']}>
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

        {/* APP MÓVIL: Residente (pre-registro de visitas) y Conserje (escaneo QR) */}
        <Route path="/m" element={
          <ProtectedRoute roles={['Residente', 'Conserje']}>
            <MobileLayout />
          </ProtectedRoute>
        }>
          <Route index element={<MobileHomeRedirect />} />

          <Route path="nueva-invitacion" element={
            <ProtectedRoute roles={['Residente']}>
              <NuevaInvitacion />
            </ProtectedRoute>
          } />
          <Route path="mis-invitaciones" element={
            <ProtectedRoute roles={['Residente']}>
              <MisInvitaciones />
            </ProtectedRoute>
          } />

          <Route path="escanear" element={
            <ProtectedRoute roles={['Conserje']}>
              <EscanearQR />
            </ProtectedRoute>
          } />
          <Route path="manual" element={
            <ProtectedRoute roles={['Conserje']}>
              <RegistroVisita />
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
