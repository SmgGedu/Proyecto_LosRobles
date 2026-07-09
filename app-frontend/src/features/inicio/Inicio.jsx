import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
    Users, UserCheck, LogIn, Clock, ShieldCheck,
    ArrowRight, Cloud, Database, MapPin, Home
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

const formatRelativo = (horaISO) => {
    if (!horaISO) return null;
    const diffMin = Math.max(0, Math.floor((Date.now() - new Date(horaISO).getTime()) / 60000));
    if (diffMin < 1) return 'Justo ahora';
    if (diffMin < 60) return `Hace ${diffMin} min`;
    const diffHoras = Math.floor(diffMin / 60);
    if (diffHoras < 24) return `Hace ${diffHoras} h`;
    return `Hace ${Math.floor(diffHoras / 24)} d`;
};

const formatFechaCorta = (horaISO) => {
    if (!horaISO) return null;
    return new Date(horaISO).toLocaleString('es-PE', {
        day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
    });
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

    // Refresca los textos relativos ("hace X min") sin volver a llamar a la API.
    const [, forceTick] = useState(0);
    useEffect(() => {
        const id = setInterval(() => forceTick(t => t + 1), 60000);
        return () => clearInterval(id);
    }, []);

    const formatHora = (horaISO) => {
        if (!horaISO) return '—';
        return new Date(horaISO).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
    };

    const ultimoMovimiento = movimientos[0];

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
                <div className="stat-card" style={{ '--card-color': '#3b82f6' }}>
                    <div className="stat-icon" style={{ backgroundColor: '#3b82f61a', color: '#3b82f6' }}>
                        <LogIn size={22}/>
                    </div>
                    <div className="stat-info">
                        <h3>{stats.visitantesHoy}</h3>
                        <p>Visitantes Hoy</p>
                        <span className="stat-caption">Registrados desde la medianoche</span>
                    </div>
                </div>

                <div className="stat-card" style={{ '--card-color': '#22c55e' }}>
                    <div className="stat-icon" style={{ backgroundColor: '#22c55e1a', color: '#22c55e' }}>
                        <UserCheck size={22}/>
                    </div>
                    <div className="stat-info">
                        <h3>{stats.activosAhora}</h3>
                        <p>Activos Ahora</p>
                        <span className="stat-caption">Dentro del condominio en este momento</span>
                    </div>
                </div>

                <div className="stat-card" style={{ '--card-color': '#6366f1' }}>
                    <div className="stat-icon" style={{ backgroundColor: '#6366f11a', color: '#6366f1' }}>
                        <Users size={22}/>
                    </div>
                    <div className="stat-info">
                        <h3>{stats.totalResidentes}</h3>
                        <p>Total Residentes</p>
                        <span className="stat-caption">Registrados en el sistema</span>
                    </div>
                </div>

                <div className="stat-card stat-card--highlight" style={{ '--card-color': '#f59e0b' }}>
                    <div className="stat-icon" style={{ backgroundColor: '#f59e0b1a', color: '#f59e0b' }}>
                        <Clock size={22}/>
                    </div>
                    <div className="stat-info">
                        <h3>{ultimoMovimiento ? formatRelativo(ultimoMovimiento.horaEntrada) : stats.ultimoIngreso}</h3>
                        <p>Último Ingreso</p>
                        <span className="stat-caption">
                            {ultimoMovimiento
                                ? `${ultimoMovimiento.nombreVisitante} · ${formatFechaCorta(ultimoMovimiento.horaEntrada)}`
                                : 'Sin movimientos registrados'}
                        </span>
                    </div>
                </div>
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
                        <div className="recent-activity-table-wrap">
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
                        </div>
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
