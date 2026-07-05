import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Building2, Search, UserX, User, UserPlus, Phone, Mail, CreditCard,
    X, UserCheck, Sparkles, Home, ChevronDown
} from 'lucide-react';
import api from '../../api/axiosConfig';
import './GestionShared.css';

const GestionDepartamentos = () => {
    const navigate = useNavigate();
    const [departamentos, setDepartamentos] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [filtroTorre, setFiltroTorre] = useState('TODAS');
    const [filtroEstado, setFiltroEstado] = useState('TODOS');
    const [loading, setLoading] = useState(true);
    const [procesando, setProcesando] = useState(null);
    const [deptoParaAsignar, setDeptoParaAsignar] = useState(null);

    const cargarDepartamentos = () => {
        api.get('/departamentos')
            .then(r => setDepartamentos(r.data))
            .catch(e => console.error('Error cargando departamentos:', e))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        cargarDepartamentos();
    }, []);

    const torres = useMemo(() => {
        const nombres = departamentos.map(d => d.bloqueTorre).filter(Boolean);
        return [...new Set(nombres)].sort();
    }, [departamentos]);

    const stats = useMemo(() => {
        const total = departamentos.length;
        const ocupados = departamentos.filter(d => d.estado === 'Ocupado').length;
        const libres = total - ocupados;
        const porcentaje = total === 0 ? 0 : Math.round((ocupados / total) * 100);
        return { total, ocupados, libres, porcentaje };
    }, [departamentos]);

    const departamentosFiltrados = useMemo(() => {
        const texto = busqueda.trim().toLowerCase();
        return departamentos.filter(d => {
            const coincideTorre = filtroTorre === 'TODAS' || d.bloqueTorre === filtroTorre;
            const coincideEstado = filtroEstado === 'TODOS' || d.estado === filtroEstado;
            const coincideTexto = texto === '' ||
                d.bloqueTorre?.toLowerCase().includes(texto) ||
                d.numeroDepa?.toLowerCase().includes(texto) ||
                d.residente?.toLowerCase().includes(texto);
            return coincideTorre && coincideEstado && coincideTexto;
        });
    }, [departamentos, busqueda, filtroTorre, filtroEstado]);

    const grupos = useMemo(() => {
        const mapa = new Map();
        departamentosFiltrados.forEach(d => {
            const clave = d.bloqueTorre || 'Sin torre';
            if (!mapa.has(clave)) mapa.set(clave, []);
            mapa.get(clave).push(d);
        });
        return [...mapa.entries()]
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map(([torre, items]) => [
                torre,
                items.sort((a, b) => (a.numeroDepa || '').localeCompare(b.numeroDepa || '', undefined, { numeric: true })),
            ]);
    }, [departamentosFiltrados]);

    const liberarDepartamento = async (depto) => {
        if (!window.confirm(`¿Dar de baja al residente del departamento ${depto.numeroDepa}${depto.bloqueTorre ? ` - Torre ${depto.bloqueTorre}` : ''}?`)) {
            return;
        }
        setProcesando(depto.id);
        try {
            const res = await api.put(`/departamentos/${depto.id}/liberar`);
            setDepartamentos(prev => prev.map(d => d.id === depto.id ? res.data : d));
        } catch (error) {
            console.error('Error liberando departamento:', error);
            alert(error.response?.data || '❌ No se pudo dar de baja al residente.');
        } finally {
            setProcesando(null);
        }
    };

    const onAsignado = (deptoActualizado) => {
        setDepartamentos(prev => prev.map(d => d.id === deptoActualizado.id ? deptoActualizado : d));
        setDeptoParaAsignar(null);
    };

    return (
        <div className="gestion-container">
            <header className="gestion-header">
                <div className="gestion-header-icon"><Building2 size={26} /></div>
                <div className="gestion-header-text">
                    <h2>Gestión de Departamentos</h2>
                    <p>Consulta la ocupación de los departamentos del condominio</p>
                </div>
            </header>

            <div className="stats-row">
                <div className="stat-card">
                    <span className="stat-valor">{stats.total}</span>
                    <span className="stat-label">Departamentos</span>
                </div>
                <div className="stat-card ocupado">
                    <span className="stat-valor">{stats.ocupados}</span>
                    <span className="stat-label">Ocupados</span>
                </div>
                <div className="stat-card libre">
                    <span className="stat-valor">{stats.libres}</span>
                    <span className="stat-label">Libres</span>
                </div>
                <div className="stat-card">
                    <span className="stat-valor">{stats.porcentaje}%</span>
                    <span className="stat-label">Ocupación</span>
                </div>
            </div>

            <div className="gestion-filtros">
                <div className="input-box">
                    <Search className="inner-icon" size={18} />
                    <input
                        type="text"
                        placeholder="Buscar por torre, número o residente..."
                        value={busqueda}
                        onChange={(e) => setBusqueda(e.target.value)}
                    />
                </div>
                <div className="input-box">
                    <Home className="inner-icon" size={18} />
                    <select value={filtroTorre} onChange={(e) => setFiltroTorre(e.target.value)}>
                        <option value="TODAS">Todas las torres</option>
                        {torres.map(t => <option key={t} value={t}>Torre {t}</option>)}
                    </select>
                </div>
                <div className="input-box">
                    <ChevronDown className="inner-icon" size={18} />
                    <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                        <option value="TODOS">Todos los estados</option>
                        <option value="Ocupado">Ocupado</option>
                        <option value="Libre">Libre</option>
                    </select>
                </div>
            </div>

            {loading ? (
                <div className="gestion-empty">
                    <p>Cargando departamentos...</p>
                </div>
            ) : departamentosFiltrados.length === 0 ? (
                <div className="gestion-empty">
                    <Building2 className="gestion-empty-icon" size={40} />
                    <p>No se encontraron departamentos.</p>
                </div>
            ) : (
                grupos.map(([torre, items]) => (
                    <div key={torre} className="torre-grupo">
                        <div className="torre-grupo-header">
                            <span>Torre {torre}</span>
                            <span className="torre-grupo-count">
                                {items.filter(d => d.estado === 'Ocupado').length}/{items.length} ocupados
                            </span>
                        </div>
                        <div className="gestion-lista">
                            {items.map(depto => (
                                <div key={depto.id} className="usuario-card">
                                    <div className="usuario-info">
                                        <div className="usuario-nombre-row">
                                            <span className="usuario-nombre">{depto.numeroDepa}</span>
                                            <span className={`badge-estado ${depto.estado === 'Ocupado' ? 'activo' : 'inactivo'}`}>
                                                {depto.estado}
                                            </span>
                                        </div>
                                        <div className="usuario-meta">
                                            <span className="meta-item"><User size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{depto.residente}</span>
                                            {depto.estado === 'Ocupado' && depto.residenteDni && (
                                                <span className="meta-item"><CreditCard size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{depto.residenteDni}</span>
                                            )}
                                            {depto.estado === 'Ocupado' && depto.residenteTelefono && (
                                                <span className="meta-item"><Phone size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{depto.residenteTelefono}</span>
                                            )}
                                            {depto.estado === 'Ocupado' && depto.residenteEmail && (
                                                <span className="meta-item"><Mail size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{depto.residenteEmail}</span>
                                            )}
                                        </div>
                                    </div>
                                    <div className="usuario-actions">
                                        {depto.estado === 'Ocupado' ? (
                                            <button className="btn-accion baja" disabled={procesando === depto.id}
                                                onClick={() => liberarDepartamento(depto)}>
                                                <UserX size={16} /> Dar de Baja
                                            </button>
                                        ) : (
                                            <button className="btn-accion activar" disabled={procesando === depto.id}
                                                onClick={() => setDeptoParaAsignar(depto)}>
                                                <UserPlus size={16} /> Asignar Residente
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))
            )}

            {deptoParaAsignar && (
                <AsignarResidenteModal
                    depto={deptoParaAsignar}
                    onClose={() => setDeptoParaAsignar(null)}
                    onAsignado={onAsignado}
                    navigate={navigate}
                />
            )}
        </div>
    );
};

const AsignarResidenteModal = ({ depto, onClose, onAsignado, navigate }) => {
    const [tab, setTab] = useState('existente');
    const [candidatos, setCandidatos] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [busqueda, setBusqueda] = useState('');
    const [seleccionado, setSeleccionado] = useState(null);
    const [asignando, setAsignando] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        api.get('/usuarios/residentes-disponibles')
            .then(r => setCandidatos(r.data))
            .catch(e => console.error('Error cargando residentes disponibles:', e))
            .finally(() => setCargando(false));
    }, []);

    const candidatosFiltrados = useMemo(() => {
        const texto = busqueda.trim().toLowerCase();
        if (texto === '') return candidatos;
        return candidatos.filter(c => c.nombreCompleto?.toLowerCase().includes(texto));
    }, [candidatos, busqueda]);

    const etiquetaDepto = `${depto.numeroDepa}${depto.bloqueTorre ? ` - Torre ${depto.bloqueTorre}` : ''}`;

    const confirmarAsignacion = async () => {
        if (!seleccionado) return;
        setAsignando(true);
        setError('');
        try {
            const res = await api.put(`/departamentos/${depto.id}/asignar`, { usuarioId: seleccionado.idUsuario });
            onAsignado(res.data);
        } catch (err) {
            console.error('Error asignando residente:', err);
            setError(err.response?.data || '❌ No se pudo asignar al residente.');
        } finally {
            setAsignando(false);
        }
    };

    const irARegistrarUsuario = () => {
        navigate('/admin/registrar-usuario', {
            state: { departamentoId: depto.id, departamentoLabel: etiquetaDepto },
        });
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-panel" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <div>
                        <h3>Asignar Residente</h3>
                        <p>Departamento {etiquetaDepto}</p>
                    </div>
                    <button className="modal-close" onClick={onClose}><X size={20} /></button>
                </div>

                <div className="modal-tabs">
                    <button className={`modal-tab ${tab === 'existente' ? 'activo' : ''}`} onClick={() => setTab('existente')}>
                        <UserCheck size={16} /> Usuario existente
                    </button>
                    <button className={`modal-tab ${tab === 'nuevo' ? 'activo' : ''}`} onClick={() => setTab('nuevo')}>
                        <Sparkles size={16} /> Crear nuevo
                    </button>
                </div>

                {error && <div className="form-error">{error}</div>}

                {tab === 'existente' ? (
                    <div className="modal-body">
                        <div className="input-box">
                            <Search className="inner-icon" size={18} />
                            <input
                                type="text"
                                placeholder="Buscar residente por nombre..."
                                value={busqueda}
                                onChange={(e) => setBusqueda(e.target.value)}
                            />
                        </div>

                        {cargando ? (
                            <p className="modal-hint">Cargando residentes disponibles...</p>
                        ) : candidatosFiltrados.length === 0 ? (
                            <p className="modal-hint">No hay residentes registrados sin departamento. Puedes crear uno nuevo en la otra pestaña.</p>
                        ) : (
                            <div className="candidatos-lista">
                                {candidatosFiltrados.map(c => (
                                    <button
                                        key={c.idUsuario}
                                        className={`candidato-item ${seleccionado?.idUsuario === c.idUsuario ? 'seleccionado' : ''}`}
                                        onClick={() => setSeleccionado(c)}
                                    >
                                        <User size={16} />
                                        <span>{c.nombreCompleto}</span>
                                    </button>
                                ))}
                            </div>
                        )}

                        <button
                            className="btn-save"
                            disabled={!seleccionado || asignando}
                            onClick={confirmarAsignacion}
                        >
                            {asignando ? 'Asignando...' : 'Confirmar Asignación'}
                        </button>
                    </div>
                ) : (
                    <div className="modal-body">
                        <p className="modal-hint">
                            Se abrirá el formulario de registro de usuario con el departamento {etiquetaDepto} ya seleccionado.
                        </p>
                        <button className="btn-save" onClick={irARegistrarUsuario}>
                            <UserPlus size={18} /> Ir a Registrar Usuario
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default GestionDepartamentos;
