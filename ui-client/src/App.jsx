import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  // useEffect se spustí při načtení stránky
  useEffect(() => {
    fetch('http://localhost:8080/transactions')
      .then(response => response.json())
      .then(data => {
        setTransactions(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Chyba při volání API:', error);
        setLoading(false);
      });
  }, []);

const handleSync = () => {
  setLoading(true);
  fetch('http://localhost:8080/moneta/246594777')
    .then(response => {
      if (response.ok) {
        alert('Synchronizace proběhla úspěšně. Načítám nová data...');
        return fetch('http://localhost:8080/transactions');
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
      alert('Chyba při synchronizaci s bankou.');
      setLoading(false);
    });
};

  const handleVsChange = (index, newValue) => {
    const updatedTransactions = [...transactions];
    updatedTransactions[index].variableSymbol = newValue;
    updatedTransactions[index].isDirty =true;
    setTransactions(updatedTransactions);
  };

  const handleIgnore = (index) => {
    const updatedTransactions = [...transactions];
    updatedTransactions[index].processingStatus = 'IGNORE';
    updatedTransactions[index].isDirty =true;
    setTransactions(updatedTransactions);
  };

  // FUNKCE 2: Odeslání dat na Spring Boot API
  const handleUpdate = (transaction) => {
    console.log("Odesílám opravu pro:", transaction);

    fetch(`http://localhost:8080/transactions/${transaction.id}`, {
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
        alert('Transakce byla odeslána k novému zpracování.');
        setTransactions(prev => prev.filter(t => t.id !== transaction.id));
      } else {
        alert('Chyba při odesílání');
      }
    })
    .catch(error => console.error('Error:', error));
  };

  return (
    <div className="container">
     <div className="toolbar" style={{ marginBottom: '20px', textAlign: 'right' }}>
       <button
         onClick={handleSync}
         disabled={loading}
         style={{ backgroundColor: '#4CAF50', color: 'white', padding: '10px 20px' }}
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
            {transactions.map((t,index) => (
              <tr key={t.id}>
                <td>{t.transactionDate}</td>
                <td>{t.transactionNumber}</td>
                <td style={{ color: t.amount < 0 ? 'red' : 'green' }}>
                  {t.amount} {t.currencyCode === 0 ? 'CZK' : t.currencyCode}
                </td>
                <td>{t.counterpartyAccountName}, {t.messageForRecipient}, {t.descr1}</td>
                <td><input type="text" value={t.variableSymbol || ''} onChange={e => handleVsChange(index,e.target.value)} /></td>
                <td>
                    <button
                    onClick={() => handleIgnore(index)}
                    style={{ backgroundColor: t.processingStatus === 'IGNORE' ? '#ffcccc' : '' }}
                  >Ignorovat</button>
                  <button
                    onClick={() => handleUpdate(t)}
                    disabled={!t.isDirty}
                    className={t.isDirty ? 'btn-active' : 'btn-disabled'}
                  >Opravit</button>
                </td>
              </tr>
            ))}
            {transactions.length === 0 && (
              <tr><td colSpan="6" style={{ textAlign: 'center' }}>Žádné nové transakce.</td></tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default App