import React, { useState, useEffect } from 'react';
import Chart from 'chart.js/auto';
import './Companies.css';

const Companies = () => {
  const [companiesData, setCompaniesData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [searchInput, setSearchInput] = useState('');
  const [stateFilter, setStateFilter] = useState('');
  const [charts, setCharts] = useState({});
  const [showRestrictedCard, setShowRestrictedCard] = useState(false);
  const [showImprovementCard, setShowImprovementCard] = useState(false);
  const [restrictedCardData, setRestrictedCardData] = useState({});
  const [improvementCardData, setImprovementCardData] = useState({});
  const [whyNotEligible, setWhyNotEligible] = useState('Loading reason...');
  const [aiRecommendations, setAiRecommendations] = useState(['Loading AI recommendation...']);
  const [isScrolling, setIsScrolling] = useState(false);

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
    if (window.feather) {
      window.feather.replace();
    }
  }, [filteredData]);

  const fetchCompaniesData = async () => {
    try {
      const response = await fetch('/api/companies/all');
      const data = await response.json();
      setCompaniesData(data);
      setFilteredData(data);
    } catch (error) {
      console.error('Error fetching companies:', error);
    }
  };

  const getStates = () => {
    return [...new Set(companiesData.map(c => c.appendedData?.['State/Province']).filter(Boolean))].sort();
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
  const filterData = async () => {
    try {
      const params = new URLSearchParams();
      if (searchInput.trim()) params.append('keyword', searchInput.trim());
      if (stateFilter) params.append('state', stateFilter);
      
      const url = `/api/companies/search/advanced${params.toString() ? '?' + params.toString() : ''}`;
      const response = await fetch(url);
      const filtered = await response.json();
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
    }
  };

  const searchData = async () => {
    await filterData();
    setTimeout(() => {
      document.querySelector('.results-section')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const clearFilters = () => {
    setSearchInput('');
    setStateFilter('');
    setShowRestrictedCard(false);
    setShowImprovementCard(false);
    setFilteredData(companiesData);
  };

  const getRecommendation = async (requestId, businessName, naicsCode, confidenceCode) => {
    setIsScrolling(true);
    setShowRestrictedCard(true);
    setShowImprovementCard(false);
    setRestrictedCardData({ requestId, businessName, naicsCode, confidenceCode });
    setWhyNotEligible('Loading reason...');
    setAiRecommendations(['Loading AI recommendation...']);

    try {
      const payload = {
        businessType: businessName,
        naicsCode: naicsCode,
        confidenceScore: confidenceCode
      };

      const response = await fetch('/api/gemini/recommendations', {
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
        
        // Parse recommendations into properly formatted bullet points
        let recommendations = [];
        if (recommendation) {
          // First try to split by newlines with bullet points
          let bulletPoints = recommendation.split(/\n\s*[•\-\*]\s*/);
          
          // If that doesn't work, try splitting by bullet points anywhere in text
          if (bulletPoints.length <= 1) {
            bulletPoints = recommendation.split(/[•\-\*]\s*/);
          }
          
          // Clean up and filter
          recommendations = bulletPoints
            .map(rec => rec.trim().replace(/^[•\-\*\n\r]+\s*/, '').replace(/\n+/g, ' ')) // Remove bullets and newlines
            .filter(rec => rec.length > 20) // Filter meaningful content
            .slice(0, 4); // Limit to 4 recommendations
        }
        
        if (recommendations.length === 0) {
          recommendations = [
            'Implement comprehensive safety protocols and training programs to reduce workplace accidents and liability risks',
            'Establish proper documentation systems for compliance with industry regulations and standards', 
            'Conduct regular risk assessments and maintain detailed records of safety measures and improvements',
            'Obtain necessary certifications and licenses required for industry operations'
          ];
        }
        
        setWhyNotEligible(cleanReason);
        setAiRecommendations(recommendations);
      }
    } catch (error) {
      setWhyNotEligible(`Failed to load recommendations for ${businessName} business`);
      setAiRecommendations(['Please try again for business-specific recommendations']);
    }

    setIsScrolling(false);
    
    // Scroll to AI Insights after loading
    setTimeout(() => {
      document.querySelector('#aiInsights')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const getScoreImprovementTips = async (requestId, businessName, naicsCode, confidenceCode) => {
    setShowImprovementCard(true);
    setImprovementCardData({ requestId, businessName, naicsCode, confidenceCode, tips: ['Loading...'] });

    const scoreImprovementTips = [
      'Verify and update all company contact information including phone, address, and business registration details',
      'Ensure NAICS codes accurately reflect primary business activities and update secondary codes if needed',
      'Complete missing data fields in business registration and provide additional documentation for verification',
      'Review and correct any data inconsistencies between different business databases and registrations'
    ];

    setTimeout(() => {
      setImprovementCardData(prev => ({ ...prev, tips: scoreImprovementTips }));
    }, 500);
  };

  const randomData = (length = 6, max = 100) => {
    return Array.from({ length }, () => Math.floor(Math.random() * max));
  };

  const updateChartsForState = (state) => {
    const stateData = companiesData.filter(c => c.appendedData?.['State/Province'] === state);
    const chartConfigs = generateRealDataCharts(stateData);

    Object.keys(charts).forEach(chartKey => {
      if (charts[chartKey] && chartConfigs[chartKey]) {
        charts[chartKey].data = chartConfigs[chartKey].data;
        charts[chartKey].update();
      }
    });
  };

  const generateRealDataCharts = (data) => {
    // Eligibility by confidence score
    const confidenceCounts = {};
    data.forEach(c => {
      const confidence = c.matchingData?.['Confidence Code'] || '0';
      confidenceCounts[confidence] = (confidenceCounts[confidence] || 0) + 1;
    });

    // Submissions by state (top 6)
    const stateCounts = {};
    data.forEach(c => {
      const state = c.appendedData?.['State/Province'];
      if (state) stateCounts[state] = (stateCounts[state] || 0) + 1;
    });
    const topStates = Object.entries(stateCounts).sort((a, b) => b[1] - a[1]).slice(0, 6);

    // Appetite share (eligible vs restricted)
    const eligible = data.filter(c => Number(c.matchingData?.['Confidence Code'] || 0) >= 5).length;
    const restricted = data.length - eligible;

    // NAICS codes distribution (top 4)
    const naicsCounts = {};
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

  const initTrends = () => {
    // Destroy existing charts first
    Object.values(charts).forEach(chart => {
      if (chart && typeof chart.destroy === 'function') {
        chart.destroy();
      }
    });

    setTimeout(() => {
      const chartConfigs = generateRealDataCharts(companiesData);
      const newCharts = {};
      
      Object.keys(chartConfigs).forEach(chartId => {
        const canvas = document.getElementById(chartId);
        if (canvas) {
          // Clear any existing chart instance
          const existingChart = Chart.getChart(canvas);
          if (existingChart) {
            existingChart.destroy();
          }
          
          const ctx = canvas.getContext('2d');
          newCharts[chartId] = new Chart(ctx, {
            ...chartConfigs[chartId],
            options: { responsive: true, maintainAspectRatio: false }
          });
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
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#ef4444" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <circle cx="12" cy="12" r="10"></circle>
                              <line x1="12" y1="8" x2="12" y2="12"></line>
                              <line x1="12" y1="16" x2="12.01" y2="16"></line>
                            </svg>
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
              <button className="pagination-btn">‹‹</button>
              <button className="pagination-btn">‹</button>
              <button className="pagination-btn">›</button>
              <button className="pagination-btn">››</button>
            </div>
          </div>
        </section>

        {/* Trends Section */}
        <section className="filter-section">
          <h3 className="results-title" style={{ marginBottom: '.25rem' }}>📊Trends</h3>
          <div className="state-toolbar">
            <button 
              className="state-btn active"
              onClick={() => {
                document.querySelectorAll('.state-btn').forEach(b => b.classList.remove('active'));
                event.target.classList.add('active');
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
                  e.target.classList.add('active');
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
                <strong>Calculation:</strong> Eligible = confidence score &ge;5, Restricted = confidence score &lt;5.
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
                <strong>Calculation:</strong> High (&ge;7), Medium (5-6), Low (&lt;5) confidence score categories.
              </div>
            </div>
          </div>
        </section>

        {/* Scrolling Message */}
        {isScrolling && (
          <div style={{ position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', background: 'rgba(0,0,0,0.8)', color: 'white', padding: '20px', borderRadius: '8px', zIndex: 9999 }}>
            Please wait... Loading AI insights
          </div>
        )}

        {/* AI Insights Section */}
        <section className="filter-section" id="aiInsights">
          <h3 className="results-title">🤖 AI Insights & Recommendations</h3>
          <div className="ai-insights-grid">
            {showRestrictedCard && (
              <div className="ai-insight-card restricted">
                <div className="insight-header">
                  <span className="insight-icon">⚠️</span>
                  <span className="insight-title">Restricted Result + AI Recommendations</span>
                </div>
                <div className="insight-business">{restrictedCardData.businessName} ({restrictedCardData.naicsCode})</div>
                <div className="insight-status">Conditional acceptance</div>
                <div className="insight-score">Low confidence score ({restrictedCardData.confidenceCode})</div>
                <div style={{ marginTop: '12px', padding: '12px', background: '#f8fafc', borderRadius: '8px' }}>
                  <div style={{ fontWeight: '600', marginBottom: '6px' }}>Why Not Eligible:</div>
                  <div style={{ color: '#374151' }}>{whyNotEligible}</div>
                </div>
                <div className="ai-recommendation restricted">
                  <span>🤖</span>
                  <div>
                    <div style={{ fontWeight: '600', marginBottom: '8px' }}>AI Recommendations:</div>
                    <div className="ai-bullet-list">
                      {aiRecommendations.map((rec, index) => (
                        <div key={index} className="ai-bullet-item">
                          <span className="ai-bullet-point">•</span>
                          <span className="ai-bullet-text">{rec.trim()}</span>
                        </div>
                      ))}
                    </div>
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
                  📊 Score improvement: {restrictedCardData.confidenceCode} → {Number(restrictedCardData.confidenceCode) + 2}
                </div>
              </div>
            )}

            {showImprovementCard && (
              <div className="ai-insight-card" style={{ borderLeft: '4px solid #3b82f6' }}>
                <div className="insight-header">
                  <span className="insight-icon">📊</span>
                  <span className="insight-title">Score Improvement: {improvementCardData.confidenceCode} → {Number(improvementCardData.confidenceCode) + 2}</span>
                </div>
                <div style={{ background: '#e0f2fe', border: '1px solid #0ea5e9', borderRadius: '8px', padding: '12px', marginTop: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                    <span style={{ color: '#ef4444' }}>🎯</span>
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