import { useState, useEffect, useMemo } from 'react';
import { Users, Search, ShieldCheck, UserX, UserCheck, Trash2, Home, CreditCard } from 'lucide-react';
import api from '../../api/axiosConfig';
import './GestionShared.css';

const GestionUsuarios = () => {
    const [usuarios, setUsuarios] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [filtroRol, setFiltroRol] = useState('TODOS');
    const [loading, setLoading] = useState(true);
    const [procesando, setProcesando] = useState(null);

    const cargarUsuarios = () => {
        api.get('/usuarios')
            .then(r => setUsuarios(r.data))
            .catch(e => console.error('Error cargando usuarios:', e))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        cargarUsuarios();
    }, []);

    const roles = useMemo(() => {
        const nombres = usuarios.map(u => u.rolNombre).filter(Boolean);
        return [...new Set(nombres)];
    }, [usuarios]);

    const usuariosFiltrados = useMemo(() => {
        return usuarios.filter(u => {
            const coincideRol = filtroRol === 'TODOS' || u.rolNombre === filtroRol;
            const texto = busqueda.trim().toLowerCase();
            const coincideTexto = texto === '' ||
                u.nombres?.toLowerCase().includes(texto) ||
                u.apellidos?.toLowerCase().includes(texto) ||
                u.dni?.toLowerCase().includes(texto) ||
                u.username?.toLowerCase().includes(texto);
            return coincideRol && coincideTexto;
        });
    }, [usuarios, busqueda, filtroRol]);

    const cambiarEstado = async (usuario, estado) => {
        setProcesando(usuario.id);
        try {
            await api.put(`/usuarios/${usuario.id}/estado`, { estado });
            setUsuarios(prev => prev.map(u => u.id === usuario.id ? { ...u, estado } : u));
        } catch (error) {
            console.error('Error cambiando estado:', error);
            alert('❌ No se pudo actualizar el estado del usuario.');
        } finally {
            setProcesando(null);
        }
    };

    const eliminarUsuario = async (usuario) => {
        if (!window.confirm(`¿Eliminar permanentemente a ${usuario.nombres} ${usuario.apellidos}? Esta acción no se puede deshacer.`)) {
            return;
        }
        setProcesando(usuario.id);
        try {
            await api.delete(`/usuarios/${usuario.id}`);
            setUsuarios(prev => prev.filter(u => u.id !== usuario.id));
        } catch (error) {
            console.error('Error eliminando usuario:', error);
            alert(error.response?.data || '❌ No se pudo eliminar el usuario.');
        } finally {
            setProcesando(null);
        }
    };

    return (
        <div className="gestion-container">
            <header className="gestion-header">
                <div className="gestion-header-icon"><Users size={26} /></div>
                <div className="gestion-header-text">
                    <h2>Gestión de Usuarios</h2>
                    <p>Administra el personal y residentes del condominio</p>
                </div>
            </header>

            <div className="gestion-filtros">
                <div className="input-box">
                    <Search className="inner-icon" size={18} />
                    <input
                        type="text"
                        placeholder="Buscar por nombre, apellido, DNI o usuario..."
                        value={busqueda}
                        onChange={(e) => setBusqueda(e.target.value)}
                    />
                </div>
                <div className="input-box">
                    <ShieldCheck className="inner-icon" size={18} />
                    <select value={filtroRol} onChange={(e) => setFiltroRol(e.target.value)}>
                        <option value="TODOS">Todos los roles</option>
                        {roles.map(rol => (
                            <option key={rol} value={rol}>{rol}</option>
                        ))}
                    </select>
                </div>
            </div>

            {loading ? (
                <div className="gestion-empty">
                    <p>Cargando usuarios...</p>
                </div>
            ) : usuariosFiltrados.length === 0 ? (
                <div className="gestion-empty">
                    <Users className="gestion-empty-icon" size={40} />
                    <p>No se encontraron usuarios.</p>
                </div>
            ) : (
                <div className="gestion-lista">
                    {usuariosFiltrados.map(usuario => (
                        <div key={usuario.id} className="usuario-card">
                            <div className="usuario-info">
                                <div className="usuario-nombre-row">
                                    <span className="usuario-nombre">{usuario.nombres} {usuario.apellidos}</span>
                                    <span className={`badge-estado ${usuario.estado ? 'activo' : 'inactivo'}`}>
                                        {usuario.estado ? 'Activo' : 'Dado de baja'}
                                    </span>
                                </div>
                                <div className="usuario-meta">
                                    <span className="meta-item"><ShieldCheck size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{usuario.rolNombre || 'Sin rol'}</span>
                                    <span className="meta-item"><CreditCard size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />{usuario.dni}</span>
                                    {usuario.departamentoNumero && (
                                        <span className="meta-item"><Home size={14} style={{ verticalAlign: 'middle', marginRight: 4 }} />
                                            {usuario.departamentoNumero}{usuario.departamentoTorre ? ` - Torre ${usuario.departamentoTorre}` : ''}
                                        </span>
                                    )}
                                </div>
                            </div>
                            <div className="usuario-actions">
                                {usuario.estado ? (
                                    <button className="btn-accion baja" disabled={procesando === usuario.id}
                                        onClick={() => cambiarEstado(usuario, false)}>
                                        <UserX size={16} /> Dar de Baja
                                    </button>
                                ) : (
                                    <>
                                        <button className="btn-accion activar" disabled={procesando === usuario.id}
                                            onClick={() => cambiarEstado(usuario, true)}>
                                            <UserCheck size={16} /> Activar Usuario
                                        </button>
                                        <button className="btn-accion eliminar" disabled={procesando === usuario.id}
                                            onClick={() => eliminarUsuario(usuario)}>
                                            <Trash2 size={16} /> Eliminar Usuario
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default GestionUsuarios;
