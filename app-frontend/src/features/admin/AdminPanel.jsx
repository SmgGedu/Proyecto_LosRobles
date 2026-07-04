import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, UserPlus, Users, Building2, FileDown, FileSpreadsheet, Calendar, ListFilter, Gauge, Save } from 'lucide-react';
import api from '../../api/axiosConfig';
import './AdminPanel.css';

const AdminPanel = () => {
    const navigate = useNavigate();
    const today = new Date().toISOString().slice(0, 10);
    const [desde, setDesde] = useState(today);
    const [hasta, setHasta] = useState(today);
    const [categoria, setCategoria] = useState('TODOS');
    const [descargando, setDescargando] = useState(null);

    const [aforoMaximo, setAforoMaximo] = useState('');
    const [tiempoMaximoVisitaMinutos, setTiempoMaximoVisitaMinutos] = useState('');
    const [guardandoAforo, setGuardandoAforo] = useState(false);
    const [mensajeAforo, setMensajeAforo] = useState(null);

    useEffect(() => {
        const cargarConfiguracion = async () => {
            try {
                const response = await api.get('/configuracion/aforo');
                setAforoMaximo(response.data.aforoMaximo);
                setTiempoMaximoVisitaMinutos(response.data.tiempoMaximoVisitaMinutos);
            } catch (error) {
                console.error('Error cargando configuración de aforo:', error);
            }
        };
        cargarConfiguracion();
    }, []);

    const guardarConfiguracionAforo = async () => {
        setGuardandoAforo(true);
        setMensajeAforo(null);
        try {
            await api.put('/configuracion/aforo', {
                aforoMaximo: Number(aforoMaximo),
                tiempoMaximoVisitaMinutos: Number(tiempoMaximoVisitaMinutos),
            });
            setMensajeAforo({ tipo: 'ok', texto: 'Configuración guardada correctamente.' });
        } catch (error) {
            console.error('Error guardando configuración de aforo:', error);
            setMensajeAforo({ tipo: 'error', texto: 'No se pudo guardar la configuración.' });
        } finally {
            setGuardandoAforo(false);
        }
    };

    const descargar = async (formato) => {
        if (!desde || !hasta) {
            alert('Selecciona el rango de fechas.');
            return;
        }
        setDescargando(formato);
        try {
            const params = { desde, hasta };
            if (categoria !== 'TODOS') params.categoria = categoria;
            const response = await api.get(`/reportes/registros/${formato}`, {
                params,
                responseType: 'blob',
            });
            const extension = formato === 'excel' ? 'xlsx' : 'pdf';
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.download = `reporte_accesos_${desde}_a_${hasta}.${extension}`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error generando reporte:', error);
            alert('❌ Error al generar el reporte.');
        } finally {
            setDescargando(null);
        }
    };

    return (
        <div className="admin-container">
            <header className="admin-header">
                <div className="admin-header-icon"><ShieldCheck size={26} /></div>
                <div className="admin-header-text">
                    <h2>Panel de Administrador</h2>
                    <p>Operaciones, auditoría y reportes del condominio</p>
                </div>
            </header>

            <div className="admin-grid">
                {/* ── Gestión del Condominio ── */}
                <section className="admin-section">
                    <h3>Gestión del Condominio</h3>
                    <div className="admin-actions">
                        <button className="btn-admin" onClick={() => navigate('/admin/registrar-usuario')}>
                            <UserPlus size={20} /> Registrar Usuario
                        </button>
                        <button className="btn-admin" onClick={() => navigate('/admin/usuarios')}>
                            <Users size={20} /> Gestión de Usuarios
                        </button>
                        <button className="btn-admin" onClick={() => navigate('/admin/departamentos')}>
                            <Building2 size={20} /> Gestión de Departamentos
                        </button>
                    </div>
                </section>

                {/* ── Configuración de Aforo ── */}
                <section className="admin-section">
                    <h3>Configuración de Aforo</h3>
                    <div className="reportes-form">
                        <div className="field-group">
                            <label>Aforo máximo (visitantes dentro a la vez)</label>
                            <div className="input-box">
                                <Gauge className="inner-icon" size={16} />
                                <input
                                    type="number"
                                    min="1"
                                    value={aforoMaximo}
                                    onChange={(e) => setAforoMaximo(e.target.value)}
                                />
                            </div>
                        </div>

                        <div className="field-group">
                            <label>Tiempo máximo de visita (minutos)</label>
                            <div className="input-box">
                                <Calendar className="inner-icon" size={16} />
                                <input
                                    type="number"
                                    min="1"
                                    value={tiempoMaximoVisitaMinutos}
                                    onChange={(e) => setTiempoMaximoVisitaMinutos(e.target.value)}
                                />
                            </div>
                        </div>

                        {mensajeAforo && (
                            <p style={{
                                color: mensajeAforo.tipo === 'ok' ? '#15803d' : '#be123c',
                                fontSize: '0.85rem',
                                fontWeight: 600,
                                margin: 0,
                            }}>
                                {mensajeAforo.texto}
                            </p>
                        )}

                        <button className="btn-admin" disabled={guardandoAforo} onClick={guardarConfiguracionAforo}>
                            <Save size={20} /> {guardandoAforo ? 'Guardando...' : 'Guardar Configuración'}
                        </button>
                    </div>
                </section>

                {/* ── Auditoría y Reportes ── */}
                <section className="admin-section">
                    <h3>Auditoría y Reportes</h3>
                    <div className="reportes-form">
                        <div className="field-group">
                            <label>Periodo</label>
                            <div className="periodo-row">
                                <div className="input-box">
                                    <Calendar className="inner-icon" size={16} />
                                    <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
                                </div>
                                <span className="periodo-sep">a</span>
                                <div className="input-box">
                                    <Calendar className="inner-icon" size={16} />
                                    <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
                                </div>
                            </div>
                        </div>

                        <div className="field-group">
                            <label>Categoría de Visita</label>
                            <div className="input-box">
                                <ListFilter className="inner-icon" size={16} />
                                <select value={categoria} onChange={(e) => setCategoria(e.target.value)}>
                                    <option value="TODOS">Todas</option>
                                    <option value="NORMAL">Normal</option>
                                    <option value="FRECUENTE">Frecuente</option>
                                    <option value="DELIVERY">Delivery</option>
                                </select>
                            </div>
                        </div>

                        <div className="reportes-actions">
                            <button className="btn-reporte pdf" disabled={descargando !== null} onClick={() => descargar('pdf')}>
                                <FileDown size={18} />
                                {descargando === 'pdf' ? 'Generando...' : 'Descargar PDF'}
                            </button>
                            <button className="btn-reporte excel" disabled={descargando !== null} onClick={() => descargar('excel')}>
                                <FileSpreadsheet size={18} />
                                {descargando === 'excel' ? 'Generando...' : 'Descargar Excel'}
                            </button>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    );
};

export default AdminPanel;
