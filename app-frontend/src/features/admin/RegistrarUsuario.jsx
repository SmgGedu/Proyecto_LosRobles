import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    UserPlus, User, Mail, Lock, CreditCard, Phone, ShieldCheck, Home, CheckCircle, AtSign
} from 'lucide-react';
import api from '../../api/axiosConfig';
import './AdminForms.css';

const RegistrarUsuario = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const departamentoPreseleccionado = location.state?.departamentoId ?? '';
    const initialFormState = {
        nombres: '',
        apellidos: '',
        email: '',
        username: '',
        password: '',
        dni: '',
        telefono: '',
        rolId: '',
        departamentoId: departamentoPreseleccionado ? String(departamentoPreseleccionado) : '',
    };

    const [formData, setFormData] = useState(initialFormState);
    const [roles, setRoles] = useState([]);
    const [departamentos, setDepartamentos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        api.get('/roles')
            .then(r => {
                setRoles(r.data);
                if (departamentoPreseleccionado) {
                    const rolResidente = r.data.find(rol => rol.nombreRol?.trim().toLowerCase() === 'residente');
                    if (rolResidente) {
                        setFormData(prev => ({ ...prev, rolId: rolResidente.id }));
                    }
                }
            })
            .catch(e => console.error('Error cargando roles:', e));
        api.get('/departamentos')
            .then(r => setDepartamentos(r.data))
            .catch(e => console.error('Error cargando departamentos:', e));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const rolSeleccionado = roles.find(r => String(r.id) === String(formData.rolId));
    const esResidente = rolSeleccionado?.nombreRol?.trim().toLowerCase() === 'residente';

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const payload = {
                nombres: formData.nombres,
                apellidos: formData.apellidos,
                email: formData.email,
                username: formData.username,
                password: formData.password,
                dni: formData.dni,
                telefono: formData.telefono,
                rol: { id: formData.rolId },
            };
            if (esResidente && formData.departamentoId) {
                payload.departamento = { id: formData.departamentoId };
            }
            await api.post('/usuarios', payload);
            alert('✅ Usuario registrado correctamente.');
            setFormData(initialFormState);
            navigate('/admin/usuarios');
        } catch (err) {
            console.error('Error registrando usuario:', err);
            setError(err.response?.data || 'Error al registrar el usuario.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="admin-form-container">
            <header className="registro-header">
                <div className="header-icon"><UserPlus size={26} /></div>
                <div className="header-text">
                    <h2>Registrar Usuario</h2>
                    <p>Condominio Los Robles | Alta de personal y residentes</p>
                </div>
            </header>

            {location.state?.departamentoLabel && (
                <div className="form-hint">
                    <Home size={16} /> Registrando residente para el departamento <strong>{location.state.departamentoLabel}</strong>.
                </div>
            )}

            {error && <div className="form-error">{error}</div>}

            <form onSubmit={handleSubmit} className="form-grid">
                <div className="field">
                    <label>Nombres</label>
                    <div className="input-box">
                        <User className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="Nombres"
                            required
                            value={formData.nombres}
                            onChange={(e) => setFormData(prev => ({ ...prev, nombres: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Apellidos</label>
                    <div className="input-box">
                        <User className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="Apellidos"
                            required
                            value={formData.apellidos}
                            onChange={(e) => setFormData(prev => ({ ...prev, apellidos: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Correo</label>
                    <div className="input-box">
                        <Mail className="inner-icon" size={18} />
                        <input
                            type="email"
                            placeholder="correo@ejemplo.com"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Nombre de Usuario</label>
                    <div className="input-box">
                        <AtSign className="inner-icon" size={18} />
                        <input
                            type="text"
                            placeholder="usuario.login"
                            required
                            value={formData.username}
                            onChange={(e) => setFormData(prev => ({ ...prev, username: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Contraseña</label>
                    <div className="input-box">
                        <Lock className="inner-icon" size={18} />
                        <input
                            type="password"
                            placeholder="Contraseña"
                            required
                            value={formData.password}
                            onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>DNI</label>
                    <div className="input-box">
                        <CreditCard className="inner-icon" size={18} />
                        <input
                            type="text"
                            maxLength="8"
                            placeholder="8 dígitos"
                            required
                            value={formData.dni}
                            onChange={(e) => setFormData(prev => ({ ...prev, dni: e.target.value.replace(/\D/g, '') }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Teléfono</label>
                    <div className="input-box">
                        <Phone className="inner-icon" size={18} />
                        <input
                            type="text"
                            maxLength="9"
                            placeholder="9XXXXXXXX"
                            value={formData.telefono}
                            onChange={(e) => setFormData(prev => ({ ...prev, telefono: e.target.value.replace(/\D/g, '').slice(0, 9) }))}
                        />
                    </div>
                </div>

                <div className="field">
                    <label>Rol</label>
                    <div className="input-box">
                        <ShieldCheck className="inner-icon" size={18} />
                        <select
                            required
                            value={formData.rolId}
                            onChange={(e) => setFormData(prev => ({ ...prev, rolId: e.target.value, departamentoId: '' }))}>
                            <option value="">Seleccione un rol...</option>
                            {roles.map(rol => (
                                <option key={rol.id} value={rol.id}>{rol.nombreRol}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {esResidente && (
                    <div className="field anim-fade">
                        <label>Departamento</label>
                        <div className="input-box">
                            <Home className="inner-icon" size={18} />
                            <select
                                required
                                value={formData.departamentoId}
                                onChange={(e) => setFormData(prev => ({ ...prev, departamentoId: e.target.value }))}>
                                <option value="">Seleccione Dpto...</option>
                                {departamentos.map(dep => (
                                    <option key={dep.id} value={dep.id}>
                                        {dep.numeroDepa}{dep.bloqueTorre ? ` - Torre ${dep.bloqueTorre}` : ''} ({dep.estado})
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                )}

                <button type="submit" className={`btn-save ${loading ? 'loading' : ''}`} disabled={loading}>
                    {loading
                        ? <><span className="spinner" /> Registrando...</>
                        : <><CheckCircle size={20} /> Registrar Usuario</>}
                </button>
            </form>
        </div>
    );
};

export default RegistrarUsuario;
