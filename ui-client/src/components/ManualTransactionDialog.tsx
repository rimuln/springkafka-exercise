import React, { useState } from 'react';
import { api } from '../services/api';
import type { Transaction } from '../types/transaction';

interface ManualTransactionDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onRefresh: () => void;
  onShowMessage: (message: string, type: 'success' | 'error' | 'info') => void;
}

const ManualTransactionDialog: React.FC<ManualTransactionDialogProps> = ({
  isOpen,
  onClose,
  onRefresh,
  onShowMessage
}) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Partial<Transaction>>({
    variableSymbol: null,
    amount: 1000,
    transactionSentDate: new Date().toISOString().split('T')[0]
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
    } catch (error) {
      console.error('Error creating manual transaction:', error);
      onShowMessage('Chyba při odesílání transakce', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
      backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000
    }}>
      <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', width: '350px', color: 'black' }}>
        <h3 style={{ marginTop: 0 }}>Ruční vložení transakce</h3>
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '10px' }}>
            <label style={{ display: 'block', fontSize: '12px' }}>Datum odeslání</label>
            <input
              type="date"
              style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              value={formData.transactionSentDate || ''}
              onChange={e => setFormData({...formData, transactionSentDate: e.target.value})}
              required
            />
          </div>
          <div style={{ marginBottom: '10px' }}>
            <label style={{ display: 'block', fontSize: '12px' }}>Variabilní symbol</label>
            <input
              type="text"
              style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              value={formData.variableSymbol || ''}
              onChange={e => setFormData({...formData, variableSymbol: e.target.value ? parseInt(e.target.value, 10) : null})}
              required
            />
          </div>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', fontSize: '12px' }}>Částka (Kč)</label>
            <input
              type="number"
              style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              value={formData.amount || ''}
              onChange={e => setFormData({...formData, amount: parseFloat(e.target.value)})}
              required
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <button
              type="button"
              onClick={onClose}
              style={{ padding: '8px 15px', cursor: 'pointer' }}
            >
              Zrušit
            </button>
            <button
              type="submit"
              disabled={loading}
              style={{
                padding: '8px 15px',
                backgroundColor: '#4CAF50',
                color: 'white',
                border: 'none',
                cursor: 'pointer',
                opacity: loading ? 0.7 : 1
              }}
            >
              {loading ? 'Odesílám...' : 'Vložit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ManualTransactionDialog;
