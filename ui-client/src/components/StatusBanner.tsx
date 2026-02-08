import React from 'react';
import type { StatusType } from '../types/statusType';

interface StatusBannerProps {
  message: string;
  type: StatusType;
}

const StatusBanner: React.FC<StatusBannerProps> = ({ message, type }) => {
  if (!message) return null;

  return (
    <div className={`status-banner ${type}`}>
      {message}
    </div>
  );
};

export default StatusBanner;