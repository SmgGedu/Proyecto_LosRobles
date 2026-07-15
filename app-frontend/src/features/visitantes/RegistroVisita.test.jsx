import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RegistroVisita from './RegistroVisita';
import axios from '../../api/axiosConfig';

vi.mock('../../api/axiosConfig', () => ({
    default: { get: vi.fn(), post: vi.fn() },
}));

const departamentos = [{ id: 10, bloqueTorre: 'A', numeroDepa: '101' }];

async function completarCamposObligatorios(user) {
    await user.type(screen.getByPlaceholderText('8 dígitos'), '12345678');
    await user.type(screen.getByPlaceholderText('Nombre del visitante'), 'Maria');
    await user.type(screen.getByPlaceholderText('Apellidos del visitante'), 'Lopez');
    await user.selectOptions(screen.getByText('Seleccione Dpto...').closest('select'), '10');
}

describe('RegistroVisita (INC-02: el botón debe resolver siempre el estado de carga)', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        axios.get.mockResolvedValue({ data: departamentos });
        vi.spyOn(window, 'alert').mockImplementation(() => {});
    });

    it('tras un registro exitoso (200), confirma, resetea el formulario y rehabilita el botón', async () => {
        const user = userEvent.setup();
        axios.post.mockResolvedValueOnce({ data: {} });

        render(<RegistroVisita />);
        await screen.findByText('101 - Torre A');
        await completarCamposObligatorios(user);

        const boton = screen.getByRole('button', { name: /finalizar registro/i });
        await user.click(boton);

        await waitFor(() => expect(window.alert).toHaveBeenCalledWith(expect.stringContaining('exitoso')));

        expect(boton).not.toBeDisabled();
        expect(screen.getByPlaceholderText('8 dígitos')).toHaveValue('');
    });

    it('tras un error del backend, muestra el error y rehabilita el botón (no se queda en "Registrando...")', async () => {
        const user = userEvent.setup();
        axios.post.mockRejectedValueOnce(new Error('fallo de red'));

        render(<RegistroVisita />);
        await screen.findByText('101 - Torre A');
        await completarCamposObligatorios(user);

        const boton = screen.getByRole('button', { name: /finalizar registro/i });
        await user.click(boton);

        await waitFor(() => expect(window.alert).toHaveBeenCalledWith(expect.stringContaining('Error')));

        expect(boton).not.toBeDisabled();
        expect(screen.queryByText(/registrando/i)).not.toBeInTheDocument();
    });
});
