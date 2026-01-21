import { useState, useEffect, useCallback } from 'react';
import { api } from '../services/api';

export const useTransactions = () => {  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState({ message: '', type: '' });

  const showMessage = useCallback((message, type = 'info') => {
    setStatus({ message, type });
    setTimeout(() => setStatus({ message: '', type: '' }), 5000);
  }, []);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.fetchTransactions();
      setTransactions(data);
    } catch (error) {
      showMessage('Nepodařilo se načíst transakce.', 'error');
    } finally {
      setLoading(false);
    }
  }, [showMessage]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const handleSync = async () => {
    setLoading(true);
    showMessage('Synchronizuji s bankou...', 'info');
    try {
      await api.syncWithBank();
      showMessage('Data z banky byla úspěšně stažena.', 'success');
      await refresh();
    } catch (error) {
      const msg = error.message === 'RATE_LIMIT'
        ? 'Zpomalte! Další synchronizace možná za chvíli.'
        : 'Bankovní API je momentálně nedostupné.';
      showMessage(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (transaction) => {
    try {
      await api.updateTransaction(transaction);
      showMessage('Změny byly odeslány ke zpracování.', 'success');
      setTransactions(prev => prev.filter(t => t.id !== transaction.id));
    } catch (error) {
      showMessage('Chyba při ukládání transakce.', 'error');
    }
  };

  return {
    transactions,
    setTransactions,
    loading,
    status,
    showMessage,
    handleSync,
    handleUpdate,
    refresh
  };
};