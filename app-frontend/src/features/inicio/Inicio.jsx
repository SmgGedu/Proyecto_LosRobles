import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
    Users, UserCheck, LogIn, Clock, ClipboardList, ShieldCheck,
    UserCog, ArrowRight, Cloud, Database, MapPin, Home
} from 'lucide-react';
import api from '../../api/axiosConfig';
import './Inicio.css';

const SALUDOS = [
    { max: 12, texto: 'Buenos días' },
    { max: 19, texto: 'Buenas tardes' },
    { max: 24, texto: 'Buenas noches' },
];

const getSaludo = () => {
    const hora = new Date().getHours();
    return (SALUDOS.find(s => hora < s.max) || SALUDOS[SALUDOS.length - 1]).texto;
};

const Inicio = () => {
    const [stats, setStats] = useState({
        visitantesHoy: 0,
        activosAhora: 0,
        totalResidentes: 0,
        ultimoIngreso: '--:--'
    });
    const [movimientos, setMovimientos] = useState([]);
    const [loading, setLoading] = useState(true);
    const username = localStorage.getItem('username') || 'Usuario';
    const role = localStorage.getItem('role');

    useEffect(() => {
        const fetchDatos = async () => {
            try {
                const [statsRes, accesosRes] = await Promise.all([
                    api.get('/dashboard/stats'),
                    api.get('/accesos')
                ]);
                setStats(statsRes.data);

                const recientes = [...(accesosRes.data || [])]
                    .sort((a, b) => new Date(b.horaEntrada) - new Date(a.horaEntrada))
                    .slice(0, 5);
                setMovimientos(recientes);
            } catch (error) {
                console.error("Error al cargar el panel de control:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchDatos();
    }, []);

    const formatHora = (horaISO) => {
        if (!horaISO) return '—';
        return new Date(horaISO).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
    };

    const cards = [
        { label: 'Visitantes Hoy', value: stats.visitantesHoy, icon: <LogIn size={22}/>, color: '#3b82f6' },
        { label: 'Activos Ahora', value: stats.activosAhora, icon: <UserCheck size={22}/>, color: '#22c55e' },
        { label: 'Total Residentes', value: stats.totalResidentes, icon: <Users size={22}/>, color: '#6366f1' },
        { label: 'Último Ingreso', value: stats.ultimoIngreso, icon: <Clock size={22}/>, color: '#f59e0b' },
    ];

    const accesosRapidos = [
        { path: '/nuevo-ingreso', label: 'Registrar Ingreso', icon: <ClipboardList size={20}/> },
        { path: '/activos', label: 'Visitantes Activos', icon: <Users size={20}/> },
    ];
    if (role === 'Administrador') {
        accesosRapidos.push({ path: '/admin', label: 'Panel de Administrador', icon: <UserCog size={20}/> });
    }

    if (loading) {
        return (
            <div className="dashboard-home">
                <div className="skeleton-header"></div>
                <div className="stats-grid">
                    {[1, 2, 3, 4].map(i => <div key={i} className="skeleton-card"></div>)}
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard-home">
            <header className="home-header">
                <div>
                    <h1>{getSaludo()}, {username}</h1>
                    <p>Estado operativo actual del Condominio Los Robles.</p>
                </div>
                <div className="home-header-date">
                    <span>{new Date().toLocaleDateString('es-PE', { weekday: 'long', day: 'numeric', month: 'long' })}</span>
                </div>
            </header>

            <div className="stats-grid">
                {cards.map((card, index) => (
                    <div key={index} className="stat-card" style={{ '--card-color': card.color }}>
                        <div className="stat-icon" style={{ backgroundColor: card.color + '1a', color: card.color }}>
                            {card.icon}
                        </div>
                        <div className="stat-info">
                            <h3>{card.value}</h3>
                            <p>{card.label}</p>
                        </div>
                    </div>
                ))}
            </div>

            <div className="quick-actions">
                {accesosRapidos.map((accion) => (
                    <Link key={accion.path} to={accion.path} className="quick-action-btn">
                        <span className="quick-action-icon">{accion.icon}</span>
                        <span>{accion.label}</span>
                        <ArrowRight size={16} className="quick-action-arrow" />
                    </Link>
                ))}
            </div>

            <div className="dashboard-main-content">
                <div className="recent-activity">
                    <div className="recent-activity-header">
                        <h2>Movimientos Recientes</h2>
                        <Link to="/activos" className="ver-todos-link">Ver todos <ArrowRight size={14}/></Link>
                    </div>

                    {movimientos.length === 0 ? (
                        <p className="recent-activity-empty">
                            Aún no hay movimientos registrados hoy. Para monitoreo en vivo del aforo, revisa "Visitantes en el Edificio".
                        </p>
                    ) : (
                        <table>
                            <thead>
                                <tr>
                                    <th>Visitante</th>
                                    <th>Depto.</th>
                                    <th>Entrada</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                                {movimientos.map((m) => (
                                    <tr key={m.id}>
                                        <td>
                                            <div className="visitante-cell">
                                                <span className="visitante-avatar">{(m.nombreVisitante || '?').charAt(0).toUpperCase()}</span>
                                                {m.nombreVisitante || 'Sin nombre'}
                                            </div>
                                        </td>
                                        <td>
                                            <span className="depto-cell"><Home size={13}/> {m.departamento || '—'}</span>
                                        </td>
                                        <td>{formatHora(m.horaEntrada)}</td>
                                        <td>
                                            <span className={`estado-badge ${m.estadoAcceso === 'ACTIVO' ? 'activo' : 'finalizado'}`}>
                                                {m.estadoAcceso === 'ACTIVO' ? 'Dentro' : 'Salió'}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

                <div className="info-side-card">
                    <h3>Estado del Sistema</h3>
                    <div className="status-item">
                        <span className="status-icon online"><Cloud size={16}/></span>
                        <span>Conectado a Azure</span>
                        <span className="dot online"></span>
                    </div>
                    <div className="status-item">
                        <span className="status-icon online"><Database size={16}/></span>
                        <span>Base de Datos: Activa</span>
                        <span className="dot online"></span>
                    </div>
                    <hr />
                    <div className="sede-info">
                        <MapPin size={15}/>
                        <span>Sede: Ica, Perú</span>
                    </div>
                    <div className="sede-badge">
                        <ShieldCheck size={14}/> Sistema Los Robles
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Inicio;
