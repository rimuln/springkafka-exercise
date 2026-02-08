import React from 'react';
import { IgnoreButton, FixButton } from './ActionButtons';
import type { Transaction } from '../types/transaction';

interface TransactionTableProps {
  transactions: Transaction[];
  loading: boolean;
  onTransactionsChange: (updatedTransactions: Transaction[]) => void;
  onUpdate: (transaction: Transaction) => void;
}

const TransactionTable: React.FC<TransactionTableProps> = ({
  transactions,
  loading,
  onTransactionsChange,
  onUpdate
}) => {

  const handleVsChange = (index: number, newValue: string) => {
    const updated = [...transactions];
    // Převedeme string na number | null pro variableSymbol
    const vsValue = newValue ? parseInt(newValue, 10) : null;
    updated[index] = {
      ...updated[index],
      variableSymbol: vsValue,
      isDirty: true
    };
    onTransactionsChange(updated);
  };

  const handleIgnore = (index: number) => {
    const updated = [...transactions];
    updated[index] = {
      ...updated[index],
      processingStatus: 'IGNORE',
      isDirty: true
    };
    onTransactionsChange(updated);
  };

  return (
    <div className="table-wrapper">
      {loading && (
        <div className="table-loading-overlay">
          <span>Aktualizuji data...</span>
        </div>
      )}

      <table className="fixed-table">
        <thead>
          <tr>
            <th style={{ width: '120px' }}>Datum</th>
            <th style={{ width: '80px' }}>Číslo</th>
            <th style={{ width: '120px' }}>Částka</th>
            <th>Zpráva</th>
            <th style={{ width: '150px' }}>VS</th>
            <th style={{ width: '200px' }}>Akce</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((t, index) => (
            <tr key={t.id}>
              <td>{t.transactionSentDate}</td>
              <td>{t.transactionNumber}</td>
              <td style={{ color: t.amount < 0 ? 'red' : 'green', fontWeight: 'bold' }}>
                {t.amount} {t.currencyCode === 0 ? 'CZK' : t.currencyCode}
              </td>
              <td title={`${t.counterpartyAccountName || ''}, ${t.messageForRecipient || ''}`}>
                <div className="text-truncate">
                  {t.counterpartyAccountName}, {t.messageForRecipient}
                </div>
              </td>
              <td>
                <input
                  type="text"
                  className="vs-input"
                  value={t.variableSymbol || ''}
                  onChange={e => handleVsChange(index, e.target.value)}
                />
              </td>
              <td>
                <div className="action-buttons">
                  <IgnoreButton
                    onClick={() => handleIgnore(index)}
                    isActive={t.processingStatus === 'IGNORE'}
                  />
                  <FixButton
                    onClick={() => onUpdate(t)}
                    disabled={!t.isDirty}
                  />
                </div>
              </td>
            </tr>
          ))}
          {!loading && transactions.length === 0 && (
            <tr>
              <td colSpan={6} style={{ textAlign: 'center', padding: '20px' }}>
                Žádné nové transakce k vyřízení.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default TransactionTable;