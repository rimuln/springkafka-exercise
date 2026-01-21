import { useState, useEffect } from 'react'
import ManualTransactionDialog from './components/ManualTransactionDialog';
import StatusBanner from './components/StatusBanner';
import './App.css'

function App() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [status, setStatus] = useState({ message: '', type: '' });

  const showMessage = (message, type = 'info') => {
    setStatus({ message, type });
    setTimeout(() => setStatus({ message: '', type: '' }), 5000);
  };

  const fetchTransactions = () => {
    setLoading(true);
    fetch('/api/transactions')
      .then(response => response.json())
      .then(data => {
        setTransactions(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Chyba při volání API:', error);
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleSync = () => {
    setLoading(true);
    fetch('/api/moneta/246594777')
      .then(response => {
        if (response.ok) {
          showMessage('Synchronizace proběhla úspěšně. Načítám nová data...', 'success');
          return fetch('/api/transactions');
          } else if (response.status === 429) {
            showMessage('Příliš mnoho požadavků. Zkuste to prosím za chvíli.', 'error');
          } else {
            throw new Error('Synchronizace selhala');
          }
      })
      .then(response => response.json())
      .then(data => {
        setTransactions(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error during sync:', error);
        showMessage('Chyba při synchronizaci s bankou.','error');
        setLoading(false);
      });
  };

  const handleVsChange = (index, newValue) => {
    const updatedTransactions = [...transactions];
    updatedTransactions[index].variableSymbol = newValue;
    updatedTransactions[index].isDirty = true;
    setTransactions(updatedTransactions);
  };

  const handleIgnore = (index) => {
    const updatedTransactions = [...transactions];
    updatedTransactions[index].processingStatus = 'IGNORE';
    updatedTransactions[index].isDirty = true;
    setTransactions(updatedTransactions);
  };

  const handleUpdate = (transaction) => {
    console.log("Odesílám opravu pro:", transaction);

    fetch(`/api/transactions/${transaction.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        id: transaction.id,
        variableSymbol: parseInt(transaction.variableSymbol, 10),
        transactionDate: transaction.transactionDate,
        transactionNumber: parseInt(transaction.transactionNumber),
        processingStatus: transaction.processingStatus
      }),
    })
    .then(response => {
      if (response.ok) {
        showMessage('Transakce byla odeslána k novému zpracování.','success');
        setTransactions(prev => prev.filter(t => t.id !== transaction.id));
      } else {
        showMessage('Chyba při odesílání','error');
      }
    })
    .catch(error => console.error('Error:', error));
  };

  return (
    <div className="container">
      <StatusBanner message={status.message} type={status.type} />

      <div className="toolbar" style={{ marginBottom: '20px', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
        <button
          onClick={() => setIsDialogOpen(true)}
          style={{ backgroundColor: '#2196F3', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          + Vložit ručně
        </button>
        <button
          onClick={handleSync}
          disabled={loading}
          style={{ backgroundColor: '#4CAF50', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          {loading ? 'Synchronizuji...' : 'Aktualizovat z Monety'}
        </button>
      </div>

      <h1>Transakce k vyřízení</h1>

      {loading ? (
        <p>Načítám data z backendu...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Datum</th>
              <th>Číslo</th>
              <th>Částka</th>
              <th>Zpráva</th>
              <th>VS</th>
              <th>Akce</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t, index) => (
              <tr key={t.id}>
                <td>{t.transactionDate}</td>
                <td>{t.transactionNumber}</td>
                <td style={{ color: t.amount < 0 ? 'red' : 'green' }}>
                  {t.amount} {t.currencyCode === 0 ? 'CZK' : t.currencyCode}
                </td>
                <td>{t.counterpartyAccountName}, {t.messageForRecipient}, {t.descr1}</td>
                <td>
                  <input
                    type="text"
                    value={t.variableSymbol || ''}
                    onChange={e => handleVsChange(index, e.target.value)}
                  />
                </td>
                <td>
                  <button
                    onClick={() => handleIgnore(index)}
                    style={{ backgroundColor: t.processingStatus === 'IGNORE' ? '#ffcccc' : '' }}
                  >
                    Ignorovat
                  </button>
                  <button
                    onClick={() => handleUpdate(t)}
                    disabled={!t.isDirty}
                    className={t.isDirty ? 'btn-active' : 'btn-disabled'}
                  >
                    Opravit
                  </button>
                </td>
              </tr>
            ))}
            {transactions.length === 0 && (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '20px' }}>
                  Žádné nové transakce k vyřízení.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}

      <ManualTransactionDialog
        isOpen={isDialogOpen}
        onClose={() => setIsDialogOpen(false)}
        onRefresh={fetchTransactions}
        onShowMessage={showMessage}
      />
    </div>
  )
}

export default App