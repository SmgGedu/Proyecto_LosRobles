import { useState, useEffect, useRef } from 'react';
import QrScanner from 'qr-scanner';
import axios from '../../api/axiosConfig';
import { ScanLine, User, CreditCard, Home, CheckCircle, XCircle, RotateCcw, Car, MessageSquare, QrCode } from 'lucide-react';
import './EscanearQR.css';

const EscanearQR = () => {
    const videoRef = useRef(null);
    const scannerRef = useRef(null);

    const [estado, setEstado] = useState('scanning'); // scanning | preview | success | error
    const [invitacion, setInvitacion] = useState(null);
    const [hashManual, setHashManual] = useState('');
    const [mensajeError, setMensajeError] = useState('');
    const [placaVehiculo, setPlacaVehiculo] = useState('');
    const [observaciones, setObservaciones] = useState('');
    const [confirmando, setConfirmando] = useState(false);
    const [camaraDisponible, setCamaraDisponible] = useState(true);

    const buscarInvitacion = async (hash) => {
        if (scannerRef.current) scannerRef.current.stop();
        setMensajeError('');
        try {
            const res = await axios.get(`/invitaciones/buscar/${hash}`);
            setInvitacion(res.data);
            setEstado('preview');
        } catch (err) {
            setMensajeError(err.response?.data?.message || 'QR inválido o no encontrado.');
            setEstado('error');
        }
    };

    useEffect(() => {
        if (estado !== 'scanning' || !videoRef.current) return undefined;

        const scanner = new QrScanner(
            videoRef.current,
            (result) => buscarInvitacion(result.data),
            { highlightScanRegion: true, highlightCodeOutline: true }
        );
        scannerRef.current = scanner;
        scanner.start().catch(() => setCamaraDisponible(false));

        return () => {
            scanner.stop();
            scanner.destroy();
            scannerRef.current = null;
        };
    }, [estado]);

    const handleBuscarManual = (e) => {
        e.preventDefault();
        if (hashManual.trim()) buscarInvitacion(hashManual.trim());
    };

    const confirmarIngreso = async () => {
        setConfirmando(true);
        try {
            await axios.post('/accesos/ingreso-qr', {
                qrHash: invitacion.codigoQrHash,
                placaVehiculo: placaVehiculo || null,
                observaciones: observaciones || null,
                objetos: [],
            });
            setEstado('success');
        } catch (err) {
            setMensajeError(err.response?.data?.message || 'No se pudo registrar el ingreso.');
            setEstado('error');
        } finally {
            setConfirmando(false);
        }
    };

    const reiniciar = () => {
        setInvitacion(null);
        setHashManual('');
        setMensajeError('');
        setPlacaVehiculo('');
        setObservaciones('');
        setEstado('scanning');
    };

    return (
        <div className="escanear-container">
            <header className="escanear-header">
                <div className="escanear-header-icon"><ScanLine size={22} /></div>
                <div>
                    <h2>Escanear QR</h2>
                    <p>Valida el ingreso de visitantes</p>
                </div>
            </header>

            {estado === 'scanning' && (
                <>
                    {camaraDisponible ? (
                        <div className="escanear-video-wrap">
                            <video ref={videoRef} className="escanear-video" />
                        </div>
                    ) : (
                        <div className="escanear-sin-camara">
                            No se pudo acceder a la cámara. Usa la búsqueda manual.
                        </div>
                    )}

                    <form className="escanear-manual" onSubmit={handleBuscarManual}>
                        <label>Código manual (si no puedes escanear)</label>
                        <div className="escanear-manual-row">
                            <div className="input-box">
                                <QrCode className="inner-icon" size={18} />
                                <input
                                    type="text"
                                    placeholder="Pega o escribe el código QR"
                                    value={hashManual}
                                    onChange={(e) => setHashManual(e.target.value)}
                                />
                            </div>
                            <button type="submit">Buscar</button>
                        </div>
                    </form>
                </>
            )}

            {estado === 'preview' && invitacion && (
                <div className="escanear-preview">
                    <div className="escanear-preview-fila">
                        <User size={16} /><span>{invitacion.nombreVisitante}</span>
                    </div>
                    <div className="escanear-preview-fila">
                        <CreditCard size={16} /><span>DNI: {invitacion.dniVisitante}</span>
                    </div>
                    <div className="escanear-preview-fila">
                        <Home size={16} /><span>{invitacion.nombreDepartamento}</span>
                    </div>
                    <div className="escanear-preview-fila">
                        <span className="escanear-anfitrion">Anfitrión: {invitacion.nombreAnfitrion}</span>
                    </div>

                    <div className="field">
                        <label>Placa de vehículo (opcional)</label>
                        <div className="input-box">
                            <Car className="inner-icon" size={18} />
                            <input
                                type="text"
                                value={placaVehiculo}
                                onChange={(e) => setPlacaVehiculo(e.target.value.toUpperCase())}
                            />
                        </div>
                    </div>

                    <div className="field">
                        <label>Observaciones (opcional)</label>
                        <div className="input-box">
                            <MessageSquare className="inner-icon" size={18} />
                            <input
                                type="text"
                                value={observaciones}
                                onChange={(e) => setObservaciones(e.target.value)}
                            />
                        </div>
                    </div>

                    <div className="escanear-acciones">
                        <button className="btn-secundario" onClick={reiniciar}>Cancelar</button>
                        <button className="btn-primario" onClick={confirmarIngreso} disabled={confirmando}>
                            {confirmando ? 'Confirmando...' : <><CheckCircle size={18} /> Confirmar ingreso</>}
                        </button>
                    </div>
                </div>
            )}

            {estado === 'success' && (
                <div className="escanear-resultado escanear-resultado--exito">
                    <CheckCircle size={40} />
                    <p>Ingreso registrado correctamente.</p>
                    <button className="btn-primario" onClick={reiniciar}>
                        <RotateCcw size={18} /> Escanear otro
                    </button>
                </div>
            )}

            {estado === 'error' && (
                <div className="escanear-resultado escanear-resultado--error">
                    <XCircle size={40} />
                    <p>{mensajeError}</p>
                    <button className="btn-primario" onClick={reiniciar}>
                        <RotateCcw size={18} /> Escanear de nuevo
                    </button>
                </div>
            )}
        </div>
    );
};

export default EscanearQR;
