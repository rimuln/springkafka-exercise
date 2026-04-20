import React, { useState } from 'react';
import { logger } from '../services/logger';
import { api } from '../services/api';
import type { Transaction } from '../types/transaction';

interface ManualTransactionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onRefresh: () => void;
  onShowMessage: (message: string, type: 'success' | 'error' | 'info') => void;
}

const today = (): string => new Date().toISOString().slice(0, 10);

const parseVs = (raw: string): number | null => {
  if (raw.trim() === '') return null;
  const n = parseInt(raw, 10);
  return Number.isFinite(n) ? n : null;
};

const ManualTransactionDialog: React.FC<ManualTransactionDialogProps> = ({
  isOpen,
  onClose,
  onRefresh,
  onShowMessage,
}) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<Transaction>>({
    variableSymbol: null,
    amount: 1000,
    transactionSentDate: today(),
  });

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.createManualTransaction(formData);
      onShowMessage('Transakce byla úspěšně vložena', 'success');
      onRefresh();
      onClose();
    } catch (error: unknown) {
      logger.error('Error creating manual transaction:', error);
      onShowMessage('Chyba při odesílání transakce', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="manual-tx-title"
    >
      <div className="modal-card">
        <h3 id="manual-tx-title" className="modal-title">
          Ruční vložení transakce
        </h3>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <label className="form-label" htmlFor="manual-tx-date">
              Datum odeslání
            </label>
            <input
              id="manual-tx-date"
              type="date"
              className="form-input"
              value={formData.transactionSentDate || ''}
              onChange={(e) => setFormData({ ...formData, transactionSentDate: e.target.value })}
              required
            />
          </div>
          <div className="form-row">
            <label className="form-label" htmlFor="manual-tx-vs">
              Variabilní symbol
            </label>
            <input
              id="manual-tx-vs"
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              className="form-input"
              value={formData.variableSymbol ?? ''}
              onChange={(e) =>
                setFormData({ ...formData, variableSymbol: parseVs(e.target.value) })
              }
              required
            />
          </div>
          <div className="form-row form-row-last">
            <label className="form-label" htmlFor="manual-tx-amount">
              Částka (Kč)
            </label>
            <input
              id="manual-tx-amount"
              type="number"
              min="0"
              step="0.01"
              className="form-input"
              value={formData.amount ?? ''}
              onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) })}
              required
            />
          </div>
          <div className="modal-actions">
            <button type="button" className="btn-base btn-cancel" onClick={onClose}>
              Zrušit
            </button>
            <button type="submit" className="btn-base btn-sync" disabled={loading}>
              {loading ? 'Odesílám...' : 'Vložit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ManualTransactionDialog;
