import { useState, useEffect } from 'react';
import axios from '../../api/axiosConfig';
import QrCodeCard from './QrCodeCard';
import {
    UserPlus, CreditCard, Phone, CalendarDays, Clock, Home, CheckCircle, PlusCircle, Star, Truck,
} from 'lucide-react';
import './NuevaInvitacion.css';

const hoyISO = () => new Date().toISOString().slice(0, 10);
const finDelDia = (fechaISO) => `${fechaISO}T23:59`;

const initialFormState = {
    dni: '',
    nombres: '',
    apellidos: '',
    telefono: '',
    tipoVisita: 'NORMAL',
    empresaDelivery: '',
    fechaProgramada: hoyISO(),
    horaExpiracion: finDelDia(hoyISO()),
};

const NuevaInvitacion = () => {
    const [perfil, setPerfil] = useState(null);
    const [cargandoPerfil, setCargandoPerfil] = useState(true);
    const [formData, setFormData] = useState(initialFormState);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');
    const [invitacionCreada, setInvitacionCreada] = useState(null);

    useEffect(() => {
        axios.get('/usuarios/me')
            .then((r) => setPerfil(r.data))
            .catch(() => setError('No se pudo cargar tu perfil. Intenta nuevamente.'))
            .finally(() => setCargandoPerfil(false));
    }, []);

    const handleDniChange = (e) => {
        const dni = e.target.value.replace(/\D/g, '').slice(0, 8);
        setFormData((prev) => ({ ...prev, dni }));
    };

    const handleDniBlur = async () => {
        if (formData.dni.length !== 8) return;
        try {
            const res = await axios.get(`/visitantes/por-dni/${formData.dni}`);
            if (res.data) {
                setFormData((prev) => ({
                    ...prev,
                    nombres: res.data.nombre || prev.nombres,
                    apellidos: res.data.apellidos || prev.apellidos,
                    telefono: res.data.telefono || prev.telefono,
                }));
            }
        } catch {
            // Visitante nuevo: no hay datos previos, se completan manualmente.
        }
    };

    const handleFechaChange = (e) => {
        const fechaProgramada = e.target.value;
        setFormData((prev) => ({ ...prev, fechaProgramada, horaExpiracion: finDelDia(fechaProgramada) }));
    };

    const asegurarVisitante = async () => {
        const esDelivery = formData.tipoVisita === 'DELIVERY';
        let visitanteExistente = null;
        try {
            const res = await axios.get(`/visitantes/por-dni/${formData.dni}`);
            visitanteExistente = res.data;
        } catch (err) {
            if (err.response?.status !== 404) throw err;
        }

        if (!visitanteExistente) {
            await axios.post('/visitantes', {
                dni: formData.dni,
                nombre: formData.nombres,
                apellidos: formData.apellidos,
                telefono: formData.telefono || null,
                empresaDelivery: esDelivery ? formData.empresaDelivery : null,
            });
            return;
        }

        // Visitante ya existente: solo lo actualizamos si hace falta marcar
        // la empresa de delivery (sin tocar sus demás datos, p. ej. si está bloqueado).
        if (esDelivery && visitanteExistente.empresaDelivery !== formData.empresaDelivery) {
            await axios.post('/visitantes', { ...visitanteExistente, empresaDelivery: formData.empresaDelivery });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setEnviando(true);
        try {
            await asegurarVisitante();
            const payload = {
                visitante: { dni: formData.dni },
                departamentoDestino: { id: perfil.departamentoId },
                fechaProgramada: formData.fechaProgramada,
                horaExpiracion: formData.horaExpiracion || null,
            };
            const res = await axios.post('/invitaciones', payload);
            setInvitacionCreada(res.data);
        } catch (err) {
            setError(err.response?.data?.message || 'No se pudo crear la invitación. Verifica los datos.');
        } finally {
            setEnviando(false);
        }
    };

    const crearOtra = () => {
        setInvitacionCreada(null);
        setFormData({ ...initialFormState, fechaProgramada: hoyISO(), horaExpiracion: finDelDia(hoyISO()) });
    };

    const esDelivery = formData.tipoVisita === 'DELIVERY';

    if (cargandoPerfil) {
        return <div className="invitacion-empty">Cargando...</div>;
    }

    if (!perfil?.departamentoId) {
        return (
            <div className="invitacion-empty invitacion-empty--warning">
                No tienes un departamento asignado. Contacta al administrador del condominio para
                poder generar invitaciones.
            </div>
        );
    }

    if (invitacionCreada) {
        return (
            <div className="invitacion-resultado">
                <div className="invitacion-resultado-titulo">
                    <CheckCircle size={20} />
                    Invitación creada
                </div>
                <QrCodeCard invitacion={invitacionCreada} />
                <button className="btn-invitacion-primario" onClick={crearOtra}>
                    <PlusCircle size={18} />
                    Crear otra invitación
                </button>
            </div>
        );
    }

    return (
        <div className="invitacion-container">
            <header className="invitacion-header">
                <div className="invitacion-header-icon"><UserPlus size={22} /></div>
                <div>
                    <h2>Nueva invitación</h2>
                    <p>Genera un código QR para tu visita</p>
                </div>
            </header>

            <div className="invitacion-depto">
                <Home size={16} />
                <span>{perfil.departamentoTorre} - {perfil.departamentoNumero}</span>
            </div>

            {error && <div className="invitacion-error">{error}</div>}

            <form onSubmit={handleSubmit} className="invitacion-form">
                <div className="field">
                    <label>DNI del visitante</label>
                    <div className="input-box">
                        <CreditCard className="inner-icon" size={18} />
                        <input
                            type="text"
                            maxLength="8"
                            placeholder="8 dígitos"
                            required
                            value={formData.dni}
                            onChange={handleDniChange}
                            onBlur={handleDniBlur}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Nombres</label>
                    <div className="input-box">
                        <UserPlus className="inner-icon" size={18} />
                        <input
                            type="text"
                            required
                            value={formData.nombres}
                            onChange={(e) => setFormData((prev) => ({ ...prev, nombres: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Apellidos</label>
                    <div className="input-box">
                        <UserPlus className="inner-icon" size={18} />
                        <input
                            type="text"
                            required
                            value={formData.apellidos}
                            onChange={(e) => setFormData((prev) => ({ ...prev, apellidos: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Teléfono (opcional)</label>
                    <div className="input-box">
                        <Phone className="inner-icon" size={18} />
                        <input
                            type="text"
                            value={formData.telefono}
                            onChange={(e) => setFormData((prev) => ({ ...prev, telefono: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Tipo de Visita</label>
                    <div className="input-box">
                        <Star className="inner-icon" size={18} />
                        <select
                            value={formData.tipoVisita}
                            onChange={(e) => setFormData((prev) => ({ ...prev, tipoVisita: e.target.value }))}
                        >
                            <option value="NORMAL">Normal</option>
                            <option value="DELIVERY">Delivery</option>
                        </select>
                    </div>
                </div>

                {esDelivery && (
                    <div className="field anim-fade">
                        <label>Empresa de Delivery</label>
                        <div className="input-box">
                            <Truck className="inner-icon" size={18} />
                            <input
                                type="text"
                                placeholder="Rappi, Glovo, PedidosYa..."
                                required
                                value={formData.empresaDelivery}
                                onChange={(e) => setFormData((prev) => ({ ...prev, empresaDelivery: e.target.value }))}
                            />
                        </div>
                    </div>
                )}

                <div className="field">
                    <label>Fecha programada</label>
                    <div className="input-box">
                        <CalendarDays className="inner-icon" size={18} />
                        <input
                            type="date"
                            required
                            min={hoyISO()}
                            value={formData.fechaProgramada}
                            onChange={handleFechaChange}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Expira a las (opcional)</label>
                    <div className="input-box">
                        <Clock className="inner-icon" size={18} />
                        <input
                            type="datetime-local"
                            value={formData.horaExpiracion}
                            onChange={(e) => setFormData((prev) => ({ ...prev, horaExpiracion: e.target.value }))}
                        />
                    </div>
                </div>

                <button type="submit" className="btn-invitacion-primario" disabled={enviando}>
                    {enviando ? 'Generando...' : <><CheckCircle size={18} /> Generar código QR</>}
                </button>
            </form>
        </div>
    );
};

export default NuevaInvitacion;
