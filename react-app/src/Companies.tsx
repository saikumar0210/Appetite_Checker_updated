import React, { useState, useEffect } from 'react';
import Chart from 'chart.js/auto';
import './Companies.css';
import { 
  CompanyResponseDTO, 
  ChartConfig, 
  RestrictedCardData, 
  ImprovementCardData 
} from './types';
import { 
  AlertCircle, 
  Bot, 
  Search, 
  AlertTriangle, 
  BarChart3, 
  Target, 
  TrendingUp,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Loader2
} from 'lucide-react';

const Companies: React.FC = () => {
  const [companiesData, setCompaniesData] = useState<CompanyResponseDTO[]>([]);
  const [filteredData, setFilteredData] = useState<CompanyResponseDTO[]>([]);
  const [searchInput, setSearchInput] = useState<string>('');
  const [stateFilter, setStateFilter] = useState<string>('');
  const [charts, setCharts] = useState<Record<string, Chart>>({});
  const [showRestrictedCard, setShowRestrictedCard] = useState<boolean>(false);
  const [showImprovementCard, setShowImprovementCard] = useState<boolean>(false);
  const [restrictedCardData, setRestrictedCardData] = useState<RestrictedCardData>({} as RestrictedCardData);
  const [improvementCardData, setImprovementCardData] = useState<ImprovementCardData>({} as ImprovementCardData);
  const [whyNotEligible, setWhyNotEligible] = useState<string>('Loading reason...');
  const [aiRecommendation, setAiRecommendation] = useState<string>('Loading AI recommendation...');
  const [isScrolling, setIsScrolling] = useState<boolean>(false);
  const [isSearching, setIsSearching] = useState<boolean>(false);

  useEffect(() => {
    fetchCompaniesData();
  }, []);

  useEffect(() => {
    if (companiesData.length > 0) {
      initTrends();
    }
  }, [companiesData]);

  useEffect(() => {
    // Initialize feather icons after component mounts
    if ((window as any).feather) {
      (window as any).feather.replace();
    }
  }, [filteredData]);

  const fetchCompaniesData = async (): Promise<void> => {
    try {
      const response = await fetch('/api/companies/all');
      const data: CompanyResponseDTO[] = await response.json();
      setCompaniesData(data);
      setFilteredData(data);
    } catch (error) {
      console.error('Error fetching companies:', error);
    }
  };

  const getStates = (): string[] => {
    const states = companiesData
      .map(c => c.appendedData?.['State/Province'])
      .filter((state): state is string => Boolean(state));
    return Array.from(new Set(states)).sort();
  };

  // ORIGINAL CLIENT-SIDE FILTERING LOGIC - COMMENTED OUT FOR FUTURE REFERENCE
  /*
  const filterData = () => {
    const keyword = searchInput.toLowerCase();
    const state = stateFilter;
    const filtered = companiesData.filter(c => {
      const matchesKeyword = (
        (c.matchingData?.['DUNS #']?.toLowerCase().includes(keyword)) ||
        (c.appendedData?.['NAICS 1 Code']?.toLowerCase().includes(keyword)) ||
        (c.appendedData?.['NAICS 1 Description']?.toLowerCase().includes(keyword)) ||
        (c.appendedData?.['NAICS 2 Description']?.toLowerCase().includes(keyword))
      );
      const matchesState = !state || c.appendedData?.['State/Province'] === state;
      return matchesKeyword && matchesState;
    });
    setFilteredData(filtered);
  };
  */

  // NEW BACKEND API FILTERING LOGIC
  const filterData = async (): Promise<void> => {
    setIsSearching(true);
    try {
      const params = new URLSearchParams();
      if (searchInput.trim()) params.append('keyword', searchInput.trim());
      if (stateFilter) params.append('state', stateFilter);
      
      const url = `/api/companies/search/advanced${params.toString() ? '?' + params.toString() : ''}`;
      const response = await fetch(url);
      const filtered: CompanyResponseDTO[] = await response.json();
      setFilteredData(filtered);
    } catch (error) {
      console.error('Error filtering data:', error);
      // Fallback to original client-side filtering if API fails
      const keyword = searchInput.toLowerCase();
      const state = stateFilter;
      const filtered = companiesData.filter(c => {
        const matchesKeyword = !keyword || (
          (c.matchingData?.['DUNS #']?.toLowerCase().includes(keyword)) ||
          (c.appendedData?.['NAICS 1 Code']?.toLowerCase().includes(keyword)) ||
          (c.appendedData?.['NAICS 1 Description']?.toLowerCase().includes(keyword)) ||
          (c.appendedData?.['NAICS 2 Description']?.toLowerCase().includes(keyword))
        );
        const matchesState = !state || c.appendedData?.['State/Province'] === state;
        return matchesKeyword && matchesState;
      });
      setFilteredData(filtered);
    } finally {
      setIsSearching(false);
    }
  };

  const searchData = async (): Promise<void> => {
    await filterData();
    setTimeout(() => {
      document.querySelector('.results-section')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const clearFilters = (): void => {
    setSearchInput('');
    setStateFilter('');
    setShowRestrictedCard(false);
    setShowImprovementCard(false);
    setFilteredData(companiesData);
  };

  const getRecommendation = async (requestId: number, businessName: string, naicsCode: string, confidenceCode: string): Promise<void> => {
    setIsScrolling(true);
    setShowRestrictedCard(true);
    setShowImprovementCard(false);
    setRestrictedCardData({ requestId, businessName, naicsCode, confidenceCode });
    setWhyNotEligible('Loading reason...');
    setAiRecommendation('Loading AI recommendation...');

    try {
      const payload = {
        businessType: businessName,
        naicsCode: naicsCode,
        confidenceScore: confidenceCode
      };

      const response = await fetch('/api/gemini/business-recommendations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const responseText = await response.text();
      const json = JSON.parse(responseText);
      
      if (json.candidates && json.candidates[0]?.content?.parts[0]?.text) {
        const text = json.candidates[0].content.parts[0].text;
        const [reason, recommendation] = text.split('**Recommendation:**');
        const cleanReason = reason.replace('**Reason:**', '').trim() || `Low confidence score (${confidenceCode}) for ${businessName} business`;
        const cleanRecommendation = recommendation ? recommendation.trim() : 'Unable to generate recommendations';
        
        setWhyNotEligible(cleanReason);
        setAiRecommendation(cleanRecommendation);
      }
    } catch (error) {
      setWhyNotEligible(`Failed to load recommendations for ${businessName} business`);
      setAiRecommendation('Please try again for business-specific recommendations');
    }

    setIsScrolling(false);
    
    // Scroll to AI Insights after loading
    setTimeout(() => {
      document.querySelector('#aiInsights')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const getScoreImprovementTips = async (requestId: number, businessName: string, naicsCode: string, confidenceCode: string): Promise<void> => {
    setShowImprovementCard(true);
    setImprovementCardData({ requestId, businessName, naicsCode, confidenceCode, tips: ['Loading...'] });

    const payload = {
      businessType: businessName,
      naicsCode: naicsCode,
      currentScore: confidenceCode,
      requestType: 'detailed_score_improvement'
    };

    try {
      const response = await fetch('/api/gemini/recommendations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const responseText = await response.text();
      const json = JSON.parse(responseText);
      
      let improvements: string[] = [];
      if (json.candidates && json.candidates[0]?.content?.parts[0]?.text) {
        const aiText = json.candidates[0].content.parts[0].text;
        improvements = aiText.split(/\n[-•*]\s*/).filter((tip: string) => tip.trim().length > 10).slice(0, 4);
      }

      /* Commenting out fallback recommendations
      if (improvements.length < 3) {
        improvements = [
          'Verify and update all company contact information including phone, address, and business registration details',
          'Ensure NAICS codes accurately reflect primary business activities and update secondary codes if needed',
          'Complete missing data fields in business registration and provide additional documentation for verification',
          'Review and correct any data inconsistencies between different business databases and registrations'
        ];
      }
      */

      if (improvements.length < 3) {
        throw new Error('Insufficient recommendations received from AI service');
      }

      setImprovementCardData(prev => ({ ...prev, tips: improvements }));
    } catch (error) {
      console.error('Error loading improvement tips:', error);
      /* Commenting out fallback tips
      const fallbackTips = [
        'Verify and update all company contact information including phone, address, and business registration details',
        'Ensure NAICS codes accurately reflect primary business activities and update secondary codes if needed',
        'Complete missing data fields in business registration and provide additional documentation for verification',
        'Review and correct any data inconsistencies between different business databases and registrations'
      ];
      setImprovementCardData(prev => ({ ...prev, tips: fallbackTips }));
      */
      setImprovementCardData(prev => ({ 
        ...prev, 
        tips: [`Error: ${error instanceof Error ? error.message : 'Failed to load recommendations. Please try again.'}`]
      }));
    }
  };

  const randomData = (length: number = 6, max: number = 100): number[] => {
    return Array.from({ length }, () => Math.floor(Math.random() * max));
  };

  const updateChartsForState = (state: string): void => {
    const stateData = companiesData.filter(c => c.appendedData?.['State/Province'] === state);
    const chartConfigs = generateRealDataCharts(stateData);

    Object.keys(charts).forEach(chartKey => {
      if (charts[chartKey] && chartConfigs[chartKey]) {
        charts[chartKey].data = chartConfigs[chartKey].data;
        charts[chartKey].update();
      }
    });
  };

  const generateRealDataCharts = (data: CompanyResponseDTO[]): Record<string, ChartConfig> => {
    // Eligibility by confidence score
    const confidenceCounts: Record<string, number> = {};
    data.forEach(c => {
      const confidence = c.matchingData?.['Confidence Code'] || '0';
      confidenceCounts[confidence] = (confidenceCounts[confidence] || 0) + 1;
    });

    // Submissions by state (top 6)
    const stateCounts: Record<string, number> = {};
    data.forEach(c => {
      const state = c.appendedData?.['State/Province'];
      if (state) stateCounts[state] = (stateCounts[state] || 0) + 1;
    });
    const topStates = Object.entries(stateCounts).sort((a, b) => b[1] - a[1]).slice(0, 6);

    // Appetite share (eligible vs restricted)
    const eligible = data.filter(c => Number(c.matchingData?.['Confidence Code'] || 0) >= 5).length;
    const restricted = data.length - eligible;

    // NAICS codes distribution (top 4)
    const naicsCounts: Record<string, number> = {};
    data.forEach(c => {
      const naics = c.appendedData?.['NAICS 1 Code'];
      if (naics) naicsCounts[naics] = (naicsCounts[naics] || 0) + 1;
    });
    const topNaics = Object.entries(naicsCounts).sort((a, b) => b[1] - a[1]).slice(0, 4);

    return {
      eligibilityChart: {
        type: 'bar',
        data: {
          labels: Object.keys(confidenceCounts),
          datasets: [{ label: 'Companies', data: Object.values(confidenceCounts), backgroundColor: ['#2563eb', '#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe', '#dbeafe'] }]
        }
      },
      submissionsChart: {
        type: 'line',
        data: {
          labels: topStates.map(([state]) => state),
          datasets: [{ label: 'Submissions', data: topStates.map(([, count]) => count), borderColor: '#ef4444', backgroundColor: 'rgba(239,68,68,0.2)', fill: true }]
        }
      },
      appetiteShareChart: {
        type: 'doughnut',
        data: { labels: ['Eligible', 'Restricted'], datasets: [{ label: 'Companies', data: [eligible, restricted], backgroundColor: ['#10b981', '#f59e0b'] }] }
      },
      rulesByProductChart: {
        type: 'bar',
        data: { labels: topNaics.map(([naics]) => naics), datasets: [{ label: 'Count', data: topNaics.map(([, count]) => count), backgroundColor: ['#10b981', '#34d399', '#6ee7b7', '#d1fae5'] }] }
      },
      submissionMixChart: {
        type: 'pie',
        data: { labels: ['High Confidence (≥7)', 'Medium (5-6)', 'Low (<5)'], datasets: [{ label: 'Distribution', data: [
          data.filter(c => Number(c.matchingData?.['Confidence Code'] || 0) >= 7).length,
          data.filter(c => { const conf = Number(c.matchingData?.['Confidence Code'] || 0); return conf >= 5 && conf < 7; }).length,
          data.filter(c => Number(c.matchingData?.['Confidence Code'] || 0) < 5).length
        ], backgroundColor: ['#22c55e', '#f59e0b', '#ef4444'] }] }
      }
    };
  };

  const initTrends = (): void => {
    // Destroy existing charts first
    Object.values(charts).forEach(chart => {
      if (chart && typeof chart.destroy === 'function') {
        chart.destroy();
      }
    });

    setTimeout(() => {
      const chartConfigs = generateRealDataCharts(companiesData);
      const newCharts: Record<string, Chart> = {};
      
      Object.keys(chartConfigs).forEach(chartId => {
        const canvas = document.getElementById(chartId) as HTMLCanvasElement;
        if (canvas) {
          // Clear any existing chart instance
          const existingChart = Chart.getChart(canvas);
          if (existingChart) {
            existingChart.destroy();
          }
          
          const ctx = canvas.getContext('2d');
          if (ctx) {
            newCharts[chartId] = new Chart(ctx, {
              ...chartConfigs[chartId],
              options: { responsive: true, maintainAspectRatio: false }
            });
          }
        }
      });
      setCharts(newCharts);
    }, 100);
  };

  return (
    <div>
      {/* Header */}
      <div className="header"></div>

      <div className="main-content">
        <h1 className="page-title">Appetite Checker</h1>

        {/* Filters */}
        <section className="filter-section">
          <div className="filter-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
            <div className="form-group">
              <label className="form-label">Select State...</label>
              <select 
                className="form-select" 
                value={stateFilter} 
                onChange={(e) => setStateFilter(e.target.value)}
              >
                <option value="">Select State...</option>
                {getStates().map(state => (
                  <option key={state} value={state}>{state}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Search by class code, NAICS code or descriptions</label>
              <input 
                type="text" 
                className="form-input" 
                placeholder="Search..." 
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
              />
            </div>
          </div>

          <div className="button-group">
            <button className="btn btn-primary" onClick={searchData}>Search</button>
            <button className="btn btn-secondary" onClick={clearFilters}>Clear All Filters</button>
          </div>
        </section>

        {/* Results Table */}
        <section className="results-section">
          <div className="results-header">
            <h3 className="results-title">Appetite Data</h3>
          </div>
          <div className="table-container">
            <table>
              <thead className="table-header">
                <tr>
                  <th>Request ID</th>
                  <th>Confidence Code</th>
                  <th>CLASS CODE</th>
                  <th>Description</th>
                  <th>NAICS Code</th>
                  <th>NAICS Description</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredData.map((c, idx) => {
                  const confidence = Number(c.matchingData?.['Confidence Code'] ?? 0);
                  return (
                    <tr key={idx} className="table-row">
                      <td>{c.matchingData?.['Request ID'] ?? 'null'}</td>
                      <td><span className="status-badge">{c.matchingData?.['Confidence Code'] ?? 'null'}</span></td>
                      <td>{c.matchingData?.['DUNS #'] ?? 'null'}</td>
                      <td>{c.appendedData?.['NAICS 1 Description'] ?? 'null'}</td>
                      <td>{c.appendedData?.['NAICS 1 Code'] ?? 'null'}</td>
                      <td>{c.appendedData?.['NAICS 2 Description'] ?? 'null'}</td>
                      <td>
                        {confidence < 5 ? (
                          <div 
                            className="tooltip" 
                            onClick={() => getRecommendation(
                              c.matchingData?.['Request ID'] || idx,
                              c.appendedData?.['NAICS 1 Description'] || 'Business',
                              c.appendedData?.['NAICS 1 Code'] || 'N/A',
                              c.matchingData?.['Confidence Code'] || 'N/A'
                            )}
                          >
                            <AlertCircle color="#ef4444" size={22} />
                            <span className="tooltiptext">
                              <strong>Not Eligible</strong><br />
                              Reason: Low confidence score<br />
                              Recommendation: Improve data quality
                            </span>
                          </div>
                        ) : (
                          <button className="quote-btn" data-idx={idx}>Quote</button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <div className="pagination">
            <div className="pagination-info">Items per page: 25 ▼</div>
            <div className="pagination-controls">
              <button className="pagination-btn"><ChevronsLeft size={16} /></button>
              <button className="pagination-btn"><ChevronLeft size={16} /></button>
              <button className="pagination-btn"><ChevronRight size={16} /></button>
              <button className="pagination-btn"><ChevronsRight size={16} /></button>
            </div>
          </div>
        </section>

        {/* Trends Section */}
        <section className="filter-section">
          <h3 className="results-title" style={{ marginBottom: '.25rem' }}><BarChart3 size={20} style={{ display: 'inline', marginRight: '8px' }} />Trends</h3>
          <div className="state-toolbar">
            <button 
              className="state-btn active"
              onClick={(event) => {
                document.querySelectorAll('.state-btn').forEach(b => b.classList.remove('active'));
                (event.target as HTMLElement).classList.add('active');
                const chartConfigs = generateRealDataCharts(companiesData);
                Object.keys(charts).forEach(chartKey => {
                  if (charts[chartKey] && chartConfigs[chartKey]) {
                    charts[chartKey].data = chartConfigs[chartKey].data;
                    charts[chartKey].update();
                  }
                });
              }}
            >
              All States
            </button>
            {getStates().map((state) => (
              <button 
                key={state}
                className="state-btn"
                onClick={(e) => {
                  document.querySelectorAll('.state-btn').forEach(b => b.classList.remove('active'));
                  (e.target as HTMLElement).classList.add('active');
                  updateChartsForState(state);
                }}
              >
                {state}
              </button>
            ))}
          </div>
          <div className="cards">
            <div className="card">
              <h3>Eligibility</h3>
              <div className="chart-wrap"><canvas id="eligibilityChart"></canvas></div>
              <div className="chart-note">
                <strong>What it shows:</strong> Distribution of companies by confidence score levels.<br/>
                <strong>Calculation:</strong> Count of companies grouped by their confidence code values (0-10).
              </div>
            </div>
            <div className="card">
              <h3>Submissions</h3>
              <div className="chart-wrap"><canvas id="submissionsChart"></canvas></div>
              <div className="chart-note">
                <strong>What it shows:</strong> Number of company submissions across top 6 states.<br/>
                <strong>Calculation:</strong> Count of companies per state, sorted by highest submissions first.
              </div>
            </div>
            <div className="card">
              <h3>Appetite Share</h3>
              <div className="chart-wrap"><canvas id="appetiteShareChart"></canvas></div>
              <div className="chart-note">
                <strong>What it shows:</strong> Proportion of eligible vs restricted companies.<br/>
                <strong>Calculation:</strong> Eligible = confidence score ≥5, Restricted = confidence score &lt;5.
              </div>
            </div>
            <div className="card">
              <h3>Rules By Product</h3>
              <div className="chart-wrap"><canvas id="rulesByProductChart"></canvas></div>
              <div className="chart-note">
                <strong>What it shows:</strong> Distribution of companies across top 4 NAICS industry codes.<br/>
                <strong>Calculation:</strong> Count of companies grouped by primary NAICS code, sorted by frequency.
              </div>
            </div>
            <div className="card">
              <h3>Submission Mix</h3>
              <div className="chart-wrap"><canvas id="submissionMixChart"></canvas></div>
              <div className="chart-note">
                <strong>What it shows:</strong> Breakdown of companies by confidence score ranges.<br/>
                <strong>Calculation:</strong> High (≥7), Medium (5-6), Low (&lt;5) confidence score categories.
              </div>
            </div>
          </div>
        </section>

        {/* Loading Messages with Context-Appropriate Graphics */}
        {isScrolling && (
          <div style={{ position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'rgba(0,0,0,0.8)', color: 'white', padding: '20px', borderRadius: '8px', zIndex: 9999, textAlign: 'center' }}>
            <Bot size={32} style={{ marginBottom: '10px' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Loader2 size={20} className="animate-spin" />
              Please wait... Loading AI insights
            </div>
          </div>
        )}
        
        {isSearching && (
          <div style={{ position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'rgba(0,0,0,0.8)', color: 'white', padding: '20px', borderRadius: '8px', zIndex: 9999, textAlign: 'center' }}>
            <Search size={32} style={{ marginBottom: '10px' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Loader2 size={20} className="animate-spin" />
              Please wait... Searching data
            </div>
          </div>
        )}

        {/* AI Insights Section */}
        <section className="filter-section" id="aiInsights">
          <h3 className="results-title"><Bot size={20} style={{ display: 'inline', marginRight: '8px' }} />AI Insights & Recommendations</h3>
          <div className="ai-insights-grid">
            {showRestrictedCard && (
              <div className="ai-insight-card restricted">
                <div className="insight-header">
                  <AlertTriangle className="insight-icon" size={20} color="#f59e0b" />
                  <span className="insight-title">Restricted Result + AI Recommendations</span>
                </div>
                <div className="insight-business">{restrictedCardData.businessName} ({restrictedCardData.naicsCode})</div>
                <div className="insight-status">Conditional acceptance</div>
                <div className="insight-score">Low confidence score ({restrictedCardData.confidenceCode})</div>
                <div style={{ marginTop: '12px', padding: '16px', background: '#f8fafc', borderRadius: '8px' }}>
                  <div style={{ fontWeight: '600', marginBottom: '8px', color: '#ef4444' }}>Why Not Eligible:</div>
                  <div style={{ color: '#374151', lineHeight: '1.6', fontSize: '14px', whiteSpace: 'pre-wrap' }}>{whyNotEligible}</div>
                </div>
                <div style={{ 
                  marginTop: '16px',
                  background: '#f0f9ff',
                  padding: '16px',
                  borderRadius: '8px',
                  border: '1px solid #bae6fd'
                }}>
                  <div style={{ 
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    marginBottom: '12px',
                    color: '#0284c7',
                    fontWeight: '600'
                  }}>
                    <Bot size={16} />
                    <span>Recommendations:</span>
                  </div>
                  <div style={{ 
                    color: '#374151',
                    fontSize: '14px',
                    lineHeight: '1.6'
                  }}>
                    {aiRecommendation.split('•').map((rec, index) => {
                      const trimmed = rec.trim();
                      if (!trimmed) return null;
                      return (
                        <div key={index} style={{
                          display: 'flex',
                          gap: '8px',
                          marginBottom: '8px',
                          paddingLeft: '8px'
                        }}>
                          <span style={{ color: '#0284c7' }}>•</span>
                          <span>{trimmed}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>
                <div 
                  className="score-improvement" 
                  onClick={() => getScoreImprovementTips(
                    restrictedCardData.requestId,
                    restrictedCardData.businessName,
                    restrictedCardData.naicsCode,
                    restrictedCardData.confidenceCode
                  )}
                  style={{ cursor: 'pointer' }}
                >
                  <TrendingUp size={16} style={{ display: 'inline', marginRight: '4px' }} />Score improvement: {restrictedCardData.confidenceCode} → {Number(restrictedCardData.confidenceCode) + 2}
                </div>
              </div>
            )}

            {showImprovementCard && (
              <div className="ai-insight-card" style={{ borderLeft: '4px solid #3b82f6' }}>
                <div className="insight-header">
                  <BarChart3 className="insight-icon" size={20} color="#3b82f6" />
                  <span className="insight-title">Score Improvement: {improvementCardData.confidenceCode} → {Number(improvementCardData.confidenceCode) + 2}</span>
                </div>
                <div style={{ background: '#e0f2fe', border: '1px solid #0ea5e9', borderRadius: '8px', padding: '12px', marginTop: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                    <Target size={16} style={{ color: '#ef4444' }} />
                    <span style={{ fontWeight: '600', color: '#1f2937' }}>AI Score Improvement Tips</span>
                  </div>
                  <ul style={{ margin: '0', paddingLeft: '20px', color: '#374151', lineHeight: '1.5' }}>
                    {improvementCardData.tips?.map((tip, index) => (
                      <li key={index} style={{ marginBottom: '6px' }}>{tip.trim()}</li>
                    ))}
                  </ul>
                </div>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Companies;