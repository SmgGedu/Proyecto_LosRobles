import { Outlet, useNavigate, Link, useLocation } from 'react-router-dom';
import { PlusCircle, ListChecks, ScanLine, ClipboardEdit, LogOut, Monitor } from 'lucide-react';
import './MobileLayout.css';

const MENU_POR_ROL = {
    Residente: [
        { path: '/m/nueva-invitacion', name: 'Invitar', icon: <PlusCircle size={22} /> },
        { path: '/m/mis-invitaciones', name: 'Mis QR', icon: <ListChecks size={22} /> },
    ],
    Conserje: [
        { path: '/m/escanear', name: 'Escanear', icon: <ScanLine size={22} /> },
        { path: '/m/manual', name: 'Manual', icon: <ClipboardEdit size={22} /> },
    ],
};

const MobileLayout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const username = localStorage.getItem('username') || 'Usuario';
    const role = localStorage.getItem('role');
    const menuItems = MENU_POR_ROL[role] || [];

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        navigate('/login');
    };

    return (
        <div className="mobile-shell">
            <header className="mobile-header">
                <div className="mobile-header-brand">
                    <img src="/images/logomin.png" alt="Los Robles" />
                    <span>Los Robles</span>
                </div>
                <div className="mobile-header-user">
                    <div className="mobile-avatar">{username.charAt(0).toUpperCase()}</div>
                    {role === 'Conserje' && (
                        <Link to="/dashboard" className="mobile-icon-btn" title="Ver panel de escritorio">
                            <Monitor size={19} />
                        </Link>
                    )}
                    <button className="mobile-icon-btn" onClick={handleLogout} title="Cerrar sesión">
                        <LogOut size={19} />
                    </button>
                </div>
            </header>

            <main className="mobile-content">
                <Outlet />
            </main>

            <nav className="mobile-tabbar">
                {menuItems.map((item) => (
                    <Link
                        key={item.path}
                        to={item.path}
                        className={`mobile-tab ${location.pathname.startsWith(item.path) ? 'active' : ''}`}
                    >
                        {item.icon}
                        <span>{item.name}</span>
                    </Link>
                ))}
            </nav>
        </div>
    );
};

export default MobileLayout;
