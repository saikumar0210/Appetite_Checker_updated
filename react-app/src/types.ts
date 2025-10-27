// TypeScript interfaces for API data types

export interface MatchingData {
  'Request ID'?: number;
  Layout?: string;
  'Match Method'?: string;
  'Match Grade'?: string;
  'Confidence Code'?: string;
  'DUNS #'?: string;
  'Matches Remaining'?: number;
  BEMFAB?: string;
}

export interface AppendedData {
  'Company Name'?: string;
  'Secondary Business Name'?: string;
  'Street Address'?: string;
  City?: string;
  'State/Province'?: string;
  'ZIP Code'?: string;
  Country?: string;
  Phone?: string;
  URL?: string;
  'CEO Title'?: string;
  'CEO First Name'?: string;
  'CEO Last Name'?: string;
  'CEO Name'?: string;
  'Line of Business'?: string;
  'Location Type'?: string;
  'Year Started'?: string;
  'Employees on Site'?: string;
  'Employees Total'?: string;
  'Sales Volume in US$'?: string;
  '4 Digit SIC 1'?: string;
  '4 Digit SIC 1 Description'?: string;
  '4 Digit SIC 2'?: string;
  '4 Digit SIC 2 Description'?: string;
  '8 Digit SIC 1'?: string;
  '8 Digit SIC 1 Description'?: string;
  '8 Digit SIC 2'?: string;
  '8 Digit SIC 2 Description'?: string;
  'NAICS 1 Code'?: string;
  'NAICS 1 Description'?: string;
  'NAICS 2 Code'?: string;
  'NAICS 2 Description'?: string;
}

export interface CompanyResponseDTO {
  matchingData?: MatchingData;
  appendedData?: AppendedData;
  geminiRecommendation?: any;
}

export interface ChartData {
  labels: string[];
  datasets: Array<{
    label: string;
    data: number[];
    backgroundColor?: string | string[];
    borderColor?: string;
    fill?: boolean;
  }>;
}

export interface ChartConfig {
  type: 'bar' | 'line' | 'doughnut' | 'pie';
  data: ChartData;
}

export interface RestrictedCardData {
  requestId: number;
  businessName: string;
  naicsCode: string;
  confidenceCode: string;
}

export interface ImprovementCardData {
  requestId: number;
  businessName: string;
  naicsCode: string;
  confidenceCode: string;
  tips: string[];
}