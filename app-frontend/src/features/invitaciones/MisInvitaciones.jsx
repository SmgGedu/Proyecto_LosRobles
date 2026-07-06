import { useState, useEffect } from 'react';
import axios from '../../api/axiosConfig';
import QrCodeCard from './QrCodeCard';
import { ListChecks, ChevronDown, ChevronUp } from 'lucide-react';
import './MisInvitaciones.css';

const ESTADO_CLASE = {
    PENDIENTE: 'badge--pendiente',
    UTILIZADA: 'badge--utilizada',
    EXPIRADA: 'badge--expirada',
};

const formatFecha = (fechaISO) => {
    if (!fechaISO) return '—';
    return new Date(`${fechaISO}T00:00:00`).toLocaleDateString('es-PE', {
        day: '2-digit', month: 'short', year: 'numeric',
    });
};

const MisInvitaciones = () => {
    const [invitaciones, setInvitaciones] = useState([]);
    const [loading, setLoading] = useState(true);
    const [abiertaId, setAbiertaId] = useState(null);

    useEffect(() => {
        axios.get('/invitaciones/mias')
            .then((r) => setInvitaciones(r.data))
            .catch((e) => console.error('Error cargando mis invitaciones:', e))
            .finally(() => setLoading(false));
    }, []);

    const toggle = (id) => setAbiertaId((prev) => (prev === id ? null : id));

    return (
        <div className="mis-invitaciones-container">
            <header className="mis-invitaciones-header">
                <ListChecks size={20} />
                <h2>Mis invitaciones</h2>
            </header>

            {loading ? (
                <div className="mis-invitaciones-empty">Cargando...</div>
            ) : invitaciones.length === 0 ? (
                <div className="mis-invitaciones-empty">Aún no has generado ninguna invitación.</div>
            ) : (
                <div className="mis-invitaciones-lista">
                    {invitaciones.map((inv) => (
                        <div key={inv.id} className="mis-invitaciones-item">
                            <button className="mis-invitaciones-fila" onClick={() => toggle(inv.id)}>
                                <div className="mis-invitaciones-info">
                                    <span className="mis-invitaciones-nombre">{inv.nombreVisitante}</span>
                                    <span className="mis-invitaciones-meta">
                                        DNI {inv.dniVisitante} · {formatFecha(inv.fechaProgramada)}
                                    </span>
                                </div>
                                <span className={`mis-invitaciones-badge ${ESTADO_CLASE[inv.estado] || ''}`}>
                                    {inv.estado}
                                </span>
                                {abiertaId === inv.id ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                            </button>
                            {abiertaId === inv.id && (
                                <div className="mis-invitaciones-detalle">
                                    <QrCodeCard invitacion={inv} />
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default MisInvitaciones;
