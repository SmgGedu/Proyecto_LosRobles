import { useState, useEffect } from 'react';

import { Users, UserCheck, LogIn, Clock } from 'lucide-react';
import api from '../../api/axiosConfig';
import './Inicio.css';

const Inicio = () => {
    const [stats, setStats] = useState({
        visitantesHoy: 0,
        activosAhora: 0,
        totalResidentes: 0,
        ultimoIngreso: '--:--'
    });
    const [loading, setLoading] = useState(true);
    const username = localStorage.getItem('username') || 'Usuario';

    useEffect(() => {
        const fetchStats = async () => {
            try {
                // Llamada a tu controlador de Spring Boot
                const response = await api.get('/dashboard/stats');
                setStats(response.data);
                setLoading(false);
            } catch (error) {
                console.error("Error al cargar estadísticas:", error);
                setLoading(false);
                // Aquí podrías setear un error para mostrar al usuario
            }
        };

        fetchStats();
    }, []);

    // Mapeamos los datos para las tarjetas
    const cards = [
        { label: 'Visitantes Hoy', value: stats.visitantesHoy, icon: <LogIn size={24}/>, color: '#3b82f6' },
        { label: 'Activos Ahora', value: stats.activosAhora, icon: <UserCheck size={24}/>, color: '#22c55e' },
        { label: 'Total Residentes', value: stats.totalResidentes, icon: <Users size={24}/>, color: '#6366f1' },
        { label: 'Último Ingreso', value: stats.ultimoIngreso, icon: <Clock size={24}/>, color: '#f59e0b' },
    ];

    if (loading) return <div className="loading-stats">Actualizando panel de control...</div>;

    return (
        <div className="dashboard-home">
            <header className="home-header">
                <h1>Bienvenido, {username}</h1>
                <p>Estado operativo actual del Condominio Los Robles.</p>
            </header>

            <div className="stats-grid">
                {cards.map((card, index) => (
                    <div key={index} className="stat-card">
                        <div className="stat-icon" style={{ backgroundColor: card.color + '20', color: card.color }}>
                            {card.icon}
                        </div>
                        <div className="stat-info">
                            <h3>{card.value}</h3>
                            <p>{card.label}</p>
                        </div>
                    </div>
                ))}
            </div>

            <div className="dashboard-main-content">
                <div className="recent-activity">
                    <h2>Movimientos Recientes</h2>
                    {/* Aquí podrías hacer otro fetch para la tabla si lo prefieres */}
                    <p style={{color: '#64748b', fontSize: '0.9rem'}}>Sincronizado con el servidor en tiempo real.</p>
                </div>

                <div className="info-side-card">
                    <h3>Estado del Sistema</h3>
                    <div className="status-item">
                        <span className="dot online"></span> Conectado a Azure
                    </div>
                    <div className="status-item">
                        <span className="dot online"></span> Base de Datos: Activa
                    </div>
                    <hr />
                    <p>Sede: Ica, Perú</p>
                </div>
            </div>
        </div>
    );
};

export default Inicio;
