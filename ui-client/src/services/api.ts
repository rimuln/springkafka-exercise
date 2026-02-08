import { logger } from '../services/logger';
import type { Transaction } from '../types/transaction';

const BASE_URL = '/api';

async function handleResponse<T>(response: Response): Promise<T | null> {
  if (response.status === 429) {
    throw new Error('RATE_LIMIT');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'API_ERROR');
  }

  const contentType = response.headers.get("content-type");
  const contentLength = response.headers.get("content-length");

  if (response.status === 204 || contentLength === "0" || !contentType || !contentType.includes("application/json")) {
    logger.debug('Response is success but empty (no JSON to parse)');
    return null;
  }
  return response.json();
}

export const api = {
  fetchTransactions: (): Promise<Transaction[]> =>
    fetch(`${BASE_URL}/transactions`).then(res => handleResponse<Transaction[]>(res))
      .then(data => data || []),

  syncWithBank: (accountNumber: string = '246594777'): Promise<void> =>
    fetch(`${BASE_URL}/moneta/${accountNumber}`).then(res => handleResponse<void>(res)),

  updateTransaction: (transaction: Transaction): Promise<Transaction> => {
    const payload = {
      id: transaction.id,
      variableSymbol: transaction.variableSymbol ? parseInt(transaction.variableSymbol.toString(), 10) : null,
      transactionSentDate: transaction.transactionSentDate,
      transactionNumber: transaction.transactionNumber ? parseInt(transaction.transactionNumber.toString(), 10) : 0,
      processingStatus: transaction.processingStatus
    };

    return fetch(`${BASE_URL}/transactions/${transaction.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).then(res => handleResponse<Transaction>(res))
      .then(data => data!);
  },

  createManualTransaction: (data: Partial<Transaction>): Promise<Transaction> =>
    fetch(`${BASE_URL}/transactions/manual`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(res => handleResponse<Transaction>(res))
      .then(data => data!)
};
