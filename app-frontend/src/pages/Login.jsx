import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
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
            const response = await api.post('/auth/login', credentials);
            // Almacenamos el Token JWT para futuras peticiones autenticadas
            localStorage.setItem('token', response.data.token);
            navigate('/dashboard');
        } catch {
            setError('Usuario o contraseña no coinciden.');
        }
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

                    <button type="submit" className="btn-login">
                        Entrar al Sistema
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