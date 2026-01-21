import React from 'react';
import Button from './common/Button';

export const ManualTransactionButton = ({ onClick }) => (
  <Button
    onClick={onClick}    className="btn-manual"
  >
    + Vložit ručně
  </Button>
);

export const SyncButton = ({ onClick, loading }) => (
  <Button
    onClick={onClick}
    disabled={loading}
    className="btn-sync"
  >
    {loading ? 'Pracuji...' : 'Aktualizovat z Monety'}
  </Button>
);

export const IgnoreButton = ({ onClick, isActive }) => (
  <Button
    onClick={onClick}
    className={`btn-ignore ${isActive ? 'active' : ''}`}
  >
    Ignorovat
  </Button>
);

export const FixButton = ({ onClick, disabled }) => (
  <Button
    onClick={onClick}
    disabled={disabled}
    className={`btn-update ${!disabled ? 'active' : 'disabled'}`}
  >
    Opravit
  </Button>
);