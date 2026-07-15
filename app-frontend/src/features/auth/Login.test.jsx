import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Login from './Login';
import api from '../../api/axiosConfig';

vi.mock('../../api/axiosConfig', () => ({
    default: { post: vi.fn() },
}));

const navigateMock = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
    const actual = await importOriginal();
    return { ...actual, useNavigate: () => navigateMock };
});

function renderLogin() {
    return render(
        <MemoryRouter>
            <Login />
        </MemoryRouter>
    );
}

describe('Login', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('con credenciales válidas, guarda el token y navega al destino según el rol', async () => {
        const user = userEvent.setup();
        api.post.mockResolvedValueOnce({ data: { token: 'jwt-falso', username: 'jperez', role: 'Residente' } });

        renderLogin();
        await user.type(screen.getByLabelText('Usuario'), 'jperez');
        await user.type(screen.getByLabelText('Contraseña'), 'clave123');
        await user.click(screen.getByRole('button', { name: /entrar al sistema/i }));

        await waitFor(() => expect(navigateMock).toHaveBeenCalledWith('/m'));
        expect(localStorage.getItem('token')).toBe('jwt-falso');
    });

    it('con credenciales inválidas (401), muestra el error y rehabilita el botón de envío', async () => {
        const user = userEvent.setup();
        api.post.mockRejectedValueOnce({ response: { status: 401 } });

        renderLogin();
        await user.type(screen.getByLabelText('Usuario'), 'jperez');
        await user.type(screen.getByLabelText('Contraseña'), 'incorrecta');
        const boton = screen.getByRole('button', { name: /entrar al sistema/i });
        await user.click(boton);

        expect(await screen.findByText('Usuario o contraseña no coinciden.')).toBeInTheDocument();
        expect(boton).not.toBeDisabled();
        expect(navigateMock).not.toHaveBeenCalled();
    });
});
