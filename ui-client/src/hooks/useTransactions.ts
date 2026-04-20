import { useEffect, useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { logger } from '../services/logger';
import { api } from '../services/api';
import type { Transaction } from '../types/transaction';
import type { StatusType } from '../types/statusType';

const TRANSACTIONS_KEY = ['transactions'] as const;

export const useTransactions = () => {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<{ message: string; type: StatusType }>({ message: '', type: '' });
  const dismissTimer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const showMessage = (message: string, type: StatusType = 'info') => {
    if (dismissTimer.current) clearTimeout(dismissTimer.current);
    setStatus({ message, type });
    dismissTimer.current = setTimeout(() => setStatus({ message: '', type: '' }), 5000);
  };

  useEffect(() => () => {
    if (dismissTimer.current) clearTimeout(dismissTimer.current);
  }, []);

  const transactionsQuery = useQuery({
    queryKey: TRANSACTIONS_KEY,
    queryFn: async () => {
      try {
        return await api.fetchTransactions();
      } catch (error: unknown) {
        logger.error('Fetch transactions failed:', error);
        showMessage('Nepodařilo se načíst transakce.', 'error');
        throw error;
      }
    },
  });

  const syncMutation = useMutation({
    mutationFn: () => api.syncWithBank(),
    onMutate: () => showMessage('Synchronizuji s bankou...', 'info'),
    onSuccess: () => {
      showMessage('Data z banky byla úspěšně stažena.', 'success');
      return queryClient.invalidateQueries({ queryKey: TRANSACTIONS_KEY });
    },
    onError: (error: unknown) => {
      logger.error('Sync failed:', error);
      const msg = error instanceof Error && error.message === 'RATE_LIMIT'
        ? 'Zpomalte! Další synchronizace možná za chvíli.'
        : 'Bankovní API je momentálně nedostupné.';
      showMessage(msg, 'error');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (transaction: Transaction) => api.updateTransaction(transaction),
    onSuccess: (_data, variables) => {
      showMessage('Změny byly odeslány ke zpracování.', 'success');
      // Optimistic remove from list — backend will re-sync on next fetch.
      queryClient.setQueryData<Transaction[]>(TRANSACTIONS_KEY, (prev) =>
        prev ? prev.filter(t => t.id !== variables.id) : prev
      );
    },
    onError: (error: unknown) => {
      logger.error('update transaction failed:', error);
      showMessage('Chyba při ukládání transakce.', 'error');
    },
  });

  const updateLocalTransaction = (next: Transaction) => {
    queryClient.setQueryData<Transaction[]>(TRANSACTIONS_KEY, (prev) =>
      prev ? prev.map(t => (t.id === next.id ? next : t)) : prev
    );
  };

  return {
    transactions: transactionsQuery.data ?? [],
    loading: transactionsQuery.isFetching || syncMutation.isPending,
    status,
    showMessage,
    handleSync: () => syncMutation.mutate(),
    handleUpdate: (transaction: Transaction) => updateMutation.mutate(transaction),
    updateLocalTransaction,
    refresh: () => queryClient.invalidateQueries({ queryKey: TRANSACTIONS_KEY }),
  };
};
