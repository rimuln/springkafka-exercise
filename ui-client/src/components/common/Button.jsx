import React from 'react';

const Button = ({ onClick, disabled, children, className, style }) => {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`btn-base ${className}`}
      style={style}
    >
      {children}
    </button>
  );
};

export default Button;