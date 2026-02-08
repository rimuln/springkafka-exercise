import { logger } from '../services/logger';
import { useState, useEffect, useCallback } from 'react';
import { api } from '../services/api';
import type { Transaction } from '../types/transaction';
import type { StatusType } from '../types/statusType';

export const useTransactions = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [status, setStatus] = useState<{ message: string; type: StatusType }>({ message: '', type: '' });

  const showMessage = useCallback((message: string, type: StatusType = 'info') => {
    setStatus({ message, type });
    setTimeout(() => setStatus({ message: '', type: '' }), 5000);
  }, []);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.fetchTransactions();
      setTransactions(data);
    } catch (error) {
      logger.error('Fetch transactions failed:', error);
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
    } catch (error: any) {
      logger.error('Sync failed:', error);
      const msg = error.message === 'RATE_LIMIT'
        ? 'Zpomalte! Další synchronizace možná za chvíli.'
        : 'Bankovní API je momentálně nedostupné.';
      showMessage(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (transaction: Transaction) => {
    try {
      await api.updateTransaction(transaction);
      showMessage('Změny byly odeslány ke zpracování.', 'success');
      setTransactions(prev => prev.filter((t: Transaction) => t.id !== transaction.id));
    } catch (error) {
      logger.error('update transaction failed:', error);
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