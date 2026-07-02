import { useState, useEffect, useMemo } from 'react';
import { Building2, Search, UserX, User } from 'lucide-react';
import api from '../../api/axiosConfig';
import './GestionShared.css';

const GestionDepartamentos = () => {
    const [departamentos, setDepartamentos] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [loading, setLoading] = useState(true);
    const [procesando, setProcesando] = useState(null);

    const cargarDepartamentos = () => {
        api.get('/departamentos')
            .then(r => setDepartamentos(r.data))
            .catch(e => console.error('Error cargando departamentos:', e))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        cargarDepartamentos();
    }, []);

    const departamentosFiltrados = useMemo(() => {
        const texto = busqueda.trim().toLowerCase();
        if (texto === '') return departamentos;
        return departamentos.filter(d =>
            d.bloqueTorre?.toLowerCase().includes(texto) ||
            d.numeroDepa?.toLowerCase().includes(texto) ||
            d.residente?.toLowerCase().includes(texto)
        );
    }, [departamentos, busqueda]);

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

    return (
        <div className="gestion-container">
            <header className="gestion-header">
                <div className="gestion-header-icon"><Building2 size={26} /></div>
                <div className="gestion-header-text">
                    <h2>Gestión de Departamentos</h2>
                    <p>Consulta la ocupación de los departamentos del condominio</p>
                </div>
            </header>

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
                <div className="gestion-lista">
                    {departamentosFiltrados.map(depto => (
                        <div key={depto.id} className="usuario-card">
                            <div className="usuario-info">
                                <div className="usuario-nombre-row">
                                    <span className="usuario-nombre">
                                        {depto.numeroDepa}{depto.bloqueTorre ? ` - Torre ${depto.bloqueTorre}` : ''}
                                    </span>
                                    <span className={`badge-estado ${depto.estado === 'Ocupado' ? 'activo' : 'inactivo'}`}>
                                        {depto.estado}
                                    </span>
                                </div>
                                <div className="usuario-meta">
                                    <span className="meta-item"><User size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{depto.residente}</span>
                                </div>
                            </div>
                            {depto.estado === 'Ocupado' && (
                                <div className="usuario-actions">
                                    <button className="btn-accion baja" disabled={procesando === depto.id}
                                        onClick={() => liberarDepartamento(depto)}>
                                        <UserX size={16} /> Dar de Baja
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default GestionDepartamentos;
