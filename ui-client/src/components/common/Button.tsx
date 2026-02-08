import React, { ReactNode, CSSProperties } from 'react';

interface ButtonProps {
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;
  disabled?: boolean;
  children: ReactNode;
  className?: string;
  style?: CSSProperties;
}

const Button: React.FC<ButtonProps> = ({
  onClick,
  disabled,
  children,
  className = '',
  style
}) => {
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