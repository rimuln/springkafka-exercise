import React from 'react';
import Button from './common/Button';

interface ActionButtonProps {
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
}

export const ManualTransactionButton: React.FC<ActionButtonProps> = ({ onClick }) => (
  <Button
    onClick={onClick}
    className="btn-manual"
  >
    + Vložit ručně
  </Button>
);

interface SyncButtonProps extends ActionButtonProps {
  loading: boolean;
}

export const SyncButton: React.FC<SyncButtonProps> = ({ onClick, loading }) => (
  <Button
    onClick={onClick}
    disabled={loading}
    className="btn-sync"
  >
    {loading ? 'Pracuji...' : 'Aktualizovat z Monety'}
  </Button>
);

interface IgnoreButtonProps extends ActionButtonProps {
  isActive?: boolean;
}

export const IgnoreButton: React.FC<IgnoreButtonProps> = ({ onClick, isActive }) => (
  <Button
    onClick={onClick}
    className={`btn-ignore ${isActive ? 'active' : ''}`}
  >
    Ignorovat
  </Button>
);

interface FixButtonProps extends ActionButtonProps {
  disabled: boolean;
}

export const FixButton: React.FC<FixButtonProps> = ({ onClick, disabled }) => (
  <Button
    onClick={onClick}
    disabled={disabled}
    className={`btn-update ${!disabled ? 'active' : 'disabled'}`}
  >
    Opravit
  </Button>
);