import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { useState } from 'react';
import ManualTransactionDialog from '../src/components/ManualTransactionDialog';

const jsonResponse = (status: number, body?: unknown): Response =>
  new Response(body === undefined ? null : JSON.stringify(body), {
    status,
    headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
  });

const Harness = ({ onClose }: { onClose: () => void }) => {
  const [open, setOpen] = useState(true);
  return (
    <>
      <button onClick={() => setOpen(true)}>open</button>
      <ManualTransactionDialog
        isOpen={open}
        onClose={() => {
          setOpen(false);
          onClose();
        }}
        onRefresh={() => {}}
        onShowMessage={() => {}}
      />
    </>
  );
};

const vsInput = () => screen.getByLabelText('Variabilní symbol') as HTMLInputElement;

describe('ManualTransactionDialog', () => {
  const fetchMock = vi.fn();

  beforeEach(() => {
    vi.stubGlobal('fetch', fetchMock);
  });

  afterEach(() => {
    cleanup();
    fetchMock.mockReset();
    vi.unstubAllGlobals();
  });

  it('shows the server message and stays open on 409 duplicate', async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(409, { message: 'Transakce s VS 36149 už byla vložena' }),
    );
    const onClose = vi.fn();
    render(<Harness onClose={onClose} />);

    fireEvent.change(vsInput(), { target: { value: '36149' } });
    fireEvent.click(screen.getByRole('button', { name: 'Vložit' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Transakce s VS 36149 už byla vložena',
    );
    expect(onClose).not.toHaveBeenCalled();
    expect(vsInput().value).toBe('36149');

    fireEvent.change(vsInput(), { target: { value: '36157' } });
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('clears the VS after a successful submit', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200));
    const onClose = vi.fn();
    render(<Harness onClose={onClose} />);

    fireEvent.change(vsInput(), { target: { value: '36149' } });
    fireEvent.click(screen.getByRole('button', { name: 'Vložit' }));
    await waitFor(() => expect(onClose).toHaveBeenCalled());

    fireEvent.click(screen.getByRole('button', { name: 'open' }));
    expect(vsInput().value).toBe('');
  });

  it('clears the VS after cancel', () => {
    const onClose = vi.fn();
    render(<Harness onClose={onClose} />);

    fireEvent.change(vsInput(), { target: { value: '36149' } });
    fireEvent.click(screen.getByRole('button', { name: 'Zrušit' }));
    fireEvent.click(screen.getByRole('button', { name: 'open' }));

    expect(vsInput().value).toBe('');
  });
});
