export interface Transaction {
  id: string;
  transactionSentDate: string;
  transactionNumber: number;
  amount: number;
  currencyCode: number | string;
  variableSymbol: number | null;
  counterpartyAccountName?: string;
  messageForRecipient?: string;
  processingStatus: 'PENDING' | 'PENDING_MANUAL' | 'AUTO_PROCESSED' | 'IGNORE' | 'MANUALY_FIXED';
  isDirty?: boolean;
}
