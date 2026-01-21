import React from 'react';

const StatusBanner = ({ message, type }) => {
  if (!message) return null;

  return (
    <div className={`status-banner ${type}`}>
      {message}
    </div>
  );
};

export default StatusBanner;