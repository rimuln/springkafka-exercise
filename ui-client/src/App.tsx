import React, { useState } from 'react';
import ManualTransactionDialog from './components/ManualTransactionDialog';
import StatusBanner from './components/StatusBanner';
import TransactionTable from './components/TransactionTable';
import { ManualTransactionButton, SyncButton } from './components/ActionButtons';
import { useTransactions } from './hooks/useTransactions';
import './App.css';

const App: React.FC = () => {
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const {
    transactions,
    loading,
    status,
    showMessage,
    handleSync,
    handleUpdate,
    updateLocalTransaction,
    refresh,
  } = useTransactions();

  return (
    <div className="container">
      <StatusBanner message={status.message} type={status.type} />

      <div className="toolbar">
        <ManualTransactionButton onClick={() => setIsDialogOpen(true)} />
        <SyncButton onClick={handleSync} loading={loading} />
      </div>

      <h1>Transakce k vyřízení</h1>

      <TransactionTable
        transactions={transactions}
        loading={loading}
        onLocalChange={updateLocalTransaction}
        onUpdate={handleUpdate}
      />

      <ManualTransactionDialog
        isOpen={isDialogOpen}
        onClose={() => setIsDialogOpen(false)}
        onRefresh={refresh}
        onShowMessage={showMessage}
      />
    </div>
  );
};

export default App;
