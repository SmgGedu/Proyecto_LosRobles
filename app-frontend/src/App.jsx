import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import MainLayout from './components/MainLayout';
import ProtectedRoute from './components/ProtectedRoute';
import RegistroVisita from './components/RegistroVisita';

// Importamos tu nueva página "Torre de Control"
import Inicio from './pages/Inicio';

/**
 * Componentes temporales (Placeholders)
 * Estos los moveremos a sus propios archivos en /pages más adelante.
 */
const Activos = () => (
    <div style={{ padding: '20px' }}>
        <h2>Lista de Visitantes en el Edificio</h2>
        <p>Aquí conectaremos la tabla con la base de datos de Azure.</p>
    </div>
);
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
          
          {/* Tu nueva página de Dashboard profesional */}
          <Route path="dashboard" element={<Inicio />} />
          
          {/* Páginas que construiremos a continuación */}
          <Route path="activos" element={<Activos />} />
          <Route path="nuevo-ingreso" element={<RegistroVisita />} />
        </Route>

        {/* Captura cualquier ruta inexistente y la manda al login */}
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;