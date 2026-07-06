import { QRCodeSVG } from 'qrcode.react';
import { User, CreditCard, Home, CalendarDays } from 'lucide-react';
import './QrCodeCard.css';

const ESTADO_INFO = {
    PENDIENTE: { label: 'Pendiente', clase: 'estado--pendiente' },
    UTILIZADA: { label: 'Utilizada', clase: 'estado--utilizada' },
    EXPIRADA: { label: 'Expirada', clase: 'estado--expirada' },
};

const formatFecha = (fechaISO) => {
    if (!fechaISO) return '—';
    return new Date(`${fechaISO}T00:00:00`).toLocaleDateString('es-PE', {
        day: '2-digit', month: 'short', year: 'numeric',
    });
};

const QrCodeCard = ({ invitacion }) => {
    const estadoInfo = ESTADO_INFO[invitacion.estado] || { label: invitacion.estado, clase: '' };

    return (
        <div className="qr-card">
            <div className={`qr-card-estado ${estadoInfo.clase}`}>{estadoInfo.label}</div>

            <div className="qr-card-code">
                <QRCodeSVG value={invitacion.codigoQrHash} size={200} level="M" />
            </div>

            <div className="qr-card-detalle">
                <div className="qr-card-fila">
                    <User size={16} />
                    <span>{invitacion.nombreVisitante || 'Visitante'}</span>
                </div>
                <div className="qr-card-fila">
                    <CreditCard size={16} />
                    <span>DNI: {invitacion.dniVisitante}</span>
                </div>
                <div className="qr-card-fila">
                    <Home size={16} />
                    <span>{invitacion.nombreDepartamento}</span>
                </div>
                <div className="qr-card-fila">
                    <CalendarDays size={16} />
                    <span>{formatFecha(invitacion.fechaProgramada)}</span>
                </div>
            </div>
        </div>
    );
};

export default QrCodeCard;
