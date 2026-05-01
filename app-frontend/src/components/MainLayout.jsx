import { useState } from 'react';
import { Outlet, useNavigate, Link, useLocation } from 'react-router-dom';
import { 
  Home, Users, ClipboardList, LogOut, Menu, ChevronLeft, ShieldCheck 
} from 'lucide-react'; 
import './MainLayout.css';

const MainLayout = () => {
    const [isCollapsed, setIsCollapsed] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const menuItems = [
        { path: '/dashboard', name: 'Inicio', icon: <Home size={22}/> },
        { path: '/activos', name: 'Visitantes Activos', icon: <Users size={22}/> },
        { path: '/nuevo-ingreso', name: 'Registrar Ingreso', icon: <ClipboardList size={22}/> },
    ];

    return (
        <div className={`dashboard-wrapper ${isCollapsed ? 'collapsed' : ''}`}>
            {/* Sidebar Moderno */}
            <aside className="modern-sidebar">
                <div className="sidebar-top">
                    <div className="logo-section">
                        <img src="/images/logomin.png" alt="Logo" />
                        {!isCollapsed && <span>Los Robles</span>}
                    </div>
                    <button className="toggle-btn" onClick={() => setIsCollapsed(!isCollapsed)}>
                        {isCollapsed ? <Menu size={20}/> : <ChevronLeft size={20}/>}
                    </button>
                </div>
                
                <nav className="modern-nav">
                    {menuItems.map((item) => (
                        <Link 
                            key={item.path} 
                            to={item.path} 
                            className={`modern-nav-item ${location.pathname === item.path ? 'active' : ''}`}
                        >
                            <span className="icon">{item.icon}</span>
                            {!isCollapsed && <span className="text">{item.name}</span>}
                        </Link>
                    ))}
                </nav>

                <div className="sidebar-footer">
                    <button onClick={handleLogout} className="modern-btn-logout">
                        <LogOut size={20}/>
                        {!isCollapsed && <span>Cerrar Sesión</span>}
                    </button>
                </div>
            </aside>

            {/* Contenido Principal */}
            <div className="main-viewport">
                <header className="modern-header">
                    <div className="header-info">
                        <ShieldCheck size={20} color="#2d5a27"/>
                        <span>Panel de Control de Seguridad</span>
                    </div>
                    <div className="user-profile">
                        <div className="avatar">F</div>
                        <span className="user-name">Jhosua Chacaltana</span>
                    </div>
                </header>
                <main className="modern-content">
                    <div className="glass-card">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    );
};

export default MainLayout;