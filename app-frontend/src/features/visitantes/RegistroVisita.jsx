import { useState, useEffect } from 'react';
import './RegistroVisita.css';
import axios from '../../api/axiosConfig';
import {
    UserPlus, CreditCard, Home, Package, Car, Plus, Trash2,
    MessageSquare, CheckCircle, Search, Truck, Star, Navigation
} from 'lucide-react';

const RegistroVisita = () => {
    const initialFormState = {
        nombres: '',
        apellidos: '',
        dni_visitante: '',
        id_departamento_destino: '',
        id_residente_que_autoriza: '',
        tipo_visita: 'NORMAL',
        empresa_delivery: '',
        tipo_ingreso: 'Peatonal',
        placa_vehiculo: '',
        observaciones: '',
        objetos: []
    };

    const [formData, setFormData] = useState(initialFormState);
    const [departamentos, setDepartamentos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [busqueda, setBusqueda] = useState('');
    const [sugerencias, setSugerencias] = useState([]);
    const [mostrarSugerencias, setMostrarSugerencias] = useState(false);

    useEffect(() => {
        axios.get('/departamentos')
            .then(r => setDepartamentos(r.data))
            .catch(e => console.error('Error cargando departamentos:', e));
    }, []);

    // Cuando el tipo cambia a FRECUENTE y ya hay 8 dígitos en el DNI, auto-rellena
    useEffect(() => {
        if (formData.tipo_visita === 'FRECUENTE' && formData.dni_visitante.length === 8) {
            fetchVisitantePorDni(formData.dni_visitante);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [formData.tipo_visita]);

    const fetchVisitantePorDni = async (dni) => {
        try {
            const res = await axios.get(`/visitantes/por-dni/${dni}`);
            if (res.data) {
                setFormData(prev => ({
                    ...prev,
                    nombres: res.data.nombre || prev.nombres,
                    apellidos: res.data.apellidos || prev.apellidos,
                }));
            }
        } catch {
            // visitante nuevo, sin datos previos
        }
    };

    const handleDniChange = (e) => {
        const dni = e.target.value.replace(/\D/g, '');
        setFormData(prev => ({ ...prev, dni_visitante: dni }));
        if (formData.tipo_visita === 'FRECUENTE' && dni.length === 8) {
            fetchVisitantePorDni(dni);
        }
    };

    const manejarBusqueda = async (e) => {
        const valor = e.target.value;
        setBusqueda(valor);
        if (valor === '') {
            setFormData(prev => ({ ...prev, id_departamento_destino: '', id_residente_que_autoriza: '' }));
        }
        if (valor.length > 2) {
            try {
                const res = await axios.get(`/usuarios/buscar-residente?nombre=${valor}`);
                setSugerencias(res.data);
                setMostrarSugerencias(true);
            } catch (e) {
                console.error('Error buscando residente:', e);
            }
        } else {
            setSugerencias([]);
            setMostrarSugerencias(false);
        }
    };

    const seleccionarResidente = (res) => {
        setBusqueda(res.nombreCompleto);
        setFormData(prev => ({
            ...prev,
            id_departamento_destino: res.idDepartamento || '',
            id_residente_que_autoriza: res.idUsuario,
        }));
        setMostrarSugerencias(false);
    };

    const agregarObjeto = () =>
        setFormData(prev => ({ ...prev, objetos: [...prev.objetos, { descripcion: '', marca: '', serie: '' }] }));

    const eliminarObjeto = (i) =>
        setFormData(prev => ({ ...prev, objetos: prev.objetos.filter((_, idx) => idx !== i) }));

    const handleObjetoChange = (i, field, value) => {
        const nuevos = [...formData.objetos];
        nuevos[i][field] = value;
        setFormData(prev => ({ ...prev, objetos: nuevos }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await axios.post('/accesos/registro-manual', formData);
            alert('✅ ¡Registro exitoso en Condominio Los Robles!');
            setFormData(initialFormState);
            setBusqueda('');
        } catch (error) {
            console.error('Error en el registro:', error);
            alert('❌ Error al registrar. Revisa la conexión.');
        } finally {
            setLoading(false);
        }
    };

    const esFrecuente = formData.tipo_visita === 'FRECUENTE';
    const esDelivery = formData.tipo_visita === 'DELIVERY';
    const esVehicular = formData.tipo_ingreso === 'Vehicular';

    return (
        <div className="registro-container">
            <header className="registro-header">
                <div className="header-icon"><UserPlus size={26} /></div>
                <div className="header-text">
                    <h2>Nuevo Registro de Ingreso</h2>
                    <p>Sede: Ica, Perú | Sistema Los Robles</p>
                </div>
            </header>

            <form onSubmit={handleSubmit} className="form-grid">

                {/* ── Fila 1: Tipo de visita + DNI ── */}
                <div className="field">
                    <label>Tipo de Visita</label>
                    <div className="input-box">
                        <Star className="inner-icon" size={18} />
                        <select value={formData.tipo_visita}
                            onChange={(e) => setFormData(prev => ({ ...prev, tipo_visita: e.target.value, empresa_delivery: '' }))}>
                            <option value="NORMAL">Normal</option>
                            <option value="FRECUENTE">Frecuente</option>
                            <option value="DELIVERY">Delivery</option>
                        </select>
                    </div>
                </div>

                <div className="field">
                    <label>DNI Visitante</label>
                    <div className="input-box">
                        <CreditCard className="inner-icon" size={18} />
                        <input
                            type="text"
                            maxLength="8"
                            placeholder="8 dígitos"
                            required
                            value={formData.dni_visitante}
                            onChange={handleDniChange}
                        />
                    </div>
                </div>

                {/* ── Banner informativo según tipo de visita ── */}
                {esFrecuente && (
                    <div className="info-banner frecuente full">
                        <Star size={15} />
                        Visitante frecuente — si ya registró visitas anteriores, sus datos se rellenan automáticamente al ingresar el DNI.
                    </div>
                )}

                {esDelivery && (
                    <div className="field full anim-fade">
                        <label>Empresa de Delivery</label>
                        <div className="input-box">
                            <Truck className="inner-icon" size={18} />
                            <input
                                type="text"
                                placeholder="Rappi, Glovo, PedidosYa..."
                                required
                                value={formData.empresa_delivery}
                                onChange={(e) => setFormData(prev => ({ ...prev, empresa_delivery: e.target.value }))}
                            />
                        </div>
                    </div>
                )}

                {/* ── Fila 2: Nombres + Apellidos ── */}
                <div className="field">
                    <label>Nombres</label>
                    <div className="input-box">
                        <UserPlus className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="Nombre del visitante"
                            required
                            value={formData.nombres}
                            onChange={(e) => setFormData(prev => ({ ...prev, nombres: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Apellidos</label>
                    <div className="input-box">
                        <UserPlus className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="Apellidos del visitante"
                            required
                            value={formData.apellidos}
                            onChange={(e) => setFormData(prev => ({ ...prev, apellidos: e.target.value }))}
                        />
                    </div>
                </div>

                {/* ── Fila 3: Anfitrión + Departamento ── */}
                <div className="field" style={{ position: 'relative' }}>
                    <label>Buscar Anfitrión</label>
                    <div className="input-box">
                        <Search className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="Escriba nombre o apellido..."
                            value={busqueda}
                            onChange={manejarBusqueda}
                            autoComplete="off"
                        />
                    </div>
                    {mostrarSugerencias && sugerencias.length > 0 && (
                        <ul className="sugerencias-list">
                            {sugerencias.map((res) => (
                                <li key={res.idUsuario} className="sugerencia-item"
                                    onMouseEnter={e => e.currentTarget.classList.add('hover')}
                                    onMouseLeave={e => e.currentTarget.classList.remove('hover')}
                                    onClick={() => seleccionarResidente(res)}>
                                    <span className="sugerencia-nombre">{res.nombreCompleto}</span>
                                    <span className="sugerencia-depto">{res.infoDepartamento}</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <div className="field">
                    <label>Departamento Destino</label>
                    <div className="input-box">
                        <Home className="inner-icon" size={18} />
                        <select
                            required
                            value={formData.id_departamento_destino}
                            onChange={(e) => setFormData(prev => ({ ...prev, id_departamento_destino: e.target.value }))}>
                            <option value="">Seleccione Dpto...</option>
                            {departamentos.map(dep => (
                                <option key={dep.id} value={dep.id}>
                                    {dep.numeroDepa}{dep.bloqueTorre ? ` - Torre ${dep.bloqueTorre}` : ''}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* ── Fila 4: Tipo de ingreso + Placa ── */}
                <div className="field">
                    <label>Tipo de Ingreso</label>
                    <div className="input-box">
                        <Navigation className="inner-icon" size={18} />
                        <select value={formData.tipo_ingreso}
                            onChange={(e) => setFormData(prev => ({ ...prev, tipo_ingreso: e.target.value, placa_vehiculo: '' }))}>
                            <option value="Peatonal">Peatonal</option>
                            <option value="Vehicular">Vehicular</option>
                        </select>
                    </div>
                </div>

                <div className={`field ${!esVehicular ? 'field--hidden' : 'anim-fade'}`}>
                    <label>Placa del Vehículo</label>
                    <div className="input-box">
                        <Car className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="ABC-123"
                            required={esVehicular}
                            value={formData.placa_vehiculo}
                            onChange={(e) => setFormData(prev => ({ ...prev, placa_vehiculo: e.target.value.toUpperCase() }))}
                        />
                    </div>
                </div>

                {/* ── Trazabilidad de Objetos ── */}
                <div className="section-title full">
                    <span>Trazabilidad de Objetos</span>
                    <button type="button" className="btn-add" onClick={agregarObjeto}>
                        <Plus size={16} /> Agregar Objeto
                    </button>
                </div>

                <div className="objetos-list full">
                    {formData.objetos.length === 0 ? (
                        <div className="empty-objects">
                            <Package size={20} />
                            <span>No hay objetos declarados</span>
                        </div>
                    ) : (
                        formData.objetos.map((obj, index) => (
                            <div key={index} className="objeto-card">
                                <div className="objeto-card-header">
                                    <div className="objeto-card-title">
                                        <Package size={14} />
                                        <span>Objeto #{index + 1}</span>
                                    </div>
                                    <button type="button" className="btn-remove-mini" onClick={() => eliminarObjeto(index)}>
                                        <Trash2 size={14} />
                                    </button>
                                </div>
                                <div className="objeto-card-body">
                                    <input placeholder="Descripción" required value={obj.descripcion}
                                        onChange={(e) => handleObjetoChange(index, 'descripcion', e.target.value)} />
                                    <input placeholder="Marca" value={obj.marca}
                                        onChange={(e) => handleObjetoChange(index, 'marca', e.target.value)} />
                                    <input placeholder="N° Serie" value={obj.serie}
                                        onChange={(e) => handleObjetoChange(index, 'serie', e.target.value)} />
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* ── Observaciones ── */}
                <div className="field full">
                    <label>Observaciones / Motivo</label>
                    <div className="input-box textarea-box">
                        <MessageSquare className="inner-icon inner-icon--top" size={18} />
                        <textarea
                            placeholder="Especifique el motivo de visita..."
                            value={formData.observaciones}
                            onChange={(e) => setFormData(prev => ({ ...prev, observaciones: e.target.value }))}
                        />
                    </div>
                </div>

                {/* ── Botón submit ── */}
                <button type="submit" className={`btn-save ${loading ? 'loading' : ''}`} disabled={loading}>
                    {loading
                        ? <><span className="spinner" /> Registrando...</>
                        : <><CheckCircle size={20} /> Finalizar Registro</>}
                </button>

            </form>
        </div>
    );
};

export default RegistroVisita;
