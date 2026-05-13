import { useState, useEffect } from 'react';
import './RegistroVisita.css';
import axios from '../api/axiosConfig';
import { 
    UserPlus, CreditCard, Home, Package, Car, Plus, Trash2, MessageSquare, CheckCircle, Search 
} from 'lucide-react';

const RegistroVisita = () => {
    const initialFormState = {
        nombres: '',
        apellidos: '',
        dni_visitante: '',
        id_departamento_destino: '',
        id_residente_que_autoriza: '', // Nuevo campo para guardar al amigo exacto
        tipo_ingreso: 'Peatonal',
        placa_vehiculo: '',
        observaciones: '',
        objetos: []
    };

    const [formData, setFormData] = useState(initialFormState);
    const [departamentos, setDepartamentos] = useState([]); 
    const [loading, setLoading] = useState(false);
    
    // Estados para el buscador inteligente
    const [busqueda, setBusqueda] = useState('');
    const [sugerencias, setSugerencias] = useState([]);
    const [mostrarSugerencias, setMostrarSugerencias] = useState(false);

    // Cargar departamentos al montar el componente
    useEffect(() => {
        const fetchDepartamentos = async () => {
            try {
                const response = await axios.get('/departamentos');
                setDepartamentos(response.data);
            } catch (error) {
                console.error("Error cargando departamentos:", error);
            }
        };
        fetchDepartamentos();
    }, []);

    // Función que se dispara al escribir en el buscador
    const manejarBusqueda = async (e) => {
        const valor = e.target.value;
        setBusqueda(valor);

        // Si borra el nombre, reseteamos el departamento elegido
        if (valor === '') {
            setFormData({ ...formData, id_departamento_destino: '', id_residente_que_autoriza: '' });
        }

        if (valor.length > 2) { 
            try {
                // Ajusta la ruta si tu controlador de usuarios tiene otro prefijo
                const response = await axios.get(`/usuarios/buscar-residente?nombre=${valor}`);
                setSugerencias(response.data);
                setMostrarSugerencias(true);
            } catch (error) {
                console.error("Error buscando residente:", error);
            }
        } else {
            setSugerencias([]);
            setMostrarSugerencias(false);
        }
    };

    const agregarObjeto = () => {
        setFormData({
            ...formData,
            objetos: [...formData.objetos, { descripcion: '', marca: '', serie: '' }]
        });
    };

    const eliminarObjeto = (index) => {
        setFormData({
            ...formData,
            objetos: formData.objetos.filter((_, i) => i !== index)
        });
    };

    const handleObjetoChange = (index, field, value) => {
        const nuevosObjetos = [...formData.objetos];
        nuevosObjetos[index][field] = value;
        setFormData({ ...formData, objetos: nuevosObjetos });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await axios.post('/accesos/registro-manual', formData); // Ajusta la ruta si es necesario
            alert('✅ ¡Registro exitoso en Condominio Los Robles!');
            setFormData(initialFormState);
            setBusqueda(''); // Limpiamos el buscador
        } catch (error) {
            console.error("Error en el registro:", error);
            alert('❌ Error al registrar. Revisa la conexión.');
        } finally {
            setLoading(false);
        }
    };

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
                <div className="field">
                    <label>Nombres</label>
                    <div className="input-box">
                        <UserPlus className="inner-icon" size={18} />
                        <input type="text" placeholder="Nombre Del Visitante" required value={formData.nombres}
                            onChange={(e) => setFormData({...formData, nombres: e.target.value})} />
                    </div>
                </div>

                <div className="field">
                    <label>Apellidos</label>
                    <div className="input-box">
                        <UserPlus className="inner-icon" size={18} />
                        <input type="text" placeholder="Apellidos Del Visitante" required value={formData.apellidos}
                            onChange={(e) => setFormData({...formData, apellidos: e.target.value})} />
                    </div>
                </div>

                <div className="field">
                    <label>DNI Visitante</label>
                    <div className="input-box">
                        <CreditCard className="inner-icon" size={18} />
                        <input type="text" maxLength="8" placeholder="8 dígitos" required value={formData.dni_visitante}
                            onChange={(e) => setFormData({...formData, dni_visitante: e.target.value.replace(/\D/g,'')})} />
                    </div>
                </div>

                {/* --- NUEVO: BUSCADOR INTELIGENTE --- */}
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

                    {/* Menú flotante de sugerencias */}
                    {mostrarSugerencias && sugerencias.length > 0 && (
                        <ul style={{
                            position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 50,
                            backgroundColor: 'white', border: '1px solid #e2e8f0', borderRadius: '0 0 8px 8px',
                            boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)', maxHeight: '200px', overflowY: 'auto',
                            margin: 0, padding: 0, listStyle: 'none'
                        }}>
                            {sugerencias.map((res) => (
                                <li
                                    key={res.idUsuario}
                                    style={{ padding: '12px', borderBottom: '1px solid #f1f5f9', cursor: 'pointer', transition: 'background 0.2s' }}
                                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f0fdf4'}
                                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    onClick={() => {
                                        setBusqueda(res.nombreCompleto);
                                        setFormData({
                                            ...formData,
                                            id_departamento_destino: res.idDepartamento || '', // Selecciona el depa
                                            id_residente_que_autoriza: res.idUsuario // Guarda al responsable
                                        });
                                        setMostrarSugerencias(false);
                                    }}
                                >
                                    <div style={{ fontWeight: 'bold', fontSize: '14px', color: '#334155' }}>
                                        {res.nombreCompleto}
                                    </div>
                                    <div style={{ fontSize: '12px', color: '#64748b' }}>
                                        {res.infoDepartamento}
                                    </div>
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
                            onChange={(e) => setFormData({...formData, id_departamento_destino: e.target.value})}
                        >
                            <option value="">Seleccione Dpto...</option>
                            {departamentos.map(dep => (
                                <option key={dep.id} value={dep.id}>
                                    {dep.numeroDepa} {dep.bloqueTorre ? `- Torre ${dep.bloqueTorre}` : ''}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="field">
                    <label>Tipo de Ingreso</label>
                    <div className="input-box">
                        <select className="custom-select-type" value={formData.tipo_ingreso}
                            onChange={(e) => setFormData({...formData, tipo_ingreso: e.target.value})}>
                            <option value="Peatonal">🚶 Peatonal</option>
                            <option value="Vehicular">🚗 Vehicular</option>
                        </select>
                    </div>
                </div>

                {formData.tipo_ingreso === 'Vehicular' && (
                    <div className="field animate-fade-in">
                        <label>Placa del Vehículo</label>
                        <div className="input-box">
                            <Car className="inner-icon" size={18} />
                            <input type="text" placeholder="ABC-123" required value={formData.placa_vehiculo}
                                onChange={(e) => setFormData({...formData, placa_vehiculo: e.target.value.toUpperCase()})} />
                        </div>
                    </div>
                )}

                <div className="section-title full">
                    <span>Trazabilidad de Objetos</span>
                    <button type="button" className="btn-add" onClick={agregarObjeto}>
                        <Plus size={16} /> Agregar Objeto
                    </button>
                </div>

                <div className="objetos-list full">
                    {formData.objetos.length === 0 ? (
                        <div className="empty-objects">No hay objetos declarados.</div>
                    ) : (
                        formData.objetos.map((obj, index) => (
                            <div key={index} className="objeto-card animate-slide-down">
                                <div className="objeto-card-header">
                                    <Package size={16} />
                                    <span>Objeto #{index + 1}</span>
                                    <button type="button" className="btn-remove-mini" onClick={() => eliminarObjeto(index)}>
                                        <Trash2 size={14} />
                                    </button>
                                </div>
                                <div className="objeto-card-body">
                                    <input placeholder="Descripción" required value={obj.descripcion}
                                        onChange={(e) => handleObjetoChange(index, 'descripcion', e.target.value)} />
                                    <input placeholder="Marca" value={obj.marca}
                                        onChange={(e) => handleObjetoChange(index, 'marca', e.target.value)} />
                                    <input placeholder="Serie" value={obj.serie}
                                        onChange={(e) => handleObjetoChange(index, 'serie', e.target.value)} />
                                </div>
                            </div>
                        ))
                    )}
                </div>

                <div className="field full">
                    <label>Observaciones / Motivo</label>
                    <div className="input-box area">
                        <MessageSquare className="inner-icon top" size={18} />
                        <textarea placeholder="Especifique el motivo de visita..." value={formData.observaciones}
                            onChange={(e) => setFormData({...formData, observaciones: e.target.value})} />
                    </div>
                </div>

                <button type="submit" className={`btn-save ${loading ? 'loading' : ''}`} disabled={loading}>
                    {loading ? "Sincronizando..." : <><CheckCircle size={20} /> Finalizar Registro</>}
                </button>
            </form>
        </div>
    );
};

export default RegistroVisita;