import React, { useState } from 'react';

const ManualTransactionDialog = ({ isOpen, onClose, onRefresh, onShowMessage }) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    variableSymbol: '',
    amount: 1000,
    transactionSentDate: new Date().toISOString().split('T')[0]
  });

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await fetch('/api/transactions/manual', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        onShowMessage('Transakce vložena', 'success')
        onRefresh();
        onClose();
      } else {
        onShowMessage('Chyba při odesílání','error');
      }
    } catch (error) {
      console.error('Error:', error);
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
            <input type="date" style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              value={formData.transactionSentDate}
              onChange={e => setFormData({...formData, transactionSentDate: e.target.value})} required />
          </div>
          <div style={{ marginBottom: '10px' }}>
            <label style={{ display: 'block', fontSize: '12px' }}>Variabilní symbol</label>
            <input type="text" style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              value={formData.variableSymbol}
              onChange={e => setFormData({...formData, variableSymbol: e.target.value})} required />
          </div>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', fontSize: '12px' }}>Částka (Kč)</label>
            <input type="number" style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}
              value={formData.amount}
              onChange={e => setFormData({...formData, amount: e.target.value})} required />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <button type="button" onClick={onClose} style={{ padding: '8px 15px', cursor: 'pointer' }}>Zrušit</button>
            <button type="submit" disabled={loading} style={{ padding: '8px 15px', backgroundColor: '#4CAF50', color: 'white', border: 'none', cursor: 'pointer' }}>
              {loading ? 'Odesílám...' : 'Vložit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ManualTransactionDialog;