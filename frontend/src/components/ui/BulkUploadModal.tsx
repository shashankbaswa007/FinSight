import React, { useState, useRef } from 'react';
import { Upload, FileSpreadsheet, Download, AlertCircle, CheckCircle2, ChevronRight, X, AlertTriangle } from 'lucide-react';
import Modal from './Modal';
import { transactionApi } from '../../api/transactions';
import type { BulkUploadPreviewResponse } from '../../types';
import { formatCurrency } from '../../utils/formatters';

interface BulkUploadModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function BulkUploadModal({ open, onClose, onSuccess }: BulkUploadModalProps) {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [preview, setPreview] = useState<BulkUploadPreviewResponse | null>(null);
  const [commitResult, setCommitResult] = useState<{ created: number; skipped: number } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const reset = () => {
    setStep(1);
    setFile(null);
    setPreview(null);
    setCommitResult(null);
    setLoading(false);
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  const downloadTemplate = async () => {
    try {
      const blob = await transactionApi.bulkDownloadTemplate();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'transactions_template.xlsx';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove();
    } catch (err) {
      console.error('Failed to download template', err);
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0];
    if (!selected) return;
    
    // Check file type
    if (!selected.name.endsWith('.xlsx')) {
      alert('Please upload an Excel (.xlsx) file');
      return;
    }

    setFile(selected);
    setLoading(true);
    try {
      const result = await transactionApi.bulkPreview(selected);
      setPreview(result);
      setStep(2);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to parse file');
      setFile(null);
    } finally {
      setLoading(false);
    }
  };

  const confirmUpload = async () => {
    if (!preview) return;
    setLoading(true);
    try {
      const result = await transactionApi.bulkCommit(preview);
      setCommitResult({ created: result.createdCount, skipped: result.skippedCount });
      setStep(3);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to commit transactions');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={open} onClose={handleClose} title="Bulk Upload Transactions" maxWidth="max-w-4xl">
      <div className="p-2">
        
        {/* STEP 1: Upload */}
        {step === 1 && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="flex-1 card p-6 flex flex-col items-center justify-center text-center border-2 border-dashed border-gray-300 dark:border-slate-600 bg-gray-50 dark:bg-slate-800/50 hover:bg-gray-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
                   onClick={() => fileInputRef.current?.click()}>
                <input 
                  type="file" 
                  ref={fileInputRef} 
                  className="hidden" 
                  accept=".xlsx"
                  onChange={handleFileChange}
                />
                <FileSpreadsheet className="h-12 w-12 text-brand-500 mb-4" />
                <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-2">
                  {loading ? 'Processing...' : 'Upload Excel File'}
                </h3>
                <p className="text-sm text-gray-500 dark:text-slate-400 max-w-[250px]">
                  Drag and drop or click to select your .xlsx file. Max 1000 rows.
                </p>
              </div>
              
              <div className="flex-1 card p-6 bg-brand-50 dark:bg-brand-900/10 border-brand-200 dark:border-brand-800">
                <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">How it works</h3>
                <ol className="list-decimal list-inside space-y-2 text-sm text-gray-700 dark:text-slate-300 mb-6">
                  <li>Download the template file below.</li>
                  <li>Fill in your transactions (Date, Amount, Type are required).</li>
                  <li>Upload the filled file here.</li>
                  <li>Preview and confirm the import.</li>
                </ol>
                <button 
                  onClick={downloadTemplate}
                  className="w-full btn-secondary flex items-center justify-center gap-2"
                >
                  <Download className="h-4 w-4" /> Download Template
                </button>
              </div>
            </div>
          </div>
        )}

        {/* STEP 2: Preview */}
        {step === 2 && preview && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div className="card p-4 flex items-center gap-4 border-l-4 border-emerald-500">
                <div className="p-3 bg-emerald-100 dark:bg-emerald-900/30 rounded-full">
                  <CheckCircle2 className="h-6 w-6 text-emerald-600 dark:text-emerald-400" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-500 dark:text-slate-400">Valid Rows</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{preview.validCount - preview.duplicateCount}</p>
                </div>
              </div>
              
              <div className={`card p-4 flex items-center gap-4 border-l-4 ${preview.duplicateCount > 0 ? 'border-amber-500' : 'border-gray-300'}`}>
                <div className={`p-3 rounded-full ${preview.duplicateCount > 0 ? 'bg-amber-100 dark:bg-amber-900/30' : 'bg-gray-100 dark:bg-slate-800'}`}>
                  <AlertTriangle className={`h-6 w-6 ${preview.duplicateCount > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-gray-400'}`} />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-500 dark:text-slate-400">Duplicates (Skipped)</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{preview.duplicateCount}</p>
                </div>
              </div>

              <div className={`card p-4 flex items-center gap-4 border-l-4 ${preview.errorCount > 0 ? 'border-red-500' : 'border-gray-300'}`}>
                <div className={`p-3 rounded-full ${preview.errorCount > 0 ? 'bg-red-100 dark:bg-red-900/30' : 'bg-gray-100 dark:bg-slate-800'}`}>
                  <AlertCircle className={`h-6 w-6 ${preview.errorCount > 0 ? 'text-red-600 dark:text-red-400' : 'text-gray-400'}`} />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-500 dark:text-slate-400">Errors</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{preview.errorCount}</p>
                </div>
              </div>
            </div>

            {preview.errorCount > 0 && (
              <div className="bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-800/30 rounded-xl overflow-hidden">
                <div className="px-4 py-3 bg-red-100 dark:bg-red-900/20 border-b border-red-200 dark:border-red-800/30 font-medium text-red-800 dark:text-red-400 flex items-center gap-2">
                  <AlertCircle className="h-4 w-4" /> Please fix these errors in your file
                </div>
                <div className="max-h-48 overflow-y-auto p-4 space-y-3 text-sm">
                  {preview.errorRows.map((err, i) => (
                    <div key={i} className="flex gap-4">
                      <span className="font-bold text-red-800 dark:text-red-400 min-w-[50px]">Row {err.rowNumber}</span>
                      <div>
                        <ul className="list-disc list-inside text-red-700 dark:text-red-300 mb-1">
                          {err.errors.map((e, j) => <li key={j}>{e}</li>)}
                        </ul>
                        <p className="text-xs text-gray-500 dark:text-slate-500 font-mono break-all bg-white dark:bg-slate-900 p-1.5 rounded">{err.rawData}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
            
            {preview.duplicateCount > 0 && (
              <div className="bg-amber-50 dark:bg-amber-900/10 border border-amber-200 dark:border-amber-800/30 rounded-xl overflow-hidden">
                <div className="px-4 py-3 bg-amber-100 dark:bg-amber-900/20 border-b border-amber-200 dark:border-amber-800/30 font-medium text-amber-800 dark:text-amber-400 flex items-center gap-2">
                  <AlertTriangle className="h-4 w-4" /> These rows look like duplicates and will be skipped
                </div>
                <div className="max-h-48 overflow-y-auto p-4 space-y-3 text-sm">
                  {preview.duplicateRows.map((dup, i) => (
                    <div key={i} className="flex gap-4">
                      <span className="font-bold text-amber-800 dark:text-amber-400 min-w-[50px]">Row {dup.rowNumber}</span>
                      <span className="text-amber-700 dark:text-amber-300">
                        {dup.date} - {formatCurrency(dup.amount)} - {dup.description || 'No description'}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {preview.validRows.length > 0 && (
              <div>
                <h4 className="font-semibold text-gray-900 dark:text-white mb-3">Valid Transactions Preview (First 50)</h4>
                <div className="border border-gray-200 dark:border-slate-700 rounded-xl overflow-hidden max-h-[300px] overflow-y-auto">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-50 dark:bg-slate-800/50 sticky top-0">
                      <tr>
                        <th className="text-left py-2 px-4 font-medium text-gray-500">Date</th>
                        <th className="text-left py-2 px-4 font-medium text-gray-500">Description</th>
                        <th className="text-left py-2 px-4 font-medium text-gray-500">Category</th>
                        <th className="text-right py-2 px-4 font-medium text-gray-500">Amount</th>
                      </tr>
                    </thead>
                    <tbody>
                      {preview.validRows.slice(0, 50).map((row, i) => (
                        <tr key={i} className="border-t border-gray-100 dark:border-slate-700/50">
                          <td className="py-2 px-4 text-gray-900 dark:text-slate-300 whitespace-nowrap">{row.date}</td>
                          <td className="py-2 px-4 text-gray-900 dark:text-slate-300 truncate max-w-[200px]">{row.description || '-'}</td>
                          <td className="py-2 px-4">
                            {row.categoryName ? (
                              <span className="bg-gray-100 dark:bg-slate-700 text-gray-800 dark:text-slate-200 px-2 py-0.5 rounded text-xs">
                                {row.categoryName}
                              </span>
                            ) : (
                              <span className="text-brand-500 text-xs italic">Auto-categorize</span>
                            )}
                          </td>
                          <td className={`py-2 px-4 text-right font-medium whitespace-nowrap ${row.type === 'INCOME' ? 'text-emerald-600' : 'text-red-600'}`}>
                            {row.type === 'INCOME' ? '+' : '-'}{formatCurrency(row.amount)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            <div className="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-slate-700">
              <button onClick={() => setStep(1)} className="btn-secondary" disabled={loading}>Go Back</button>
              <button 
                onClick={confirmUpload} 
                className="btn-primary" 
                disabled={loading || preview.validRows.length === 0}
              >
                {loading ? 'Importing...' : `Import ${preview.validRows.length} Transactions`}
              </button>
            </div>
          </div>
        )}

        {/* STEP 3: Result */}
        {step === 3 && commitResult && (
          <div className="py-8 flex flex-col items-center justify-center text-center space-y-4 animate-fade-in">
            <div className="h-20 w-20 bg-emerald-100 dark:bg-emerald-900/30 rounded-full flex items-center justify-center mb-4">
              <CheckCircle2 className="h-10 w-10 text-emerald-600 dark:text-emerald-400" />
            </div>
            <h2 className="text-3xl font-bold text-gray-900 dark:text-white">Import Complete!</h2>
            <p className="text-lg text-gray-600 dark:text-slate-400 max-w-md">
              Successfully imported <span className="font-bold text-gray-900 dark:text-white">{commitResult.created}</span> transactions.
            </p>
            {commitResult.skipped > 0 && (
              <p className="text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-900/20 px-4 py-2 rounded-lg">
                Note: {commitResult.skipped} rows were skipped due to errors or being duplicates.
              </p>
            )}
            <div className="pt-6">
              <button onClick={() => {
                onSuccess();
                handleClose();
              }} className="btn-primary px-8">
                View Transactions
              </button>
            </div>
          </div>
        )}
        
      </div>
    </Modal>
  );
}
