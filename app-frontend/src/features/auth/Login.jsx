import { useState, useEffect } from 'react';
import api from '../../api/axiosConfig';
import { useNavigate } from 'react-router-dom';
import './Login.css';

/**
 * Componente Login: Maneja la autenticación de los conserjes del Condominio Los Robles.
 * Incluye un reloj en tiempo real y protección contra el autocompletado del navegador.
 */
function Login() {
    // Estado para almacenar las credenciales del formulario
    const [credentials, setCredentials] = useState({ username: '', password: '' });

    // Estado para capturar y mostrar mensajes de error de la API
    const [error, setError] = useState('');

    // Estado para indicar que se está reintentando la conexión (cold start del backend)
    const [conectando, setConectando] = useState(false);

    // Estado para el reloj digital de la interfaz
    const [hora, setHora] = useState(new Date().toLocaleTimeString());

    const navigate = useNavigate();

    /**
     * EFECTO: Mantiene el reloj actualizado cada segundo.
     * El 'clearInterval' asegura que el temporizador se detenga si el usuario sale de la página.
     */
    useEffect(() => {
        const timer = setInterval(() => {
            setHora(new Date().toLocaleTimeString());
        }, 1000);
        return () => clearInterval(timer);
    }, []);

    /**
     * Captura los cambios en los campos de texto y los guarda en el estado 'credentials'.
     * @param {Event} e - Evento de cambio del input.
     */
    const handleChange = (e) => {
        setCredentials({ ...credentials, [e.target.name]: e.target.value });
    };

    /**
     * Envía las credenciales al backend de Spring Boot.
     * Si es exitoso, almacena el JWT en el almacenamiento local y redirige al Dashboard.
     * @param {Event} e - Evento de envío del formulario.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(''); // Limpia errores previos antes de intentar

        try {
            await intentarLogin();
        } catch (err) {
            const status = err.response?.status;
            if (status === 401 || status === 403) {
                setError('Usuario o contraseña no coinciden.');
                return;
            }

            // Errores 5xx o de red suelen deberse a que el servidor (backend/BD)
            // recién está "despertando". Reintentamos una vez automáticamente.
            setConectando(true);
            setError('El servidor está iniciando, esto puede tardar unos segundos...');
            try {
                await new Promise((resolve) => setTimeout(resolve, 5000));
                await intentarLogin();
            } catch (err2) {
                const status2 = err2.response?.status;
                setError(status2 === 401 || status2 === 403
                    ? 'Usuario o contraseña no coinciden.'
                    : 'No se pudo conectar con el servidor. Intenta nuevamente en unos momentos.');
            } finally {
                setConectando(false);
            }
        }
    };

    const intentarLogin = async () => {
        const response = await api.post('/auth/login', credentials);
        // Almacenamos el Token JWT para futuras peticiones autenticadas
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('username', response.data.username);
        localStorage.setItem('role', response.data.role);

        // Residentes y conserjes usan la app móvil; el administrador entra
        // directo al panel de escritorio.
        const esMobil = response.data.role === 'Residente' || response.data.role === 'Conserje';
        navigate(esMobil ? '/m' : '/dashboard');
    };

    return (
        <div className="login-container">
            <div className="login-card">
                {/* Logo institucional ubicado en la carpeta public/images */}
                <img src="/images/logo.png" alt="Los Robles Logo" className="login-logo" />

                {/* Visualización de la hora actual para el conserje */}
                <div className="reloj-login">{hora}</div>

                <h2>Acceso Administrativo</h2>
                <p>Ingrese sus credenciales</p>

                {/* Alerta de error: Solo se muestra si el login falla */}
                {error && <div className="error-msg">{error}</div>}

                {/*
                    autoComplete="off": Evita que el navegador sugiera datos antiguos.
                */}
                <form onSubmit={handleSubmit} autoComplete="off">
                    <div className="form-group">
                        <label htmlFor="username">Usuario</label>
                        <input
                            id="username"
                            name="username"
                            type="text"
                            placeholder="Nombre de usuario"
                            onChange={handleChange}
                            // 'new-password' es un truco para forzar al navegador a no autocompletar
                            autoComplete="new-password"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Contraseña</label>
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="••••••••"
                            onChange={handleChange}
                            autoComplete="new-password"
                            required
                        />
                    </div>

                    <button type="submit" className="btn-login" disabled={conectando}>
                        {conectando ? 'Conectando...' : 'Entrar al Sistema'}
                    </button>
                </form>

                <a href="#" className="forgot-password" onClick={(e) => {
                    e.preventDefault();
                    alert("Por favor, solicite el restablecimiento de su clave al Jefe de Seguridad o Administrador del condominio.");
                }}>
                    ¿Problemas con su acceso? Olvidé mi contraseña
                </a>
            </div>
        </div>
    );
}

export default Login;
