const BASE_URL = '/api';

async function handleResponse(response) {
  if (response.status === 429) {
    throw new Error('RATE_LIMIT');
  }
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'API_ERROR');
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export const api = {
  fetchTransactions: () =>
    fetch(`${BASE_URL}/transactions`).then(handleResponse),

  syncWithBank: (accountNumber = '246594777') =>
    fetch(`${BASE_URL}/moneta/${accountNumber}`).then(handleResponse),

  updateTransaction: (transaction) => {
    const payload = {
      id: transaction.id,
      variableSymbol: parseInt(transaction.variableSymbol, 10),
      transactionSentDate: transaction.transactionSentDate,
      transactionNumber: parseInt(transaction.transactionNumber),
      processingStatus: transaction.processingStatus
    };

    return fetch(`${BASE_URL}/transactions/${transaction.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).then(handleResponse);
  },

  // POST: Ruční vložení
  createManualTransaction: (data) =>
    fetch(`${BASE_URL}/transactions/manual`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then(handleResponse)
};