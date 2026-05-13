import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

import { ReportColumnDef } from './reporting.types';

/**
 * Format any value for export. We intentionally keep this lossy on purpose —
 * exported files should look the same as what the user sees in the grid, so
 * we stringify dates / money via the same rules as the grid renderer.
 */
function formatCell(value: unknown, col: ReportColumnDef): string {
  if (value === null || value === undefined) {
    return '';
  }
  if (col.type === 'DATETIME' || col.type === 'DATE') {
    try {
      const d = new Date(String(value));
      if (Number.isNaN(d.getTime())) return String(value);
      if (col.type === 'DATE') {
        return d.toLocaleDateString();
      }
      return d.toLocaleString();
    } catch {
      return String(value);
    }
  }
  if (col.type === 'MONEY') {
    const n = Number(value);
    if (Number.isNaN(n)) return String(value);
    return n.toFixed(2);
  }
  if (col.type === 'BOOLEAN') {
    return value ? 'Yes' : 'No';
  }
  return String(value);
}

function buildMatrix(
  columns: ReportColumnDef[],
  rows: Array<Record<string, unknown>>
): { header: string[]; body: string[][] } {
  const header = columns.map((c) => c.label);
  const body = rows.map((row) =>
    columns.map((col) => formatCell(row[col.key], col))
  );
  return { header, body };
}

function suggestFilename(reportName: string, ext: string): string {
  const safe = reportName.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
  const ts = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
  return `${safe || 'report'}-${ts}.${ext}`;
}

export function exportCsv(
  reportName: string,
  columns: ReportColumnDef[],
  rows: Array<Record<string, unknown>>
): void {
  const { header, body } = buildMatrix(columns, rows);
  const escape = (cell: string) => {
    if (/[",\n]/.test(cell)) {
      return '"' + cell.replace(/"/g, '""') + '"';
    }
    return cell;
  };
  const lines = [header.map(escape).join(',')];
  for (const row of body) {
    lines.push(row.map(escape).join(','));
  }
  const blob = new Blob(['\uFEFF' + lines.join('\r\n')], {
    type: 'text/csv;charset=utf-8;',
  });
  saveAs(blob, suggestFilename(reportName, 'csv'));
}

export function exportExcel(
  reportName: string,
  columns: ReportColumnDef[],
  rows: Array<Record<string, unknown>>
): void {
  const { header, body } = buildMatrix(columns, rows);
  const aoa = [header, ...body];
  const worksheet = XLSX.utils.aoa_to_sheet(aoa);
  worksheet['!cols'] = header.map((h) => ({ wch: Math.max(12, h.length + 2) }));
  const workbook = XLSX.utils.book_new();
  const sheetName = (reportName || 'Report').slice(0, 28);
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName || 'Report');
  const out = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([out], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  saveAs(blob, suggestFilename(reportName, 'xlsx'));
}

export function exportPdf(
  reportName: string,
  columns: ReportColumnDef[],
  rows: Array<Record<string, unknown>>
): void {
  const { header, body } = buildMatrix(columns, rows);
  const landscape = columns.length > 5;
  const doc = new jsPDF({ orientation: landscape ? 'landscape' : 'portrait', unit: 'pt' });

  doc.setFontSize(14);
  doc.text(reportName || 'Report', 40, 40);
  doc.setFontSize(10);
  doc.setTextColor(120);
  doc.text(`Generated ${new Date().toLocaleString()}`, 40, 58);
  doc.setTextColor(0);

  autoTable(doc, {
    head: [header],
    body,
    startY: 80,
    styles: { fontSize: 8, cellPadding: 4, overflow: 'linebreak' },
    headStyles: { fillColor: [33, 150, 243], textColor: 255 },
    alternateRowStyles: { fillColor: [245, 247, 250] },
    margin: { left: 40, right: 40 },
  });
  doc.save(suggestFilename(reportName, 'pdf'));
}
