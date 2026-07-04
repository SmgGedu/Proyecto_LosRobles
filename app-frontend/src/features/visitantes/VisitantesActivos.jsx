import { useState, useEffect } from 'react';
import { Users, Clock, Car, Home, RefreshCw, LogOut, Package, ShieldCheck, Truck, Star, QrCode, AlertTriangle, MapPin } from 'lucide-react';
import axios from '../../api/axiosConfig';
import './VisitantesActivos.css';

const REFRESCO_MS = 17000;

const VisitantesActivos = () => {
    const [visitantes, setVisitantes] = useState([]);
    const [total, setTotal] = useState(0);
    const [aforoMaximo, setAforoMaximo] = useState(0);
    const [aforoExcedido, setAforoExcedido] = useState(false);
    const [desglosePorZona, setDesglosePorZona] = useState([]);
    const [loading, setLoading] = useState(true);
    const [registrandoSalida, setRegistrandoSalida] = useState(null);

    const fetchActivos = async () => {
        try {
            const response = await axios.get('/accesos/activos');
            setVisitantes(response.data.visitantes || []);
            setTotal(response.data.totalEnEdificio || 0);
            setAforoMaximo(response.data.aforoMaximo || 0);
            setAforoExcedido(Boolean(response.data.aforoExcedido));
            setDesglosePorZona(response.data.desglosePorZona || []);
        } catch (error) {
            console.error('Error cargando visitantes activos:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const cargar = async () => {
            await fetchActivos();
        };
        cargar();
        const intervalo = setInterval(cargar, REFRESCO_MS);
        return () => clearInterval(intervalo);
    }, []);

    const registrarSalida = async (id) => {
        setRegistrandoSalida(id);
        try {
            await axios.put(`/accesos/salida/${id}`);
            await fetchActivos();
        } catch (error) {
            console.error('Error registrando salida:', error);
            alert('❌ Error al registrar salida.');
        } finally {
            setRegistrandoSalida(null);
        }
    };

    const formatHora = (horaISO) => {
        if (!horaISO) return '—';
        return new Date(horaISO).toLocaleTimeString('es-PE', {
            hour: '2-digit', minute: '2-digit'
        });
    };

    const formatTranscurrido = (minutos) => {
        if (minutos == null) return '—';
        if (minutos < 60) return `${minutos} min`;
        const horas = Math.floor(minutos / 60);
        const resto = minutos % 60;
        return `${horas}h ${resto}m`;
    };

    const ocupacionRatio = aforoMaximo > 0 ? total / aforoMaximo : 0;
    const aforoBadgeClase = aforoExcedido
        ? 'activos-badge--danger'
        : ocupacionRatio >= 0.9
            ? 'activos-badge--warning'
            : '';

    return (
        <div className="activos-container">
            <div className="activos-header">
                <div className="activos-header-left">
                    <div className="activos-header-icon">
                        <Users size={26} />
                    </div>
                    <div className="activos-header-text">
                        <h2>Visitantes en el Edificio</h2>
                        <p>Sede: Ica, Perú | Sistema Los Robles</p>
                    </div>
                </div>
                <div className="activos-header-right">
                    <div className={`activos-badge ${aforoBadgeClase}`}>
                        <ShieldCheck size={16} />
                        <span>{total}{aforoMaximo > 0 ? ` / ${aforoMaximo}` : ''} dentro ahora</span>
                    </div>
                    <button className="btn-refresh" onClick={fetchActivos}>
                        <RefreshCw size={16} />
                        Actualizar
                    </button>
                </div>
            </div>

            {aforoExcedido && (
                <div className="alerta-aforo">
                    <AlertTriangle size={18} />
                    Se superó el aforo máximo configurado ({aforoMaximo}).
                </div>
            )}

            {desglosePorZona.length > 0 && (
                <div className="zonas-row">
                    {desglosePorZona.map((z) => (
                        <div key={z.zona} className="zona-chip">
                            <MapPin size={13} />
                            <span>{z.zona || 'Sin zona'}</span>
                            <strong>{z.cantidad}</strong>
                        </div>
                    ))}
                </div>
            )}

            {loading ? (
                <div className="activos-empty">
                    <p>Cargando visitantes...</p>
                </div>
            ) : visitantes.length === 0 ? (
                <div className="activos-empty">
                    <Users size={48} className="activos-empty-icon" />
                    <p>No hay visitantes en el edificio ahora mismo.</p>
                </div>
            ) : (
                <div className="activos-lista">
                    {visitantes.map((v) => (
                        <div key={v.id} className={`visitante-card ${v.excedeTiempoEsperado ? 'overstay' : ''}`}>
                            <div className="visitante-card-body">
                                <div className="visitante-info">
                                    <div className="visitante-nombre-row">
                                        <span className="visitante-nombre">{v.nombreVisitante}</span>
                                        {v.tipoVisita === 'DELIVERY' && (
                                            <span className="badge-tipo delivery">
                                                <Truck size={12} /> Delivery
                                            </span>
                                        )}
                                        {v.tipoVisita === 'FRECUENTE' && (
                                            <span className="badge-tipo frecuente">
                                                <Star size={12} /> Frecuente
                                            </span>
                                        )}
                                        {v.tipoIngreso === 'QR' && (
                                            <span className="badge-tipo qr">
                                                <QrCode size={12} /> QR
                                            </span>
                                        )}
                                        {v.excedeTiempoEsperado && (
                                            <span className="badge-tipo overstay-badge">
                                                <AlertTriangle size={12} /> Tiempo excedido
                                            </span>
                                        )}
                                    </div>
                                    <div className="visitante-dni">DNI: {v.dniVisitante}</div>
                                    <div className="visitante-meta">
                                        <span className="meta-item">
                                            <Home size={14} /> {v.departamento}
                                        </span>
                                        <span className="meta-item">
                                            <Clock size={14} /> Ingreso: {formatHora(v.horaEntrada)}
                                        </span>
                                        <span className="meta-item">
                                            <Clock size={14} /> Transcurrido: {formatTranscurrido(v.minutosTranscurridos)}
                                        </span>
                                        {v.placaVehiculo && (
                                            <span className="meta-item">
                                                <Car size={14} /> {v.placaVehiculo}
                                            </span>
                                        )}
                                        {v.objetos && v.objetos.length > 0 && (
                                            <span className="meta-item">
                                                <Package size={14} />
                                                {v.objetos.length} objeto{v.objetos.length !== 1 ? 's' : ''}
                                            </span>
                                        )}
                                    </div>
                                    <div className="visitante-anfitrion">
                                        Autorizado por: <strong>{v.nombreAnfitrion}</strong>
                                    </div>
                                </div>
                                <button
                                    className={`btn-salida ${registrandoSalida === v.id ? 'loading' : ''}`}
                                    onClick={() => registrarSalida(v.id)}
                                    disabled={registrandoSalida === v.id}
                                >
                                    <LogOut size={16} />
                                    {registrandoSalida === v.id ? 'Registrando...' : 'Registrar Salida'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default VisitantesActivos;
